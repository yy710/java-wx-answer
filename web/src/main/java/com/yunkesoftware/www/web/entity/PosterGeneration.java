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
@TableName("poster_generation")
public class PosterGeneration {
    @TableId("id") private String id;
    @TableField("user_id") private String userId;
    @TableField("template_id") private String templateId;
    @TableField("template_version") private Integer templateVersion;
    @TableField("input_sha256") private String inputSha256;
    @TableField("result_url") private String resultUrl;
    @TableField("status") private String status;
    @TableField("expires_at") private LocalDateTime expiresAt;
    @TableField("error_code") private String errorCode;
    @TableField("created_at") private LocalDateTime createdAt;
}
