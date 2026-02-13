package com.yunkesoftware.www.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.web.entity.TopicRecordActivity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.web.vo.WalletRankVo;

/**
 * <p>
 * 答题活动记录 服务类
 * </p>
 *
 * @author yk
 * @since 2026-01-19
 */
public interface TopicRecordActivityService extends IService<TopicRecordActivity> {

    Page<TopicRecordActivity> pageByQuery(TopicRecordActivity topicRecordActivity);

    Long getRankNum();

    Page<WalletRankVo> pageRank(PageCurrency query);
}
