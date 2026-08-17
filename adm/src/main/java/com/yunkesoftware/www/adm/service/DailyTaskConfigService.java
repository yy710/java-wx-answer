package com.yunkesoftware.www.adm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunkesoftware.www.adm.entity.DailyTaskConfig;

public interface DailyTaskConfigService extends IService<DailyTaskConfig> {
    DailyTaskConfig updateWithVersion(DailyTaskConfig incoming, String operatorId);
}
