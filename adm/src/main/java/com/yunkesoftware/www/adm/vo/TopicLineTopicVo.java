package com.yunkesoftware.www.adm.vo;

import com.yunkesoftware.www.adm.entity.TopicLineTopic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TopicLineTopicVo {

    @Schema(description = "线路id")
    private String id;

    @Schema(description = "线路-题目关联关系列表")
    private List<TopicLineTopic> topicLineTopicList;
}
