package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TopicRecordService;
import com.yunkesoftware.www.adm.entity.TopicRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-15
 */
@Tag(name = "答题活动-记录")
@RestController
@RequestMapping("/sys/topicRecord")
public class TopicRecordController {
    @Resource
    private TopicRecordService topicRecordService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TopicRecord>> page(@RequestBody TopicRecord topicRecord) {
        Page<TopicRecord> pageResult = topicRecordService.pageByQuery(topicRecord);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TopicRecord> getOneById(@RequestParam("id") String id) {
        TopicRecord topicRecord = topicRecordService.getOneById(id);
        return CommonResult.success(topicRecord);
    }

}
