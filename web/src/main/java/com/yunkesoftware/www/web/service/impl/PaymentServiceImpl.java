package com.yunkesoftware.www.web.service.impl;

import com.yunkesoftware.www.web.entity.Payment;
import com.yunkesoftware.www.web.mapper.PaymentMapper;
import com.yunkesoftware.www.web.service.PaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 活动支付信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-19
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

}
