package com.yunkesoftware.www.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserVo implements Serializable {
    @Schema(description = "主键")
    private String id;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "头像")
    private String pic;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
