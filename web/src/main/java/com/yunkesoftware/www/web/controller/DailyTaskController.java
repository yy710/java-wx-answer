package com.yunkesoftware.www.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.dto.DailyTaskRequests;
import com.yunkesoftware.www.web.service.DailyTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "每日任务与保存分享积分")
@RestController
@RequestMapping("/wx/dailyTask")
public class DailyTaskController {
    @Resource
    private DailyTaskService dailyTaskService;

    @Operation(summary = "每日任务状态")
    @GetMapping("/status")
    public CommonResult<Map<String, Object>> status() {
        return CommonResult.success(dailyTaskService.status(userId()));
    }

    @Operation(summary = "开始每日知识问答")
    @PostMapping("/quiz/start")
    public CommonResult<Map<String, Object>> startQuiz() {
        return CommonResult.success(dailyTaskService.startQuiz(userId()));
    }

    @Operation(summary = "提交每日知识问答")
    @PostMapping("/quiz/submit")
    public CommonResult<Map<String, Object>> submitQuiz(@RequestBody DailyTaskRequests.QuizSubmit request) {
        return CommonResult.success(dailyTaskService.submitQuiz(userId(), request));
    }

    @Operation(summary = "开始每日消保视频")
    @PostMapping("/video/start")
    public CommonResult<Map<String, Object>> startVideo(@RequestBody(required = false) DailyTaskRequests.VideoStart request) {
        return CommonResult.success(dailyTaskService.startVideo(userId(), request));
    }

    @Operation(summary = "上报视频有效观看心跳")
    @PostMapping("/video/progress")
    public CommonResult<Map<String, Object>> videoProgress(@RequestBody DailyTaskRequests.VideoProgress request) {
        return CommonResult.success(dailyTaskService.videoProgress(userId(), request));
    }

    @Operation(summary = "申领每日消保视频积分")
    @PostMapping("/video/claim")
    public CommonResult<Map<String, Object>> claimVideo(@RequestBody DailyTaskRequests.VideoClaim request) {
        return CommonResult.success(dailyTaskService.claimVideo(userId(), request));
    }

    @Operation(summary = "申领民生实事积分")
    @PostMapping("/affair/claim")
    public CommonResult<Map<String, Object>> claimAffair(@RequestBody DailyTaskRequests.AffairClaim request) {
        return CommonResult.success(dailyTaskService.claimAffair(userId(), request));
    }

    @Operation(summary = "准备保存分享令牌")
    @PostMapping("/share/prepare")
    public CommonResult<Map<String, Object>> prepareShare(@RequestBody DailyTaskRequests.SharePrepare request) {
        return CommonResult.success(dailyTaskService.prepareShare(userId(), request));
    }

    @Operation(summary = "申领保存或分享积分")
    @PostMapping("/share/claim")
    public CommonResult<Map<String, Object>> claimShare(@RequestBody DailyTaskRequests.ShareClaim request) {
        return CommonResult.success(dailyTaskService.claimShare(userId(), request));
    }

    private String userId() {
        StpUtil.checkLogin();
        return StpUtil.getLoginIdAsString();
    }
}
