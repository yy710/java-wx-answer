package com.yunkesoftware.www.adm.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.adm.entity.Topic;
import com.yunkesoftware.www.adm.entity.TopicItem;
import com.yunkesoftware.www.adm.mapper.TopicItemMapper;
import com.yunkesoftware.www.adm.mapper.TopicMapper;
import com.yunkesoftware.www.adm.service.TopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 题目信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Resource
    private TopicItemMapper topicItemMapper;

    @Override
    public void addOrModify(Topic topic) {
        if (StringUtils.hasLength(topic.getId())) {
            baseMapper.updateById(topic);
            topicItemMapper.delete(new LambdaQueryWrapper<TopicItem>()
                    .eq(TopicItem::getTopicId, topic.getId()));
        } else {
            baseMapper.insert(topic);
        }
        if (topic.getTopicItemList() != null && topic.getTopicItemList().size() > 0) {
            for (TopicItem topicItem : topic.getTopicItemList()) {
                topicItem.setTopicId(topic.getId());
                topicItem.setId(IdUtil.getSnowflakeNextIdStr());
            }
            topicItemMapper.insertBatch(topic.getTopicItemList());
        }
    }

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        topicItemMapper.delete(new LambdaQueryWrapper<TopicItem>()
                .in(TopicItem::getTopicId, ids));

    }

    @Override
    public Topic getOneById(String id) {
        Topic topic = baseMapper.selectById(id);
        if (topic != null) {
            List<TopicItem> topicItemList = topicItemMapper.selectList(new LambdaQueryWrapper<TopicItem>()
                    .eq(TopicItem::getTopicId, id));
            topic.setTopicItemList(topicItemList);
        }
        return topic;
    }
}
