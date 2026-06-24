package com.yunkesoftware.www.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.web.entity.TicketActivity;
import com.yunkesoftware.www.web.entity.TicketRecord;
import com.yunkesoftware.www.web.mapper.TicketActivityMapper;
import com.yunkesoftware.www.web.mapper.TicketRecordMapper;
import com.yunkesoftware.www.web.service.TicketActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 投票活动信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Service
public class TicketActivityServiceImpl extends ServiceImpl<TicketActivityMapper, TicketActivity> implements TicketActivityService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TicketRecordMapper ticketRecordMapper;

    private TicketActivity getOpenTicketActivity() {
        TicketActivity ticketActivity = (TicketActivity) redisTemplate.opsForValue().get(RedisKey.TICKET_ACTIVITY);
        if (ticketActivity == null) {
            LocalDateTime nowTime = LocalDateTime.now();
            ticketActivity = baseMapper.selectOne(new LambdaQueryWrapper<TicketActivity>()
                    .le(TicketActivity::getStartTime, nowTime)
                    .gt(TicketActivity::getEndTime, nowTime)
                    .select(TicketActivity::getId, TicketActivity::getStartTime, TicketActivity::getEndTime,
                            TicketActivity::getStartTicketTime, TicketActivity::getEndTicketTime,
                            TicketActivity::getTicketLimit, TicketActivity::getTicketUserNum,
                            TicketActivity::getTicketTotal, TicketActivity::getViewNum, TicketActivity::getTicketUserMultiple,
                            TicketActivity::getTicketMultiple, TicketActivity::getViewMultiple, TicketActivity::getDescr));

            if (ticketActivity != null) {
                long seconds = Math.max(Duration.between(nowTime, ticketActivity.getEndTime()).getSeconds(), 1);
                redisTemplate.opsForValue().set(RedisKey.TICKET_ACTIVITY, ticketActivity, seconds, TimeUnit.SECONDS);
            }
        }
        return ticketActivity;
    }

    private int multiply(Integer value, Integer multiple) {
        return (value == null ? 0 : value) * (multiple == null ? 1 : multiple);
    }

    private void fillFinishedTicketActivity(TicketActivity ticketActivity) {
        if (ticketActivity.getEndTicketTime() == null || !LocalDateTime.now().isAfter(ticketActivity.getEndTicketTime())) {
            return;
        }
        Long myTicketNum = ticketRecordMapper.selectCount(new LambdaQueryWrapper<TicketRecord>()
                .eq(TicketRecord::getTicketActivityId, ticketActivity.getId())
                .eq(TicketRecord::getUserId, StpUtil.getLoginIdAsString()));
        ticketActivity.setMyTicketNum(myTicketNum);
        ticketActivity.setTicketTotal(multiply(ticketActivity.getTicketTotal(), ticketActivity.getTicketMultiple()));
        ticketActivity.setTicketUserNum(multiply(ticketActivity.getTicketUserNum(), ticketActivity.getTicketUserMultiple()));
        ticketActivity.setViewNum(multiply(ticketActivity.getViewNum(), ticketActivity.getViewMultiple()));
        ticketActivity.setFinishFlag(true);
    }

    @Override
    public TicketActivity getOpen() {
        TicketActivity ticketActivity = getOpenTicketActivity();
        if (ticketActivity != null) {
            fillFinishedTicketActivity(ticketActivity);
        }
        return ticketActivity;
    }

    @Override
    public Integer surplusTicket() {
        TicketActivity ticketActivity = getOpenTicketActivity();
        if (ticketActivity != null) {
            Long myTicketNum = ticketRecordMapper.selectCount(new LambdaQueryWrapper<TicketRecord>()
                    .eq(TicketRecord::getTicketActivityId, ticketActivity.getId())
                    .like(TicketRecord::getCreateTime, LocalDate.now())
                    .eq(TicketRecord::getUserId, StpUtil.getLoginIdAsString()));
            return ticketActivity.getTicketLimit() - myTicketNum.intValue();
        }
        return 0;
    }
}
