package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunkesoftware.www.query.PageCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 后台用户表
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
@Schema(name = "adm对象", description = "后台用户表")
public class SysUser extends PageCurrency {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    @Schema(description = "账户")
    @TableField(value = "username")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "头像")
    private String icon;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "备注")
    private String note;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "手机号")
    @TableField(exist = false)
    private String phone;


}
