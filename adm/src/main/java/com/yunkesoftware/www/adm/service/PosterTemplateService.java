package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.PosterTemplate;

public interface PosterTemplateService extends IService<PosterTemplate> {
    PosterTemplate getWithAssets(String id);
    PosterTemplate saveTemplate(PosterTemplate template, String operatorId);
    void publish(String id, Integer version);
    void disable(String id, Integer version);
    PosterTemplate preview(PosterTemplate template);
}
