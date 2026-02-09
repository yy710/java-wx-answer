package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 风险阅读记录
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("risk_warning_record")
@Schema(name = "RiskWarningRecord对象", description = "风险阅读记录")
public class RiskWarningRecord extends PageCurrency {

    @Schema(description = "风险阅读id")
    @TableField("risk_warn_id")
    @ExcelProperty("风险阅读id")
    private String riskWarnId;

    @Schema(description = "用户id")
    @TableField("user_id")
    @ExcelProperty("用户id")
    private String userId;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "昵称")
    @TableField(exist = false)
    private String nickName;
}
