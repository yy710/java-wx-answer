package com.yunkesoftware.www.adm.vo;

import com.yunkesoftware.www.adm.entity.TicketActivityVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TicketActivityVo {
    @Schema(description = "投票活动id")
    private String id;

    @Schema(description = "视频列表")
    private List<TicketActivityVideo> videoList;
}
