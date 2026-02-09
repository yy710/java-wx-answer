package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.UserWallet;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.web.query.ScanPayQuery;

/**
 * <p>
 * 用户钱包 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface UserWalletService extends IService<UserWallet> {

    void scanPay(ScanPayQuery query);

}
