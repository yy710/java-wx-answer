package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.utils.LocalDateUtils;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 答题记录-题目选项
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record_topic_item")
@Schema(name = "TopicRecordTopicItem对象", description = "答题记录-题目选项")
public class TopicRecordTopicItem {

    @Schema(description = "答题记录题目id")
    @TableField("topic_record_topic_id")
    @ExcelIgnore
    private String topicRecordTopicId;

    @Schema(description = "题目选项id")
    @TableField("topic_item_id")
    @ExcelIgnore
    private String topicItemId;

    @Schema(description = "题目选项名称")
    @TableField("topic_item_title")
    @ExcelProperty("题目选项名称")
    private String topicItemTitle;

    @Schema(description = "是否选中")
    @TableField("check_flag")
    @ExcelProperty("是否选中")
    private Boolean checkFlag;

    @Schema(description = "是否是答案")
    @TableField("answer_flag")
    @ExcelProperty("是否是答案")
    private Boolean answerFlag;
}
