package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.TopicRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.web.vo.TopicRecordVo;

/**
 * <p>
 * 用户答题记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicRecordService extends IService<TopicRecord> {

    void add(TopicRecordVo vo);

    TopicRecord getOneByTopicLineId(String topicLineId);
}
