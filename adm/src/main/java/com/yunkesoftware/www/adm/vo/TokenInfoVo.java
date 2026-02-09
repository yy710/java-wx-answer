package com.yunkesoftware.www.adm.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenInfoVo implements Serializable {

    private String tokenName;

    private String tokenValue;
}
