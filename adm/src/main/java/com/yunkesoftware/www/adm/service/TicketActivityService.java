package com.yunkesoftware.www.adm.service;

import com.yunkesoftware.www.adm.entity.TicketActivity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 投票活动信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface TicketActivityService extends IService<TicketActivity> {

    void addOrModify(TicketActivity ticketActivity);

    void delete(List<String> ids);
}
