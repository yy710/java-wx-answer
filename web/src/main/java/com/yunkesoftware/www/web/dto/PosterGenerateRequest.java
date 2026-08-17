package com.yunkesoftware.www.web.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PosterGenerateRequest {
    private String templateId;
    private Integer templateVersion;
    private Map<String, String> custom;
}
