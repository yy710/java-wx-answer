package com.yunkesoftware.www.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 客户服务
 * </p>
 *
 * @author cuiyq
 * @since 2024-11-15
 */
@Getter
@Setter
@TableName("yk_custom_server")
@Schema(name = "CustomServer对象", description = "客户服务")
public class CustomServer {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "电话号码")
    @TableField("phone")
    private String phone;

    @Schema(description = "类型")
    @TableField("type")
    private Integer type;

    @Schema(description = "appId")
    @TableField("app_id")
    private String appId;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "图片")
    @TableField("pic")
    private String pic;
}
