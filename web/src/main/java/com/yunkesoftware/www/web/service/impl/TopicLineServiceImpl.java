package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.web.entity.TopicActivity;
import com.yunkesoftware.www.web.entity.TopicLine;
import com.yunkesoftware.www.web.entity.UserWallet;
import com.yunkesoftware.www.web.mapper.TopicActivityMapper;
import com.yunkesoftware.www.web.mapper.TopicLineMapper;
import com.yunkesoftware.www.web.mapper.TopicRecordMapper;
import com.yunkesoftware.www.web.mapper.UserWalletMapper;
import com.yunkesoftware.www.web.service.TopicLineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 答题活动-线路 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Service
public class TopicLineServiceImpl extends ServiceImpl<TopicLineMapper, TopicLine> implements TopicLineService {
    @Resource
    private TopicActivityMapper topicActivityMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TopicRecordMapper topicRecordMapper;
    @Resource
    private UserWalletMapper userWalletMapper;


    @Override
    public List<TopicLine> listByQuery() {
        String userId = StpUtil.getLoginIdAsString();

        LocalDateTime nowTime = LocalDateTime.now();

        TopicActivity topicActivity = (TopicActivity) redisTemplate.opsForValue().get(RedisKey.TOPIC_ACTIVITY);

        if (topicActivity == null) {
            topicActivity = topicActivityMapper.selectOne(new LambdaQueryWrapper<TopicActivity>()
                    .le(TopicActivity::getStartTime, nowTime)
                    .gt(TopicActivity::getEndTime, nowTime)
                    .select(TopicActivity::getId, TopicActivity::getEndTime, TopicActivity::getLimitNum));
            if (topicActivity == null) {
                return new ArrayList<>();
            }
            long seconds = Duration.between(nowTime, topicActivity.getEndTime()).getSeconds();
            redisTemplate.opsForValue().set(RedisKey.TOPIC_ACTIVITY, topicActivity, seconds, TimeUnit.SECONDS);
        }

        List<TopicLine> topicLineList = baseMapper.selectList(new LambdaQueryWrapper<TopicLine>()
                .eq(TopicLine::getTopicActivityId, topicActivity.getId())
                .eq(TopicLine::getStatus, true)
                .orderByAsc(TopicLine::getSeq)
                .last("LIMIT 7"));

        // 50积分点亮一个站点
        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        int i = 0;
        if (userWallet != null) {
            i = userWallet.getAmount().intValue() / 50;
        }
        if (i > 0) {
            for (TopicLine topicLine : topicLineList) {
                topicLine.setDoneFlag(topicLine.getLightSeq() <= i);
            }
        }
        return topicLineList;
    }

    @Override
    public Boolean checkContinue(String id) {
        String userId = StpUtil.getLoginIdAsString();
        TopicActivity topicActivity = (TopicActivity) redisTemplate.opsForValue().get(RedisKey.TOPIC_ACTIVITY);
        if (topicActivity == null) {
            return false;
        }
        if (topicActivity.getLimitNum() != null) {
            // 当日答题线路数
            Set<String> lineIdList = topicRecordMapper.listLineId(userId, topicActivity.getId(), LocalDate.now(), id);
            if (lineIdList == null || lineIdList.contains(id)) {
                return true;
            }
            return lineIdList.size() < topicActivity.getLimitNum();
        }
        return true;
    }
}
