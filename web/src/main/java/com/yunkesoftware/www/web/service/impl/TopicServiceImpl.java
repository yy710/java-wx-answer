package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.web.entity.*;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.TopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
    @Resource
    private TopicRecordSingleMapper topicRecordSingleMapper;
    @Resource
    private TopicRecordMapper topicRecordMapper;
    @Resource
    private TopicRecordTopicMapper topicRecordTopicMapper;
    @Resource
    private TopicRecordTopicItemMapper topicRecordTopicItemMapper;

    @Override
    public List<Topic> listByQuery(String topicLineId) {
        // 用户如果已经答过则查询记录数据
        // 如果全部答对则直接缓存起来供用户再次查看
        List<Topic> topicList = (List<Topic>) redisTemplate.opsForValue().get(RedisKey.TOPIC_DATA_KEY + topicLineId);
        if (topicList == null) {
            topicList = topicLineTopicMapper.listByTopicLineId(topicLineId);
            // 缓存5分钟
            for (Topic topic : topicList) {
                buildTopicItemList(topic);
            }
            redisTemplate.opsForValue().set(RedisKey.TOPIC_DATA_KEY + topicLineId, topicList, 10, TimeUnit.MINUTES);
        }

        // TODO 全部答对的用户的记录数据直接增加缓存加快响应速度
        String userId = StpUtil.getLoginIdAsString();

        TopicRecord topicRecord = topicRecordMapper.selectOne(new LambdaQueryWrapper<TopicRecord>()
                .eq(TopicRecord::getTopicLineId, topicLineId)
                .eq(TopicRecord::getUserId, userId)
                .orderByDesc(TopicRecord::getId)
                .select(TopicRecord::getId, TopicRecord::getRightNum, TopicRecord::getTotalNum)
                .last("LIMIT 1"));
        // 当前线路用户已有答题记录则查询用户的答题记录
        if (topicRecord != null) {
            List<TopicRecordTopic> recordTopicList = topicRecordTopicMapper.selectList(new LambdaQueryWrapper<TopicRecordTopic>()
                    .eq(TopicRecordTopic::getTopicRecordId, topicRecord.getId()));
            if (recordTopicList.size() > 0) {
                for (Topic topic : topicList) {
                    for (TopicRecordTopic recordTopic : recordTopicList) {
                        if (recordTopic.getTopicId().equals(topic.getId())) {
                            topic.setAgainType(recordTopic.getAnswerFlag() ? 3 : 2);
                            List<TopicRecordTopicItem> recordTopicItemList = topicRecordTopicItemMapper.selectList(new LambdaQueryWrapper<TopicRecordTopicItem>()
                                    .eq(TopicRecordTopicItem::getTopicRecordTopicId, recordTopic.getId()));
                            for (TopicItem topicItem : topic.getTopicItemList()) {
                                for (TopicRecordTopicItem item : recordTopicItemList) {
                                    if (item.getTopicItemId().equals(topicItem.getId())) {
                                        topicItem.setCheckFlag(item.getCheckFlag());
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            // 如果全部答对则缓存起来
        }
        return topicList;
    }

    @Override
    public List<Topic> listRandom() {
        String userId = StpUtil.getLoginIdAsString();
        List<Topic> dataList = baseMapper.selectList(null);
        List<String> rightTopicIdList = topicRecordSingleMapper.listRightTopicId(userId);
        List<Topic> topicAllList = new ArrayList<>();
        if (rightTopicIdList.size() > 0) {
            for (Topic topic : dataList) {
                if (rightTopicIdList.contains(topic.getId())) {
                    continue;
                }
                topicAllList.add(topic);
            }
        } else {
            topicAllList = dataList;
        }
        // 随机抽取20道题目
        int total = Math.min(topicAllList.size(), 10);
        List<Topic> changeList = new ArrayList<>(topicAllList);
        Collections.shuffle(changeList, new Random());//打乱顺序
        List<Topic> resultList = changeList.subList(0, total);
        for (Topic topic : resultList) {
            buildTopicItemList(topic);
        }
        return resultList;
    }

    private void buildTopicItemList(Topic topic) {
        List<TopicItem> topicItemList = topicItemMapper.selectList(new LambdaQueryWrapper<TopicItem>()
                .eq(TopicItem::getTopicId, topic.getId())
                .select(TopicItem::getId, TopicItem::getTopicId, TopicItem::getTitle, TopicItem::getAnswerFlag)
                .orderByAsc(TopicItem::getSeq));
        topic.setTopicItemList(topicItemList);
    }
}
