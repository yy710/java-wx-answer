package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.TopicRecordActivityService;
import com.yunkesoftware.www.web.entity.TopicRecordActivity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-19
 */
@Tag(name = "答题活动记录")
@RestController
@RequestMapping("/wx/topicRecordActivity")
public class TopicRecordActivityController {
    @Resource
    private TopicRecordActivityService topicRecordActivityService;


    @Operation(summary = "排行榜")
    @PostMapping("/page")
    public CommonResult<Page<TopicRecordActivity>> page(@RequestBody TopicRecordActivity topicRecordActivity) {

        Page<TopicRecordActivity> pageResult = topicRecordActivityService.pageByQuery(topicRecordActivity);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "获取当前用户的名次")
    @GetMapping("/getRank")
    public CommonResult<Long> getRank() {
        Long rankNum = topicRecordActivityService.getRankNum();
        return CommonResult.success(rankNum);
    }
}
