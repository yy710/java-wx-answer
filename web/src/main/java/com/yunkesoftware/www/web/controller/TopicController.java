package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.TopicService;
import com.yunkesoftware.www.web.entity.Topic;
import com.yunkesoftware.www.web.vo.TopicLineDataVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-16
 */
@Tag(name = "答题活动-题目信息")
@RestController
@RequestMapping("/wx/topic")
public class TopicController {
    @Resource
    private TopicService topicService;

    @Operation(summary = "根据线路随机生成指定的题目数")
    @GetMapping("/list")
    public CommonResult<TopicLineDataVo> list(@RequestParam(value = "topicLineId") String topicLineId) {
        TopicLineDataVo dataVo = topicService.listByQuery(topicLineId);
        return CommonResult.success(dataVo);
    }

    @Operation(summary = "获取随机题目(趣味答题已回答正确的进行过滤)")
    @GetMapping("/listRandom")
    public CommonResult<List<Topic>> listRandom() {
        List<Topic> resultList = topicService.listRandom();
        return CommonResult.success(resultList);
    }
}
