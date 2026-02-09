package com.yunkesoftware.www.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 投票活动信息
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("ticket_activity")
@Schema(name = "TicketActivity对象", description = "投票活动信息")
public class TicketActivity implements Serializable {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "投票结束时间")
    @TableField("end_ticket_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime endTicketTime;

    @Schema(description = "投票开始时间")
    @TableField("start_ticket_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime startTicketTime;

    @Schema(description = "每个用户每天多少票")
    @TableField("ticket_limit")
    private Integer ticketLimit;

    @Schema(description = "投票人数")
    @TableField("ticket_user_num")
    private Integer ticketUserNum;

    @Schema(description = "总票数")
    @TableField("ticket_total")
    private Integer ticketTotal;

    @Schema(description = "浏览量")
    @TableField("view_num")
    private Integer viewNum;

    @Schema(description = "投票人数显示倍数")
    @TableField("ticket_user_multiple")
    private Integer ticketUserMultiple;

    @Schema(description = "总票数显示倍数")
    @TableField("ticket_multiple")
    private Integer ticketMultiple;

    @Schema(description = "浏览量显示倍数")
    @TableField("view_multiple")
    private Integer viewMultiple;

    @Schema(description = "规则说明")
    @TableField("descr")
    private String descr;


    @Schema(description = "我的票数")
    @TableField(exist = false)
    private long myTicketNum;

    @Schema(description = "是否结束")
    @TableField(exist = false)
    private boolean finishFlag;
}
