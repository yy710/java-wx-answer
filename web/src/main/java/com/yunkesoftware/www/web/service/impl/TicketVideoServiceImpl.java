package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.utils.IpUtils;
import com.yunkesoftware.www.web.entity.*;
import com.yunkesoftware.www.web.mapper.*;
import com.yunkesoftware.www.web.service.TicketVideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 视频信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Service
public class TicketVideoServiceImpl extends ServiceImpl<TicketVideoMapper, TicketVideo> implements TicketVideoService {
    @Resource
    private TicketRecordMapper ticketRecordMapper;
    @Resource
    private TicketActivityMapper ticketActivityMapper;
    @Resource
    private RewardSetMapper rewardSetMapper;
    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;

    @Override
    public Page<TicketVideo> pageByQuery(TicketVideo ticketVideo) {
        Page<TicketVideo> pageParam = new Page<>(ticketVideo.getPageNum(), ticketVideo.getPageSize());
        Page<TicketVideo> pageResult = baseMapper.selectPage(pageParam, new LambdaQueryWrapper<TicketVideo>()
                .eq(TicketVideo::getActivityId, ticketVideo.getActivityId())
                .eq(TicketVideo::getStatus, true)
                .orderByAsc(TicketVideo::getSeq));
        if (StpUtil.isLogin()) {
            String userId = StpUtil.getLoginIdAsString();
            UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId)
                    .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
            if (userWallet != null) {
                for (TicketVideo record : pageResult.getRecords()) {
                    UserWalletRecord checkData = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                            .eq(UserWalletRecord::getWalletId, userWallet.getId())
                            .eq(UserWalletRecord::getEventId, record.getId())
                            .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                            .select(UserWalletRecord::getId));
                    record.setRewardFlag(checkData != null);
                }
            }

        }
        return pageResult;
    }

    @Override
    public void doVote(String id) {
        // 每个活动每个用户有固定的票数
        TicketVideo ticketVideo = baseMapper.selectOne(new LambdaQueryWrapper<TicketVideo>()
                .eq(TicketVideo::getId, id)
                .select(TicketVideo::getId, TicketVideo::getStatus, TicketVideo::getActivityId, TicketVideo::getTicketTotal));
        if (ticketVideo == null || !ticketVideo.getStatus()) {
            throw new YunKeException(ExceptionEnum.FAIL, "视频不存在或已下架-请刷新页面^_^");
        }
        TicketActivity ticketActivity = ticketActivityMapper.selectById(ticketVideo.getActivityId());
        if (ticketActivity == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "活动不存在或已下架-请刷新页面^_^");
        }
        LocalDateTime nowTime = LocalDateTime.now();
        if (ticketActivity.getStartTicketTime().isAfter(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票未开始,最早投票时间：" + ticketActivity.getStartTicketTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (ticketActivity.getEndTime().isBefore(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "活动已结束-请刷新页面^_^");
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

        TicketRecord checkData = ticketRecordMapper.selectOne(new LambdaQueryWrapper<TicketRecord>()
                .eq(TicketRecord::getUserId, userId)
                .eq(TicketRecord::getTicketActivityId, ticketActivity.getId())
                .last("LIMIT 1")
                .select(TicketRecord::getId));


        // 生成投票记录(修改视频+活动投票数)
        TicketRecord ticketRecord = new TicketRecord();
        ticketRecord.setUserId(userId);
        ticketRecord.setTicketActivityId(ticketActivity.getId());
        ticketRecord.setTicketVideoId(ticketVideo.getId());
        ticketRecord.setIpAddr(IpUtils.getIpAddr());
        ticketRecordMapper.insert(ticketRecord);


        baseMapper.update(new LambdaUpdateWrapper<TicketVideo>()
                .eq(TicketVideo::getId, id)
                .set(TicketVideo::getTicketTotal, ticketVideo.getTicketTotal() + 1));

        ticketActivityMapper.update(new LambdaUpdateWrapper<TicketActivity>()
                .eq(TicketActivity::getId, ticketActivity.getId())
                .set(TicketActivity::getTicketTotal, ticketActivity.getTicketTotal() + 1)
                .set(checkData == null, TicketActivity::getTicketUserNum, ticketActivity.getTicketUserNum() + 1));

    }

    @Override
    public void doReward(String id) {
        TicketVideo ticketVideo = baseMapper.selectById(id);
        if (ticketVideo == null || ticketVideo.getRewardAmount().compareTo(BigDecimal.ZERO) < 1) {
            return;
        }
        String userId = StpUtil.getLoginIdAsString();
        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        UserWalletRecord walletRecord = new UserWalletRecord();
        walletRecord.setEventId(id);
        walletRecord.setChangeAmount(ticketVideo.getRewardAmount());
        walletRecord.setEventType(UserWalletEventEnum.VIDEO.getKey());
        if (userWallet == null) {
            // 直接进行奖励赠送即可
            userWallet = new UserWallet();
            userWallet.setUserId(userId);
            userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
            userWallet.setAmount(ticketVideo.getRewardAmount());
            userWallet.setVersion(0);
            userWalletMapper.insert(userWallet);
            walletRecord.setAfterAmount(ticketVideo.getRewardAmount());
        } else {
            UserWalletRecord checkData = userWalletRecordMapper.selectOne(new LambdaQueryWrapper<UserWalletRecord>()
                    .eq(UserWalletRecord::getWalletId, userWallet.getId())
                    .eq(UserWalletRecord::getEventId, id)
                    .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey())
                    .select(UserWalletRecord::getId));
            if (checkData != null) {
                return;
            }
            RewardSet rewardSet = rewardSetMapper.selectOne(new LambdaQueryWrapper<RewardSet>()
                    .eq(RewardSet::getType, 2)
                    .last("LIMIT 1"));
            if (rewardSet != null) {
                Long rewardNum = userWalletRecordMapper.selectCount(new LambdaQueryWrapper<UserWalletRecord>()
                        .eq(UserWalletRecord::getWalletId, userWallet.getId())
                        .eq(UserWalletRecord::getEventType, UserWalletEventEnum.VIDEO.getKey()));
                if (rewardNum >= rewardSet.getRewardLimit()) {
                    return;
                }
            }

            BigDecimal afterAmount = userWallet.getAmount().add(ticketVideo.getRewardAmount());
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
    public TicketVideo getOneById(String id) {
        TicketVideo ticketVideo = baseMapper.selectById(id);
        if (ticketVideo != null) {
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
                ticketVideo.setRewardFlag(checkData != null);
            }
        }
        return ticketVideo;
    }
}
