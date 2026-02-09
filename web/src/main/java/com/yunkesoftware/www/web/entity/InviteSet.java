package com.yunkesoftware.www.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 拉新奖励设置
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("invite_set")
@Schema(name = "InviteSet对象", description = "拉新奖励设置")
public class InviteSet {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "奖励限制次数")
    @TableField("reward_limit")
    private Integer rewardLimit;
}
