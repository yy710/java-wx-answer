package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TopicRecordSingle;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 趣味答题记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-02-12
 */
public interface TopicRecordSingleService extends IService<TopicRecordSingle> {

    Page<TopicRecordSingle> pageByQuery(TopicRecordSingle topicRecordSingle);

    TopicRecordSingle getOneById(String id);
}
