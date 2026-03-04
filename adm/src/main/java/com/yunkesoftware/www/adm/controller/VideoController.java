package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.adm.service.VideoService;
import com.yunkesoftware.www.adm.entity.Video;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yk
 * @since 2026-01-14
 */
@Tag(name = "投票或积分-视频")
@RestController
@RequestMapping("/sys/video")
public class VideoController {
    @Resource
    private VideoService videoService;


    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<Video>> page(@RequestBody Video video) {
        return CommonResult.success(videoService.page(new Page<>(video.getPageNum(), video.getPageSize()),
                new LambdaQueryWrapper<Video>()
                        .like(StringUtils.hasLength(video.getTitle()), Video::getTitle, video.getTitle())
                        .orderByAsc(Video::getSeq)));
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/getOneById")
    public CommonResult<Video> getOneById(@RequestParam("id") String id) {
        return CommonResult.success(videoService.getById(id));
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    @YunkeSysLog("活动视频-添加或修改")
    public CommonResult<Object> addOrModify(@Valid @RequestBody Video video) {
        videoService.addOrModify(video);
        return CommonResult.success();
    }


    @Operation(summary = "通过Id批量删除")
    @DeleteMapping("/delete")
    @YunkeSysLog("活动视频-删除")
    public CommonResult<Boolean> delete(@RequestBody List<String> ids) {
        return CommonResult.status(videoService.removeByIds(ids));
    }

}
