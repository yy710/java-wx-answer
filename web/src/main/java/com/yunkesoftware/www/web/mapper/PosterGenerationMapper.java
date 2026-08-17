package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.PosterGeneration;
import org.apache.ibatis.annotations.Param;

public interface PosterGenerationMapper extends BaseMapper<PosterGeneration> {
    PosterGeneration selectCache(@Param("userId") String userId, @Param("templateId") String templateId, @Param("templateVersion") Integer templateVersion, @Param("inputSha256") String inputSha256);
}
