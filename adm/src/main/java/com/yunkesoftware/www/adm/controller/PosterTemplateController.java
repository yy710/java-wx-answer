package com.yunkesoftware.www.adm.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.PosterTemplate;
import com.yunkesoftware.www.adm.service.PosterTemplateService;
import com.yunkesoftware.www.annotation.YunkeSysLog;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "海报模板管理")
@RestController
@RequestMapping("/sys/posterTemplate")
public class PosterTemplateController {
    @Resource private PosterTemplateService service;

    @PostMapping("/page")
    @Operation(summary = "模板分页")
    public CommonResult<Page<PosterTemplate>> page(@RequestBody PosterTemplate query) { return CommonResult.success(service.page(new Page<>(query.getPageNum(), query.getPageSize()))); }

    @GetMapping("/getOneById")
    public CommonResult<PosterTemplate> getOneById(@RequestParam("id") String id) { return CommonResult.success(service.getWithAssets(id)); }

    @PostMapping("/addOrModify")
    @YunkeSysLog("海报模板保存")
    public CommonResult<PosterTemplate> addOrModify(@RequestBody PosterTemplate template) { return CommonResult.success(service.saveTemplate(template, StpUtil.getLoginIdAsString())); }

    @PostMapping("/publish")
    @YunkeSysLog("海报模板发布")
    public CommonResult<Boolean> publish(@RequestParam("id") String id, @RequestParam(value = "version", required = false) Integer version) { service.publish(id, version); return CommonResult.success(true); }

    @PostMapping("/disable")
    @YunkeSysLog("海报模板停用")
    public CommonResult<Boolean> disable(@RequestParam("id") String id, @RequestParam(value = "version", required = false) Integer version) { service.disable(id, version); return CommonResult.success(true); }

    @PostMapping("/preview")
    public CommonResult<PosterTemplate> preview(@RequestBody PosterTemplate template) { return CommonResult.success(service.preview(template)); }
}
