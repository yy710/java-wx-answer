package com.yunkesoftware.www.web.service.impl;

import com.yunkesoftware.www.web.entity.DailyTaskConfig;
import com.yunkesoftware.www.web.mapper.DailyTaskConfigMapper;
import com.yunkesoftware.www.web.service.DailyTaskConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class DailyTaskConfigServiceImpl implements DailyTaskConfigService {
    public static final String CACHE_KEY = "daily-task:config:v1";

    @Resource
    private DailyTaskConfigMapper mapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public DailyTaskConfig get() {
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached instanceof DailyTaskConfig config) return config;
        } catch (Exception e) {
            log.warn("读取每日任务 Redis 缓存失败，回退 MySQL", e);
        }
        DailyTaskConfig config = mapper.selectById(1);
        if (config != null) {
            try {
                redisTemplate.opsForValue().set(CACHE_KEY, config, Duration.ofSeconds(60));
            } catch (Exception e) {
                log.warn("写入每日任务 Redis 缓存失败，继续使用 MySQL", e);
            }
        }
        return config;
    }
}
