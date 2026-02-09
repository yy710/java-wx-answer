package com.yunkesoftware.www.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TopicRecordVo {

    @Schema(description = "答题活动路线id")
    @NotBlank(message = "未知答题活动路线")
    private String topicLineId;

    @Schema(description = "题目列表+题目选项")
    @NotEmpty(message = "题目信息不能为空")
    private List<TopicRecordTopicVo> topicVoList;

    @Schema(description = "答题时间")
    @NotNull(message = "未获取到答题时间-请刷新重试")
    private Integer usedTime;

}
