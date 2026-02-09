package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.yunkesoftware.www.query.PageCurrency;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 答题活动-题目选项
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_item")
@Schema(name = "TopicItem对象", description = "答题活动-题目选项")
public class TopicItem extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "题目id")
    @TableField("topic_id")
    @ExcelIgnore
    private String topicId;

    @Schema(description = "标题")
    @TableField("title")
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "排序")
    @TableField("seq")
    @ExcelProperty("排序")
    private Integer seq;

    @Schema(description = "是否正确选项")
    @TableField("answer_flag")
    @ExcelProperty("是否正确选项")
    private Boolean answerFlag;
}
