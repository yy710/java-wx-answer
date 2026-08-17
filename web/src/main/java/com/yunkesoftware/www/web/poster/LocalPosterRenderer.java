package com.yunkesoftware.www.web.poster;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.GetObjectRequest;
import com.yunkesoftware.www.config.CosClientProperties;
import com.yunkesoftware.www.web.entity.PosterTemplateAsset;
import com.yunkesoftware.www.web.entity.User;
import jakarta.annotation.Resource;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 受限 JSON -> SVG -> PNG 的本地渲染实现。 */
@Service("localPosterRenderer")
public class LocalPosterRenderer implements PosterRenderer {
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    @Resource private COSClient cosClient;
    @Resource private CosClientProperties cosProperties;

    @Override
    public byte[] render(JsonNode root, List<PosterTemplateAsset> assets, User user, Map<String, String> custom) throws Exception {
        int width = root.get("canvas").get("width").asInt();
        int height = root.get("canvas").get("height").asInt();
        Map<String, PosterTemplateAsset> assetMap = new HashMap<>();
        assets.forEach(asset -> assetMap.put(asset.getAssetKey(), asset));
        StringBuilder svg = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                .append(width).append("\" height=\"").append(height)
                .append("\"><rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>");
        for (JsonNode layer : root.get("layers")) {
            String type = layer.get("type").asText();
            int x = layer.get("x").asInt(), y = layer.get("y").asInt();
            int w = layer.get("width").asInt(), h = layer.get("height").asInt();
            if ("staticImage".equals(type)) {
                appendAsset(svg, assetMap.get(layer.get("assetKey").asText()), x, y, w, h);
                continue;
            }
            if ("dynamicImage".equals(type)) {
                String avatar = value(layer.get("field").asText(), user, custom);
                if (isTrustedCosUrl(avatar)) {
                    appendTrustedDynamicImage(svg, avatar, x, y, w, h);
                } else {
                    svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                            .append("\" width=\"").append(w).append("\" height=\"").append(h)
                            .append("\" rx=\"8\" fill=\"#e5e7eb\"/>");
                }
                continue;
            }
            if ("qrCode".equals(type)) {
                appendQr(svg, value(layer.get("field").asText(), user, custom), x, y, w, h);
                continue;
            }
            String text = "staticText".equals(type) ? layer.path("text").asText("") : value(layer.get("field").asText(), user, custom);
            int font = layer.path("fontSize").asInt(28);
            String color = layer.path("color").asText("#222222");
            int maxLength = layer.path("maxLength").asInt(200);
            String safeText = text.substring(0, Math.min(text.length(), maxLength));
            svg.append("<text x=\"").append(x).append("\" y=\"").append(y + Math.min(h - 2, font))
                    .append("\" font-family=\"sans-serif\" font-size=\"").append(font)
                    .append("\" fill=\"").append(xml(color)).append("\">")
                    .append(xml(safeText)).append("</text>");
        }
        svg.append("</svg>");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
        transcoder.transcode(new TranscoderInput(new java.io.StringReader(svg.toString())), new TranscoderOutput(output));
        return output.toByteArray();
    }

