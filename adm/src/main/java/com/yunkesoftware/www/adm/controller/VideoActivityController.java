package com.yunkesoftware.www.adm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.VideoActivityService;
import com.yunkesoftware.www.adm.entity.VideoActivity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-02-11
 */
@Tag(name = "视频积分活动")
@RestController
@RequestMapping("/sys/videoActivity")
public class VideoActivityController {
    @Resource
    private VideoActivityService videoActivityService;

    @Operation(summary = "获取全部列表")
    @PostMapping("/list")
    @EasyExcelExport()
    public CommonResult<List<VideoActivity>> list(@RequestBody VideoActivity videoActivity) {
        return CommonResult.success(videoActivityService.list(new LambdaQueryWrapper<>(videoActivity)));
    }

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<VideoActivity>> page(@RequestBody VideoActivity videoActivity) {
        return CommonResult.success(videoActivityService.page(new Page<>(videoActivity.getPageNum(), videoActivity.getPageSize()),
                new LambdaQueryWrapper<VideoActivity>()
                        .like(StringUtils.hasLength(videoActivity.getTitle()), VideoActivity::getTitle, videoActivity.getTitle())
                        .orderByAsc(VideoActivity::getStartTime)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<VideoActivity> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(videoActivityService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody VideoActivity videoActivity) {
        videoActivityService.addOrModify(videoActivity);
        return CommonResult.success();
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        videoActivityService.delete(ids);
        return CommonResult.success();
    }
}
