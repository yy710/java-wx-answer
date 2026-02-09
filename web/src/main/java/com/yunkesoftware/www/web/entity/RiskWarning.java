package com.yunkesoftware.www.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 风险提示
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("risk_warning")
@Schema(name = "RiskWarning对象", description = "风险提示")
public class RiskWarning extends PageCurrency {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "获得奖励人数")
    @TableField("reward_num")
    private Integer rewardNum;

    @Schema(description = "是否开启")
    @TableField("status")
    private Boolean status;

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "阅读人数")
    @TableField("view_num")
    private Integer viewNum;

    @Schema(description = "分类id")
    @TableField("category_id")
    private String categoryId;

    @Schema(description = "介绍信息")
    @TableField("descr")
    private String descr;

    @Schema(description = "阅读时长最低几秒")
    @TableField("time_min")
    private Integer timeMin;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "是否奖励")
    @TableField(exist = false)
    private boolean rewardFlag;

    @Schema(description = "是否已读")
    @TableField(exist = false)
    private boolean readFlag;
}
