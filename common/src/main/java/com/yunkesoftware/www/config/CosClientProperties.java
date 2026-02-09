package com.yunkesoftware.www.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "upload")
public class CosClientProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
}
