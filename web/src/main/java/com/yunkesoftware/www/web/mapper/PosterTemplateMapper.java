package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.PosterTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PosterTemplateMapper extends BaseMapper<PosterTemplate> {
    List<PosterTemplate> listPublished();
    PosterTemplate selectPublished(@Param("id") String id, @Param("version") Integer version);
}
