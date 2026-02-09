package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 奖励设置
 * </p>
 *
 * @author yk
 * @since 2026-01-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("reward_set")
@Schema(name = "RewardSet对象", description = "奖励设置")
public class RewardSet {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "1-风险阅读 2-视频学习")
    @TableField("type")
    private Integer type;

    @Schema(description = "奖励次数")
    @TableField("reward_limit")
    private Integer rewardLimit;

    @Schema(description = "是否首次奖励")
    @TableField("first_flag")
    private Boolean firstFlag;

}
