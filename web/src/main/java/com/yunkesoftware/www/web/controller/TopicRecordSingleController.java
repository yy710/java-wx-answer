package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.TopicRecordSingleService;
import com.yunkesoftware.www.web.entity.TopicRecordSingle;
import com.yunkesoftware.www.web.vo.TopicSingleResultVo;
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
@RequestMapping("/wx/topicRecordSingle")
public class TopicRecordSingleController {
    @Resource
    private TopicRecordSingleService topicRecordSingleService;

    @Operation(summary = "新增")
    @PostMapping("/add")
    public CommonResult<TopicSingleResultVo> add(@RequestBody TopicRecordSingle recordSingle) {
        TopicSingleResultVo resultVo = topicRecordSingleService.add(recordSingle);
        return CommonResult.success(resultVo);
    }
}
