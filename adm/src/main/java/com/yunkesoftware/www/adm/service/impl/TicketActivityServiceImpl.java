package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.adm.entity.TicketActivity;
import com.yunkesoftware.www.adm.mapper.TicketActivityMapper;
import com.yunkesoftware.www.adm.service.TicketActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
 * 投票活动信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Service
public class TicketActivityServiceImpl extends ServiceImpl<TicketActivityMapper, TicketActivity> implements TicketActivityService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addOrModify(TicketActivity ticketActivity) {
        if (ticketActivity.getEndTime().isBefore(ticketActivity.getStartTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间不能早于开始时间");
        }
        if (ticketActivity.getStartTicketTime().isAfter(ticketActivity.getEndTicketTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票开始时间不能晚于投票结束时间");
        }
        LocalDateTime nowTime = LocalDateTime.now();
        if (!ticketActivity.getEndTime().isAfter(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间不能早于当前时间");
        }
        if (ticketActivity.getStartTicketTime().isBefore(ticketActivity.getStartTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票开始时间不能早于开始时间");
        }
        if (ticketActivity.getEndTicketTime().isAfter(ticketActivity.getEndTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "投票结束时间不能晚于结束时间");
        }
        // 开始时间-结束时间不能和其他数据有重叠
        TicketActivity checkData = baseMapper.selectOne(new LambdaQueryWrapper<TicketActivity>()
                .le(TicketActivity::getStartTime, ticketActivity.getEndTime())
                .gt(TicketActivity::getEndTime, ticketActivity.getStartTime())
                .ne(StringUtils.hasLength(ticketActivity.getId()), TicketActivity::getId, ticketActivity.getId())
                .select(TicketActivity::getId, TicketActivity::getTitle)
                .last("LIMIT 1"));
        if (checkData != null) {
            throw new YunKeException(ExceptionEnum.FAIL, "时间与(" + checkData.getTitle() + ")时间重叠");
        }
        if (StringUtils.hasLength(ticketActivity.getId())) {
            ticketActivity.setTicketUserNum(null);
            ticketActivity.setTicketTotal(null);
            ticketActivity.setViewNum(null);
            baseMapper.updateById(ticketActivity);
            redisTemplate.delete(RedisKey.TICKET_ACTIVITY);
        } else {
            ticketActivity.setTicketUserNum(0);
            ticketActivity.setTicketTotal(0);
            ticketActivity.setViewNum(0);
            baseMapper.insert(ticketActivity);
        }
    }

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        redisTemplate.delete(RedisKey.TICKET_ACTIVITY);
    }
}
