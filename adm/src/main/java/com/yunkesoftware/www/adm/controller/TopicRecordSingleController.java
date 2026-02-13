package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TopicRecordSingleService;
import com.yunkesoftware.www.adm.entity.TopicRecordSingle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-02-12
 */
@Tag(name = "趣味答题记录")
@RestController
@RequestMapping("/sys/topicRecordSingle")
public class TopicRecordSingleController {
    @Resource
    private TopicRecordSingleService topicRecordSingleService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TopicRecordSingle>> page(@RequestBody TopicRecordSingle topicRecordSingle) {
        Page<TopicRecordSingle> pageResult = topicRecordSingleService.pageByQuery(topicRecordSingle);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TopicRecordSingle> getOneById(@RequestParam("id") String id) {
        TopicRecordSingle topicRecordSingle = topicRecordSingleService.getOneById(id);
        return CommonResult.success(topicRecordSingle);
    }
}
