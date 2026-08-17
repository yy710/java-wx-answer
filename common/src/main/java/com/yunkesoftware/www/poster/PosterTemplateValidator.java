package com.yunkesoftware.www.poster;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/** 受限海报 JSON 校验器；模板永远不是可执行代码。 */
public final class PosterTemplateValidator {
    private static final Set<String> ROOT_KEYS = Set.of("canvas", "declaredCustomFields", "layers");
    private static final Set<String> LAYER_TYPES = Set.of("staticImage", "dynamicImage", "staticText", "dynamicText", "editableText", "qrCode");
    private static final Pattern CUSTOM = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,31}");
    private static final Pattern FIELD = Pattern.compile("(?:user\\.(?:avatar|nickname)|generatedAt|custom\\.[A-Za-z][A-Za-z0-9_]{0,31})");
    private static final Pattern COLOR = Pattern.compile("#[0-9A-Fa-f]{6}");

    private PosterTemplateValidator() { }

    public static void validate(JsonNode root) {
        if (root == null || !root.isObject()) invalid("模板必须是 JSON 对象");
        Iterator<String> names = root.fieldNames();
        while (names.hasNext()) if (!ROOT_KEYS.contains(names.next())) invalid("模板包含未允许字段");
        JsonNode canvas = required(root, "canvas");
        int width = integer(canvas, "width", 320, 2048), height = integer(canvas, "height", 320, 2048);
        if ((long) width * height > 2048L * 2048L) invalid("画布像素过大");
        JsonNode declaredNode = required(root, "declaredCustomFields");
        if (!declaredNode.isArray() || declaredNode.size() > 8) invalid("自定义字段声明不合法");
        Set<String> declared = new HashSet<>();
        declaredNode.forEach(node -> { if (!node.isTextual() || !CUSTOM.matcher(node.textValue()).matches()) invalid("自定义字段声明不合法"); declared.add(node.textValue()); });
        JsonNode layers = required(root, "layers");
        if (!layers.isArray() || layers.size() < 1 || layers.size() > 80) invalid("图层数量必须在 1-80");
        Set<String> staticAssetKeys = new HashSet<>();
        layers.forEach(layer -> validateLayer(layer, width, height, declared, staticAssetKeys));
    }

    private static void validateLayer(JsonNode layer, int width, int height, Set<String> declared, Set<String> staticAssetKeys) {
        if (!layer.isObject() || layer.get("type") == null || !layer.get("type").isTextual() || !LAYER_TYPES.contains(layer.get("type").textValue())) invalid("图层类型不允许");
        Iterator<String> names = layer.fieldNames();
        Set<String> allowed = Set.of("type", "x", "y", "width", "height", "assetKey", "field", "text", "fontSize", "color", "maxLength");
        while (names.hasNext()) if (!allowed.contains(names.next())) invalid("图层字段不允许");
        double x = number(layer, "x", 0), y = number(layer, "y", 0), layerWidth = number(layer, "width", 1), layerHeight = number(layer, "height", 1);
        if (x < 0 || y < 0 || layerWidth <= 0 || layerHeight <= 0 || x + layerWidth > width || y + layerHeight > height) invalid("图层坐标或尺寸不合法");
        String type = layer.get("type").textValue();
        if ("staticImage".equals(type)) {
            if (layer.get("assetKey") == null || !layer.get("assetKey").isTextual() || layer.get("assetKey").textValue().isBlank()) invalid("静态图片必须指定素材");
            if (!staticAssetKeys.add(layer.get("assetKey").textValue())) invalid("静态图片素材键不能重复");
        }
        if (Set.of("dynamicImage", "dynamicText", "editableText", "qrCode").contains(type)) {
            if (layer.get("field") == null || !layer.get("field").isTextual() || !FIELD.matcher(layer.get("field").textValue()).matches()) invalid("动态字段不在白名单");
            String field = layer.get("field").textValue();
            if (field.startsWith("custom.") && !declared.contains(field.substring(7))) invalid("custom 字段未声明");
        }
        if ("dynamicImage".equals(type) && !"user.avatar".equals(layer.get("field").textValue())) invalid("动态图像只能使用 user.avatar");
        if ("editableText".equals(type) && !layer.get("field").textValue().startsWith("custom.")) invalid("可编辑文本只能使用 custom 字段");
        if ("staticText".equals(type) && (layer.get("text") == null || !layer.get("text").isTextual() || layer.get("text").textValue().length() > 500)) invalid("静态文本不合法");
        if (layer.has("color") && (!layer.get("color").isTextual() || !COLOR.matcher(layer.get("color").textValue()).matches())) invalid("字体颜色不合法");
        if (layer.has("fontSize") && (!layer.get("fontSize").canConvertToInt() || layer.get("fontSize").asInt() < 10 || layer.get("fontSize").asInt() > 160)) invalid("字体大小不合法");
        if (layer.has("maxLength") && (!layer.get("maxLength").canConvertToInt() || layer.get("maxLength").asInt() < 1 || layer.get("maxLength").asInt() > 200)) invalid("文本长度限制不合法");
    }

    private static JsonNode required(JsonNode root, String name) { JsonNode value = root.get(name); if (value == null) invalid("缺少字段: " + name); return value; }
    private static int integer(JsonNode node, String name, int min, int max) { JsonNode value = required(node, name); if (!value.canConvertToInt() || value.asInt() < min || value.asInt() > max) invalid(name + "不合法"); return value.asInt(); }
    private static double number(JsonNode node, String name, double fallback) { JsonNode value = node.get(name); if (value == null || !value.isNumber() || !Double.isFinite(value.asDouble())) invalid(name + "不合法"); return value.asDouble(); }
    private static void invalid(String message) { throw new IllegalArgumentException(message); }
}
