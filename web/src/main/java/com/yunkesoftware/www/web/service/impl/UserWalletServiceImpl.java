package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.Payment;
import com.yunkesoftware.www.web.entity.UserWallet;
import com.yunkesoftware.www.web.entity.UserWalletRecord;
import com.yunkesoftware.www.web.mapper.PaymentMapper;
import com.yunkesoftware.www.web.mapper.UserWalletMapper;
import com.yunkesoftware.www.web.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.web.query.ScanPayQuery;
import com.yunkesoftware.www.web.service.UserWalletService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;

    @Override
    public UserWallet getOrCreateByType(Integer type) {
        String userId = StpUtil.getLoginIdAsString();
        UserWallet userWallet = baseMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, type));
        if (userWallet != null) {
            return userWallet;
        }

        userWallet = new UserWallet();
        userWallet.setUserId(userId);
        userWallet.setType(type);
        userWallet.setAmount(BigDecimal.ZERO);
        userWallet.setVersion(0);
        try {
            baseMapper.insert(userWallet);
            return userWallet;
        } catch (DuplicateKeyException ignored) {
            return baseMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId)
                    .eq(UserWallet::getType, type));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserWallet rewardIntegral(String userId, String eventId, Integer eventType, BigDecimal rewardAmount) {
        if (rewardAmount == null || rewardAmount.compareTo(BigDecimal.ZERO) < 1) {
            throw new YunKeException(ExceptionEnum.FAIL, "奖励积分无效");
        }

        UserWallet userWallet = baseMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        if (userWallet == null) {
            userWallet = new UserWallet();
            userWallet.setUserId(userId);
            userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
            userWallet.setAmount(rewardAmount);
            userWallet.setVersion(0);
            try {
                int insertRow = baseMapper.insert(userWallet);
                if (insertRow < 1) {
                    throw new YunKeException(ExceptionEnum.FAIL, "积分入账失败-请刷新重试");
                }
                insertWalletRecord(userWallet.getId(), eventId, eventType, rewardAmount, userWallet.getAmount());
                return userWallet;
            } catch (DuplicateKeyException ignored) {
                userWallet = baseMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getUserId, userId)
                        .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
            }
        }

        if (userWallet == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "积分账户异常-请刷新重试");
        }
        if (userWallet.getAmount().compareTo(BigDecimal.valueOf(4000)) >= 0) {
            throw new YunKeException(ExceptionEnum.FAIL, "已达积分上限");
        }

        BigDecimal afterAmount = userWallet.getAmount().add(rewardAmount);
        int updateRow = baseMapper.update(new LambdaUpdateWrapper<UserWallet>()
                .eq(UserWallet::getId, userWallet.getId())
                .eq(UserWallet::getVersion, userWallet.getVersion())
                .set(UserWallet::getVersion, userWallet.getVersion() + 1)
                .set(UserWallet::getAmount, afterAmount));
        if (updateRow < 1) {
            throw new YunKeException(ExceptionEnum.FAIL, "积分入账失败-请刷新重试");
        }

        userWallet.setAmount(afterAmount);
        userWallet.setVersion(userWallet.getVersion() + 1);
        insertWalletRecord(userWallet.getId(), eventId, eventType, rewardAmount, afterAmount);
        return userWallet;
    }

    private void insertWalletRecord(String walletId, String eventId, Integer eventType, BigDecimal changeAmount, BigDecimal afterAmount) {
        UserWalletRecord walletRecord = new UserWalletRecord();
        walletRecord.setWalletId(walletId);
        walletRecord.setEventId(eventId);
        walletRecord.setEventType(eventType);
        walletRecord.setChangeAmount(changeAmount);
        walletRecord.setAfterAmount(afterAmount);
        walletRecord.setStatus(true);
        userWalletRecordMapper.insert(walletRecord);
    }

    @Override
    public void scanPay(ScanPayQuery query) {
        String userId = StpUtil.getLoginIdAsString();
        Payment payment = paymentMapper.selectById(query.getPaymentId());
        if (payment == null || !payment.getStatus()) {
            throw new YunKeException(ExceptionEnum.FAIL, "收款方不存在");
        }
        UserWallet userWallet = baseMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        if (userWallet == null || userWallet.getAmount().compareTo(query.getAmount()) < 0) {
            throw new YunKeException(ExceptionEnum.FAIL, "剩余积分不足");
        }
        BigDecimal afterAmount = userWallet.getAmount().subtract(query.getAmount());
        int updateRow = baseMapper.update(new LambdaUpdateWrapper<UserWallet>()
                .eq(UserWallet::getId, userWallet.getId())
                .eq(UserWallet::getVersion, userWallet.getVersion())
                .set(UserWallet::getVersion, userWallet.getVersion() + 1)
                .set(UserWallet::getAmount, afterAmount));
        if (updateRow < 1) {
            throw new YunKeException(ExceptionEnum.FAIL, "网络异常-请刷新重试");
        }
        UserWalletRecord walletRecord = new UserWalletRecord();
        walletRecord.setEventId(payment.getId());
        walletRecord.setWalletId(userWallet.getId());
        walletRecord.setStatus(true);
        walletRecord.setChangeAmount(query.getAmount().negate());
        walletRecord.setAfterAmount(afterAmount);
        walletRecord.setEventType(UserWalletEventEnum.SCAN_PAY.getKey());
        userWalletRecordMapper.insert(walletRecord);
    }

    @Override
    public Boolean checkMax() {
        UserWallet userWallet = baseMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, StpUtil.getLoginIdAsString())
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        return userWallet != null && userWallet.getAmount().compareTo(BigDecimal.valueOf(4000)) > -1;
    }


}
