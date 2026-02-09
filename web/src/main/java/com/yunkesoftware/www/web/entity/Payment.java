package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 活动支付信息
 * </p>
 *
 * @author yk
 * @since 2026-01-19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("payment")
@Schema(name = "Payment对象", description = "活动支付信息")
public class Payment {

    @Schema(description = "id")
    @TableId("id")
    private String id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "描述")
    @TableField("descr")
    private String descr;

    @Schema(description = "是否删除")
    @TableField("delete_flag")
    private Boolean deleteFlag;

    @Schema(description = "是否启用")
    @TableField("status")
    private Boolean status;
}
