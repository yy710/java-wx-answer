package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.TopicRecordSingle;
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

    void add(TopicRecordSingle recordSingle);
}
