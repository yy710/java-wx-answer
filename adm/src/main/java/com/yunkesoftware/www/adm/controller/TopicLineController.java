package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.adm.entity.TopicLineTopic;
import com.yunkesoftware.www.adm.vo.TopicLineTopicVo;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.TopicLineService;
import com.yunkesoftware.www.adm.entity.TopicLine;
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
@Tag(name = "答题活动-线路")
@RestController
@RequestMapping("/sys/topicLine")
public class TopicLineController {
    @Resource
    private TopicLineService topicLineService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<TopicLine>> page(@RequestBody TopicLine topicLine) {
        return CommonResult.success(topicLineService.page(new Page<>(topicLine.getPageNum(), topicLine.getPageSize()),
                new LambdaQueryWrapper<TopicLine>()
                        .eq(TopicLine::getTopicActivityId, topicLine.getTopicActivityId())
                        .like(StringUtils.hasLength(topicLine.getTitle()), TopicLine::getTitle, topicLine.getTitle())));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<TopicLine> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(topicLineService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody TopicLine topicLine) {
        topicLineService.addOrModify(topicLine);
        return CommonResult.success();
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Object> delete(@RequestBody List<String> ids) {
        topicLineService.delete(ids);
        return CommonResult.success();
    }


    @Operation(summary = "设置题目")
    @PostMapping("/setTopic")
    public CommonResult<Object> setTopic(@RequestBody TopicLineTopicVo vo) {
        topicLineService.setTopic(vo);
        return CommonResult.success();
    }

    @Operation(summary = "查询题目")
    @GetMapping("/getTopic")
    public CommonResult<List<TopicLineTopic>> getTopic(@RequestParam(value = "id") String id) {
        List<TopicLineTopic> resultList = topicLineService.getTopic(id);
        return CommonResult.success(resultList);
    }
}
