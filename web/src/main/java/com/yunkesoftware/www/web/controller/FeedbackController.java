package com.yunkesoftware.www.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.entity.Feedback;
import com.yunkesoftware.www.web.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "意见反馈")
@RestController
@RequestMapping("/wx/feedback")
public class FeedbackController {

    @Resource
    private  FeedbackService feedbackService;

    @Operation(summary = "提交意见反馈")
    @PostMapping("/add")
    public CommonResult<Object> addFeedback(@RequestBody Feedback feedback) {
        String userId = StpUtil.getLoginIdAsString();
        feedback.setUserId(userId);
        feedback.setHandleFlag(false); // 设置为未处理
        boolean save = feedbackService.save(feedback);
        return CommonResult.status(save);
    }

    @Operation(summary = "反馈记录")
    @GetMapping("/page")
    public CommonResult<Page<Feedback>> listFeedback(PageCurrency pageCurrency) {
        Page<Feedback> pageParam = new Page<>(pageCurrency.getPageNum(), pageCurrency.getPageSize());

        String userId = StpUtil.getLoginIdAsString();

        LambdaQueryWrapper<Feedback> queryWrapper = new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, userId)
                .orderByDesc(Feedback::getCreateTime);
        Page<Feedback> page = feedbackService.page(pageParam, queryWrapper);
        return CommonResult.success(page);

    }

    @Operation(summary = "详情")
    @GetMapping("/getOneById")
    public CommonResult<Feedback> getOneById(String id) {
        Feedback feedback = feedbackService.getById(id);
        return CommonResult.success(feedback);
    }

}
