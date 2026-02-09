package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TicketRecord;
import com.yunkesoftware.www.adm.mapper.TicketRecordMapper;
import com.yunkesoftware.www.adm.service.TicketRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 投票记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Service
public class TicketRecordServiceImpl extends ServiceImpl<TicketRecordMapper, TicketRecord> implements TicketRecordService {

    @Override
    public Page<TicketRecord> pageByQuery(TicketRecord ticketRecord) {
        Page<TicketRecord> pageParam = new Page<>(ticketRecord.getPageNum(), ticketRecord.getPageSize());
        return baseMapper.pageByQuery(pageParam, ticketRecord);
    }
}
