package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.Payment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 活动支付信息 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface PaymentService extends IService<Payment> {

    Page<Payment> pageByQuery(Payment payment);
}
