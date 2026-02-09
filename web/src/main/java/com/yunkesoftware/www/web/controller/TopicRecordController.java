package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.TopicRecordService;
import com.yunkesoftware.www.web.entity.TopicRecord;
import com.yunkesoftware.www.web.vo.TopicRecordVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-16
 */
@Tag(name = "答题活动-记录")
@RestController
@RequestMapping("/wx/topicRecord")
public class TopicRecordController {
    @Resource
    private TopicRecordService topicRecordService;

    @Operation(summary = "新增答题记录")
    @PostMapping("/add")
    public CommonResult<Object> add(@Valid @RequestBody TopicRecordVo vo) {
        topicRecordService.add(vo);
        return CommonResult.success();
    }

    @Operation(summary = "查询答题记录")
    @GetMapping("/getOneByTopicLineId")
    public CommonResult<TopicRecord> getOneByTopicLineId(@RequestParam(value = "topicLineId") String topicLineId) {
        TopicRecord topicRecord = topicRecordService.getOneByTopicLineId(topicLineId);
        return CommonResult.success(topicRecord);
    }


}
