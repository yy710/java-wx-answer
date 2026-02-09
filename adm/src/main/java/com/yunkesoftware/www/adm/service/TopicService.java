package com.yunkesoftware.www.adm.service;

import com.yunkesoftware.www.adm.entity.Topic;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 题目信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TopicService extends IService<Topic> {

    void addOrModify(Topic topic);

    void delete(List<String> ids);

    Topic getOneById(String id);
}
