package com.yunkesoftware.www.adm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@TableName("poster_template_asset")
public class PosterTemplateAsset {
    @TableId("id") private String id;
    @TableField("template_id") private String templateId;
    @TableField("asset_key") private String assetKey;
    @TableField("file_url") private String fileUrl;
    @TableField("mime_type") private String mimeType;
    @TableField("width") private Integer width;
    @TableField("height") private Integer height;
    @TableField("sha256") private String sha256;
}
