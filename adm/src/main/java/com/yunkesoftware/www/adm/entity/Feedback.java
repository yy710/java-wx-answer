package com.yunkesoftware.www.adm.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.utils.LocalDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户反馈
 * </p>
 *
 * @author cuiyq
 * @since 2023-07-03
 */
@Getter
@Setter
@TableName("yk_feedback")
@Schema(name = "Feedback对象", description = "用户反馈")
public class Feedback extends PageCurrency {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    @ExcelProperty("主键")
    private String id;

    @Schema(description = "用户id")
    @TableField("user_id")
    @ExcelProperty("用户id")
    private String userId;

    @Schema(description = "问题类型")
    @TableField("problem_type")
    @ExcelProperty("问题类型")
    private Integer problemType;

    @Schema(description = "处理结果 0未处理 1已处理")
    @TableField("handle_flag")
    @ExcelProperty("处理结果 0未处理 1已处理")
    private Boolean handleFlag;

    @Schema(description = "反馈内容")
    @TableField("content")
    @ExcelProperty("反馈内容")
    private String content;

    @Schema(description = "图片url")
    @TableField("img_url")
    @ExcelProperty("图片url")
    private String imgUrl;

    @Schema(description = "处理结果内容")
    @TableField("handel_response")
    @ExcelProperty("处理结果内容")
    private String handelResponse;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @ExcelProperty("修改时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime updateTime;


    @TableField(exist = false)
    @ExcelProperty("用户昵称")
    private String nickName;

    @TableField(exist = false)
    @ExcelProperty("用户电话")
    private String mobile;
}
