package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TicketRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 投票记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface TicketRecordService extends IService<TicketRecord> {

    Page<TicketRecord> pageByQuery(TicketRecord ticketRecord);
}
