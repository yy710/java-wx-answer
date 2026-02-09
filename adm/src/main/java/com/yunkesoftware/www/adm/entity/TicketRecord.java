package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 投票记录
 * </p>
 *
 * @author yk
 * @since 2026-01-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("ticket_record")
@Schema(name = "TicketRecord对象", description = "投票记录")
public class TicketRecord extends PageCurrency {


    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "用户id")
    @TableField("user_id")
    @ExcelProperty("用户id")
    private String userId;

    @Schema(description = "投票活动id")
    @TableField("ticket_activity_id")
    @ExcelProperty("投票活动id")
    private String ticketActivityId;

    @Schema(description = "活动下的视频id")
    @TableField("ticket_video_id")
    @ExcelProperty("活动下的视频id")
    private String ticketVideoId;

    @Schema(description = "ip地址")
    @TableField("ip_addr")
    @ExcelProperty("ip地址")
    private String ipAddr;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "昵称")
    @TableField(exist = false)
    private String nickName;
}
