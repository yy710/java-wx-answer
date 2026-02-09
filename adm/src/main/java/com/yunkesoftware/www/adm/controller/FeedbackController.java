package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.adm.entity.Feedback;
import com.yunkesoftware.www.adm.service.FeedbackService;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cuiyq
 * @since 2023-07-03
 */
@Tag(name = "用户反馈")
@RestController
@RequestMapping("/sys/feedback")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<Feedback>> page(@RequestBody Feedback feedback) {
        Page<Feedback> pageResult = feedbackService.pageByQuery(feedback);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Feedback> getOneById(String id) {
        return CommonResult.success(feedbackService.getById(id));
    }

    @Operation(summary = "修改")
    @PostMapping("/modify")
    @YunkeSysLog(value = "意见反馈-处理")
    public CommonResult<Object> modify(@RequestBody Feedback feedback) {
        feedbackService.modify(feedback);
        return CommonResult.success();
    }

    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    @YunkeSysLog("意见反馈-删除")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(feedbackService.removeByIds(ids));
    }
}
