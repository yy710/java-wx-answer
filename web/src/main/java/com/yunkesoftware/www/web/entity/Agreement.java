package com.yunkesoftware.www.web.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunkesoftware.www.query.PageCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;



/**
 * <p>
 * 协议
 * </p>
 *
 * @author g
 * @since 2023-09-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("yk_agreement")
@Schema(name = "Agreement对象", description = "协议")
public class Agreement extends PageCurrency {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "类型")
    @TableField("type")
    @ExcelProperty("类型")
    private Integer type;

    @TableField("content")
    @ExcelProperty("内容")
    private String content;

    @TableField("title")
    @ExcelProperty("标题")
    private String title;
}
