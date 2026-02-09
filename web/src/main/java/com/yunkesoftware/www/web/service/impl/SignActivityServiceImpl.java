package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.SignActivity;
import com.yunkesoftware.www.web.entity.UserWallet;
import com.yunkesoftware.www.web.entity.UserWalletRecord;
import com.yunkesoftware.www.web.mapper.SignActivityMapper;
import com.yunkesoftware.www.web.mapper.UserWalletMapper;
import com.yunkesoftware.www.web.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.web.service.SignActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 签到活动 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
@Service
public class SignActivityServiceImpl extends ServiceImpl<SignActivityMapper, SignActivity> implements SignActivityService {
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserWalletMapper userWalletMapper;

    @Override
    public BigDecimal dailySign() {
        String userId = StpUtil.getLoginIdAsString();
        LocalDateTime nowTime = LocalDateTime.now();
        SignActivity signActivity = (SignActivity) redisTemplate.opsForValue().get(RedisKey.SIGN_ACTIVITY);

        if (signActivity == null) {
            signActivity = baseMapper.selectOne(new LambdaQueryWrapper<SignActivity>()
                    .le(SignActivity::getStartTime, nowTime)
                    .gt(SignActivity::getEndTime, nowTime));
            if (signActivity != null) {
                long seconds = Duration.between(nowTime, signActivity.getEndTime()).getSeconds();
                redisTemplate.opsForValue().set(RedisKey.SIGN_ACTIVITY, signActivity, seconds, TimeUnit.SECONDS);
            }
        }
        if (signActivity == null || nowTime.isAfter(signActivity.getEndTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "无进行中的签到活动");
        }
        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));

        UserWalletRecord walletRecord = new UserWalletRecord();
        walletRecord.setChangeAmount(signActivity.getRewardAmount());
        walletRecord.setEventId(signActivity.getId());
        walletRecord.setEventType(UserWalletEventEnum.DAILY_SIGN.getKey());

        if (userWallet == null) {
            userWallet = new UserWallet();
            userWallet.setUserId(userId);
            userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
            userWallet.setAmount(signActivity.getRewardAmount());
            int insertRow = userWalletMapper.insert(userWallet);
            walletRecord.setAfterAmount(signActivity.getRewardAmount());
            walletRecord.setStatus(insertRow > 0);
        } else {
            UserWalletRecord checkWallet = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                    .eq(UserWalletRecord::getWalletId, userWallet.getId())
                    .eq(UserWalletRecord::getEventId, signActivity.getId())
                    .eq(UserWalletRecord::getEventType, UserWalletEventEnum.DAILY_SIGN.getKey())
                    .orderByDesc(UserWalletRecord::getId)
                    .select(UserWalletRecord::getId, UserWalletRecord::getCreateTime)
                    .last("LIMIT 1"));
            if (checkWallet != null && checkWallet.getCreateTime().toLocalDate().equals(nowTime.toLocalDate())) {
                throw new YunKeException(ExceptionEnum.FAIL, "今天已签到");
            }
            BigDecimal afterAmount = userWallet.getAmount().add(signActivity.getRewardAmount());
            int updateRow = userWalletMapper.update(new LambdaUpdateWrapper<UserWallet>()
                    .eq(UserWallet::getId, userWallet.getId())
                    .eq(UserWallet::getVersion, userWallet.getVersion())
                    .set(UserWallet::getVersion, userWallet.getVersion() + 1)
                    .set(UserWallet::getAmount, afterAmount));
            if (updateRow == 0) {
                throw new YunKeException(ExceptionEnum.FAIL, "网络异常-请刷新重试");
            }
            walletRecord.setAfterAmount(afterAmount);
            walletRecord.setStatus(true);
        }

        walletRecord.setWalletId(userWallet.getId());
        userWalletRecordMapper.insert(walletRecord);

        return signActivity.getRewardAmount();
    }
}
