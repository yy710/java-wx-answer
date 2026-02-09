package com.yunkesoftware.www.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.web.entity.UserWalletRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.web.query.UserWalletRecordQuery;

/**
 * <p>
 * 用户钱包记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface UserWalletRecordService extends IService<UserWalletRecord> {

    Page<UserWalletRecord> pageByQuery(UserWalletRecordQuery query);
}
