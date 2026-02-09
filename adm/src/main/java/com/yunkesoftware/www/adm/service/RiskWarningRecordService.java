package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.RiskWarningRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 风险阅读记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
public interface RiskWarningRecordService extends IService<RiskWarningRecord> {

    Page<RiskWarningRecord> pageByQuery(RiskWarningRecord riskWarningRecord);
}
