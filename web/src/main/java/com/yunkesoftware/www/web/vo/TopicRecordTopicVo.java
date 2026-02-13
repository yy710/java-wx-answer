package com.yunkesoftware.www.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TopicRecordTopicVo {
    @Schema(description = "题目id")
    @NotBlank(message = "未获取题目信息")
    private String id;

    @Schema(description = "题目选项")
    @NotEmpty(message = "未获取题目选项信息")
    private List<TopicRecordTopicItemVo> recordTopicItemVoList;

    @Schema(description = "1-首次答题 2-答错 3-答对")
    private Integer againType;
}
