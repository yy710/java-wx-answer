package com.yunkesoftware.www.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.web.entity.Video;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 视频信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface VideoService extends IService<Video> {

    Page<Video> pageByQuery(Video video);

    void doVote(String id);

    void doReward(String id);

    Video getOneById(String id);
}
