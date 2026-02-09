package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TopicRecordTopicService;
import com.yunkesoftware.www.adm.entity.TopicRecordTopic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-15
 */
@Tag(name = "答题活动-题目记录")
@RestController
@RequestMapping("/sys/topicRecordTopic")
public class TopicRecordTopicController {
    @Resource
    private TopicRecordTopicService topicRecordTopicService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TopicRecordTopic>> page(@RequestBody TopicRecordTopic topicRecordTopic) {
        return CommonResult.success(topicRecordTopicService.page(new Page<>(topicRecordTopic.getPageNum(), topicRecordTopic.getPageSize()),
                null));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TopicRecordTopic> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(topicRecordTopicService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody TopicRecordTopic topicRecordTopic) {
        return CommonResult.status(topicRecordTopicService.saveOrUpdate(topicRecordTopic));
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(topicRecordTopicService.removeByIds(ids));
    }
}
