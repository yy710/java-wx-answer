package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.entity.Area;
import com.yunkesoftware.www.adm.mapper.AreaMapper;
import com.yunkesoftware.www.adm.service.AreaService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 地区管理 服务实现类
 * </p>
 *
 * @author g
 * @since 2023-12-11
 */
@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {


    @Resource
    private  RedisTemplate<String, List<Area>> redisTemplate;
    @Override
    public List<Area> getAreaTree() {
        List<Area> areaList = redisTemplate.opsForValue().get("areaTree:");
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
