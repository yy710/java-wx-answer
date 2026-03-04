package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.vo.ViewNumVo;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.result.CommonResult;
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
        Integer viewNum = (Integer) redisTemplate.opsForValue().get(RedisKey.USER_VIEW_NUM);
        ViewNumVo viewNumVo = new ViewNumVo();
        viewNumVo.setViewNum(viewNum);
        recordList.add(viewNumVo);
        pageResult.setRecords(recordList);
        return CommonResult.success(pageResult);
    }

    @PostMapping("/addOrModify")
    public CommonResult<Object> addOrModify(@Valid @RequestBody ViewNumVo viewNumVo) {
        redisTemplate.opsForValue().set(RedisKey.USER_VIEW_NUM, viewNumVo.getViewNum());
        return CommonResult.success();
    }
}
