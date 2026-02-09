package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TopicService;
import com.yunkesoftware.www.adm.entity.Topic;
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
@Tag(name = "答题活动-题目信息")
@RestController
@RequestMapping("/sys/topic")
public class TopicController {
    @Resource
    private TopicService topicService;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<Topic>> page(@RequestBody Topic topic) {
        return CommonResult.success(topicService.page(new Page<>(topic.getPageNum(), topic.getPageSize()),
                new LambdaQueryWrapper<Topic>()
                        .like(StringUtils.hasLength(topic.getTitle()), Topic::getTitle, topic.getTitle())));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Topic> getOneById(@RequestParam("id") String id) {
        Topic topic = topicService.getOneById(id);
        return CommonResult.success(topic);
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Object> addOrModify(@RequestBody Topic topic) {
        topicService.addOrModify(topic);
        return CommonResult.success();
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        topicService.delete(ids);
        return CommonResult.success();
    }
}
