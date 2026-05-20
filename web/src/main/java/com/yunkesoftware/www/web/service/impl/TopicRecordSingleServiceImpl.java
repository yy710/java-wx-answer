package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.*;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.TimeLimitService;
import com.yunkesoftware.www.web.service.TopicRecordSingleService;
import com.yunkesoftware.www.web.service.UserWalletService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.web.vo.TopicSingleResultVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private TimeLimitService timeLimitService;
    @Resource
    private UserWalletService userWalletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TopicSingleResultVo add(TopicRecordSingle recordSingle) {

        String userId = StpUtil.getLoginIdAsString();
        Topic topic = topicMapper.selectById(recordSingle.getTopicId());
        if (topic == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "题目不存在-请刷新重试");
        }

        String id = IdUtil.getSnowflakeNextIdStr();
        recordSingle.setUserId(userId);
        recordSingle.setCreateTime(null);
        recordSingle.setTopicTitle(topic.getTitle());
        recordSingle.setId(id);
        recordSingle.setRightFlag(false);
        recordSingle.setRewardAmount(topic.getRewardAmount());

        // 只有首次答对才积分
//        TopicRecordSingle checkData = baseMapper.selectOne(new LambdaQueryWrapper<TopicRecordSingle>()
//                .eq(TopicRecordSingle::getUserId, userId)
//                .eq(TopicRecordSingle::getTopicId, recordSingle.getTopicId())
//                .select(TopicRecordSingle::getId)
//                .last("LIMIT 1"));

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

        TopicSingleResultVo resultVo = new TopicSingleResultVo();
        resultVo.setRewardAmount(BigDecimal.ZERO);
        if (!recordSingle.getRightFlag()) {
            return resultVo;
        }
        if (topic.getRewardAmount().compareTo(BigDecimal.ZERO) < 1) {
            return resultVo;
        }

        try {
            // 检查时间限制
            timeLimitService.checkTimeLimit();
        } catch (YunKeException e) {
            resultVo.setMsg(e.getMessage());
            return resultVo;
        }

//        if (checkData != null) {
//            resultVo.setMsg("只有首次答题可获得积分奖励！");
//            return resultVo;
//        }
        userWalletService.rewardIntegral(userId, id, UserWalletEventEnum.SINGLE_TOPIC_REWARD.getKey(), topic.getRewardAmount());
        resultVo.setRewardAmount(topic.getRewardAmount());
        return resultVo;
    }
}
