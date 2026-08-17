package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 题目信息 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicMapper extends BaseMapper<Topic> {

    List<Topic> listNewOrWrong(@Param("rightIds") Set<String> rightIds, @Param("topicNum") Integer topicNum);

    List<Topic> listRight(@Param("rightIds") Set<String> rightIds, @Param("topicNum") Integer topicNum);

    List<Topic> listDailyTaskTopics(@Param("topicNum") Integer topicNum);
}
