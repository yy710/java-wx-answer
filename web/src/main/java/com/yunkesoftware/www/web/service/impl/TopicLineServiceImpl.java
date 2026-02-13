package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.web.entity.TopicActivity;
import com.yunkesoftware.www.web.entity.TopicLine;
import com.yunkesoftware.www.web.entity.TopicRecord;
import com.yunkesoftware.www.web.mapper.TopicActivityMapper;
import com.yunkesoftware.www.web.mapper.TopicLineMapper;
import com.yunkesoftware.www.web.mapper.TopicRecordMapper;
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

        List<TopicLine> topicLineList = (List<TopicLine>) redisTemplate.opsForValue().get(RedisKey.TOPIC_LINE_KEY + topicActivity.getId());
        if (topicLineList == null) {
            topicLineList = baseMapper.selectList(new LambdaQueryWrapper<TopicLine>()
                    .eq(TopicLine::getTopicActivityId, topicActivity.getId())
                    .eq(TopicLine::getStatus, true)
                    .orderByAsc(TopicLine::getSeq)
                    .last("LIMIT 7"));
            long seconds = Duration.between(nowTime, topicActivity.getEndTime()).getSeconds();
            redisTemplate.opsForValue().set(RedisKey.TOPIC_LINE_KEY + topicActivity.getId(), topicLineList, seconds, TimeUnit.SECONDS);
        }

        for (TopicLine topicLine : topicLineList) {
            TopicRecord checkData = topicRecordMapper.selectOne(new LambdaQueryWrapper<TopicRecord>()
                    .eq(TopicRecord::getTopicLineId, topicLine.getId())
                    .eq(TopicRecord::getUserId, userId)
                    .select(TopicRecord::getId, TopicRecord::getTopicActivityId, TopicRecord::getRightNum)
                    .last("LIMIT 1"));
            topicLine.setDoneFlag(checkData != null && checkData.getRightNum() > 0);
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
            int todayNum = topicRecordMapper.countUserTodayNum(userId, topicActivity.getId(), LocalDate.now(),id);
            return todayNum < topicActivity.getLimitNum();
        }
        return true;
    }
}
