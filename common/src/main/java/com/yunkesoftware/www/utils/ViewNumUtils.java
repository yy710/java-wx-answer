package com.yunkesoftware.www.utils;

import com.yunkesoftware.www.constant.RedisKey;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ThreadLocalRandom;

public class ViewNumUtils {
    private static final int DEFAULT_VIEW_MULTIPLE = 1;
    private static final int DEFAULT_RANDOM_MAX = 0;

    public static Integer incrementRealViewNum(RedisTemplate<String, Object> redisTemplate) {
        Object current = redisTemplate.opsForValue().get(RedisKey.USER_VIEW_NUM);
        if (current == null) {
            redisTemplate.opsForValue().set(RedisKey.USER_VIEW_NUM, 1);
            return 1;
        }
        Long next = redisTemplate.opsForValue().increment(RedisKey.USER_VIEW_NUM);
        if (next != null) {
            return next.intValue();
        }
        return getRealViewNum(redisTemplate);
    }

    public static Integer getRealViewNum(RedisTemplate<String, Object> redisTemplate) {
        return getInt(redisTemplate.opsForValue().get(RedisKey.USER_VIEW_NUM), 0);
    }

    public static Integer getViewMultiple(RedisTemplate<String, Object> redisTemplate) {
        return Math.max(DEFAULT_VIEW_MULTIPLE, getInt(redisTemplate.opsForValue().get(RedisKey.USER_VIEW_NUM_MULTIPLE), DEFAULT_VIEW_MULTIPLE));
    }

    public static Integer getRandomMax(RedisTemplate<String, Object> redisTemplate) {
        return Math.max(DEFAULT_RANDOM_MAX, getInt(redisTemplate.opsForValue().get(RedisKey.USER_VIEW_NUM_RANDOM_MAX), DEFAULT_RANDOM_MAX));
    }

    public static Integer getDisplayViewNum(RedisTemplate<String, Object> redisTemplate) {
        return calculateDisplayViewNum(getRealViewNum(redisTemplate), getViewMultiple(redisTemplate), getRandomMax(redisTemplate));
    }

    public static Integer calculateDisplayViewNum(Integer realViewNum, Integer viewMultiple, Integer randomMax) {
        int safeRealViewNum = Math.max(0, realViewNum == null ? 0 : realViewNum);
        int safeViewMultiple = Math.max(DEFAULT_VIEW_MULTIPLE, viewMultiple == null ? DEFAULT_VIEW_MULTIPLE : viewMultiple);
        int safeRandomMax = Math.max(DEFAULT_RANDOM_MAX, randomMax == null ? DEFAULT_RANDOM_MAX : randomMax);
        int randomNum = safeRandomMax > 0 ? ThreadLocalRandom.current().nextInt(safeRandomMax + 1) : 0;
        return safeRealViewNum * safeViewMultiple + randomNum;
    }

    public static void saveDisplayConfig(RedisTemplate<String, Object> redisTemplate, Integer viewMultiple, Integer randomMax) {
        redisTemplate.opsForValue().set(RedisKey.USER_VIEW_NUM_MULTIPLE, Math.max(DEFAULT_VIEW_MULTIPLE, viewMultiple == null ? DEFAULT_VIEW_MULTIPLE : viewMultiple));
        redisTemplate.opsForValue().set(RedisKey.USER_VIEW_NUM_RANDOM_MAX, Math.max(DEFAULT_RANDOM_MAX, randomMax == null ? DEFAULT_RANDOM_MAX : randomMax));
    }

    private static Integer getInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
