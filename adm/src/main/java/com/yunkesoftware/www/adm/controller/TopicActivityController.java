package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TopicActivityService;
import com.yunkesoftware.www.adm.entity.TopicActivity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-15
 */
@Tag(name = "答题活动")
@RestController
@RequestMapping("/sys/topicActivity")
public class TopicActivityController {
    @Resource
    private TopicActivityService topicActivityService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TopicActivity>> page(@RequestBody TopicActivity topicActivity) {
        return CommonResult.success(topicActivityService.page(new Page<>(topicActivity.getPageNum(), topicActivity.getPageSize()),
                new LambdaQueryWrapper<TopicActivity>()
                        .like(StringUtils.hasLength(topicActivity.getTitle()), TopicActivity::getTitle, topicActivity.getTitle())));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TopicActivity> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(topicActivityService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@Valid @RequestBody TopicActivity topicActivity) {
        topicActivityService.addOrModify(topicActivity);
        return CommonResult.success();
    }

    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(topicActivityService.removeByIds(ids));
    }
}
