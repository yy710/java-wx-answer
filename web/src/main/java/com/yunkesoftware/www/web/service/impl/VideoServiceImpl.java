package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.utils.IpUtils;
import com.yunkesoftware.www.web.entity.*;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.UserWalletService;
import com.yunkesoftware.www.web.service.VideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 视频信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
    @Resource
    private TicketRecordMapper ticketRecordMapper;
    @Resource
    private TicketActivityMapper ticketActivityMapper;
    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private VideoActivityMapper videoActivityMapper;
    @Resource
    private TicketActivityVideoMapper ticketActivityVideoMapper;
    @Resource
    private VideoActivityVideoMapper videoActivityVideoMapper;
    @Resource
    private UserWalletService userWalletService;

    private VideoActivity getOpenVideoActivity() {
        VideoActivity videoActivity = (VideoActivity) redisTemplate.opsForValue().get(RedisKey.VIDEO_ACTIVITY);
        if (videoActivity == null) {
            LocalDateTime nowTime = LocalDateTime.now();
            videoActivity = videoActivityMapper.selectOne(new LambdaQueryWrapper<VideoActivity>()
                    .le(VideoActivity::getStartTime, nowTime)
                    .gt(VideoActivity::getEndTime, nowTime));
            if (videoActivity != null) {
                long seconds = Math.max(Duration.between(nowTime, videoActivity.getEndTime()).getSeconds(), 1);
                redisTemplate.opsForValue().set(RedisKey.VIDEO_ACTIVITY, videoActivity, seconds, TimeUnit.SECONDS);
            }
        }
        return videoActivity;
    }

    private TicketActivity getOpenTicketActivity() {
        TicketActivity ticketActivity = (TicketActivity) redisTemplate.opsForValue().get(RedisKey.TICKET_ACTIVITY);
        if (ticketActivity == null) {
            LocalDateTime nowTime = LocalDateTime.now();
            ticketActivity = ticketActivityMapper.selectOne(new LambdaQueryWrapper<TicketActivity>()
                    .le(TicketActivity::getStartTime, nowTime)
                    .gt(TicketActivity::getEndTime, nowTime));
            if (ticketActivity != null) {
                long seconds = Math.max(Duration.between(nowTime, ticketActivity.getEndTime()).getSeconds(), 1);
                redisTemplate.opsForValue().set(RedisKey.TICKET_ACTIVITY, ticketActivity, seconds, TimeUnit.SECONDS);
            }
        }
        return ticketActivity;
    }

    @Override
    public Page<Video> pageTicket(Video video) {
        Page<Video> pageParam = new Page<>(video.getPageNum(), video.getPageSize());
        TicketActivity ticketActivity = getOpenTicketActivity();
        if (ticketActivity == null) {
            return pageParam;
        }
        video.setActivityId(ticketActivity.getId());
        Page<Video> pageResult = ticketActivityVideoMapper.pageByQuery(pageParam, video);

        if (!StpUtil.isLogin()) {
            return pageResult;
        }
        String userId = StpUtil.getLoginIdAsString();
        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));

        for (Video record : pageResult.getRecords()) {
            if (record.getRewardAmount().compareTo(BigDecimal.ZERO) > 0 && userWallet != null) {
                UserWalletRecord checkData = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                        .eq(UserWalletRecord::getWalletId, userWallet.getId())
                        .eq(UserWalletRecord::getEventId, record.getId())
                        .eq(UserWalletRecord::getEventType, UserWalletEventEnum.TICKET_VIDEO.getKey())
                        .select(UserWalletRecord::getId));
                record.setGetFlag(checkData != null);
            }

            Long ticketNum = ticketRecordMapper.selectCount(new LambdaQueryWrapper<TicketRecord>()
                    .eq(TicketRecord::getTicketVideoId, record.getId())
                    .eq(TicketRecord::getUserId, userId));
            record.setTicketNum(ticketNum);

            // 投票人数+总票数乘以倍数
            if (ticketActivity.getTicketMultiple() != null && record.getTicketTotal() != null) {
                record.setTicketTotal(record.getTicketTotal() * ticketActivity.getTicketMultiple());
            }
        }
        return pageResult;
    }

    @Override
    public Page<Video> pageIntegral(Video video) {
        Page<Video> pageParam = new Page<>(video.getPageNum(), video.getPageSize());
        VideoActivity videoActivity = getOpenVideoActivity();
        if (videoActivity == null) {
            return pageParam;
        }
        video.setActivityId(videoActivity.getId());
        Page<Video> pageResult = videoActivityVideoMapper.pageByQuery(pageParam, video);
        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, StpUtil.getLoginIdAsString())
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        if (userWallet != null) {
            for (Video record : pageResult.getRecords()) {
                UserWalletRecord checkRecord = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                        .eq(UserWalletRecord::getWalletId, userWallet.getId())
                        .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                        .eq(UserWalletRecord::getEventId, record.getId())
                        .select(UserWalletRecord::getId)
                        .last("LIMIT 1"));
                record.setGetFlag(checkRecord != null);
            }
        }
        return pageResult;
    }


    @Override
    public void doVote(String id) {
        // 每个活动每个用户有固定的票数
        Video video = baseMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getId, id)
                .select(Video::getId, Video::getStatus, Video::getTicketTotal));
        if (video == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "视频不存在或已下架-请刷新页面^_^");
        }
        LocalDateTime nowTime = LocalDateTime.now();

        TicketActivity ticketActivity = getOpenTicketActivity();
        if (ticketActivity == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "无进行中的投票活动-请刷新页面^_^");
        }

        if (ticketActivity.getStartTicketTime().isAfter(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票未开始,最早投票时间：" + ticketActivity.getStartTicketTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (ticketActivity.getEndTicketTime().isBefore(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票时间已截止");
        }
        TicketActivityVideo ticketActivityVideo = ticketActivityVideoMapper.selectOne(new LambdaQueryWrapper<TicketActivityVideo>()
                .eq(TicketActivityVideo::getVideoId, id)
                .eq(TicketActivityVideo::getTicketActivityId, ticketActivity.getId()));
        if (ticketActivityVideo == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "当前视频不可投票-请刷新页面重试");
        }


        // 检查用户当前活动是否还有剩余票
        String userId = StpUtil.getLoginIdAsString();
        // 已经使用的票数
        Long usedTicketNum = ticketRecordMapper.selectCount(new LambdaQueryWrapper<TicketRecord>()
                .eq(TicketRecord::getTicketActivityId, ticketActivity.getId())
                .eq(TicketRecord::getUserId, userId)
                .like(TicketRecord::getCreateTime, LocalDate.now()));
        if (usedTicketNum >= ticketActivity.getTicketLimit()) {
            throw new YunKeException(ExceptionEnum.FAIL, "当日票数已用完");
        }

        // 生成投票记录(修改视频+活动投票数)
        TicketRecord ticketRecord = new TicketRecord();
        ticketRecord.setUserId(userId);
        ticketRecord.setTicketActivityId(ticketActivity.getId());
        ticketRecord.setTicketVideoId(video.getId());
        ticketRecord.setIpAddr(IpUtils.getIpAddr());
        ticketRecordMapper.insert(ticketRecord);

        baseMapper.update(new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, id)
                .set(Video::getTicketTotal, video.getTicketTotal() + 1));
        // 更新投票人数+活动总票数
        long ticketUser = ticketRecordMapper.countUser(ticketActivity.getId());
        ticketActivityMapper.update(new LambdaUpdateWrapper<TicketActivity>()
                .eq(TicketActivity::getId, ticketActivity.getId())
                .set(TicketActivity::getTicketTotal, ticketActivity.getTicketTotal() + 1)
                .set(TicketActivity::getTicketUserNum, ticketUser));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doReward(String id, Integer activityType) {
        Video video = baseMapper.selectById(id);
        if (video == null || !video.getStatus()) {
            throw new YunKeException(ExceptionEnum.FAIL, "视频不存在或已下架-请刷新页面重试");
        }
        String userId = StpUtil.getLoginIdAsString();
        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        BigDecimal rewardAmount;
        Integer eventType;
        if (activityType == 1) { //视频活动
            VideoActivity videoActivity = getOpenVideoActivity();
            if (videoActivity == null) {
                throw new YunKeException(ExceptionEnum.FAIL, "无进行中的视频奖励积分活动-请刷新页面重试");
            }
            VideoActivityVideo videoActivityVideo = videoActivityVideoMapper.selectOne(new LambdaQueryWrapper<VideoActivityVideo>()
                    .eq(VideoActivityVideo::getVideoActivityId, videoActivity.getId())
                    .eq(VideoActivityVideo::getVideoId, id));
            if (videoActivityVideo == null || videoActivityVideo.getRewardAmount().compareTo(BigDecimal.ZERO) < 1) {
                throw new YunKeException(ExceptionEnum.FAIL, "当前视频不可获得积分");
            }
            if (userWallet != null) {
                //检查是否已获得过当前视频的积分
                UserWalletRecord checkRecord = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                        .eq(UserWalletRecord::getWalletId, userWallet.getId())
                        .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                        .eq(UserWalletRecord::getEventId, id)
                        .select(UserWalletRecord::getId)
                        .last("LIMIT 1"));
                if (checkRecord != null) {
                    throw new YunKeException(ExceptionEnum.FAIL, "已获得当前视频的积分");
                }
            }
            eventType = UserWalletEventEnum.VIDEO.getKey();
            rewardAmount = videoActivityVideo.getRewardAmount();
        } else { //投票视频获得积分
            TicketActivity ticketActivity = getOpenTicketActivity();
            if (ticketActivity == null) {
                throw new YunKeException(ExceptionEnum.FAIL, "无进行中的投票活动-请刷新页面重试");
            }
            TicketActivityVideo ticketActivityVideo = ticketActivityVideoMapper.selectOne(new LambdaQueryWrapper<TicketActivityVideo>()
                    .eq(TicketActivityVideo::getVideoId, id)
                    .eq(TicketActivityVideo::getTicketActivityId, ticketActivity.getId()));
            if (ticketActivityVideo == null || ticketActivityVideo.getRewardAmount().compareTo(BigDecimal.ZERO) < 1) {
                throw new YunKeException(ExceptionEnum.FAIL, "当前视频不可获得积分");
            }
            if (userWallet != null) {
                //检查是否已获得过当前视频的积分
                UserWalletRecord checkRecord = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                        .eq(UserWalletRecord::getWalletId, userWallet.getId())
                        .eq(UserWalletRecord::getEventType, UserWalletEventEnum.TICKET_VIDEO.getKey())
                        .eq(UserWalletRecord::getEventId, id)
                        .select(UserWalletRecord::getId)
                        .last("LIMIT 1"));
                if (checkRecord != null) {
                    throw new YunKeException(ExceptionEnum.FAIL, "已获得当前视频的积分");
                }
            }
            eventType = UserWalletEventEnum.TICKET_VIDEO.getKey();
            rewardAmount = ticketActivityVideo.getRewardAmount();
        }
        if (rewardAmount.compareTo(BigDecimal.ZERO) < 1) {
            throw new YunKeException(ExceptionEnum.FAIL, "当前视频不可获得积分");
        }
        userWalletService.rewardIntegral(userId, id, eventType, rewardAmount);
    }

    @Override
    public Video getOneById(String id, Integer activityType) {
        Video video = baseMapper.selectById(id);
        if (video != null) {
            if (activityType == 2) { //投票的视频详情
                TicketActivity ticketActivity = getOpenTicketActivity();
                if (ticketActivity != null) {
                    TicketActivityVideo ticketActivityVideo = ticketActivityVideoMapper.selectOne(new LambdaQueryWrapper<TicketActivityVideo>()
                            .eq(TicketActivityVideo::getVideoId, id)
                            .eq(TicketActivityVideo::getTicketActivityId, ticketActivity.getId()));
                    if (ticketActivityVideo != null) {
                        video.setMinTime(ticketActivityVideo.getMinTime());
                        video.setRewardAmount(ticketActivityVideo.getRewardAmount());
                    }
                }
            } else {
                VideoActivity videoActivity = getOpenVideoActivity();
                if (videoActivity != null) {
                    VideoActivityVideo videoActivityVideo = videoActivityVideoMapper.selectOne(new LambdaQueryWrapper<VideoActivityVideo>()
                            .eq(VideoActivityVideo::getVideoId, id)
                            .eq(VideoActivityVideo::getVideoActivityId, videoActivity.getId()));
                    if (videoActivityVideo != null) {
                        video.setMinTime(videoActivityVideo.getMinTime());
                        video.setRewardAmount(videoActivityVideo.getRewardAmount());
                    }
                }
            }

            if (video.getRewardAmount() != null && video.getRewardAmount().compareTo(BigDecimal.ZERO) > 0) {
                String userId = StpUtil.getLoginIdAsString();
                UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getUserId, userId)
                        .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
                if (userWallet != null) {
                    UserWalletRecord checkData = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                            .eq(UserWalletRecord::getWalletId, userWallet.getId())
                            .eq(UserWalletRecord::getEventId, id)
                            .eq(activityType == 2, UserWalletRecord::getEventType, UserWalletEventEnum.TICKET_VIDEO.getKey())
                            .eq(activityType == 1, UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                            .select(UserWalletRecord::getId));
                    video.setGetFlag(checkData != null);
                }
            }
        }
        return video;
    }


}
