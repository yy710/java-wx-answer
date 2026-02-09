package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TopicRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户答题记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TopicRecordMapper extends BaseMapper<TopicRecord> {

    Page<TopicRecord> pageByQuery(Page<TopicRecord> pageParam, @Param("param") TopicRecord topicRecord);
}
