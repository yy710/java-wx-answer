package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TopicRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户答题记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TopicRecordService extends IService<TopicRecord> {

    Page<TopicRecord> pageByQuery(TopicRecord topicRecord);

    TopicRecord getOneById(String id);
}
