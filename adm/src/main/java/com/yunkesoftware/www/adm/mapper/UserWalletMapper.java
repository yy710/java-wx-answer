package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.UserWallet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户钱包 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    Page<UserWallet> pageByQuery(Page<UserWallet> pageParam, @Param("param") UserWallet userWallet);
}
