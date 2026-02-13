package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.VideoService;
import com.yunkesoftware.www.web.entity.Video;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * @author yk
 * @since 2026-01-15
 */
@Tag(name = "投票活动-视频")
@RestController
@RequestMapping("/wx/video")
public class VideoController {
    @Resource
    private VideoService videoService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult<Page<Video>> page(@RequestBody Video video) {
        Page<Video> pageResult = videoService.pageByQuery(video);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Video> getOneById(@RequestParam("id") String id) {
        Video video = videoService.getOneById(id);
        return CommonResult.success(video);
    }

    @Operation(summary = "投票")
    @GetMapping("/doVote")
    public CommonResult<Object> doVote(@RequestParam("id") String id) {
        videoService.doVote(id);
        return CommonResult.success();
    }

    @Operation(summary = "获得积分奖励")
    @GetMapping("/doReward")
    public CommonResult<Object> doReward(@RequestParam("id") String id) {
        videoService.doReward(id);
        return CommonResult.success();
    }
}
