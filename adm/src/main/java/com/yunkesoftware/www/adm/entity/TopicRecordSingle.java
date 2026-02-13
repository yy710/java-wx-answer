package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

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
public class TopicRecordSingle extends PageCurrency {


    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "题目id")
    @TableField("topic_id")
    @ExcelProperty("题目id")
    private String topicId;

    @Schema(description = "题目名称")
    @TableField("topic_title")
    @ExcelProperty("题目名称")
    private String topicTitle;

    @Schema(description = "用户id")
    @TableField("user_id")
    @ExcelProperty("用户id")
    private String userId;

    @Schema(description = "是否答对")
    @TableField("right_flag")
    @ExcelProperty("是否答对")
    private Boolean rightFlag;

    @Schema(description = "答对奖励积分数")
    @TableField("reward_amount")
    @ExcelProperty("答对奖励积分数")
    private BigDecimal rewardAmount;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "用户昵称")
    @TableField(exist = false)
    @ExcelProperty("用户昵称")
    private String nickName;

    @Schema(description = "题目选项")
    @TableField(exist = false)
    private List<TopicRecordSingleItem> singleItemList;
}
