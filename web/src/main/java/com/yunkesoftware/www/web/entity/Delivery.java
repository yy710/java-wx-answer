package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 物流公司
 * </p>
 *
 * @author cuiyq
 * @since 2024-11-12
 */
@Getter
@Setter
@TableName("yk_delivery")
@Schema(name = "Delivery对象", description = "物流公司")
public class Delivery {

    @Schema(description = "物流公司ID")
    @TableId("id")
    private String id;

    @Schema(description = "物流公司名称")
    @TableField("dvy_name")
    private String dvyName;

    @Schema(description = "快递缩写")
    @TableField("code")
    private String code;

    @Schema(description = "图片")
    @TableField("pic")
    private String pic;
}
