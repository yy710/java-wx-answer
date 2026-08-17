package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.DailyTaskVideoSession;
import org.apache.ibatis.annotations.Param;

public interface DailyTaskVideoSessionMapper extends BaseMapper<DailyTaskVideoSession> {
    DailyTaskVideoSession selectByUserDateForUpdate(@Param("userId") String userId, @Param("taskDate") String taskDate);
}
