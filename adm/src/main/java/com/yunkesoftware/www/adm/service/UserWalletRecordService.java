package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.UserWalletRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户钱包记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface UserWalletRecordService extends IService<UserWalletRecord> {

    Page<UserWalletRecord> pageByQuery(UserWalletRecord userWalletRecord);
}
