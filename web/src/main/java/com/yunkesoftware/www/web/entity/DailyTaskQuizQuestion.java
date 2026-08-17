package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@TableName("daily_task_quiz_question")
public class DailyTaskQuizQuestion {
    @TableId("id")
    private String id;
    @TableField("session_id")
    private String sessionId;
    @TableField("sequence_no")
    private Integer sequenceNo;
    @TableField("topic_id")
    private String topicId;
    @TableField("question_snapshot")
    private String questionSnapshot;
    @TableField("correct_option_snapshot")
    private String correctOptionSnapshot;
    @TableField("user_option")
    private String userOption;
    @TableField("is_correct")
    private Boolean correct;
    @TableField("requested_points")
    private BigDecimal requestedPoints;
    @TableField("awarded_points")
    private BigDecimal awardedPoints;
}
