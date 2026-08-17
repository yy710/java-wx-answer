package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.DailyTaskActionToken;
import org.apache.ibatis.annotations.Param;

public interface DailyTaskActionTokenMapper extends BaseMapper<DailyTaskActionToken> {
    DailyTaskActionToken selectForUpdate(@Param("tokenHash") String tokenHash);
}
