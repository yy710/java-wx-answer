package com.yunkesoftware.www.web.vo;

import com.yunkesoftware.www.web.entity.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TopicLineDataVo {
    @Schema(description = "是否首次")
    private Boolean firstFlag;

    @Schema(description = "时间是否满足条件")
    private Boolean timeFlag;

    @Schema(description = "题目信息数据")
    private List<Topic> topicList;
}
