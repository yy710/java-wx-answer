package com.yunkesoftware.www.web.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenInfoVo implements Serializable {

    private String tokenName;

    /** token 值 */
    private String tokenValue;

}
