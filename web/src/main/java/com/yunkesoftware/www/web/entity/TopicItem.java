package com.yunkesoftware.www.web.entity;

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
 * 答题活动-题目选项
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_item")
@Schema(name = "TopicItem对象", description = "答题活动-题目选项")
public class TopicItem implements Serializable {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "题目id")
    @TableField("topic_id")
    private String topicId;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "是否正确选项")
    @TableField("answer_flag")
    private Boolean answerFlag;
}
