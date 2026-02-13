package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.TopicRecordSingle;
import com.yunkesoftware.www.adm.entity.TopicRecordSingleItem;
import com.yunkesoftware.www.adm.entity.User;
import com.yunkesoftware.www.adm.mapper.TopicRecordSingleItemMapper;
import com.yunkesoftware.www.adm.mapper.TopicRecordSingleMapper;
import com.yunkesoftware.www.adm.mapper.UserMapper;
import com.yunkesoftware.www.adm.service.TopicRecordSingleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 趣味答题记录 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-02-12
 */
@Service
public class TopicRecordSingleServiceImpl extends ServiceImpl<TopicRecordSingleMapper, TopicRecordSingle> implements TopicRecordSingleService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private TopicRecordSingleItemMapper topicRecordSingleItemMapper;

    @Override
    public Page<TopicRecordSingle> pageByQuery(TopicRecordSingle topicRecordSingle) {
        Page<TopicRecordSingle> pageParam = new Page<>(topicRecordSingle.getPageNum(), topicRecordSingle.getPageSize());
        return baseMapper.pageByQuery(pageParam, topicRecordSingle);
    }

    @Override
    public TopicRecordSingle getOneById(String id) {
        TopicRecordSingle topicRecordSingle = baseMapper.selectById(id);
        if (topicRecordSingle != null) {
            User user = userMapper.selectById(topicRecordSingle.getUserId());
            if (user != null) {
                topicRecordSingle.setNickName(user.getNickName());
            }
            topicRecordSingle.setSingleItemList(topicRecordSingleItemMapper.selectList(new LambdaQueryWrapper<TopicRecordSingleItem>()
                    .eq(TopicRecordSingleItem::getTopicRecordSingleId, id)));
        }
        return topicRecordSingle;
    }
}
