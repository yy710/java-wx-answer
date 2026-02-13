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
import com.yunkesoftware.www.web.service.VideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
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

    @Override
    public Page<Video> pageByQuery(Video video) {
        Page<Video> pageParam = new Page<>(video.getPageNum(), video.getPageSize());
        Page<Video> pageResult = baseMapper.selectPage(pageParam, new LambdaQueryWrapper<Video>()
                .eq(video.getTicketFlag() != null, Video::getTicketFlag, video.getTicketFlag())
                .eq(video.getRewardFlag() != null, Video::getRewardFlag, video.getRewardFlag())
                .eq(Video::getStatus, true)
                .orderByAsc(Video::getSeq));
        if (StpUtil.isLogin()) {
            String userId = StpUtil.getLoginIdAsString();
            UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId)
                    .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
            if (userWallet != null) {
                for (Video record : pageResult.getRecords()) {
                    if (Boolean.TRUE.equals(video.getRewardFlag())) {
                        UserWalletRecord checkData = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                                .eq(UserWalletRecord::getWalletId, userWallet.getId())
                                .eq(UserWalletRecord::getEventId, record.getId())
                                .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                                .select(UserWalletRecord::getId));
                        record.setGetFlag(checkData != null);
                    }
                    if (Boolean.TRUE.equals(video.getTicketFlag())) {
                        Long ticketNum = ticketRecordMapper.selectCount(new LambdaQueryWrapper<TicketRecord>()
                                .eq(TicketRecord::getTicketVideoId, record.getId())
                                .eq(TicketRecord::getUserId, userId));
                        record.setTicketNum(ticketNum);
                    }
                }
            }

        }
        return pageResult;
    }

    @Override
    public void doVote(String id) {
        // 每个活动每个用户有固定的票数
        Video video = baseMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getId, id)
                .select(Video::getId, Video::getStatus, Video::getTicketTotal, Video::getTicketFlag));
        if (video == null || !video.getStatus()) {
            throw new YunKeException(ExceptionEnum.FAIL, "视频不存在或已下架-请刷新页面^_^");
        }
        if (!Boolean.TRUE.equals(video.getTicketFlag())) {
            throw new YunKeException(ExceptionEnum.FAIL, "当前视频不可投票");
        }
        LocalDateTime nowTime = LocalDateTime.now();

        TicketActivity ticketActivity = (TicketActivity) redisTemplate.opsForValue().get(RedisKey.TICKET_ACTIVITY);
        if (ticketActivity == null) {
            ticketActivity = ticketActivityMapper.selectOne(new LambdaQueryWrapper<TicketActivity>()
                    .le(TicketActivity::getStartTime, nowTime)
                    .gt(TicketActivity::getEndTime, nowTime));
            if (ticketActivity == null) {
                throw new YunKeException(ExceptionEnum.FAIL, "无进行中的投票活动-请刷新页面^_^");
            }
            Duration duration = Duration.between(nowTime, ticketActivity.getEndTime());
            redisTemplate.opsForValue().set(RedisKey.TICKET_ACTIVITY, ticketActivity, duration.getSeconds(), TimeUnit.SECONDS);
        }

        if (ticketActivity.getStartTicketTime().isAfter(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票未开始,最早投票时间：" + ticketActivity.getStartTicketTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (ticketActivity.getEndTicketTime().isBefore(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票时间已截止");
        }
        // 检查用户当前活动是否还有剩余票
        String userId = StpUtil.getLoginIdAsString();
        // 已经使用的票数
        Long usedTicketNum = ticketRecordMapper.selectCount(new LambdaQueryWrapper<TicketRecord>()
                .eq(TicketRecord::getTicketActivityId, ticketActivity.getId())
                .eq(TicketRecord::getUserId, userId));
        if (usedTicketNum >= ticketActivity.getTicketLimit()) {
            throw new YunKeException(ExceptionEnum.FAIL, "您已没有剩余票数了");
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
        // 当前投票活动投票用户数
        TicketRecord checkData = ticketRecordMapper.selectOne(new LambdaQueryWrapper<TicketRecord>()
                .eq(TicketRecord::getUserId, userId)
                .eq(TicketRecord::getTicketActivityId, ticketActivity.getId())
                .last("LIMIT 1")
                .select(TicketRecord::getId));
        ticketActivityMapper.update(new LambdaUpdateWrapper<TicketActivity>()
                .eq(TicketActivity::getId, ticketActivity.getId())
                .set(TicketActivity::getTicketTotal, ticketActivity.getTicketTotal() + 1)
                .set(checkData == null, TicketActivity::getTicketUserNum, ticketActivity.getTicketUserNum() + 1));

    }

    @Override
    public void doReward(String id) {
        VideoActivity videoActivity = (VideoActivity) redisTemplate.opsForValue().get(RedisKey.VIDEO_ACTIVITY);
        LocalDateTime nowTime = LocalDateTime.now();
        if (videoActivity == null) {
            videoActivity = videoActivityMapper.selectOne(new LambdaQueryWrapper<VideoActivity>()
                    .le(VideoActivity::getStartTime, nowTime)
                    .gt(VideoActivity::getEndTime, nowTime));
            if (videoActivity == null) {
                return;
//                throw new YunKeException(ExceptionEnum.FAIL, "无进行中的视频奖励积分活动-请刷新页面重试");
            }
            Duration duration = Duration.between(nowTime, videoActivity.getEndTime());
            redisTemplate.opsForValue().set(RedisKey.VIDEO_ACTIVITY, videoActivity, duration.getSeconds(), TimeUnit.SECONDS);
        }

        Video video = baseMapper.selectById(id);
        if (video == null || Boolean.FALSE.equals(video.getRewardFlag())
                || video.getRewardAmount().compareTo(BigDecimal.ZERO) < 1) {
            return;
//            throw new YunKeException(ExceptionEnum.FAIL, "当前视频不存在或不支持赠送积分-请刷新页面重试");
        }
        String userId = StpUtil.getLoginIdAsString();
        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        UserWalletRecord walletRecord = new UserWalletRecord();
        walletRecord.setEventId(id);
        walletRecord.setChangeAmount(video.getRewardAmount());
        walletRecord.setEventType(UserWalletEventEnum.VIDEO.getKey());
        if (userWallet == null) {
            // 直接进行奖励赠送即可
            userWallet = new UserWallet();
            userWallet.setUserId(userId);
            userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
            userWallet.setAmount(video.getRewardAmount());
            userWallet.setVersion(0);
            userWalletMapper.insert(userWallet);
            walletRecord.setAfterAmount(video.getRewardAmount());
        } else {
            UserWalletRecord checkData = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                    .eq(UserWalletRecord::getWalletId, userWallet.getId())
                    .eq(UserWalletRecord::getEventId, id)
                    .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                    .select(UserWalletRecord::getId));
            if (checkData != null) {
                return;
//                throw new YunKeException(ExceptionEnum.FAIL, "当前视频已获得过积分奖励");
            }

            if (videoActivity.getRewardLimit() != null) {
                Long rewardNum = userWalletRecordMapper.selectCount(new LambdaQueryWrapper<UserWalletRecord>()
                        .eq(UserWalletRecord::getWalletId, userWallet.getId())
                        .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey()));
                if (rewardNum >= videoActivity.getRewardLimit()) {
                    return;
//                    throw new YunKeException(ExceptionEnum.FAIL, "视频积分奖励次数已达上限：" + rewardNum + "次！");
                }
            }

            BigDecimal afterAmount = userWallet.getAmount().add(video.getRewardAmount());
            int updateRow = userWalletMapper.update(new LambdaUpdateWrapper<UserWallet>()
                    .eq(UserWallet::getId, userWallet.getId())
                    .eq(UserWallet::getVersion, userWallet.getVersion())
                    .set(UserWallet::getAmount, afterAmount)
                    .set(UserWallet::getVersion, userWallet.getVersion() + 1));
            walletRecord.setStatus(updateRow > 0);
            walletRecord.setAfterAmount(afterAmount);
        }
        walletRecord.setWalletId(userWallet.getId());
        userWalletRecordMapper.insert(walletRecord);
    }

    @Override
    public Video getOneById(String id) {
        Video video = baseMapper.selectById(id);
        if (video != null) {
            String userId = StpUtil.getLoginIdAsString();
            UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId)
                    .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
            if (userWallet != null) {
                UserWalletRecord checkData = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                        .eq(UserWalletRecord::getWalletId, userWallet.getId())
                        .eq(UserWalletRecord::getEventId, id)
                        .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                        .select(UserWalletRecord::getId));
                video.setRewardFlag(checkData != null);
            }
        }
        return video;
    }
}
