package com.yunkesoftware.www.adm.mapper;

import com.yunkesoftware.www.adm.entity.TopicLineTopic;
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

    void insertBatch(@Param("dataList") List<TopicLineTopic> topicLineTopicList);
}
