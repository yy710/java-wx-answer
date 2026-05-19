package com.yunkesoftware.www.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.constant.RedisKey;
import com.yunkesoftware.www.entity.IntroduceSet;
import com.yunkesoftware.www.web.entity.Introduce;
import com.yunkesoftware.www.web.mapper.IntroduceMapper;
import com.yunkesoftware.www.web.service.IntroduceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 介绍信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-03-11
 */
@Service
public class IntroduceServiceImpl extends ServiceImpl<IntroduceMapper, Introduce> implements IntroduceService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Page<Introduce> pageByQuery(Introduce introduce) {
        Page<Introduce> pageParam = new Page<>(introduce.getPageNum(), introduce.getPageSize());
        IntroduceSet introduceSet = (IntroduceSet) redisTemplate.opsForValue().get(RedisKey.INTRODUCE_SET);
        if (introduceSet == null
                || introduceSet.getStartTime().isAfter(LocalDateTime.now())
                || introduceSet.getEndTime().isBefore(LocalDateTime.now())) {
            return pageParam;
        }
        LambdaQueryWrapper<Introduce> queryWrapper = new LambdaQueryWrapper<Introduce>()
                .orderByAsc(Introduce::getSeq)
                .eq(Introduce::getStatus, true)
                .select(Introduce::getId, Introduce::getTitle, Introduce::getCreateTime, Introduce::getAuthor);
        return baseMapper.selectPage(pageParam, queryWrapper);
    }
}
