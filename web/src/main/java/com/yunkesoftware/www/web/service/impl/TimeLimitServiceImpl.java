package com.yunkesoftware.www.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.entity.IntroduceSet;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.entity.TopicActivity;
import com.yunkesoftware.www.web.mapper.TopicActivityMapper;
import com.yunkesoftware.www.web.service.TimeLimitService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimeLimitServiceImpl implements TimeLimitService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TopicActivityMapper topicActivityMapper;

    @Override
    public void checkTimeLimit() {
        checkTimeLimit(false);
    }

    @Override
    public void checkSingleTopicTimeLimit() {
        checkTimeLimit(true);
    }

    private void checkTimeLimit(boolean useTopicActivityFallback) {
        LocalDateTime nowTime = LocalDateTime.now();
        IntroduceSet introduceSet = (IntroduceSet) redisTemplate.opsForValue().get(RedisKey.INTRODUCE_SET);
        if (introduceSet == null || introduceSet.getStartTime() == null || introduceSet.getEndTime() == null) {
            introduceSet = useTopicActivityFallback ? buildIntroduceSetFromTopicActivity(nowTime) : null;
            if (introduceSet == null) {
                String message = useTopicActivityFallback
                        ? "请先在管理后台系统设置或地图答题活动中设置开始时间和结束时间"
                        : "请先在管理后台系统设置中设置活动开始时间和结束时间";
                throw new YunKeException(ExceptionEnum.FAIL, message);
            }
            redisTemplate.opsForValue().set(RedisKey.INTRODUCE_SET, introduceSet);
        }
        LocalDateTime startTime = introduceSet.getStartTime();
        LocalDateTime endTime = introduceSet.getEndTime();
        if (nowTime.isBefore(startTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "获得积分开始时间为：" + startTime.format(FORMATTER));
        }
        if (nowTime.isAfter(endTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "获得积分结束时间为：" + endTime.format(FORMATTER));
        }
    }

    private IntroduceSet buildIntroduceSetFromTopicActivity(LocalDateTime nowTime) {
        TopicActivity topicActivity = selectTopicActivityForTimeLimit(nowTime);
        if (topicActivity == null || topicActivity.getStartTime() == null || topicActivity.getEndTime() == null) {
            return null;
        }
        IntroduceSet introduceSet = new IntroduceSet();
        introduceSet.setStartTime(topicActivity.getStartTime());
        introduceSet.setEndTime(topicActivity.getEndTime());
        return introduceSet;
    }

    private TopicActivity selectTopicActivityForTimeLimit(LocalDateTime nowTime) {
        TopicActivity topicActivity = topicActivityMapper.selectOne(new LambdaQueryWrapper<TopicActivity>()
                .isNotNull(TopicActivity::getStartTime)
                .isNotNull(TopicActivity::getEndTime)
                .le(TopicActivity::getStartTime, nowTime)
                .ge(TopicActivity::getEndTime, nowTime)
                .select(TopicActivity::getStartTime, TopicActivity::getEndTime)
                .orderByAsc(TopicActivity::getStartTime)
                .last("LIMIT 1"));
        if (topicActivity != null) {
            return topicActivity;
        }
        topicActivity = topicActivityMapper.selectOne(new LambdaQueryWrapper<TopicActivity>()
                .isNotNull(TopicActivity::getStartTime)
                .isNotNull(TopicActivity::getEndTime)
                .gt(TopicActivity::getStartTime, nowTime)
                .select(TopicActivity::getStartTime, TopicActivity::getEndTime)
                .orderByAsc(TopicActivity::getStartTime)
                .last("LIMIT 1"));
        if (topicActivity != null) {
            return topicActivity;
        }
        return topicActivityMapper.selectOne(new LambdaQueryWrapper<TopicActivity>()
                .isNotNull(TopicActivity::getStartTime)
                .isNotNull(TopicActivity::getEndTime)
                .lt(TopicActivity::getEndTime, nowTime)
                .select(TopicActivity::getStartTime, TopicActivity::getEndTime)
                .orderByDesc(TopicActivity::getEndTime)
                .last("LIMIT 1"));
    }
}
