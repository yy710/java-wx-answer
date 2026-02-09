package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.Area;
import com.yunkesoftware.www.adm.service.AreaService;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author g
 * @since 2023-12-11
 */
@Tag(name = "地区管理")
@RestController
@RequestMapping("/sys/area")
public class AreaController {

    @Resource
    private AreaService areaService;


    // 分页获取
    @GetMapping("/listByPid")
    @Operation(summary = "获取省市区信息", description = "根据省市区的pid获取地址信息")
    @Parameter(name = "pid", description = "省市区的pid(pid为0获取所有省份)", required = true)
    public CommonResult<List<Area>> listByPid(Long pid) {
        return CommonResult.success(areaService.list(new LambdaQueryWrapper<Area>().eq(Area::getParentId, pid)));
    }

    @Operation(summary = "分页")
    @PostMapping("/page")
    public CommonResult page(@RequestBody Area area) {
        return CommonResult.success(areaService.page(new Page<>(area.getPageNum(), area.getPageSize()), new LambdaQueryWrapper<>(area)));
    }

    @PostMapping("/list")
    @Operation(summary = "列表查询")
    public CommonResult list(@RequestBody Area area) {
        List<Area> list = areaService.list(new LambdaQueryWrapper<>(area));
        return CommonResult.success(list);
    }


    @Operation(summary = "区域树")
    @GetMapping("/getAreaTree")
    public CommonResult<List<Area>> getAreaTree() {
        return CommonResult.success(areaService.getAreaTree());
    }

}
