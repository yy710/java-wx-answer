package com.yunkesoftware.www.adm.service;

import com.yunkesoftware.www.adm.entity.VideoActivity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 视频积分活动 服务类
 * </p>
 *
 * @author yk
 * @since 2026-02-11
 */
public interface VideoActivityService extends IService<VideoActivity> {

    void addOrModify(VideoActivity videoActivity);

    void delete(List<String> ids);
}
