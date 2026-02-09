package com.yunkesoftware.www.adm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.RiskWarning;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 风险提示 Mapper 接口
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
public interface RiskWarningMapper extends BaseMapper<RiskWarning> {

    Page<RiskWarning> pageByQuery(Page<RiskWarning> pageParam, @Param("param") RiskWarning riskWarning);
}
