package com.yunkesoftware.www.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.web.entity.Area;
import com.yunkesoftware.www.web.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "地区信息")
@RestController
@RequestMapping("/wx/area")
public class AreaController {
    @Resource
    private AreaService areaService;

    @Operation(summary = "父id查询区域")
    @GetMapping("/listByPid")
    public CommonResult<List<Area>> list(Long pid) {
        List<Area> areaList = areaService.list(new LambdaQueryWrapper<Area>()
                .eq(Area::getParentId, pid));
        return CommonResult.success(areaList);
    }


//    @Operation(summary = "更改省市区code")
//    @PostMapping("/changeCode")
//    public CommonResult<ChangeAreaCodeVo> changeCode(@RequestBody ChangeAreaCodeVo codeVo) {
//        ChangeAreaCodeVo resultVo = areaService.changeCode(codeVo);
//        return CommonResult.success(resultVo);
//    }
}
