package com.yunkesoftware.www.adm.vo;

import com.yunkesoftware.www.adm.entity.VideoActivityVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class VideoActivityVo {
    @Schema(description = "视频活动id")
    @NotBlank(message = "视频活动id不能为空")
    private String id;

    @Schema(description = "关联的视频信息")
    private List<VideoActivityVideo> videoList;
}
