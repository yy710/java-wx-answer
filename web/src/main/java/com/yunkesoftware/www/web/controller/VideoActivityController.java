package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.service.VideoActivityService;
import com.yunkesoftware.www.web.entity.VideoActivity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author yk
 * @since 2026-02-11
 */
@Tag(name = "视频积分活动")
@RestController
@RequestMapping("/wx/videoActivity")
public class VideoActivityController {
    @Resource
    private VideoActivityService videoActivityService;

    @Operation(summary = "获取进行中的视频活动")
    @GetMapping("/getOneById")
    public CommonResult<VideoActivity> getOpen() {
        VideoActivity videoActivity = videoActivityService.getOpen();
        return CommonResult.success(videoActivity);
    }

}
