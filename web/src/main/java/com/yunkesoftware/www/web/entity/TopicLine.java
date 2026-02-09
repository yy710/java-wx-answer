package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 答题活动-线路
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("topic_line")
@Schema(name = "TopicLine对象", description = "答题活动-线路")
public class TopicLine implements Serializable {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "活动id")
    @TableField("topic_activity_id")
    private String topicActivityId;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "排序")
    @TableField("seq")
    private Integer seq;

    @Schema(description = "是否开启")
    @TableField("status")
    private Boolean status;

    @Schema(description = "答题用户数")
    @TableField("user_num")
    private Integer userNum;

    @Schema(description = "图片")
    @TableField("pic")
    private String pic;

    @Schema(description = "图片高度")
    @TableField("height")
    private Double height;

    @Schema(description = "图片宽度")
    @TableField("width")
    private Double width;
    @Schema(description = "是否完成")
    @TableField(exist = false)
    private boolean doneFlag;
}
