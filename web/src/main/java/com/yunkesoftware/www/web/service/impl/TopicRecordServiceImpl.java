package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.*;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.TimeLimitService;
import com.yunkesoftware.www.web.service.TopicRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.web.vo.TopicRecordTopicItemVo;
import com.yunkesoftware.www.web.vo.TopicRecordTopicVo;
import com.yunkesoftware.www.web.vo.TopicRecordVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用户答题记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Service
public class TopicRecordServiceImpl extends ServiceImpl<TopicRecordMapper, TopicRecord> implements TopicRecordService {
    @Resource
    private TopicRecordTopicMapper topicRecordTopicMapper;
    @Resource
    private TopicRecordTopicItemMapper topicRecordTopicItemMapper;
    @Resource
    private TopicActivityMapper topicActivityMapper;
    @Resource
    private TopicLineMapper topicLineMapper;
    @Resource
    private TopicItemMapper topicItemMapper;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;
    @Resource
    private TopicRecordActivityMapper topicRecordActivityMapper;
    @Resource
    private TimeLimitService timeLimitService;


    @Override
    public void add(TopicRecordVo vo) {
        TopicLine topicLine = topicLineMapper.selectById(vo.getTopicLineId());
        if (topicLine == null || Boolean.FALSE.equals(topicLine.getStatus())) {
            throw new YunKeException(ExceptionEnum.FAIL, "当前地图活动已关闭-请刷新页面重试");
        }
        TopicActivity topicActivity = topicActivityMapper.selectById(topicLine.getTopicActivityId());
        LocalDateTime nowTime = LocalDateTime.now();
        if (topicActivity == null || topicActivity.getEndTime().isBefore(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "当前活动已结束-请刷新页面重试");
        }

        String userId = StpUtil.getLoginIdAsString();

        // 首次答题才能获得积分
        TopicRecord checkData = baseMapper.selectOne(new LambdaQueryWrapper<TopicRecord>()
                .eq(TopicRecord::getTopicLineId, topicLine.getId())
                .eq(TopicRecord::getUserId, userId)
                .last("LIMIT 1"));

        BigDecimal rewardAmount = BigDecimal.ZERO;// 奖励积分数
        int rightNum = 0; // 答对的题目数

        TopicRecord topicRecord = new TopicRecord();
        topicRecord.setId(IdUtil.getSnowflakeNextIdStr());
        topicRecord.setUserId(userId);
        topicRecord.setTopicActivityId(topicActivity.getId());
        topicRecord.setTopicLineId(topicLine.getId());
        topicRecord.setTopicActivityTitle(topicActivity.getTitle());
        topicRecord.setTopicLineTitle(topicLine.getTitle());
        topicRecord.setUsedTime(vo.getUsedTime());

        List<TopicRecordTopic> recordTopicList = new ArrayList<>();
        List<TopicRecordTopicItem> recordTopicItemList = new ArrayList<>();

        for (TopicRecordTopicVo topicVo : vo.getTopicVoList()) {
            Topic topic = topicMapper.selectById(topicVo.getId());
            TopicRecordTopic topicRecordTopic = new TopicRecordTopic();
            topicRecordTopic.setId(IdUtil.getSnowflakeNextIdStr());
            topicRecordTopic.setTopicId(topicVo.getId());
            topicRecordTopic.setTopicTitle(topic.getTitle());
            topicRecordTopic.setTopicRecordId(topicRecord.getId());
            topicRecordTopic.setRewardAmount(topic.getRewardAmount());
            topicRecordTopic.setUserId(userId);

            boolean answerFlag = false;
            // 判断用户选项是否正确
            for (TopicRecordTopicItemVo topicItemVo : topicVo.getRecordTopicItemVoList()) {
                TopicItem checkItem = topicItemMapper.selectById(topicItemVo.getTopicItemId());
                if (checkItem == null) {
                    throw new YunKeException(ExceptionEnum.FAIL, "未知题目选项信息-请刷新页面重新答题");
                }

                TopicRecordTopicItem recordTopicItem = new TopicRecordTopicItem();
                recordTopicItem.setTopicRecordTopicId(topicRecordTopic.getId());
                recordTopicItem.setTopicItemId(checkItem.getId());
                recordTopicItem.setTopicItemTitle(checkItem.getTitle());
                recordTopicItem.setAnswerFlag(checkItem.getAnswerFlag()); // 是否正确答案
                recordTopicItem.setCheckFlag(topicItemVo.getCheckFlag()); // 用户选择
                if (Boolean.TRUE.equals(topicItemVo.getCheckFlag())
                        && Boolean.TRUE.equals(checkItem.getAnswerFlag())) {
                    rightNum++;
                    answerFlag = true;
                    if (checkData == null) { //第一次答题获得奖励
                        rewardAmount = rewardAmount.add(topic.getRewardAmount());
                    }
                }
                recordTopicItemList.add(recordTopicItem);
            }
            topicRecordTopic.setAnswerFlag(answerFlag);
            recordTopicList.add(topicRecordTopic);
        }

        topicRecord.setRewardAmount(rewardAmount);
        topicRecord.setRightNum(rightNum);
        topicRecord.setTotalNum(vo.getTopicVoList().size());

        baseMapper.insert(topicRecord);
        topicRecordTopicMapper.insertBatch(recordTopicList);
        topicRecordTopicItemMapper.insertBatch(recordTopicItemList);
        // 进行奖励积分赠送
        if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                // 检查是否满足时间限制
                timeLimitService.checkTimeLimit();
                UserWalletRecord walletRecord = new UserWalletRecord();
                walletRecord.setEventId(topicRecord.getId());
                walletRecord.setEventType(UserWalletEventEnum.TOPIC_REWARD.getKey());
                walletRecord.setChangeAmount(rewardAmount);

                UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey())
                        .eq(UserWallet::getUserId, userId));

                if (userWallet == null) {
                    userWallet = new UserWallet();
                    userWallet.setVersion(0);
                    userWallet.setUserId(userId);
                    userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
                    userWallet.setAmount(rewardAmount);
                    int insertRow = userWalletMapper.insert(userWallet);
                    walletRecord.setStatus(insertRow > 0);
                    walletRecord.setAfterAmount(rewardAmount);
                } else {
                    if (userWallet.getAmount().compareTo(BigDecimal.valueOf(4000)) >= 0) {
                        throw new YunKeException(ExceptionEnum.FAIL, "已达积分上限");
                    }
                    BigDecimal afterAmount = userWallet.getAmount().add(rewardAmount);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TopicRecordActivity topicRecordActivity = new TopicRecordActivity();
        topicRecordActivity.setTopicActivityId(topicActivity.getId());
        topicRecordActivity.setTotalRewardAmount(rewardAmount);
        topicRecordActivity.setUserId(userId);
        topicRecordActivityMapper.insert(topicRecordActivity);
    }

}
