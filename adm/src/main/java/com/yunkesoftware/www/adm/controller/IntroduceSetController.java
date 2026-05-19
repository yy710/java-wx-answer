package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.entity.IntroduceSet;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author yk
 * @since 2025-11-07
 */
@Tag(name = "介绍信息-设置")
@RestController
@RequestMapping("/sys/introduceSet")
public class IntroduceSetController {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "分页")
    @PostMapping("/page")
    @EasyExcelExport()
    public CommonResult<Page<IntroduceSet>> page(@RequestBody PageCurrency query) {
        Page<IntroduceSet> pageResult = new Page<>(query.getPageNum(), query.getPageSize());
        List<IntroduceSet> records = new ArrayList<>();
        IntroduceSet introduceSet = (IntroduceSet) redisTemplate.opsForValue().get(RedisKey.INTRODUCE_SET);
        if (introduceSet == null) {
            introduceSet = new IntroduceSet();
        }
        records.add(introduceSet);
        pageResult.setRecords(records);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "添加或修改")
    @PostMapping("/addOrModify")
    public CommonResult<Boolean> addOrModify(@RequestBody IntroduceSet introduceSet) {
        redisTemplate.opsForValue().set(RedisKey.INTRODUCE_SET, introduceSet);
        return CommonResult.success();
    }


}
