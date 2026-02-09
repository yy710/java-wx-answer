package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.UserWalletRecord;
import com.yunkesoftware.www.adm.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.adm.service.UserWalletRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import org.springframework.stereotype.Service;

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
    public Page<UserWalletRecord> pageByQuery(UserWalletRecord userWalletRecord) {
        Page<UserWalletRecord> pageParam = new Page<>(userWalletRecord.getPageNum(), userWalletRecord.getPageSize());
        Page<UserWalletRecord> pageResult = baseMapper.pageByQuery(pageParam, userWalletRecord);
        for (UserWalletRecord record : pageResult.getRecords()) {
            record.setEventTypeName(UserWalletEventEnum.keyToLabel(record.getEventType()));
            record.setBeforeAmount(record.getAfterAmount().subtract(record.getChangeAmount()));
        }
        return pageResult;
    }
}
