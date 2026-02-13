package com.yunkesoftware.www.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.web.entity.VideoActivity;
import com.yunkesoftware.www.web.mapper.VideoActivityMapper;
import com.yunkesoftware.www.web.service.VideoActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 视频积分活动 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-02-11
 */
@Service
public class VideoActivityServiceImpl extends ServiceImpl<VideoActivityMapper, VideoActivity> implements VideoActivityService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public VideoActivity getOpen() {
        VideoActivity videoActivity = (VideoActivity) redisTemplate.opsForValue().get(RedisKey.VIDEO_ACTIVITY);
        if (videoActivity == null) {
            LocalDateTime nowTime = LocalDateTime.now();
            videoActivity = baseMapper.selectOne(new LambdaQueryWrapper<VideoActivity>()
                    .le(VideoActivity::getStartTime, nowTime)
                    .gt(VideoActivity::getEndTime, nowTime));
            if (videoActivity != null) {
                long cacheSecond = Duration.between(nowTime, videoActivity.getEndTime()).getSeconds();
                redisTemplate.opsForValue().set(RedisKey.VIDEO_ACTIVITY, videoActivity, cacheSecond, TimeUnit.SECONDS);
            }
        }
        return videoActivity;
    }
}
