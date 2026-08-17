package com.yunkesoftware.www.web.poster;

import com.fasterxml.jackson.databind.JsonNode;
import com.yunkesoftware.www.web.entity.PosterTemplateAsset;
import com.yunkesoftware.www.web.entity.User;

import java.util.List;
import java.util.Map;

/** 海报渲染适配器；业务服务不得直接依赖具体渲染库。 */
public interface PosterRenderer {
    byte[] render(JsonNode template, List<PosterTemplateAsset> assets, User user, Map<String, String> custom) throws Exception;
}
