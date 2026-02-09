package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.TopicRecordTopicItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 答题记录-题目选项 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicRecordTopicItemMapper extends BaseMapper<TopicRecordTopicItem> {

    void insertBatch(@Param("dataList") List<TopicRecordTopicItem> recordTopicItemList);
}
