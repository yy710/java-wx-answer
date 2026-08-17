package com.yunkesoftware.www.web.mapper;

import com.yunkesoftware.www.web.entity.UserWalletRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * <p>
 * 用户钱包记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface UserWalletRecordMapper extends BaseMapper<UserWalletRecord> {

    int countTodayNum(@Param("walletId") String walletId, @Param("eventType") Integer eventType, @Param("dateParam") LocalDate dateParam);

    BigDecimal sumPositiveToday(@Param("walletId") String walletId, @Param("dateParam") LocalDate dateParam);

    BigDecimal sumPositiveBetween(@Param("walletId") String walletId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    UserWalletRecord findPositiveEvent(@Param("walletId") String walletId, @Param("eventId") String eventId, @Param("eventType") Integer eventType);
}
