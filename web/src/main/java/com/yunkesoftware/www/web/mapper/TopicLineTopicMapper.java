package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.Topic;
import com.yunkesoftware.www.web.entity.TopicLineTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 线路-题目信息 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicLineTopicMapper extends BaseMapper<TopicLineTopic> {

    List<Topic> listByTopicLineId(@Param("topicLineId") String topicLineId);
}
