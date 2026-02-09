package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class RiskWarningRecord {

    @Schema(description = "风险阅读id")
    @TableId("risk_warn_id")
    private String riskWarnId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;
}
