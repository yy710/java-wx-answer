package com.yunkesoftware.www.adm.service;

import com.yunkesoftware.www.adm.entity.TopicLine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 答题活动-线路 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
public interface TopicLineService extends IService<TopicLine> {

    void delete(List<String> ids);

    void addOrModify(TopicLine topicLine);
}
