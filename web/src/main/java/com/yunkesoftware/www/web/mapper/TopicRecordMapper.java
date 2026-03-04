package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.TopicRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.vo.TopicRankVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户答题记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicRecordMapper extends BaseMapper<TopicRecord> {

    Integer countGtTotalRewardAmount(@Param("topicActivityId") String topicActivityId, @Param("userId") String userId, @Param("totalRewardAmount") BigDecimal totalRewardAmount);

    Set<String> listLineId(@Param("userId") String userId, @Param("topicActivityId") String topicActivityId,
                           @Param("dateParam") LocalDate dateParam, @Param("topicLineId") String topicLineId);
}
