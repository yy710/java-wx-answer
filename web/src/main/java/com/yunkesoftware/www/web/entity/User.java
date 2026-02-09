package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * <p>
 * 客户信息
 * </p>
 *
 * @author cuiyq
 * @since 2024-10-24
 */
@Getter
@Setter
@TableName("yk_user")
@Schema(name = "User对象", description = "客户信息")
public class User {
    @Schema(description = "用户id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "手机号")
    @TableField("mobile")
    private String mobile;

    @Schema(description = "wx用户唯一标识")
    @TableField("open_id")
    private String openId;

    @Schema(description = "昵称")
    @TableField("nick_name")
    private String nickName;

    @Schema(description = "头像")
    @TableField("pic")
    private String pic;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "邀请人id")
    @TableField("parent_id")
    private String parentId;
}
