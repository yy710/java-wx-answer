package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TicketRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 投票记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface TicketRecordMapper extends BaseMapper<TicketRecord> {

    Page<TicketRecord> pageByQuery(Page<TicketRecord> pageParam, @Param("param") TicketRecord ticketRecord);
}
