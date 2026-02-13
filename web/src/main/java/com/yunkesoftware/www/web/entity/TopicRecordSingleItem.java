package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 趣味答题记录-题目选项
 * </p>
 *
 * @author yk
 * @since 2026-02-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_record_single_item")
@Schema(name = "TopicRecordSingleItem对象", description = "趣味答题记录-题目选项")
public class TopicRecordSingleItem {

    @Schema(description = "答题记录题目id")
    @TableField("topic_record_single_id")
    private String topicRecordSingleId;

    @Schema(description = "题目选项id")
    @TableField("topic_item_id")
    private String topicItemId;

    @Schema(description = "题目选项名称")
    @TableField("topic_item_title")
    private String topicItemTitle;

    @Schema(description = "是否选中")
    @TableField("check_flag")
    private Boolean checkFlag;

    @Schema(description = "是否是答案")
    @TableField("answer_flag")
    private Boolean answerFlag;
}
