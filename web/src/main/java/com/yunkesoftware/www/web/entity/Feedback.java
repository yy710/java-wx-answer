package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 意见反馈(Feedback)表实体类
 *
 * @author zxp
 * @since 2025-05-12 18:09:31
 */
@EqualsAndHashCode(callSuper = false)
@Data
@TableName("yk_feedback")
@Schema(name = "Feedback对象", description = "意见反馈")
public class Feedback {

    @Schema(description = "用户id")
    @TableField(value = "user_id")
    private String userId;

    @Schema(description = "问题类型：1-功能异常，2-新功能建议，3-体验问题，4-其他")
    @TableField(value = "problem_type")
    private Integer problemType;

    @Schema(description = "处理结果 0(false)未处理 1(true)已处理")
    @TableField(value = "handle_flag")
    private Boolean handleFlag;

    @Schema(description = "反馈内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "图片url")
    @TableField(value = "img_url")
    private String imgUrl;

    @Schema(description = "处理结果内容")
    @TableField(value = "handel_response")
    private String handelResponse;

    @Schema(description = "创建时间")
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(value = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}

