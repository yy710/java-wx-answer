package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TopicRecord;
import com.yunkesoftware.www.adm.entity.TopicRecordTopic;
import com.yunkesoftware.www.adm.entity.TopicRecordTopicItem;
import com.yunkesoftware.www.adm.entity.User;
import com.yunkesoftware.www.adm.mapper.TopicRecordMapper;
import com.yunkesoftware.www.adm.mapper.TopicRecordTopicItemMapper;
import com.yunkesoftware.www.adm.mapper.TopicRecordTopicMapper;
import com.yunkesoftware.www.adm.mapper.UserMapper;
import com.yunkesoftware.www.adm.service.TopicRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户答题记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Service
public class TopicRecordServiceImpl extends ServiceImpl<TopicRecordMapper, TopicRecord> implements TopicRecordService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private TopicRecordTopicMapper topicRecordTopicMapper;
    @Resource
    private TopicRecordTopicItemMapper topicRecordTopicItemMapper;

    @Override
    public Page<TopicRecord> pageByQuery(TopicRecord topicRecord) {
        Page<TopicRecord> pageParam = new Page<>(topicRecord.getPageNum(), topicRecord.getPageSize());
        return baseMapper.pageByQuery(pageParam, topicRecord);
    }

    @Override
    public TopicRecord getOneById(String id) {
        TopicRecord topicRecord = baseMapper.selectById(id);
        if (topicRecord != null) {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, topicRecord.getUserId())
                    .select(User::getId, User::getNickName));
            if (user != null) {
                topicRecord.setNickName(user.getNickName());
            }

            List<TopicRecordTopic> recordTopicList = topicRecordTopicMapper.selectList(new LambdaQueryWrapper<TopicRecordTopic>()
                    .eq(TopicRecordTopic::getTopicRecordId, id));
            for (TopicRecordTopic recordTopic : recordTopicList) {
                List<TopicRecordTopicItem> recordTopicItemList = topicRecordTopicItemMapper.selectList(new LambdaQueryWrapper<TopicRecordTopicItem>()
                        .eq(TopicRecordTopicItem::getTopicRecordTopicId, recordTopic.getId()));
                recordTopic.setTopicRecordTopicItemList(recordTopicItemList);
            }
            topicRecord.setTopicRecordTopicList(recordTopicList);
        }
        return topicRecord;
    }
}
