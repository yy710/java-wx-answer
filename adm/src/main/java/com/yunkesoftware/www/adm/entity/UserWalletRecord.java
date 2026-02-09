package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户钱包记录
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user_wallet_record")
@Schema(name = "UserWalletRecord对象", description = "用户钱包记录")
public class UserWalletRecord extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;


    @Schema(description = "昵称")
    @TableField(exist = false)
    @ExcelProperty("用户昵称")
    private String nickName;

    @Schema(description = "钱包id")
    @TableField("wallet_id")
    @ExcelIgnore
    private String walletId;

    @Schema(description = "原始金额")
    @TableField(exist = false)
    private BigDecimal beforeAmount;

    @Schema(description = "变动数")
    @TableField("change_amount")
    @ExcelProperty("变动数")
    private BigDecimal changeAmount;

    @Schema(description = "剩余数")
    @TableField("after_amount")
    @ExcelProperty("剩余数")
    private BigDecimal afterAmount;


    @Schema(description = "事件名称")
    @TableField(exist = false)
    @ExcelProperty("事件名称")
    private String eventTypeName;

    @Schema(description = "事件id")
    @TableField("event_id")
    @ExcelIgnore
    private String eventId;

    @Schema(description = "事件类型")
    @TableField("event_type")
    @ExcelIgnore
    private Integer eventType;

    @Schema(description = "是否成功")
    @TableField("status")
    @ExcelIgnore
    private Boolean status;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

}
