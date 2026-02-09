package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.web.entity.TopicActivity;
import com.yunkesoftware.www.web.entity.TopicRecordActivity;
import com.yunkesoftware.www.web.entity.User;
import com.yunkesoftware.www.web.mapper.TopicRecordActivityMapper;
import com.yunkesoftware.www.web.mapper.UserMapper;
import com.yunkesoftware.www.web.service.TopicRecordActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 答题活动记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-19
 */
@Service
public class TopicRecordActivityServiceImpl extends ServiceImpl<TopicRecordActivityMapper, TopicRecordActivity> implements TopicRecordActivityService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserMapper userMapper;

    @Override
    public Page<TopicRecordActivity> pageByQuery(TopicRecordActivity topicRecordActivity) {
        Page<TopicRecordActivity> pageParam = new Page<>(topicRecordActivity.getPageNum(), topicRecordActivity.getPageSize());
        TopicActivity topicActivity = (TopicActivity) redisTemplate.opsForValue().get(RedisKey.TOPIC_ACTIVITY);
        if (topicActivity == null) {
            return pageParam;
        }
        LambdaQueryWrapper<TopicRecordActivity> queryWrapper = new LambdaQueryWrapper<TopicRecordActivity>()
                .eq(TopicRecordActivity::getTopicActivityId, topicActivity.getId())
                .orderByDesc(TopicRecordActivity::getTotalRewardAmount);
        Page<TopicRecordActivity> pageResult = baseMapper.selectPage(pageParam, queryWrapper);
        for (TopicRecordActivity record : pageResult.getRecords()) {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, record.getUserId())
                    .select(User::getNickName));
            if (user != null) {
                record.setNickName(user.getNickName());
            }
        }
        return pageResult;
    }

    @Override
    public Long getRankNum() {
        TopicActivity topicActivity = (TopicActivity) redisTemplate.opsForValue().get(RedisKey.TOPIC_ACTIVITY);
        if (topicActivity == null) {
            return null;
        }
        String userId = StpUtil.getLoginIdAsString();
        TopicRecordActivity userData = baseMapper.selectOne(new LambdaQueryWrapper<TopicRecordActivity>()
                .eq(TopicRecordActivity::getUserId, userId)
                .eq(TopicRecordActivity::getTopicActivityId, topicActivity.getId())
                .select(TopicRecordActivity::getTotalRewardAmount));
        if (userData != null) {
            LambdaQueryWrapper<TopicRecordActivity> queryWrapper = new LambdaQueryWrapper<TopicRecordActivity>()
                    .eq(TopicRecordActivity::getTopicActivityId, topicActivity.getId());

            queryWrapper.ne(TopicRecordActivity::getUserId, userId)
                    .ge(TopicRecordActivity::getTotalRewardAmount, userData.getTotalRewardAmount());
            Long preNum = baseMapper.selectCount(queryWrapper);
            return preNum + 1;
        } else {
            return null;
        }
    }
}
