package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@TableName("daily_task_action_token")
public class DailyTaskActionToken {
    @TableId("id")
    private String id;
    @TableField("token_hash")
    private String tokenHash;
    @TableField("user_id")
    private String userId;
    @TableField("action_type")
    private String actionType;
    @TableField("asset_type")
    private String assetType;
    @TableField("asset_id")
    private String assetId;
    @TableField("expires_at")
    private LocalDateTime expiresAt;
    @TableField("used_at")
    private LocalDateTime usedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
