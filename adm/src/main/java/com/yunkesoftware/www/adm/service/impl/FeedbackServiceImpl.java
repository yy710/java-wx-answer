package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.entity.Feedback;
import com.yunkesoftware.www.adm.mapper.FeedbackMapper;
import com.yunkesoftware.www.adm.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户反馈 服务实现类
 * </p>
 *
 * @author cuiyq
 * @since 2023-07-03
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    public void modify(Feedback feedback) {
        baseMapper.update(new LambdaUpdateWrapper<Feedback>()
                .eq(Feedback::getId, feedback.getId())
                .set(Feedback::getHandelResponse, feedback.getHandelResponse())
                .set(Feedback::getHandleFlag, 1)
                .set(Feedback::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public Page<Feedback> pageByQuery(Feedback feedback) {
        Page<Feedback> pageParam = new Page<>(feedback.getPageNum(), feedback.getPageSize());
        return baseMapper.pageByQuery(pageParam, feedback);
    }
}
