package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.utils.LocalDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author guozhijie
 * @since 2021-04-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SysRole对象", description = "系统角色")
public class SysRole extends PageCurrency {

    private static final long serialVersionUID = 1L;

    private String id;

    private String code;

    private String name;

    @TableField(value = "create_Time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @TableField(value = "update_Time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer status;


}
