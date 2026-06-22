package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.vo.ViewNumVo;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.utils.ViewNumUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sys/viewNumSet")
public class ViewNumSetController {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/page")
    public CommonResult<Page<ViewNumVo>> page() {
        Page<ViewNumVo> pageResult = new Page<>();
        List<ViewNumVo> recordList = new ArrayList<>();
        ViewNumVo viewNumVo = new ViewNumVo();
        Integer realViewNum = ViewNumUtils.getRealViewNum(redisTemplate);
        Integer viewMultiple = ViewNumUtils.getViewMultiple(redisTemplate);
        Integer randomMax = ViewNumUtils.getRandomMax(redisTemplate);
        Integer displayViewNum = ViewNumUtils.calculateDisplayViewNum(realViewNum, viewMultiple, randomMax);
        viewNumVo.setRealViewNum(realViewNum);
        viewNumVo.setViewNum(displayViewNum);
        viewNumVo.setDisplayViewNum(displayViewNum);
        viewNumVo.setViewMultiple(viewMultiple);
        viewNumVo.setRandomMax(randomMax);
        recordList.add(viewNumVo);
        pageResult.setRecords(recordList);
        pageResult.setTotal(1);
        return CommonResult.success(pageResult);
    }

    @PostMapping("/addOrModify")
    public CommonResult<Object> addOrModify(@Valid @RequestBody ViewNumVo viewNumVo) {
        ViewNumUtils.saveDisplayConfig(redisTemplate, viewNumVo.getViewMultiple(), viewNumVo.getRandomMax());
        return CommonResult.success();
    }
}
