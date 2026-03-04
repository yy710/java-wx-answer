package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.Topic;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.web.vo.TopicLineDataVo;

import java.util.List;

/**
 * <p>
 * 题目信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicService extends IService<Topic> {

    TopicLineDataVo listByQuery(String topicLineId);

    List<Topic> listRandom();
}
