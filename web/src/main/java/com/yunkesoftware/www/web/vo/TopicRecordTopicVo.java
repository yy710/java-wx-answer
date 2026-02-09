package com.yunkesoftware.www.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TopicRecordTopicVo {
    @Schema(description = "题目id")
    private String id;

    @Schema(description = "题目选项")
    private List<TopicRecordTopicItemVo> recordTopicItemVoList;
}
