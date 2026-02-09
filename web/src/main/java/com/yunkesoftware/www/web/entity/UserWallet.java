package com.yunkesoftware.www.web.entity;

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
public class UserWallet {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "类型")
    @TableField("type")
    private Integer type;

    @Schema(description = "数量")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "乐观锁版本号")
    @TableField("version")
    private Integer version;
}
