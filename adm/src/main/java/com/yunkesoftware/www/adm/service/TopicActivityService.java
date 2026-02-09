package com.yunkesoftware.www.adm.service;

import com.yunkesoftware.www.adm.entity.TopicActivity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 答题活动 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TopicActivityService extends IService<TopicActivity> {

    void addOrModify(TopicActivity topicActivity);
}
