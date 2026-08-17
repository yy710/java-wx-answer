package com.yunkesoftware.www.web.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.DailyTaskConfig;
import com.yunkesoftware.www.web.entity.RewardClaim;
import com.yunkesoftware.www.web.entity.UserWallet;
import com.yunkesoftware.www.web.entity.UserWalletRecord;
import com.yunkesoftware.www.web.mapper.DailyTaskConfigMapper;
import com.yunkesoftware.www.web.mapper.RewardClaimMapper;
import com.yunkesoftware.www.web.mapper.UserWalletMapper;
import com.yunkesoftware.www.web.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.web.service.RewardPositiveService;
import com.yunkesoftware.www.web.vo.RewardPositiveResult;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 所有正向积分的唯一结算入口。占位、任务完成、钱包更新和流水写入必须在本事务内完成。
 */
@Service
public class RewardPositiveServiceImpl implements RewardPositiveService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final BigDecimal DEFAULT_WALLET_MAX = new BigDecimal("4000.00");
    private static final BigDecimal DEFAULT_DAILY_MAX = new BigDecimal("60.00");

    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;
    @Resource
    private RewardClaimMapper rewardClaimMapper;
    @Resource
    private DailyTaskConfigMapper dailyTaskConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RewardPositiveResult reward(String userId, String eventId, Integer eventType, BigDecimal requestedPoints) {
        if (userId == null || userId.isBlank() || eventId == null || eventId.isBlank()
                || eventType == null || requestedPoints == null || requestedPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new YunKeException(ExceptionEnum.FAIL, "奖励积分无效");
        }
        BigDecimal requested = money(requestedPoints);
        LocalDate taskDate = LocalDate.now(BUSINESS_ZONE);
        String claimEventId = claimKey(eventType, eventId);

        // The wallet row is locked before the idempotency read so concurrent reward
        // requests for a newly-created account serialize on the same unique wallet.
        UserWallet wallet = userWalletMapper.selectIntegralForUpdate(userId, UserWalletTypeEnum.INTEGRAL.getKey());
        if (wallet == null) {
            UserWallet seed = new UserWallet().setId(IdUtil.getSnowflakeNextIdStr()).setUserId(userId)
                    .setType(UserWalletTypeEnum.INTEGRAL.getKey()).setAmount(BigDecimal.ZERO).setVersion(0);
            try {
                userWalletMapper.insert(seed);
            } catch (DuplicateKeyException ignored) {
                // 并发请求已经创建钱包；下面用 FOR UPDATE 重新锁定。
            }
            wallet = userWalletMapper.selectIntegralForUpdate(userId, UserWalletTypeEnum.INTEGRAL.getKey());
        }
        if (wallet == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "积分账户异常-请刷新重试");
        }
        if (wallet.getAmount() == null) wallet.setAmount(BigDecimal.ZERO);
        if (wallet.getVersion() == null) wallet.setVersion(0);

        RewardClaim existing = rewardClaimMapper.selectForUpdate(userId, claimEventId);
        if (existing != null) {
            return result(existing.getRequestedPoints(), existing.getAwardedPoints(), "ALREADY_CLAIMED",
                    wallet.getAmount(), null, true, wallet.getId());
        }

        RewardClaim claim = new RewardClaim().setId(IdUtil.getSnowflakeNextIdStr()).setUserId(userId)
                .setEventId(claimEventId).setEventType(eventType).setTaskDate(taskDate)
                .setRequestedPoints(requested).setAwardedPoints(BigDecimal.ZERO).setStatus("PENDING")
                .setCreatedAt(now());
        try {
            rewardClaimMapper.insert(claim);
        } catch (DuplicateKeyException duplicate) {
            RewardClaim retry = rewardClaimMapper.selectForUpdate(userId, claimEventId);
            return result(retry == null ? requested : retry.getRequestedPoints(), retry == null ? BigDecimal.ZERO : retry.getAwardedPoints(),
                    "ALREADY_CLAIMED", wallet.getAmount(), null, true, wallet.getId());
        }

        String ledgerEventId = ledgerEventId(eventId, eventType);
        UserWalletRecord historical = userWalletRecordMapper.findPositiveEvent(wallet.getId(), ledgerEventId, eventType);
        if (historical != null) {
            claim.setAwardedPoints(money(historical.getChangeAmount())).setStatus("SUCCESS").setClaimedAt(historical.getCreateTime());
            rewardClaimMapper.updateById(claim);
            return result(requested, historical.getChangeAmount(), "ALREADY_CLAIMED", wallet.getAmount(), historical.getId(), true, wallet.getId());
        }

        DailyTaskConfig config = dailyTaskConfigMapper.selectById(1);
        BigDecimal walletLimit = config == null || config.getWalletMaxPoints() == null ? DEFAULT_WALLET_MAX : money(config.getWalletMaxPoints());
        BigDecimal dailyLimit = config == null || config.getDailyPositiveMaxPoints() == null ? DEFAULT_DAILY_MAX : money(config.getDailyPositiveMaxPoints());
        if (walletLimit.compareTo(BigDecimal.ZERO) < 0 || dailyLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new YunKeException(ExceptionEnum.FAIL, "积分上限配置无效");
        }
        BigDecimal todayPositive = positiveBetween(wallet.getId(), taskDate);
        if (todayPositive == null) todayPositive = BigDecimal.ZERO;
        todayPositive = money(todayPositive);
        BigDecimal dailyRemaining = maxZero(dailyLimit.subtract(todayPositive));
        BigDecimal walletRemaining = maxZero(walletLimit.subtract(money(wallet.getAmount())));
        BigDecimal awarded = requested.min(dailyRemaining).min(walletRemaining).setScale(2, RoundingMode.DOWN);
        String reason = reason(requested, awarded, dailyRemaining, walletRemaining);
        BigDecimal after = money(wallet.getAmount());
        String walletRecordId = null;
        if (awarded.compareTo(BigDecimal.ZERO) > 0) {
            after = after.add(awarded).setScale(2, RoundingMode.DOWN);
            int updated = userWalletMapper.update(new LambdaUpdateWrapper<UserWallet>()
                    .eq(UserWallet::getId, wallet.getId()).eq(UserWallet::getVersion, wallet.getVersion())
                    .set(UserWallet::getAmount, after).set(UserWallet::getVersion, wallet.getVersion() + 1));
            if (updated != 1) throw new YunKeException(ExceptionEnum.FAIL, "积分入账失败-请刷新重试");
            walletRecordId = IdUtil.getSnowflakeNextIdStr();
            UserWalletRecord record = new UserWalletRecord().setId(walletRecordId).setWalletId(wallet.getId())
                    .setEventId(ledgerEventId).setEventType(eventType).setChangeAmount(awarded)
                    .setAfterAmount(after).setStatus(true).setCreateTime(now());
            userWalletRecordMapper.insert(record);
            wallet.setVersion(wallet.getVersion() + 1);
        }
        claim.setAwardedPoints(awarded).setStatus(awarded.compareTo(BigDecimal.ZERO) > 0 ? "SUCCESS" : "CAPPED")
                .setClaimedAt(now());
        rewardClaimMapper.updateById(claim);
        return result(requested, awarded, reason, after, walletRecordId, false, wallet.getId());
    }

    private RewardPositiveResult result(BigDecimal requested, BigDecimal awarded, String reason, BigDecimal walletAmount,
                                        String walletRecordId, boolean duplicate, String walletId) {
        LocalDate date = LocalDate.now(BUSINESS_ZONE);
        BigDecimal earned = positiveBetween(walletId, date);
        if (earned == null) earned = BigDecimal.ZERO;
        DailyTaskConfig config = dailyTaskConfigMapper.selectById(1);
        BigDecimal limit = config == null || config.getDailyPositiveMaxPoints() == null ? DEFAULT_DAILY_MAX : money(config.getDailyPositiveMaxPoints());
        return new RewardPositiveResult().setCompleted(true).setRequestedPoints(format(requested)).setAwardedPoints(format(awarded))
                .setAwardReason(reason).setDailyEarnedPoints(format(earned)).setDailyRemainingPoints(format(maxZero(limit.subtract(earned))))
                .setWalletPoints(format(walletAmount)).setWalletRecordId(walletRecordId).setDuplicate(duplicate);
    }

    private static String claimKey(Integer eventType, String eventId) {
        String value = eventType + ":" + eventId;
        return value.length() <= 128 ? value : value.substring(0, 64) + ":" + sha256(value).substring(0, 32);
    }

    private static String ledgerEventId(String eventId, Integer eventType) {
        if (eventId.matches("\\d+")) {
            try {
                BigInteger numeric = new BigInteger(eventId);
                if (numeric.signum() >= 0 && numeric.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) return eventId;
            } catch (NumberFormatException ignored) {
                // fall through to the bounded hash representation
            }
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((eventType + ":" + eventId).getBytes(StandardCharsets.UTF_8));
            BigInteger numeric = new BigInteger(1, digest);
            // user_wallet_record.event_id is BIGINT in the legacy schema. Keep the
            // compatibility key positive and within signed BIGINT instead of writing
            // a hexadecimal string that MySQL would truncate or reject.
            BigInteger range = BigInteger.valueOf(8_000_000_000_000_000_000L);
            return numeric.mod(range).add(BigInteger.valueOf(1_000_000_000_000_000_000L)).toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String reason(BigDecimal requested, BigDecimal awarded, BigDecimal daily, BigDecimal wallet) {
        if (awarded.compareTo(requested) == 0) return "FULL";
        if (daily.compareTo(requested) < 0 && wallet.compareTo(requested) < 0) return "DAILY_AND_WALLET_CAP";
        if (daily.compareTo(requested) < 0) return "PARTIAL_DAILY_CAP";
        if (wallet.compareTo(requested) < 0) return "PARTIAL_WALLET_CAP";
        return "NOT_ELIGIBLE";
    }

    private static BigDecimal money(BigDecimal value) { return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.DOWN); }
    private static BigDecimal maxZero(BigDecimal value) { return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2) : money(value); }
    private static String format(BigDecimal value) { return money(value).toPlainString(); }
    private static LocalDateTime now() { return LocalDateTime.now(BUSINESS_ZONE); }
    private BigDecimal positiveBetween(String walletId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay(BUSINESS_ZONE).toLocalDateTime();
        LocalDateTime end = date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toLocalDateTime();
        return userWalletRecordMapper.sumPositiveBetween(walletId, start, end);
    }
    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte item : digest) result.append(String.format("%02x", item));
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
