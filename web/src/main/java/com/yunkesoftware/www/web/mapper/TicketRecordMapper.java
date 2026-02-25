package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.TicketRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 投票记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TicketRecordMapper extends BaseMapper<TicketRecord> {

    long countUser(@Param("activityId") String id);
}
