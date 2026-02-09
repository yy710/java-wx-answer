package com.yunkesoftware.www.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.web.entity.Area;
import com.yunkesoftware.www.web.mapper.AreaMapper;
import com.yunkesoftware.www.web.service.AreaService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public List<Area> listByPid(String pid) {
        return baseMapper.selectList(new LambdaQueryWrapper<Area>()
                .eq(Area::getParentId, pid));
    }

    @Override
    public List<Area> listTree() {
        List<Area> areaList = (List<Area>) redisTemplate.opsForValue().get("areaTree:");
        if (areaList != null && areaList.size() > 0) {
            return areaList;
        }
        List<Area> resultList = baseMapper.selectList(null);
        List<Area> result = resultList.stream().filter(area -> area.getLevel() == 1)
                .peek(area -> area.setChildren(getChildren(area, resultList)))
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set("areaTree:", result, 7, TimeUnit.DAYS);
        return result;
    }



    private List<Area> getChildren(Area area, List<Area> resultList) {
        return resultList.stream()
                .filter(childArea -> childArea.getParentId().equals(area.getAreaId()))
                .peek(childArea -> childArea.setChildren(getChildren(childArea, resultList)))
                .collect(Collectors.toList());
    }

}
