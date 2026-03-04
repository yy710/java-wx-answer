package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;

import com.yunkesoftware.www.web.entity.Topic;
import com.yunkesoftware.www.web.entity.TopicItem;
import com.yunkesoftware.www.web.entity.TopicLine;
import com.yunkesoftware.www.web.entity.TopicRecord;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.TopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.web.vo.TopicLineDataVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private TopicItemMapper topicItemMapper;
    @Resource
    private TopicRecordSingleMapper topicRecordSingleMapper;
    @Resource
    private TopicLineMapper topicLineMapper;
    @Resource
    private TopicRecordMapper topicRecordMapper;
    @Resource
    private TopicRecordTopicMapper topicRecordTopicMapper;

    @Override
    public TopicLineDataVo listByQuery(String topicLineId) {
        TopicLine topicLine = topicLineMapper.selectById(topicLineId);
        if (topicLine == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "地图数据不存在-请刷新重试");
        }
        if (topicLine.getTopicNum() == null || topicLine.getTopicNum() <= 0) {
            throw new YunKeException(ExceptionEnum.FAIL, "当前地图活动不支持答题-请联系管理人员配置");
        }

        TopicLineDataVo topicLineDataVo = new TopicLineDataVo();
        String userId = StpUtil.getLoginIdAsString();
        //优先未答/错题
        Set<String> rightTopicIdList = topicRecordTopicMapper.listRightTopicId(userId);

        List<Topic> topicList = baseMapper.listNewOrWrong(rightTopicIdList, topicLine.getTopicNum());
        // 没答过+错题总数不足的。需要从已答正确的中获取剩余数量补齐
        if (rightTopicIdList.size() > 0 && topicList.size() < topicLine.getTopicNum()) {
            List<Topic> rightTopicList = baseMapper.listRight(rightTopicIdList, topicLine.getTopicNum() - topicList.size());
            topicList.addAll(rightTopicList);
        }
        //题目不够-不考虑了
        // 用户已经答对的题目进行过滤筛选
        for (Topic topic : topicList) {
            buildTopicItemList(topic);
        }
        topicLineDataVo.setTopicList(topicList);
        // 判断用户当前地图是否是首次答题
        TopicRecord checkTopicRecord = topicRecordMapper.selectOne(new LambdaQueryWrapper<TopicRecord>()
                .eq(TopicRecord::getUserId, userId)
                .eq(TopicRecord::getTopicLineId, topicLine.getId())
                .last("LIMIT 1"));
        // 时间是否符合条件
        topicLineDataVo.setFirstFlag(checkTopicRecord == null);
        topicLineDataVo.setTimeFlag(true);
        return topicLineDataVo;
    }

    @Override
    public List<Topic> listRandom() {
        String userId = StpUtil.getLoginIdAsString();
        List<Topic> dataList = baseMapper.selectList(null);
        Set<String> rightTopicIdList = topicRecordSingleMapper.listRightTopicId(userId);
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
