package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.UserWallet;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户钱包 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface UserWalletService extends IService<UserWallet> {

    Page<UserWallet> pageByQuery(UserWallet userWallet);
}
