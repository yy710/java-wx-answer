package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.adm.entity.Topic;
import com.yunkesoftware.www.adm.entity.TopicActivity;
import com.yunkesoftware.www.adm.entity.TopicLine;
import com.yunkesoftware.www.adm.entity.TopicLineTopic;
import com.yunkesoftware.www.adm.mapper.TopicActivityMapper;
import com.yunkesoftware.www.adm.mapper.TopicLineMapper;
import com.yunkesoftware.www.adm.mapper.TopicLineTopicMapper;
import com.yunkesoftware.www.adm.mapper.TopicMapper;
import com.yunkesoftware.www.adm.service.TopicLineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.vo.TopicLineTopicVo;
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
 * 答题活动-线路 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Service
public class TopicLineServiceImpl extends ServiceImpl<TopicLineMapper, TopicLine> implements TopicLineService {
    @Resource
    private TopicLineTopicMapper topicLineTopicMapper;
    @Resource
    private TopicActivityMapper topicActivityMapper;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        topicLineTopicMapper.delete(new LambdaQueryWrapper<TopicLineTopic>()
                .in(TopicLineTopic::getTopicLineId, ids));
    }

    @Override
    public void setTopic(TopicLineTopicVo vo) {
        TopicLine checkData = baseMapper.selectById(vo.getId());
        if (checkData == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "答题活动线路数据不存在");
        }
        TopicActivity topicActivity = topicActivityMapper.selectById(checkData.getTopicActivityId());
        if (topicActivity == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "答题活动数据不存在");
        }
        if (topicActivity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new YunKeException(ExceptionEnum.FAIL, "答题活动已结束-请勿更改");
        }

        if (vo.getTopicLineTopicList() == null || vo.getTopicLineTopicList().size() > 7) {
            throw new YunKeException(ExceptionEnum.FAIL, "请设置最多7个题目");
        }
        topicLineTopicMapper.delete(new LambdaQueryWrapper<TopicLineTopic>()
                .eq(TopicLineTopic::getTopicLineId, vo.getId()));
        for (TopicLineTopic lineTopic : vo.getTopicLineTopicList()) {
            if (lineTopic.getRewardAmount() == null) {
                throw new YunKeException(ExceptionEnum.FAIL, "请设置题目奖励积分数");
            }
            lineTopic.setTopicLineId(vo.getId());
        }
        topicLineTopicMapper.insertBatch(vo.getTopicLineTopicList());
    }

    @Override
    public List<TopicLineTopic> getTopic(String id) {
        List<TopicLineTopic> resultList = topicLineTopicMapper.selectList(new LambdaQueryWrapper<TopicLineTopic>()
                .eq(TopicLineTopic::getTopicLineId, id));
        for (TopicLineTopic lineTopic : resultList) {
            Topic topic = topicMapper.selectOne(new LambdaQueryWrapper<Topic>()
                    .eq(Topic::getId, lineTopic.getTopicId())
                    .select(Topic::getTitle));
            if (topic != null) {
                lineTopic.setTopicName(topic.getTitle());
            }
        }
        return resultList;
    }

    @Override
    public void addOrModify(TopicLine topicLine) {
        if (StringUtils.hasLength(topicLine.getId())) {
            baseMapper.updateById(topicLine);
            if (!StringUtils.hasLength(topicLine.getTopicActivityId())) {
                TopicLine checkData = baseMapper.selectById(topicLine.getId());
                topicLine.setTopicActivityId(checkData.getTopicActivityId());
            }
        } else {
            baseMapper.insert(topicLine);
        }
        redisTemplate.delete(RedisKey.TOPIC_LINE_KEY + topicLine.getTopicActivityId());
    }
}
