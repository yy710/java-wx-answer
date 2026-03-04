package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.web.entity.Video;
import com.yunkesoftware.www.web.entity.VideoActivityVideo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 视频活动-视频信息 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-02-26
 */
public interface VideoActivityVideoMapper extends BaseMapper<VideoActivityVideo> {

    Page<Video> pageByQuery(Page<Video> pageParam, @Param("param") Video video);
}
