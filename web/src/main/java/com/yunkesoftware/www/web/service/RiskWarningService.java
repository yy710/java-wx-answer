package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.RiskWarning;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 风险提示 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface RiskWarningService extends IService<RiskWarning> {

    BigDecimal readFinish(String id);

    RiskWarning getOneById(String id);
}
