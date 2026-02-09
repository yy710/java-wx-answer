package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.RiskWarning;
import com.yunkesoftware.www.adm.mapper.RiskWarningMapper;
import com.yunkesoftware.www.adm.service.RiskWarningService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 风险提示 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Service
public class RiskWarningServiceImpl extends ServiceImpl<RiskWarningMapper, RiskWarning> implements RiskWarningService {

    @Override
    public Page<RiskWarning> pageByQuery(RiskWarning riskWarning) {
        Page<RiskWarning> pageParam = new Page<>(riskWarning.getPageNum(), riskWarning.getPageSize());
        return baseMapper.pageByQuery(pageParam, riskWarning);
    }
}
