package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.DailyTaskQuizQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DailyTaskQuizQuestionMapper extends BaseMapper<DailyTaskQuizQuestion> {
    List<DailyTaskQuizQuestion> listBySession(@Param("sessionId") String sessionId);
}
