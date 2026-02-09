package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.UserWallet;
import com.yunkesoftware.www.adm.mapper.UserWalletMapper;
import com.yunkesoftware.www.adm.service.UserWalletService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户钱包 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet> implements UserWalletService {

    @Override
    public Page<UserWallet> pageByQuery(UserWallet userWallet) {
        Page<UserWallet> pageParam = new Page<>(userWallet.getPageNum(), userWallet.getPageSize());
        return baseMapper.pageByQuery(pageParam, userWallet);
    }
}
