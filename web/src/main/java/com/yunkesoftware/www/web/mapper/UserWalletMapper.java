package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.web.entity.UserWallet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.vo.WalletRankVo;
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

    Page<WalletRankVo> pageRank(Page<WalletRankVo> pageParam);

    UserWallet selectIntegralForUpdate(@Param("userId") String userId, @Param("type") Integer type);
}
