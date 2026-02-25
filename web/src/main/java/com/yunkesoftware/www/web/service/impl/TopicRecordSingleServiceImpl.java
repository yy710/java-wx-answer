package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.web.entity.*;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.TimeLimitService;
import com.yunkesoftware.www.web.service.TopicRecordSingleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 趣味答题记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-02-12
 */
@Service
public class TopicRecordSingleServiceImpl extends ServiceImpl<TopicRecordSingleMapper, TopicRecordSingle> implements TopicRecordSingleService {
    @Resource
    private TopicRecordSingleItemMapper topicRecordSingleItemMapper;
    @Resource
    private TopicItemMapper topicItemMapper;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;
    @Resource
    private TimeLimitService timeLimitService;

    @Override
    public void add(TopicRecordSingle recordSingle) {
        String userId = StpUtil.getLoginIdAsString();
        TopicRecordSingle checkData = baseMapper.selectOne(new LambdaQueryWrapper<TopicRecordSingle>()
                .eq(TopicRecordSingle::getUserId, userId)
                .eq(TopicRecordSingle::getTopicId, recordSingle.getTopicId())
                .eq(TopicRecordSingle::getRightFlag, true)
                .select(TopicRecordSingle::getId)
                .last("LIMIT 1"));
        if (checkData != null) {
            return;
        }
        Topic topic = topicMapper.selectById(recordSingle.getTopicId());
        if (topic == null) {
            return;
        }

        String id = IdUtil.getSnowflakeNextIdStr();
        recordSingle.setUserId(userId);
        recordSingle.setCreateTime(null);
        recordSingle.setTopicTitle(topic.getTitle());
        recordSingle.setId(id);
        recordSingle.setRightFlag(false);
        recordSingle.setRewardAmount(topic.getRewardAmount());

        for (TopicRecordSingleItem singleItem : recordSingle.getRecordSingleItemList()) {
            singleItem.setTopicRecordSingleId(id);
            singleItem.setAnswerFlag(false);
            if (Boolean.TRUE.equals(singleItem.getCheckFlag())) {
                TopicItem topicItem = topicItemMapper.selectById(singleItem.getTopicItemId());
                if (Boolean.TRUE.equals(topicItem.getAnswerFlag())) {
                    recordSingle.setRightFlag(true);
                    singleItem.setAnswerFlag(true);
                }
            }
        }
        baseMapper.insert(recordSingle);
        topicRecordSingleItemMapper.insertBatch(recordSingle.getRecordSingleItemList());

        if (recordSingle.getRightFlag() && topic.getRewardAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 检查时间限制
            timeLimitService.checkTimeLimit();
            UserWalletRecord walletRecord = new UserWalletRecord();
            walletRecord.setEventId(id);
            walletRecord.setEventType(UserWalletEventEnum.SINGLE_TOPIC_REWARD.getKey());
            walletRecord.setChangeAmount(topic.getRewardAmount());
            // 赠送用户积分
            UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId)
                    .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
            if (userWallet == null) {
                userWallet = new UserWallet();
                userWallet.setUserId(userId);
                userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
                userWallet.setAmount(recordSingle.getRewardAmount());
                int insertRow = userWalletMapper.insert(userWallet);
                walletRecord.setStatus(insertRow > 0);
                walletRecord.setAfterAmount(userWallet.getAmount());
            } else {
                BigDecimal afterAmount = userWallet.getAmount().add(recordSingle.getRewardAmount());
                int updateRow = userWalletMapper.update(new LambdaUpdateWrapper<UserWallet>()
                        .eq(UserWallet::getId, userWallet.getId())
                        .eq(UserWallet::getVersion, userWallet.getVersion())
                        .set(UserWallet::getVersion, userWallet.getVersion() + 1)
                        .set(UserWallet::getAmount, afterAmount));
                walletRecord.setStatus(updateRow > 0);
                walletRecord.setAfterAmount(afterAmount);
            }
            walletRecord.setWalletId(userWallet.getId());
            userWalletRecordMapper.insert(walletRecord);
        }
    }
}
