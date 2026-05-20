package com.yunkesoftware.www.web.service.impl;

import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.service.TimeLimitService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TimeLimitServiceImpl implements TimeLimitService {

    @Override
    public void checkTimeLimit() {
        // 3月1日00:01-5月20日23:00
        LocalDateTime nowTime = LocalDateTime.now();
        // 2026-03-01 00:01:00 2026-05-20 23:00:00
        LocalDateTime startTime = LocalDateTime.of(2026, 5, 21, 0, 1);
        if (nowTime.isBefore(startTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "获得积分开始时间为：2026-03-01 00:01:00");
        }
        LocalDateTime endTime = LocalDateTime.of(2026, 5, 31, 23, 59);
        if (nowTime.isAfter(endTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "获得积分结束时间为：2026-05-20 23:00:00");
        }
    }
}
