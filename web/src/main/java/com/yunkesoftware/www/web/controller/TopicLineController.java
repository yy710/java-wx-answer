package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.TopicLineService;
import com.yunkesoftware.www.web.entity.TopicLine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-16
 */
@Tag(name = "答题活动-线路")
@RestController
@RequestMapping("/wx/topicLine")
public class TopicLineController {
    @Resource
    private TopicLineService topicLineService;

    @Operation(summary = "获取全部列表")
    @GetMapping("/list")
    public CommonResult<List<TopicLine>> list() {
        List<TopicLine> resultList = topicLineService.listByQuery();
        return CommonResult.success(resultList);
    }
}
