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
 * 字典类型表
 * </p>
 *
 * @author cuiyongqiang
 * @since 2022-09-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SysDictType对象", description = "字典类型表")
public class SysDictType extends PageCurrency {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description ="字典主键")
    @TableId(value = "dict_id", type = IdType.AUTO)
    private Long dictId;

    @Schema(description ="字典名称")
    private String dictName;

    @Schema(description ="字典类型")
    private String dictType;

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


}
