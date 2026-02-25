package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.*;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.RiskWarningService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.web.service.TimeLimitService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 风险提示 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Service
public class RiskWarningServiceImpl extends ServiceImpl<RiskWarningMapper, RiskWarning> implements RiskWarningService {
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;
    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private RiskWarningRecordMapper riskWarningRecordMapper;
    @Resource
    private RewardSetMapper rewardSetMapper;
    @Resource
    private TimeLimitService timeLimitService;

    @Override
    public BigDecimal readFinish(String id) {
        //2026-2-24新增检查积分时间 3月1日00:01-3月14日16:00
        timeLimitService.checkTimeLimit();

        RiskWarning checkData = baseMapper.selectOne(new LambdaQueryWrapper<RiskWarning>()
                .eq(RiskWarning::getId, id)
                .select(RiskWarning::getId, RiskWarning::getStatus, RiskWarning::getRewardAmount, RiskWarning::getRewardNum));
        if (checkData == null || !checkData.getStatus()
                || checkData.getRewardAmount().compareTo(BigDecimal.ZERO) < 1) {
            throw new YunKeException(ExceptionEnum.FAIL, "风险提示不存在或已删除或未设置积分奖励");
        }

        String userId = StpUtil.getLoginIdAsString();

        RewardSet rewardSet = rewardSetMapper.selectOne(new LambdaQueryWrapper<RewardSet>()
                .eq(RewardSet::getType, 1));
        // 首次阅读+每日阅读数量限制
        if (rewardSet != null) {
            // 当日首次阅读才进行赠送
            if (rewardSet.getFirstFlag()) {
                RiskWarningRecord riskWarningRecord = riskWarningRecordMapper.selectOne(new LambdaQueryWrapper<RiskWarningRecord>()
                        .eq(RiskWarningRecord::getUserId, userId)
                        .eq(RiskWarningRecord::getRiskWarnId, id)
                        .last("LIMIT 1"));
                // 今天之前已经阅读过(不算首次阅读不进行奖励赠送)
                if (riskWarningRecord != null && riskWarningRecord.getCreateTime().toLocalDate().isBefore(LocalDate.now())) {
                    String formatTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(riskWarningRecord.getCreateTime());
                    throw new YunKeException(ExceptionEnum.FAIL, "首次阅读才可获得积分，当前首次阅读时间为：" + formatTime);
                }
            }
        }

        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        UserWalletRecord walletRecord = new UserWalletRecord();
        walletRecord.setEventId(id);
        walletRecord.setEventType(UserWalletEventEnum.RISK_READ.getKey());
        walletRecord.setChangeAmount(checkData.getRewardAmount());

        if (userWallet == null) {
            userWallet = new UserWallet();
            userWallet.setUserId(userId);
            userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
            userWallet.setAmount(checkData.getRewardAmount());
            userWallet.setVersion(0);
            int insertRow = userWalletMapper.insert(userWallet);
            walletRecord.setWalletId(userWallet.getId());
            walletRecord.setStatus(insertRow > 0);
            walletRecord.setAfterAmount(userWallet.getAmount());
            userWalletRecordMapper.insert(walletRecord);

            checkData.setRewardNum(checkData.getRewardNum() + 1);
            baseMapper.updateById(checkData);
            return checkData.getRewardAmount();
        } else {
            // 检查当天阅读奖励次数
            if (rewardSet != null) {
                int dailyRewardNum = userWalletRecordMapper.countTodayNum(userWallet.getId(), UserWalletEventEnum.RISK_READ.getKey(), LocalDate.now());
                if (rewardSet.getRewardLimit() <= dailyRewardNum) {
                    throw new YunKeException(ExceptionEnum.FAIL, "今日已达到次数上限" + rewardSet.getRewardLimit());
                }
            }
            // 没有赠送过才进行赠送
            UserWalletRecord checkRecord = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                    .eq(UserWalletRecord::getWalletId, userWallet.getId())
                    .eq(UserWalletRecord::getEventId, id)
                    .eq(UserWalletRecord::getEventType, UserWalletEventEnum.RISK_READ.getKey())
                    .select(UserWalletRecord::getId, UserWalletRecord::getStatus)
                    .last("LIMIT 1"));
            if (checkRecord == null) {
                BigDecimal afterAmount = userWallet.getAmount().add(checkData.getRewardAmount());
                int updateRow = userWalletMapper.update(new LambdaUpdateWrapper<UserWallet>()
                        .eq(UserWallet::getId, userWallet.getId())
                        .eq(UserWallet::getVersion, userWallet.getVersion())
                        .set(UserWallet::getVersion, userWallet.getVersion() + 1)
                        .set(UserWallet::getAmount, afterAmount));
                walletRecord.setWalletId(userWallet.getId());
                walletRecord.setStatus(updateRow > 0);
                walletRecord.setAfterAmount(afterAmount);
                userWalletRecordMapper.insert(walletRecord);
                checkData.setRewardNum(checkData.getRewardNum() + 1);
                baseMapper.updateById(checkData);
                return checkData.getRewardAmount();
            }
        }
        return BigDecimal.ZERO;
    }


    @Override
    public RiskWarning getOneById(String id) {
        RiskWarning riskWarning = baseMapper.selectById(id);
        if (riskWarning != null) {
            String userId = StpUtil.getLoginIdAsString();

            RiskWarningRecord riskWarningRecord = riskWarningRecordMapper.selectOne(new LambdaQueryWrapper<RiskWarningRecord>()
                    .eq(RiskWarningRecord::getRiskWarnId, id)
                    .eq(RiskWarningRecord::getUserId, userId));

            if (riskWarningRecord != null) {
                riskWarning.setReadFlag(true);
                UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getUserId, StpUtil.getLoginIdAsString())
                        .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
                if (userWallet != null) {
                    UserWalletRecord checkRecord = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                            .eq(UserWalletRecord::getWalletId, userWallet.getId())
                            .eq(UserWalletRecord::getEventId, id)
                            .eq(UserWalletRecord::getEventType, UserWalletEventEnum.RISK_READ.getKey())
                            .select(UserWalletRecord::getId, UserWalletRecord::getStatus));
                    riskWarning.setRewardFlag(checkRecord != null);
                }
            } else {
                riskWarningRecord = new RiskWarningRecord();
                riskWarningRecord.setRiskWarnId(id);
                riskWarningRecord.setUserId(userId);
                riskWarningRecordMapper.insert(riskWarningRecord);
            }
            baseMapper.update(new LambdaUpdateWrapper<RiskWarning>()
                    .eq(RiskWarning::getId, id)
                    .set(RiskWarning::getViewNum, riskWarning.getViewNum() + 1));
        }
        return riskWarning;
    }
}
