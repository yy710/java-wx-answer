package com.yunkesoftware.www.adm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.adm.entity.SignActivity;
import com.yunkesoftware.www.adm.entity.TicketActivity;
import com.yunkesoftware.www.adm.entity.TopicActivity;
import com.yunkesoftware.www.adm.entity.UserWallet;
import com.yunkesoftware.www.adm.entity.UserWalletRecord;
import com.yunkesoftware.www.adm.entity.VideoActivity;
import com.yunkesoftware.www.adm.mapper.SignActivityMapper;
import com.yunkesoftware.www.adm.mapper.TicketActivityMapper;
import com.yunkesoftware.www.adm.mapper.TopicActivityMapper;
import com.yunkesoftware.www.adm.mapper.UserWalletMapper;
import com.yunkesoftware.www.adm.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.adm.mapper.VideoActivityMapper;
import com.yunkesoftware.www.adm.vo.ViewNumVo;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.result.CommonResult;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.utils.ViewNumUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/viewNumSet")
public class ViewNumSetController {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private SignActivityMapper signActivityMapper;
    @Resource
    private TicketActivityMapper ticketActivityMapper;
    @Resource
    private TopicActivityMapper topicActivityMapper;
    @Resource
    private VideoActivityMapper videoActivityMapper;
    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;

    @PostMapping("/page")
    public CommonResult<Page<ViewNumVo>> page() {
        Page<ViewNumVo> pageResult = new Page<>();
        List<ViewNumVo> recordList = new ArrayList<>();
        ViewNumVo viewNumVo = new ViewNumVo();
        Integer realViewNum = ViewNumUtils.getRealViewNum(redisTemplate);
        Integer viewMultiple = ViewNumUtils.getViewMultiple(redisTemplate);
        Integer randomMax = ViewNumUtils.getRandomMax(redisTemplate);
        Integer displayViewNum = ViewNumUtils.calculateDisplayViewNum(realViewNum, viewMultiple, randomMax);
        viewNumVo.setRealViewNum(realViewNum);
        viewNumVo.setViewNum(displayViewNum);
        viewNumVo.setDisplayViewNum(displayViewNum);
        viewNumVo.setViewMultiple(viewMultiple);
        viewNumVo.setRandomMax(randomMax);
        recordList.add(viewNumVo);
        pageResult.setRecords(recordList);
        pageResult.setTotal(1);
        return CommonResult.success(pageResult);
    }

    @PostMapping("/addOrModify")
    public CommonResult<Object> addOrModify(@Valid @RequestBody ViewNumVo viewNumVo) {
        ViewNumUtils.saveDisplayConfig(redisTemplate, viewNumVo.getViewMultiple(), viewNumVo.getRandomMax());
        return CommonResult.success();
    }

    @PostMapping("/setRewardActivityTime")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Map<String, Integer>> setRewardActivityTime(@RequestBody RewardActivityTimeRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "请选择开始时间和结束时间");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间必须晚于开始时间");
        }

        LocalDateTime nowTime = LocalDateTime.now();
        int signCount = signActivityMapper.update(null, new LambdaUpdateWrapper<SignActivity>()
                .set(SignActivity::getStartTime, request.getStartTime())
                .set(SignActivity::getEndTime, request.getEndTime())
                .set(SignActivity::getUpdateTime, nowTime));
        int ticketCount = ticketActivityMapper.update(null, new LambdaUpdateWrapper<TicketActivity>()
                .set(TicketActivity::getStartTime, request.getStartTime())
                .set(TicketActivity::getEndTime, request.getEndTime())
                .set(TicketActivity::getStartTicketTime, request.getStartTime())
                .set(TicketActivity::getEndTicketTime, request.getEndTime())
                .set(TicketActivity::getUpdateTime, nowTime));
        int topicCount = topicActivityMapper.update(null, new LambdaUpdateWrapper<TopicActivity>()
                .set(TopicActivity::getStartTime, request.getStartTime())
                .set(TopicActivity::getEndTime, request.getEndTime())
                .set(TopicActivity::getUpdateTime, nowTime));
        int videoCount = videoActivityMapper.update(null, new LambdaUpdateWrapper<VideoActivity>()
                .set(VideoActivity::getStartTime, request.getStartTime())
                .set(VideoActivity::getEndTime, request.getEndTime())
                .set(VideoActivity::getUpdateTime, nowTime));

        redisTemplate.delete(Arrays.asList(
                RedisKey.SIGN_ACTIVITY,
                RedisKey.TICKET_ACTIVITY,
                RedisKey.TOPIC_ACTIVITY,
                RedisKey.VIDEO_ACTIVITY
        ));

        Map<String, Integer> result = new HashMap<>();
        result.put("signActivity", signCount);
        result.put("ticketActivity", ticketCount);
        result.put("topicActivity", topicCount);
        result.put("videoActivity", videoCount);
        return CommonResult.success(result);
    }

    @PostMapping("/resetUserIntegral")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Map<String, Integer>> resetUserIntegral() {
        int recordCount = userWalletRecordMapper.delete(new QueryWrapper<UserWalletRecord>()
                .inSql("wallet_id", "SELECT id FROM user_wallet WHERE type = " + UserWalletTypeEnum.INTEGRAL.getKey()));
        int walletCount = userWalletMapper.update(null, new LambdaUpdateWrapper<UserWallet>()
                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey())
                .set(UserWallet::getAmount, BigDecimal.ZERO)
                .setSql("version = IFNULL(version, 0) + 1"));

        Map<String, Integer> result = new HashMap<>();
        result.put("wallet", walletCount);
        result.put("walletRecord", recordCount);
        return CommonResult.success(result);
    }

    public static class RewardActivityTimeRequest {
        @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
        private LocalDateTime startTime;
        @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
        private LocalDateTime endTime;

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
    }
}
