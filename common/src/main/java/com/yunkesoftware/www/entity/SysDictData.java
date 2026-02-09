package com.yunkesoftware.www.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunkesoftware.www.query.PageCurrency;
import com.yunkesoftware.www.utils.LocalDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典数据表
 * </p>
 *
 * @author cuiyongqiang
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SysDictData对象", description = "字典数据表")
public class SysDictData extends PageCurrency {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description ="字典编码")
    @TableId(value = "dict_code", type = IdType.AUTO)
    private Long dictCode;

    @Schema(description ="字典排序")
    private Integer dictSort;

    @Schema(description ="字典标签")
    private String dictLabel;

    @Schema(description ="字典键值")
    private String dictValue;

    @Schema(description ="字典类型")
    private String dictType;

    @Schema(description ="样式属性（其他样式扩展）")
    private String cssClass;

    @Schema(description ="表格回显样式")
    private String listClass;

    @Schema(description ="是否默认（Y是 N否）")
    private String isDefault;

    @Schema(description ="状态（0正常 1停用）")
    private Integer status;

    @Schema(description ="创建者")
    private String createBy;

    @Schema(description ="创建时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime createTime;

    @Schema(description ="更新者")
    private String updateBy;

    @Schema(description ="更新时间")
    @JsonFormat(pattern = LocalDateUtils.PATTERN_YYYYMMDDHHMMSS)
    private LocalDateTime updateTime;

    @Schema(description ="备注")
    private String remark;

    @Schema(description ="关联码")
    private String relatedDictCode;


}
