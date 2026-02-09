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
 * 答题活动
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_activity")
@Schema(name = "TopicActivity对象", description = "答题活动")
public class TopicActivity implements Serializable {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "开始时间")
    @TableField("start_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime endTime;

    @Schema(description = "浏览次数")
    @TableField("view_num")
    private Integer viewNum;

    @Schema(description = "参与人数")
    @TableField("user_num")
    private Integer userNum;

    @Schema(description = "每日答题线路限制")
    @TableField("limit_num")
    private Integer limitNum;
}
