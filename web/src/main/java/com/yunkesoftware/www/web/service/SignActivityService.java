package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.SignActivity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 签到活动 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
public interface SignActivityService extends IService<SignActivity> {

    BigDecimal dailySign();
}
