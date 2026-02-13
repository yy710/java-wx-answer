package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 趣味答题记录
 * </p>
 *
 * @author yk
 * @since 2026-02-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record_single")
@Schema(name = "TopicRecordSingle对象", description = "趣味答题记录")
public class TopicRecordSingle {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "题目id")
    @TableField("topic_id")
    private String topicId;

    @Schema(description = "题目名称")
    @TableField("topic_title")
    private String topicTitle;

    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

    @Schema(description = "是否答对")
    @TableField("right_flag")
    private Boolean rightFlag;

    @Schema(description = "答对奖励积分数")
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @TableField(exist = false)
    @Schema(description = "题目选项列表")
    private List<TopicRecordSingleItem> recordSingleItemList;

}
