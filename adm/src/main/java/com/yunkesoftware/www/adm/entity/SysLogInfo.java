package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 系统日志
 * </p>
 *
 * @author guozhijie
 * @since 2021-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SysLogInfo对象", description = "系统日志")
public class SysLogInfo extends PageCurrency {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description ="用户id")
    private String userId;

    @Schema(description ="用户名")
    private String username;

    @Schema(description ="用户操作")
    private String operation;

    @Schema(description ="请求方法")
    private String method;

    @Schema(description ="请求参数")
    private String params;

    @Schema(description ="执行时长(毫秒)")
    private Long time;

    @Schema(description ="IP地址")
    private String ip;

    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS, timezone = "GMT+8")
    @Schema(description ="创建时间")
    private LocalDateTime createTime;

    @Schema(description ="日志类型")
    private String type;

}
