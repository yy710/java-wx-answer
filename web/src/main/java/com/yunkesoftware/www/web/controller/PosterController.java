package com.yunkesoftware.www.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.dto.PosterGenerateRequest;
import com.yunkesoftware.www.web.service.PosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "海报模板与生成")
@RestController
@RequestMapping("/wx")
public class PosterController {
    @Resource private PosterService posterService;

    @Operation(summary = "已发布海报模板")
    @GetMapping("/posterTemplate/list")
    public CommonResult<Map<String, Object>> list() {
        StpUtil.checkLogin();
        return CommonResult.success(Map.of("templates", posterService.listPublished()));
    }

    @Operation(summary = "生成海报")
    @PostMapping("/poster/generate")
    public CommonResult<Map<String, Object>> generate(@RequestBody PosterGenerateRequest request) { StpUtil.checkLogin(); return CommonResult.success(posterService.generate(StpUtil.getLoginIdAsString(), request)); }

    @Operation(summary = "读取已生成海报地址")
    @GetMapping("/poster/generated/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) {
        StpUtil.checkLogin();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).cacheControl(CacheControl.noStore())
                .body(posterService.download(StpUtil.getLoginIdAsString(), id));
    }
}
