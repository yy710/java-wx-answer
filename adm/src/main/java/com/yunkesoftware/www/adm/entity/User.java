package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.utils.LocalDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author cuiyq
 * @since 2025-02-13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("yk_user")
@Schema(name = "User对象", description = "用户表")
public class User extends PageCurrency {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "用户昵称")
    @TableField("nick_name")
    @ExcelProperty("用户昵称")
    private String nickName;

    @Schema(description = "手机号码")
    @TableField("mobile")
    @ExcelProperty("手机号码")
    private String mobile;


    @Schema(description = "头像图片路径")
    @TableField("pic")
    @ExcelProperty("头像图片路径")
    private String pic;

    @Schema(description = "微信openid")
    @TableField("open_id")
    @ExcelProperty("微信openid")
    private String openId;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @ExcelProperty("修改时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除标识 0-未删除 1-已删除")
    @TableField("delete_flag")
    private Byte deleteFlag;

}
