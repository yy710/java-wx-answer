package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.entity.TopicLine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 答题活动-线路 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
public interface TopicLineService extends IService<TopicLine> {

    List<TopicLine> listByQuery();

    Boolean checkContinue(String id);
}
