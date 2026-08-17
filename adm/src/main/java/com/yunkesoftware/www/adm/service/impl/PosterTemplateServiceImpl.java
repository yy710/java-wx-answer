package com.yunkesoftware.www.adm.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.yunkesoftware.www.adm.entity.PosterTemplate;
import com.yunkesoftware.www.adm.entity.PosterTemplateAsset;
import com.yunkesoftware.www.adm.mapper.PosterTemplateAssetMapper;
import com.yunkesoftware.www.adm.mapper.PosterTemplateMapper;
import com.yunkesoftware.www.adm.service.PosterTemplateService;
import com.yunkesoftware.www.config.CosClientProperties;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.poster.PosterTemplateValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.GetObjectRequest;

import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.net.URI;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Service
public class PosterTemplateServiceImpl extends ServiceImpl<PosterTemplateMapper, PosterTemplate> implements PosterTemplateService {
    @Resource private PosterTemplateAssetMapper assetMapper;
    @Resource private ObjectMapper objectMapper;
    @Resource private CosClientProperties cosProperties;
    @Resource private COSClient cosClient;
    private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
    private static final Pattern SHA256 = Pattern.compile("[0-9a-fA-F]{64}");

    @Override
    public PosterTemplate getWithAssets(String id) {
        PosterTemplate template = baseMapper.selectById(id);
        if (template != null) template.setAssets(assetMapper.selectList(new LambdaQueryWrapper<PosterTemplateAsset>().eq(PosterTemplateAsset::getTemplateId, id)));
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PosterTemplate saveTemplate(PosterTemplate template, String operatorId) {
        if (template == null || template.getName() == null || template.getName().isBlank() || template.getRestrictedJson() == null) throw new YunKeException(ExceptionEnum.FAIL, "POSTER_SCHEMA_INVALID:模板名称和 JSON 必填");
        JsonNode root;
        try {
            root = objectMapper.readTree(template.getRestrictedJson());
            PosterTemplateValidator.validate(root);
        } catch (Exception e) { throw new YunKeException(ExceptionEnum.FAIL, "POSTER_SCHEMA_INVALID:模板 JSON 不符合受限协议"); }
        int[] canvas = canvasSize(template.getRestrictedJson());
        boolean isNew = template.getId() == null || template.getId().isBlank();
        if (isNew) { template.setId(IdUtil.getSnowflakeNextIdStr()).setTemplateVersion(template.getTemplateVersion() == null ? 1 : template.getTemplateVersion()).setStatus("DRAFT").setCreatedBy(operatorId); }
        template.setCanvasWidth(canvas[0]).setCanvasHeight(canvas[1]);
        if (isNew) {
            baseMapper.insert(template);
        } else {
            baseMapper.updateById(template);
        }
        Set<String> requiredAssets = new HashSet<>();
        root.path("layers").forEach(layer -> {
            if ("staticImage".equals(layer.path("type").asText())) requiredAssets.add(layer.path("assetKey").asText());
        });
        Set<String> submittedAssets = new HashSet<>();
        if (template.getAssets() != null) for (PosterTemplateAsset asset : template.getAssets()) {
            validateAsset(asset);
            if (!submittedAssets.add(asset.getAssetKey()) || !requiredAssets.contains(asset.getAssetKey())) throw new YunKeException(ExceptionEnum.FAIL, "POSTER_ASSET_INVALID:素材键与模板图层不匹配");
        }
        if (!submittedAssets.equals(requiredAssets)) throw new YunKeException(ExceptionEnum.FAIL, "POSTER_ASSET_INVALID:模板静态图片素材未完整登记");
        assetMapper.delete(new LambdaQueryWrapper<PosterTemplateAsset>().eq(PosterTemplateAsset::getTemplateId, template.getId()));
        if (template.getAssets() != null) for (PosterTemplateAsset asset : template.getAssets()) {
            asset.setId(asset.getId() == null ? IdUtil.getSnowflakeNextIdStr() : asset.getId()).setTemplateId(template.getId());
            assetMapper.insert(asset);
        }
        return getWithAssets(template.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(String id, Integer version) {
        PosterTemplate template = baseMapper.selectById(id); if (template == null || (version != null && !version.equals(template.getTemplateVersion()))) throw new YunKeException(ExceptionEnum.FAIL, "POSTER_TEMPLATE_NOT_FOUND:模板不存在");
        baseMapper.update(null, new LambdaUpdateWrapper<PosterTemplate>().eq(PosterTemplate::getId, id).set(PosterTemplate::getStatus, "DISABLED"));
        baseMapper.update(null, new LambdaUpdateWrapper<PosterTemplate>().eq(PosterTemplate::getId, id).eq(PosterTemplate::getTemplateVersion, template.getTemplateVersion()).set(PosterTemplate::getStatus, "PUBLISHED").set(PosterTemplate::getPublishedAt, LocalDateTime.now()));
    }

    @Override
    public void disable(String id, Integer version) { baseMapper.update(null, new LambdaUpdateWrapper<PosterTemplate>().eq(PosterTemplate::getId, id).eq(version != null, PosterTemplate::getTemplateVersion, version).set(PosterTemplate::getStatus, "DISABLED")); }

    @Override
    public PosterTemplate preview(PosterTemplate template) {
        if (template == null || template.getRestrictedJson() == null) throw new YunKeException(ExceptionEnum.FAIL, "POSTER_SCHEMA_INVALID:模板 JSON 必填");
        JsonNode root;
        try { root = objectMapper.readTree(template.getRestrictedJson()); PosterTemplateValidator.validate(root); }
        catch (Exception e) { throw new YunKeException(ExceptionEnum.FAIL, "POSTER_SCHEMA_INVALID:模板 JSON 不符合受限协议"); }
        int[] canvas = canvasSize(template.getRestrictedJson());
        template.setCanvasWidth(canvas[0]).setCanvasHeight(canvas[1]);
        Set<String> requiredAssets = new HashSet<>();
        root.path("layers").forEach(layer -> { if ("staticImage".equals(layer.path("type").asText())) requiredAssets.add(layer.path("assetKey").asText()); });
        Set<String> submitted = new HashSet<>();
        if (template.getAssets() != null) for (PosterTemplateAsset asset : template.getAssets()) {
            validateAsset(asset);
            if (!submitted.add(asset.getAssetKey()) || !requiredAssets.contains(asset.getAssetKey())) throw new YunKeException(ExceptionEnum.FAIL, "POSTER_ASSET_INVALID:素材键与模板图层不匹配");
        }
        if (!submitted.equals(requiredAssets)) throw new YunKeException(ExceptionEnum.FAIL, "POSTER_ASSET_INVALID:模板静态图片素材未完整登记");
        try {
            template.setPreviewUrl(renderPreview(root, template.getAssets() == null ? java.util.List.of() : template.getAssets()));
        } catch (Exception e) {
            throw new YunKeException(ExceptionEnum.FAIL, "POSTER_RENDER_FAILED:服务端预览失败");
        }
        template.setStatus("PREVIEW");
        return template;
    }

    private String renderPreview(JsonNode root, java.util.List<PosterTemplateAsset> assets) throws Exception {
        int width = root.path("canvas").path("width").asInt();
        int height = root.path("canvas").path("height").asInt();
        Map<String, PosterTemplateAsset> assetMap = new LinkedHashMap<>();
        assets.forEach(asset -> assetMap.put(asset.getAssetKey(), asset));
        StringBuilder svg = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width).append("\" height=\"").append(height).append("\"><rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>");
        for (JsonNode layer : root.path("layers")) {
            String type = layer.path("type").asText(); int x = layer.path("x").asInt(); int y = layer.path("y").asInt(); int w = layer.path("width").asInt(); int h = layer.path("height").asInt();
            if ("staticText".equals(type)) {
                String text = xml(layer.path("text").asText("")); int font = layer.path("fontSize").asInt(28); String color = xml(layer.path("color").asText("#222222"));
                svg.append("<text x=\"").append(x).append("\" y=\"").append(y + Math.min(h - 2, font)).append("\" font-family=\"sans-serif\" font-size=\"").append(font).append("\" fill=\"").append(color).append("\">").append(text).append("</text>");
            } else if ("staticImage".equals(type)) {
                PosterTemplateAsset asset = assetMap.get(layer.path("assetKey").asText());
                byte[] bytes = readAsset(asset);
                svg.append("<image x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(w).append("\" height=\"").append(h).append("\" preserveAspectRatio=\"xMidYMid slice\" href=\"data:").append(asset.getMimeType()).append(";base64,").append(Base64.getEncoder().encodeToString(bytes)).append("\"/>");
            } else if ("dynamicImage".equals(type)) {
                // 管理端没有运行时用户头像，使用与用户渲染器一致的安全占位图。
                svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(w).append("\" height=\"").append(h).append("\" rx=\"12\" fill=\"#e5e7eb\"/>");
            } else {
                String field = layer.path("field").asText(""); String label = field.startsWith("custom.") ? "{" + field.substring(7) + "}" : field;
                int font = layer.path("fontSize").asInt(28); svg.append("<text x=\"").append(x).append("\" y=\"").append(y + Math.min(h - 2, font)).append("\" font-family=\"sans-serif\" font-size=\"").append(font).append("\" fill=\"#222222\">").append(xml(label)).append("</text>");
            }
        }
        svg.append("</svg>");
        ByteArrayOutputStream output = new ByteArrayOutputStream(); PNGTranscoder transcoder = new PNGTranscoder(); transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width); transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height); transcoder.transcode(new TranscoderInput(new java.io.StringReader(svg.toString())), new TranscoderOutput(output));
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
    }

