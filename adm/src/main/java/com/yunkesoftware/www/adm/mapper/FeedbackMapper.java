package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.Feedback;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户反馈 Mapper 接口
 * </p>
 *
 * @author cuiyq
 * @since 2023-07-12
 */
public interface FeedbackMapper extends BaseMapper<Feedback> {

    Page<Feedback> pageByQuery(Page<Feedback> pageParam, @Param("param") Feedback feedback);
}
