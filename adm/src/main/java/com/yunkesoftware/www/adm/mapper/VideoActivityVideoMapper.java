package com.yunkesoftware.www.adm.mapper;

import com.yunkesoftware.www.adm.entity.VideoActivityVideo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 视频活动-视频信息 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-02-26
 */
public interface VideoActivityVideoMapper extends BaseMapper<VideoActivityVideo> {

    void insertBatch(@Param("dateList") List<VideoActivityVideo> videoActivityVideoList);
}
