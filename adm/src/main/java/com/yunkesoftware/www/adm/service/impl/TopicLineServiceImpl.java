package com.yunkesoftware.www.adm.service.impl;


import com.yunkesoftware.www.adm.entity.TopicLine;
import com.yunkesoftware.www.adm.mapper.TopicLineMapper;
import com.yunkesoftware.www.adm.service.TopicLineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
    }


    @Override
    public void addOrModify(TopicLine topicLine) {
        if (StringUtils.hasLength(topicLine.getId())) {
            baseMapper.updateById(topicLine);
        } else {
            baseMapper.insert(topicLine);
        }

    }
}
