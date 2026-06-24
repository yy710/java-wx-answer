package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.adm.entity.TopicActivity;
import com.yunkesoftware.www.adm.mapper.TopicActivityMapper;
import com.yunkesoftware.www.adm.service.TopicActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 答题活动 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Service
public class TopicActivityServiceImpl extends ServiceImpl<TopicActivityMapper, TopicActivity> implements TopicActivityService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addOrModify(TopicActivity topicActivity) {
        if (!topicActivity.getEndTime().isAfter(topicActivity.getStartTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间必须晚于开始时间");
        }
        LocalDateTime nowTime = LocalDateTime.now();
        if (!topicActivity.getEndTime().isAfter(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间必须晚于当前时间");
        }
        // 开始时间-结束时间不能和其他数据有重叠
        TopicActivity checkData = baseMapper.selectOne(new LambdaQueryWrapper<TopicActivity>()
                .le(TopicActivity::getStartTime, topicActivity.getEndTime())
                .gt(TopicActivity::getEndTime, topicActivity.getStartTime())
                .ne(StringUtils.hasLength(topicActivity.getId()), TopicActivity::getId, topicActivity.getId())
                .select(TopicActivity::getId, TopicActivity::getTitle)
                .last("LIMIT 1"));
        if (checkData != null) {
            throw new YunKeException(ExceptionEnum.FAIL, "时间与(" + checkData.getTitle() + ")时间重叠");
        }
        if (StringUtils.hasLength(topicActivity.getId())) {
            topicActivity.setUserNum(null);
            topicActivity.setCreateTime(null);
            baseMapper.updateById(topicActivity);
        } else {
            topicActivity.setUserNum(0);
            baseMapper.insert(topicActivity);
        }
        redisTemplate.delete(RedisKey.TOPIC_ACTIVITY);
    }

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        redisTemplate.delete(RedisKey.TOPIC_ACTIVITY);
    }
}
