package com.yunkesoftware.www.web.service.impl;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.yunkesoftware.www.config.CosClientProperties;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.poster.PosterTemplateValidator;
import com.yunkesoftware.www.web.dto.PosterGenerateRequest;
import com.yunkesoftware.www.web.entity.DailyTaskConfig;
import com.yunkesoftware.www.web.entity.PosterGeneration;
import com.yunkesoftware.www.web.entity.PosterTemplate;
import com.yunkesoftware.www.web.entity.User;
import com.yunkesoftware.www.web.mapper.DailyTaskConfigMapper;
import com.yunkesoftware.www.web.mapper.PosterGenerationMapper;
import com.yunkesoftware.www.web.mapper.PosterTemplateAssetMapper;
import com.yunkesoftware.www.web.mapper.PosterTemplateMapper;
import com.yunkesoftware.www.web.mapper.UserMapper;
import com.yunkesoftware.www.web.service.PosterService;
import com.yunkesoftware.www.web.poster.PosterRenderer;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PosterServiceImpl implements PosterService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    @Resource private PosterTemplateMapper templateMapper;
    @Resource private PosterTemplateAssetMapper assetMapper;
    @Resource private PosterGenerationMapper generationMapper;
    @Resource private DailyTaskConfigMapper configMapper;
    @Resource private UserMapper userMapper;
    @Resource private COSClient cosClient;
    @Resource private CosClientProperties cosProperties;
    @Resource private ObjectMapper objectMapper;
    @Resource(name = "localPosterRenderer") private PosterRenderer posterRenderer;

    @Override
    public List<Map<String, Object>> listPublished() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PosterTemplate template : templateMapper.listPublished()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", template.getId()); item.put("name", template.getName()); item.put("version", template.getTemplateVersion());
            item.put("canvasWidth", template.getCanvasWidth()); item.put("canvasHeight", template.getCanvasHeight()); item.put("previewUrl", template.getPreviewUrl());
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generate(String userId, PosterGenerateRequest request) {
        if (request == null || request.getTemplateId() == null || request.getTemplateId().isBlank()) fail("POSTER_TEMPLATE_NOT_PUBLISHED", "模板参数无效");
        PosterTemplate template = templateMapper.selectPublished(request.getTemplateId(), request.getTemplateVersion());
        if (template == null) fail("POSTER_TEMPLATE_NOT_PUBLISHED", "模板未发布或不存在");
        JsonNode root;
        try { root = objectMapper.readTree(template.getRestrictedJson()); PosterTemplateValidator.validate(root); }
        catch (Exception e) { fail("POSTER_SCHEMA_INVALID", "模板 JSON 不符合受限协议"); return Map.of(); }
        Map<String, String> custom = request.getCustom() == null ? Map.of() : request.getCustom();
        validateCustomFields(root, custom);
        Map<String, Object> input = new LinkedHashMap<>(); input.put("templateId", template.getId()); input.put("templateVersion", template.getTemplateVersion()); input.put("custom", custom);
        String inputHash = sha256(json(input));
        PosterGeneration cached = generationMapper.selectCache(userId, template.getId(), template.getTemplateVersion(), inputHash);
        if (cached != null && "SUCCEEDED".equals(cached.getStatus()) && cached.getExpiresAt() != null && cached.getExpiresAt().isAfter(now())) return generationApi(cached, true);
        if (cached != null) generationMapper.deleteById(cached.getId());
        User user = userMapper.selectById(userId);
        byte[] png;
        try { png = posterRenderer.render(root, assetMapper.listByTemplate(template.getId()), user, custom); }
        catch (Exception e) { fail("POSTER_RENDER_FAILED", "海报渲染失败"); return Map.of(); }
        String generationId = IdUtil.getSnowflakeNextIdStr();
        String key = "poster/" + now().toLocalDate() + "/" + generationId + ".png";
        String url = upload(key, png);
        DailyTaskConfig taskConfig = configMapper.selectById(1);
        int cacheDays = taskConfig == null || taskConfig.getPosterCacheDays() == null ? 7 : taskConfig.getPosterCacheDays();
        PosterGeneration generation = new PosterGeneration().setId(generationId).setUserId(userId).setTemplateId(template.getId())
                .setTemplateVersion(template.getTemplateVersion()).setInputSha256(inputHash).setResultUrl(url).setStatus("SUCCEEDED")
                .setExpiresAt(now().plusDays(cacheDays)).setCreatedAt(now());
        try { generationMapper.insert(generation); } catch (DuplicateKeyException duplicate) { generation = generationMapper.selectCache(userId, template.getId(), template.getTemplateVersion(), inputHash); }
        return generationApi(generation, false);
    }

    @Override
    public byte[] download(String userId, String generationId) {
        PosterGeneration generation = generationMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PosterGeneration>()
                .eq(PosterGeneration::getId, generationId).eq(PosterGeneration::getUserId, userId));
        if (generation == null || !"SUCCEEDED".equals(generation.getStatus()) || generation.getExpiresAt().isBefore(now())) fail("POSTER_GENERATION_EXPIRED", "海报不存在或已过期");
        try {
            URI uri = URI.create(generation.getResultUrl());
            String allowedHost = cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com";
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !allowedHost.equalsIgnoreCase(uri.getHost()) || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                fail("POSTER_DOWNLOAD_FAILED", "海报地址不受信任");
            }
            try (InputStream input = uri.toURL().openStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                input.transferTo(output);
                if (output.size() > MAX_IMAGE_BYTES) fail("POSTER_DOWNLOAD_FAILED", "海报文件过大");
                return output.toByteArray();
            }
        } catch (YunKeException e) {
            throw e;
        } catch (Exception e) {
            throw new YunKeException(ExceptionEnum.FAIL, "POSTER_DOWNLOAD_FAILED:海报读取失败");
        }
    }

    private String upload(String key, byte[] bytes) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            ObjectMetadata metadata = new ObjectMetadata(); metadata.setContentLength(bytes.length); metadata.setContentType("image/png");
            cosClient.putObject(new PutObjectRequest(cosProperties.getBucket(), key, input, metadata));
            return "https://" + cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com/" + key;
        } catch (Exception e) { throw new YunKeException(ExceptionEnum.FAIL, "POSTER_UPLOAD_FAILED:海报上传失败"); }
    }

    private void validateCustomFields(JsonNode root, Map<String, String> custom) {
        if (custom.size() > 8 || custom.keySet().stream().anyMatch(key -> key == null || !key.matches("[A-Za-z][A-Za-z0-9_]{0,31}"))
                || custom.values().stream().anyMatch(value -> value == null || value.length() > 200)) {
            fail("POSTER_CUSTOM_FIELD_INVALID", "自定义海报字段不合法");
        }
        Set<String> declaredFields = new HashSet<>();
        root.path("declaredCustomFields").forEach(node -> declaredFields.add(node.asText()));
        if (custom.keySet().stream().anyMatch(key -> !declaredFields.contains(key))) {
            fail("POSTER_CUSTOM_FIELD_INVALID", "自定义字段未被模板声明");
        }
    }

    private Map<String, Object> generationApi(PosterGeneration row, boolean cached) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generationId", row.getId());
        response.put("status", row.getStatus());
        response.put("downloadUrl", "/wx/poster/generated/" + row.getId() + "/download");
        response.put("previewUrl", row.getResultUrl());
        response.put("expiresAt", row.getExpiresAt().toString());
        response.put("cached", cached);
        return response;
    }
    private String json(Object value) { try { return objectMapper.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
    private String sha256(String value) { try { byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder out = new StringBuilder(); for (byte b : bytes) out.append(String.format("%02x", b)); return out.toString(); } catch (Exception e) { throw new IllegalStateException(e); } }
    private static LocalDateTime now() { return LocalDateTime.now(ZONE); }
    private static void fail(String code, String message) { throw new YunKeException(ExceptionEnum.FAIL, code + ":" + message); }
}
