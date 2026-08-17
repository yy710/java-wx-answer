package com.yunkesoftware.www.adm.service.impl;

import com.yunkesoftware.www.adm.entity.Video;
import com.yunkesoftware.www.adm.mapper.VideoMapper;
import com.yunkesoftware.www.adm.service.VideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 视频信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Override
    public void addOrModify(Video video) {
        if (Boolean.TRUE.equals(video.getDailyTaskEnabled()) && (video.getDurationSeconds() == null || video.getDurationSeconds() <= 0)) {
            throw new YunKeException(ExceptionEnum.FAIL, "VIDEO_DURATION_UNTRUSTED:启用每日任务的视频必须填写可信时长");
        }
        video.setCreateTime(null);
        if (StringUtils.hasLength(video.getId())) {
            baseMapper.updateById(video);
        } else {
            baseMapper.insert(video);
        }
    }
}
