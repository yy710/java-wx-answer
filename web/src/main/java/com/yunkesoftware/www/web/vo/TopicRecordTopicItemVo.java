package com.yunkesoftware.www.web.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TopicRecordTopicItemVo {

    @Schema(description = "答题记录题目id")
    private String topicRecordItemId;

    @Schema(description = "题目选项id")
    private String topicItemId;

    @Schema(description = "是否选中")
    private Boolean checkFlag;
}
