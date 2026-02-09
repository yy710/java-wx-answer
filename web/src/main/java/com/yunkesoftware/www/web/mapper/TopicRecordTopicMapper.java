package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.TopicRecordTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 答题记录题目 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicRecordTopicMapper extends BaseMapper<TopicRecordTopic> {

    void insertBatch(@Param("dataList") List<TopicRecordTopic> recordTopicList);
}
