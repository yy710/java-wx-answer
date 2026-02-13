package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TopicRecordSingle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 趣味答题记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-02-12
 */
public interface TopicRecordSingleMapper extends BaseMapper<TopicRecordSingle> {

    Page<TopicRecordSingle> pageByQuery(Page<TopicRecordSingle> pageParam, @Param("param") TopicRecordSingle topicRecordSingle);
}
