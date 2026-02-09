package com.yunkesoftware.www.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class LoginVo {

    @Schema(description = "code")
    private String code;

    @Schema(description = "邀请人id")
    private String parentId;

}
