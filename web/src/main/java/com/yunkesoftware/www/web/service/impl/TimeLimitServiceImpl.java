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
        // 3月1日00:01-3月14日16:00
//        LocalDateTime nowTime = LocalDateTime.now();
//        // 2026-03-01 00:01:00 2026-03-14 16:00:00
//        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 0, 1);
//        if (nowTime.isBefore(startTime)) {
//            throw new YunKeException(ExceptionEnum.FAIL, "获得积分开始时间为：2026-03-01 00:01:00");
//        }
//        LocalDateTime endTime = LocalDateTime.of(2026, 3, 14, 16, 0);
//        if (nowTime.isAfter(endTime)) {
//            throw new YunKeException(ExceptionEnum.FAIL, "获得积分结束时间为：2026-03-14 16:00:00");
//        }
    }
}
