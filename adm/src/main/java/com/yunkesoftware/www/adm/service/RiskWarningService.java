package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.RiskWarning;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 风险提示 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface RiskWarningService extends IService<RiskWarning> {

    Page<RiskWarning> pageByQuery(RiskWarning riskWarning);
}
