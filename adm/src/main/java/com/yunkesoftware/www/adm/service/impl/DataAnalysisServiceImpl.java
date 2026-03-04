package com.yunkesoftware.www.adm.service.impl;

import com.yunkesoftware.www.adm.mapper.TicketRecordMapper;
import com.yunkesoftware.www.adm.mapper.UserMapper;
import com.yunkesoftware.www.adm.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.adm.service.DataAnalysisService;
import com.yunkesoftware.www.adm.vo.DataAnalysisVo;
import com.yunkesoftware.www.constant.RedisKey;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TicketRecordMapper ticketRecordMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;

    @Override
    public DataAnalysisVo indexData() {
        // 缓存10分钟
        DataAnalysisVo analysisVo = (DataAnalysisVo) redisTemplate.opsForValue().get(RedisKey.INDEX_ANALYSIS);
        if (analysisVo != null) {
            return analysisVo;
        }

        analysisVo = new DataAnalysisVo();
        LocalDate nowDate = LocalDate.now();
        Long totalUserNum = userMapper.selectCount(null);
        analysisVo.setTotalUserNum(totalUserNum);
        Long todayUserNum = userMapper.countByDate(nowDate);
        analysisVo.setTodayUserNum(todayUserNum);
        // 总投票数
        Long totalTicketNum = ticketRecordMapper.selectCount(null);
        analysisVo.setTotalTicketNum(totalTicketNum);
        // 今日投票数
        Long todayTicketNum = ticketRecordMapper.countByDate(nowDate);
        analysisVo.setTodayTicketNum(todayTicketNum);

        // 今日获得积分的用户数
        Long todayIntegralUserNum = userWalletRecordMapper.countUserNum(nowDate);
        analysisVo.setTodayIntegralUserNum(todayIntegralUserNum);
        // 今日获得积分总数
        BigDecimal todayIntegralAmount = userWalletRecordMapper.countIntegralAmount(nowDate);
        analysisVo.setTodayIntegralAmount(todayIntegralAmount == null ? BigDecimal.ZERO : todayIntegralAmount);

        // 总获得积分的用户数
        Long totalIntegralUserNum = userWalletRecordMapper.countUserNum(null);
        analysisVo.setTotalIntegralUserNum(totalIntegralUserNum);
        // 积分总数
        BigDecimal totalIntegralAmount = userWalletRecordMapper.countIntegralAmount(null);
        analysisVo.setTotalIntegralAmount(totalIntegralAmount == null ? BigDecimal.ZERO : totalIntegralAmount);
        redisTemplate.opsForValue().set(RedisKey.INDEX_ANALYSIS, analysisVo, 10, TimeUnit.MINUTES);
        return analysisVo;
    }
}