    private void appendAsset(StringBuilder svg, PosterTemplateAsset asset, int x, int y, int w, int h) {
        if (asset == null || asset.getFileUrl() == null || !asset.getFileUrl().startsWith("https://")) throw new IllegalArgumentException("海报素材不存在");
        try {
            URI uri = URI.create(asset.getFileUrl());
            String allowedHost = cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com";
            if (!allowedHost.equalsIgnoreCase(uri.getHost()) || uri.getRawQuery() != null || uri.getRawFragment() != null) throw new IllegalArgumentException("素材来源不受信任");
            byte[] bytes;
            try (InputStream input = cosClient.getObject(new GetObjectRequest(cosProperties.getBucket(), uri.getPath().replaceFirst("^/", ""))).getObjectContent(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                input.transferTo(out);
                bytes = out.toByteArray();
            }
            if (bytes.length < 1 || bytes.length > MAX_IMAGE_BYTES) throw new IllegalArgumentException("素材过大");
            if (!sha256(bytes).equalsIgnoreCase(asset.getSha256())) throw new IllegalArgumentException("素材 SHA-256 不匹配");
            validateImageHeader(bytes, asset);
            svg.append("<image x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(w).append("\" height=\"").append(h).append("\" preserveAspectRatio=\"xMidYMid slice\" href=\"data:").append(xml(asset.getMimeType())).append(";base64,").append(Base64.getEncoder().encodeToString(bytes)).append("\"/>");
        } catch (Exception e) { throw new IllegalArgumentException("海报素材读取失败", e); }
    }

    private void appendTrustedDynamicImage(StringBuilder svg, String fileUrl, int x, int y, int w, int h) {
        try {
            URI uri = URI.create(fileUrl);
            String allowedHost = cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com";
            if (!allowedHost.equalsIgnoreCase(uri.getHost()) || uri.getRawQuery() != null || uri.getRawFragment() != null) throw new IllegalArgumentException("头像来源不受信任");
            byte[] bytes;
            try (InputStream input = cosClient.getObject(new GetObjectRequest(cosProperties.getBucket(), uri.getPath().replaceFirst("^/", ""))).getObjectContent(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                input.transferTo(out); bytes = out.toByteArray();
            }
            if (bytes.length < 1 || bytes.length > MAX_IMAGE_BYTES) throw new IllegalArgumentException("头像过大");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) throw new IllegalArgumentException("头像格式不支持");
            ByteArrayOutputStream normalized = new ByteArrayOutputStream();
            ImageIO.write(image, "png", normalized);
            svg.append("<image x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(w).append("\" height=\"").append(h).append("\" preserveAspectRatio=\"xMidYMid slice\" href=\"data:image/png;base64,").append(Base64.getEncoder().encodeToString(normalized.toByteArray())).append("\"/>");
        } catch (Exception e) {
            throw new IllegalArgumentException("头像读取失败", e);
        }
    }

    private boolean isTrustedCosUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("https://")) return false;
        try {
            URI uri = URI.create(fileUrl);
            String allowedHost = cosProperties.getBucket() + ".cos." + cosProperties.getRegion() + ".myqcloud.com";
            return allowedHost.equalsIgnoreCase(uri.getHost()) && uri.getRawQuery() == null && uri.getRawFragment() == null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void validateImageHeader(byte[] bytes, PosterTemplateAsset asset) throws Exception {
        String mime = asset.getMimeType();
        boolean png = bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47;
        boolean jpeg = bytes.length >= 2 && bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8;
        boolean webp = bytes.length >= 12 && bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46 && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50;
        if (("image/png".equals(mime) && !png) || ("image/jpeg".equals(mime) && !jpeg) || ("image/webp".equals(mime) && !webp)) throw new IllegalArgumentException("素材文件头与 MIME 不匹配");
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null || image.getWidth() != asset.getWidth() || image.getHeight() != asset.getHeight()) throw new IllegalArgumentException("素材尺寸与登记值不匹配");
    }

    private void appendQr(StringBuilder svg, String value, int x, int y, int w, int h) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(value == null ? "" : value, BarcodeFormat.QR_CODE, w, h);
        svg.append("<g>");
        for (int row = 0; row < h; row++) for (int col = 0; col < w; col++) if (matrix.get(col, row)) svg.append("<rect x=\"").append(x + col).append("\" y=\"").append(y + row).append("\" width=\"1\" height=\"1\" fill=\"#000000\"/>");
        svg.append("</g>");
    }

    private String value(String field, User user, Map<String, String> custom) {
        if ("user.nickname".equals(field)) return user == null || user.getNickName() == null ? "" : user.getNickName();
        if ("user.avatar".equals(field)) return user == null || user.getPic() == null ? "" : user.getPic();
        if ("generatedAt".equals(field)) return java.time.OffsetDateTime.now(java.time.ZoneOffset.ofHours(8)).toString();
        if (field != null && field.startsWith("custom.")) return custom.getOrDefault(field.substring(7), "");
        return "";
    }

    private static String xml(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;"); }
    private static String sha256(byte[] bytes) throws Exception { StringBuilder out = new StringBuilder(); for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) out.append(String.format("%02x", value)); return out.toString(); }
}
