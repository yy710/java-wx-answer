package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.DailyTaskQuizSession;
import org.apache.ibatis.annotations.Param;

public interface DailyTaskQuizSessionMapper extends BaseMapper<DailyTaskQuizSession> {
    DailyTaskQuizSession selectByUserDateForUpdate(@Param("userId") String userId, @Param("taskDate") String taskDate);
}
