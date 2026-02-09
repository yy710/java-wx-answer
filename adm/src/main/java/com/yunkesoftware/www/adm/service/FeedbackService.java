package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.Feedback;

public interface FeedbackService extends IService<Feedback> {
    void modify(Feedback feedback);
    Page<Feedback> pageByQuery(Feedback feedback);
}
