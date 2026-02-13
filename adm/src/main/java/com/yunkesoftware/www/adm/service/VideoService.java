package com.yunkesoftware.www.adm.service;

import com.yunkesoftware.www.adm.entity.Video;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 视频信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface VideoService extends IService<Video> {

    void addOrModify(Video ticketVideo);
}
