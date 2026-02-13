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
 * 答题活动-线路
 * </p>
 *
 * @author yk
 * @since 2026-01-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_line")
@Schema(name = "TopicLine对象", description = "答题活动-线路")
public class TopicLine extends PageCurrency {

    @Schema(description = "id")
    @TableId("id")
    @ExcelIgnore
    private String id;

    @Schema(description = "活动id")
    @TableField("topic_activity_id")
    @ExcelProperty("活动id")
    private String topicActivityId;

    @Schema(description = "标题")
    @TableField("title")
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "排序")
    @TableField("seq")
    @ExcelProperty("排序")
    private Integer seq;

    @Schema(description = "是否开启")
    @TableField("status")
    @ExcelProperty("是否开启")
    private Boolean status;

    @Schema(description = "答题用户数")
    @TableField("user_num")
    @ExcelProperty("答题用户数")
    private Integer userNum;

    @Schema(description = "描述")
    @TableField("descr")
    private String descr;
}
