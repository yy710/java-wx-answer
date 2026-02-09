package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.RiskWarningRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 风险阅读记录 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
public interface RiskWarningRecordMapper extends BaseMapper<RiskWarningRecord> {

    Page<RiskWarningRecord> pageByQuery(Page<RiskWarningRecord> pageParam, @Param("param") RiskWarningRecord riskWarningRecord);
}
