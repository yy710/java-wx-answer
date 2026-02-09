package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.UserWalletRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户钱包记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface UserWalletRecordMapper extends BaseMapper<UserWalletRecord> {

    Page<UserWalletRecord> pageByQuery(Page<UserWalletRecord> pageParam, @Param("param") UserWalletRecord userWalletRecord);
}
