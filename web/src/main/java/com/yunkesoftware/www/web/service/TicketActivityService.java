package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.TicketActivity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 投票活动信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TicketActivityService extends IService<TicketActivity> {

    TicketActivity getOpen();

    Integer surplusTicket(String id);
}
