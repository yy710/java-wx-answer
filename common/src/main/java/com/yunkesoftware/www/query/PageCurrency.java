package com.yunkesoftware.www.query;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Map;


public class PageCurrency implements Serializable {

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }


    public Long getStartSortId() {
        return startSortId;
    }

    public void setStartSortId(Long startSortId) {
        this.startSortId = startSortId;
    }


    public Long getEndSortId() {
        return endSortId;
    }


    public void setEndSortId(Long endSortId) {
        this.endSortId = endSortId;
    }

    /**
     * 页码，从1开始
     */
    @ExcelIgnore
    @TableField(exist = false)
    @Schema(description = "页码，从1开始")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Long pageNum = 1L;


    /**
     * 页面大小
     */
    @ExcelIgnore
    @Schema(description = "页面大小")
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Long pageSize = 15L;


    @ExcelIgnore
    @Schema(description = "开始排序位置",hidden = true)
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Long startSortId;

    @TableField(exist = false)
    @Schema(description = "结束排序位置",hidden = true)
    @ExcelIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Long endSortId;

    @JsonIgnore
    public Integer getSortFlag() {
        return sortFlag;
    }

    public void setSortFlag(Integer sortFlag) {
        this.sortFlag = sortFlag;
    }

    @TableField(exist = false)
    @Schema(description = "前移 后移",hidden = true)
    @ExcelIgnore

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Integer sortFlag;

    public Map<String, Object> getSearchMap() {
        return searchMap;
    }

    public void setSearchMap(Map<String, Object> searchMap) {
        this.searchMap = searchMap;
    }

    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(hidden = true)
    @ExcelIgnore
    public Map<String, Object> searchMap;

}
