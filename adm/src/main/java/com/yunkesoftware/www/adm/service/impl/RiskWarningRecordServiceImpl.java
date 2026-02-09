package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.RiskWarningRecord;
import com.yunkesoftware.www.adm.entity.UserWalletRecord;
import com.yunkesoftware.www.adm.mapper.RiskWarningRecordMapper;
import com.yunkesoftware.www.adm.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.adm.service.RiskWarningRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 风险阅读记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
@Service
public class RiskWarningRecordServiceImpl extends ServiceImpl<RiskWarningRecordMapper, RiskWarningRecord> implements RiskWarningRecordService {
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;

    @Override
    public Page<RiskWarningRecord> pageByQuery(RiskWarningRecord riskWarningRecord) {
        Page<RiskWarningRecord> pageParam = new Page<>(riskWarningRecord.getPageNum(), riskWarningRecord.getPageSize());
        Page<RiskWarningRecord> pageResult = baseMapper.pageByQuery(pageParam, riskWarningRecord);
//        for (RiskWarningRecord record : pageResult.getRecords()) {
//
//        }
        return pageResult;
    }
}