    private byte[] readAsset(PosterTemplateAsset asset) throws Exception {
        URI uri = URI.create(asset.getFileUrl()); String host = cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com";
        if (!host.equalsIgnoreCase(uri.getHost()) || uri.getRawQuery() != null || uri.getRawFragment() != null) throw new IllegalArgumentException("素材地址不受信任");
        try (InputStream input = cosClient.getObject(new GetObjectRequest(cosProperties.getBucket(), uri.getPath().replaceFirst("^/", ""))).getObjectContent(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            input.transferTo(output);
            byte[] bytes = output.toByteArray();
            if (bytes.length < 1 || bytes.length > 10 * 1024 * 1024 || !sha256(bytes).equalsIgnoreCase(asset.getSha256())) throw new IllegalArgumentException("素材大小或 SHA-256 不匹配");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null || image.getWidth() != asset.getWidth() || image.getHeight() != asset.getHeight()) throw new IllegalArgumentException("素材尺寸不匹配");
            return bytes;
        }
    }

    private static String sha256(byte[] bytes) throws Exception { StringBuilder result = new StringBuilder(); for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) result.append(String.format("%02x", value)); return result.toString(); }

    private static String xml(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }

    private int[] canvasSize(String json) {
        try { com.fasterxml.jackson.databind.JsonNode canvas = objectMapper.readTree(json).get("canvas"); return new int[]{canvas.get("width").asInt(), canvas.get("height").asInt()}; }
        catch (Exception e) { throw new YunKeException(ExceptionEnum.FAIL, "POSTER_SCHEMA_INVALID:画布尺寸读取失败"); }
    }

    private void validateAsset(PosterTemplateAsset asset) {
        if (asset == null || asset.getAssetKey() == null || asset.getAssetKey().isBlank()
                || asset.getFileUrl() == null || !asset.getFileUrl().startsWith("https://")
                || !IMAGE_MIME_TYPES.contains(asset.getMimeType()) || asset.getWidth() == null || asset.getHeight() == null
                || asset.getWidth() < 1 || asset.getWidth() > 4096 || asset.getHeight() < 1 || asset.getHeight() > 4096
                || asset.getSha256() == null || !SHA256.matcher(asset.getSha256()).matches()) {
            throw new YunKeException(ExceptionEnum.FAIL, "POSTER_ASSET_INVALID:素材字段不合法");
        }
        try {
            URI uri = URI.create(asset.getFileUrl());
            String allowedHost = cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com";
            if (!allowedHost.equalsIgnoreCase(uri.getHost()) || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("素材必须来自当前COS且不得包含外部查询参数");
            }
        } catch (Exception e) {
            throw new YunKeException(ExceptionEnum.FAIL, "POSTER_ASSET_INVALID:素材地址不受信任");
        }
    }
}
