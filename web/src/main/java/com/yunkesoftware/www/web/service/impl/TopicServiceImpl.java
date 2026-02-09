package com.yunkesoftware.www.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.web.entity.Topic;
import com.yunkesoftware.www.web.entity.TopicItem;
import com.yunkesoftware.www.web.mapper.TopicItemMapper;
import com.yunkesoftware.www.web.mapper.TopicLineTopicMapper;
import com.yunkesoftware.www.web.mapper.TopicMapper;
import com.yunkesoftware.www.web.service.TopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 题目信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Resource
    private TopicLineTopicMapper topicLineTopicMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TopicItemMapper topicItemMapper;

    @Override
    public List<Topic> listByQuery(String topicLineId) {
        List<Topic> topicList = (List<Topic>) redisTemplate.opsForValue().get(RedisKey.TOPIC_DATA_KEY + topicLineId);

        if (topicList == null) {
            topicList = topicLineTopicMapper.listByTopicLineId(topicLineId);
            // 缓存5分钟
            for (Topic topic : topicList) {
                List<TopicItem> topicItemList = topicItemMapper.selectList(new LambdaQueryWrapper<TopicItem>()
                        .eq(TopicItem::getTopicId, topic.getId())
                        .select(TopicItem::getId, TopicItem::getTopicId, TopicItem::getTitle, TopicItem::getAnswerFlag)
                        .orderByAsc(TopicItem::getSeq));
                topic.setTopicItemList(topicItemList);
            }
            redisTemplate.opsForValue().set(RedisKey.TOPIC_DATA_KEY + topicLineId, topicList, 5, TimeUnit.MINUTES);
        }
        return topicList;
    }
}
