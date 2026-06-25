package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.Topic;
import com.yunkesoftware.www.web.entity.TopicRecordSingle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 趣味答题记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-02-12
 */
public interface TopicRecordSingleMapper extends BaseMapper<TopicRecordSingle> {

    Set<String> listRightTopicId(@Param("userId") String userId);

    String selectRewardedRecordId(@Param("userId") String userId, @Param("topicId") String topicId);

}
