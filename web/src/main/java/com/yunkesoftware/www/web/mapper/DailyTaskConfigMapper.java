package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.DailyTaskConfig;

public interface DailyTaskConfigMapper extends BaseMapper<DailyTaskConfig> {
    DailyTaskConfig selectSingletonForUpdate();
}
