package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.PosterTemplateAsset;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PosterTemplateAssetMapper extends BaseMapper<PosterTemplateAsset> {
    List<PosterTemplateAsset> listByTemplate(@Param("templateId") String templateId);
}
