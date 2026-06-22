package com.yunkesoftware.www.web.service.impl;

import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.entity.IntroduceSet;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.service.TimeLimitService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimeLimitServiceImpl implements TimeLimitService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void checkTimeLimit() {
        LocalDateTime nowTime = LocalDateTime.now();
        IntroduceSet introduceSet = (IntroduceSet) redisTemplate.opsForValue().get(RedisKey.INTRODUCE_SET);
        if (introduceSet == null || introduceSet.getStartTime() == null || introduceSet.getEndTime() == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "请先在管理后台系统设置中设置活动开始时间和结束时间");
        }
        LocalDateTime startTime = introduceSet.getStartTime();
        LocalDateTime endTime = introduceSet.getEndTime();
        if (nowTime.isBefore(startTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "获得积分开始时间为：" + startTime.format(FORMATTER));
        }
        if (nowTime.isAfter(endTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "获得积分结束时间为：" + endTime.format(FORMATTER));
        }
    }
}
