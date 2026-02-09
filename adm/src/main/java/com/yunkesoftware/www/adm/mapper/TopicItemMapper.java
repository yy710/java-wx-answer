package com.yunkesoftware.www.adm.mapper;

import com.yunkesoftware.www.adm.entity.TopicItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 答题活动-题目选项 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TopicItemMapper extends BaseMapper<TopicItem> {

    void insertBatch(@Param("dataList") List<TopicItem> topicItemList);
}
