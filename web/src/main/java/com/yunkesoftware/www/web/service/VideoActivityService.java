package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.VideoActivity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 视频积分活动 服务类
 * </p>
 *
 * @author yk
 * @since 2026-02-11
 */
public interface VideoActivityService extends IService<VideoActivity> {

    VideoActivity getOpen();
}
