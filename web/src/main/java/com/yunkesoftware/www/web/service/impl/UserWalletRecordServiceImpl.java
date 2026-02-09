package com.yunkesoftware.www.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.web.entity.UserWalletRecord;
import com.yunkesoftware.www.web.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.web.query.UserWalletRecordQuery;
import com.yunkesoftware.www.web.service.UserWalletRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 * 用户钱包记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Service
public class UserWalletRecordServiceImpl extends ServiceImpl<UserWalletRecordMapper, UserWalletRecord> implements UserWalletRecordService {

    @Override
    public Page<UserWalletRecord> pageByQuery(UserWalletRecordQuery query) {
        Page<UserWalletRecord> pageParam = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<UserWalletRecord> queryWrapper = new LambdaQueryWrapper<UserWalletRecord>()
                .eq(UserWalletRecord::getWalletId, query.getWalletId())
                .gt(Objects.equals(1, query.getInOut()), UserWalletRecord::getChangeAmount, 0)
                .lt(Objects.equals(2, query.getInOut()), UserWalletRecord::getChangeAmount, 0)
                .ge(query.getStartTime() != null, UserWalletRecord::getCreateTime, query.getStartTime())
                .le(query.getEndTime() != null, UserWalletRecord::getCreateTime, query.getEndTime())
                .orderByDesc(UserWalletRecord::getId);
        Page<UserWalletRecord> pageResult = baseMapper.selectPage(pageParam, queryWrapper);
        for (UserWalletRecord record : pageResult.getRecords()) {
            record.setEventTypeName(UserWalletEventEnum.keyToLabel(record.getEventType()));
        }
        return pageResult;
    }
}
