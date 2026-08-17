package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.DailyTaskClaim;
import org.apache.ibatis.annotations.Param;

public interface DailyTaskClaimMapper extends BaseMapper<DailyTaskClaim> {
    DailyTaskClaim selectByUserDateType(@Param("userId") String userId, @Param("taskDate") String taskDate, @Param("taskType") String taskType);
}
