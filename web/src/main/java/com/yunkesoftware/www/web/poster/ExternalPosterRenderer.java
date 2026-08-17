package com.yunkesoftware.www.web.poster;

import com.fasterxml.jackson.databind.JsonNode;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.PosterTemplateAsset;
import com.yunkesoftware.www.web.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** 第一阶段禁止外部渲染服务；保留适配器以避免管理端录入外部地址。 */
@Service("externalPosterRenderer")
public class ExternalPosterRenderer implements PosterRenderer {
    @Override
    public byte[] render(JsonNode template, List<PosterTemplateAsset> assets, User user, Map<String, String> custom) {
        throw new YunKeException(ExceptionEnum.FAIL, "NOT_CONFIGURED:外部海报渲染器未配置");
    }
}
