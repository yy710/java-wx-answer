package com.yunkesoftware.www.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.web.mapper.FeedbackMapper;
import com.yunkesoftware.www.web.entity.Feedback;
import com.yunkesoftware.www.web.service.FeedbackService;
import org.springframework.stereotype.Service;

/**
 * 意见反馈(Feedback)表服务实现类
 *
 * @author zxp
 * @since 2025-05-12 18:10:11
 */
@Service("feedbackService")
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

}

