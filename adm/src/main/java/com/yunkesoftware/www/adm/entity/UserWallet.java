package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户钱包
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user_wallet")
@Schema(name = "UserWallet对象", description = "用户钱包")
public class UserWallet extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "用户id")
    @TableField("user_id")
    @ExcelProperty("用户id")
    private String userId;

    @Schema(description = "类型")
    @TableField("type")
    @ExcelProperty("类型")
    private Integer type;

    @Schema(description = "数量")
    @TableField("amount")
    @ExcelProperty("数量")
    private BigDecimal amount;

    @Schema(description = "乐观锁版本号")
    @TableField("version")
    @ExcelProperty("乐观锁版本号")
    private Integer version;

    @Schema(description = "昵称")
    @TableField(exist = false)
    private String nickName;
}
