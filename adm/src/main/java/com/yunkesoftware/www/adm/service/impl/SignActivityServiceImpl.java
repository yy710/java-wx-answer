package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunkesoftware.www.adm.entity.SignActivity;
import com.yunkesoftware.www.adm.mapper.SignActivityMapper;
import com.yunkesoftware.www.adm.service.SignActivityService;
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
 * 签到活动 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
@Service
public class SignActivityServiceImpl extends ServiceImpl<SignActivityMapper, SignActivity> implements SignActivityService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addOrModify(SignActivity signActivity) {
        if (!signActivity.getEndTime().isAfter(signActivity.getStartTime())) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间必须晚于开始时间");
        }
        LocalDateTime nowTime = LocalDateTime.now();
        if (!signActivity.getEndTime().isAfter(nowTime)) {
            throw new YunKeException(ExceptionEnum.FAIL, "结束时间必须晚于当前时间");
        }
        // 开始和结束时间不能和其他活动时间有重叠
        SignActivity checkData = baseMapper.selectOne(new LambdaQueryWrapper<SignActivity>()
                .ne(StringUtils.hasLength(signActivity.getId()), SignActivity::getId, signActivity.getId())
                .le(SignActivity::getStartTime, signActivity.getEndTime())
                .gt(SignActivity::getEndTime, signActivity.getStartTime())
                .select(SignActivity::getId, SignActivity::getTitle));
        if (checkData != null) {
            throw new YunKeException(ExceptionEnum.FAIL, "时间和已有活动-" + checkData.getTitle() + "-重叠");
        }
        saveOrUpdate(signActivity);
        redisTemplate.delete(RedisKey.SIGN_ACTIVITY);
    }

    @Override
    public void delete(List<String> ids) {
        baseMapper.deleteByIds(ids);
        redisTemplate.delete(RedisKey.SIGN_ACTIVITY);
    }
}
