package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.adm.entity.Video;
import com.yunkesoftware.www.adm.entity.VideoActivity;
import com.yunkesoftware.www.adm.entity.VideoActivityVideo;
import com.yunkesoftware.www.adm.mapper.VideoActivityMapper;
import com.yunkesoftware.www.adm.mapper.VideoActivityVideoMapper;
import com.yunkesoftware.www.adm.mapper.VideoMapper;
import com.yunkesoftware.www.adm.service.VideoActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.vo.VideoActivityVo;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

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
    @Resource
    private VideoActivityVideoMapper videoActivityVideoMapper;
    @Resource
    private VideoMapper videoMapper;

    @Override
    public void addOrModify(VideoActivity videoActivity) {
        if (videoActivity.getStartTime().isAfter(videoActivity.getEndTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "开始时间不能大于结束时间");
        }
        LocalDateTime nowTime = LocalDateTime.now();
        if (nowTime.isAfter(videoActivity.getEndTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间不能小于当前时间");
        }
        // 校验时间不能和其它活动重复
        LambdaQueryWrapper<VideoActivity> queryWrapper = new LambdaQueryWrapper<VideoActivity>()
                .ne(StringUtils.hasLength(videoActivity.getId()), VideoActivity::getId, videoActivity.getId())
                .le(VideoActivity::getStartTime, videoActivity.getEndTime())
                .gt(VideoActivity::getEndTime, videoActivity.getStartTime())
                .last("LIMIT 1")
                .select(VideoActivity::getId, VideoActivity::getTitle);
        VideoActivity checkData = baseMapper.selectOne(queryWrapper);
        if (checkData != null) {
            throw new YunKeException(ExceptionEnum.FAIL, "时间与-" + checkData.getTitle() + "-重叠");
        }
        if (StringUtils.hasLength(videoActivity.getId())) {
            baseMapper.updateById(videoActivity);
        } else {
            baseMapper.insert(videoActivity);
        }
        redisTemplate.delete(RedisKey.VIDEO_ACTIVITY);
    }

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        redisTemplate.delete(RedisKey.VIDEO_ACTIVITY);
    }

    @Override
    public void setVideo(VideoActivityVo vo) {
        videoActivityVideoMapper.delete(new LambdaQueryWrapper<VideoActivityVideo>()
                .eq(VideoActivityVideo::getVideoActivityId, vo.getId()));
        if (vo.getVideoList() != null && vo.getVideoList().size() > 0) {
            for (VideoActivityVideo activityVideo : vo.getVideoList()) {
                activityVideo.setVideoActivityId(vo.getId());
            }
            videoActivityVideoMapper.insertBatch(vo.getVideoList());
        }

    }

    @Override
    public List<VideoActivityVideo> getVideo(String id) {
        List<VideoActivityVideo> resultList = videoActivityVideoMapper.selectList(new LambdaQueryWrapper<VideoActivityVideo>()
                .eq(VideoActivityVideo::getVideoActivityId, id));
        for (VideoActivityVideo activityVideo : resultList) {
            Video video = videoMapper.selectById(activityVideo.getVideoId());
            activityVideo.setVideoTitle(video.getTitle());
            activityVideo.setVideoUrlPic(video.getUrlPic());
            activityVideo.setVideoUrlVideo(video.getUrlVideo());
        }

        return resultList;
    }
}
