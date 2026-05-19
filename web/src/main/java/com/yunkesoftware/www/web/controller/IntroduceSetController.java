package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.entity.IntroduceSet;
import com.yunkesoftware.www.result.CommonResult;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/wx/introduceSet")
public class IntroduceSetController {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @GetMapping("/checkStatus")
    public CommonResult<Boolean> checkStatus() {
        IntroduceSet introduceSet = (IntroduceSet) redisTemplate.opsForValue().get(RedisKey.INTRODUCE_SET);
        if (introduceSet != null) {
            LocalDateTime nowTime = LocalDateTime.now();
            if (nowTime.isAfter(introduceSet.getStartTime()) && nowTime.isBefore(introduceSet.getEndTime())) {
                return CommonResult.success(true);
            }
        }
        return CommonResult.success(false);
    }
}
