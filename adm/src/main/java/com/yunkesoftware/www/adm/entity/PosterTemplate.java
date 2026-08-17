package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yunkesoftware.www.query.PageCurrency;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("poster_template")
public class PosterTemplate extends PageCurrency {
    @TableId("id") private String id;
    @TableField("name") private String name;
    @TableField("status") private String status;
    @TableField("template_version") private Integer templateVersion;
    @TableField("canvas_width") private Integer canvasWidth;
    @TableField("canvas_height") private Integer canvasHeight;
    @TableField("restricted_json") private String restrictedJson;
    @TableField("preview_url") private String previewUrl;
    @TableField("published_at") private LocalDateTime publishedAt;
    @TableField("created_by") private String createdBy;
    @TableField(exist = false) private List<PosterTemplateAsset> assets;
}
