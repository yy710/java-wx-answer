package com.yunkesoftware.www.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosClientConfig {
    @Resource
    private CosClientProperties cosClientProperties;

    @Bean
    public COSClient initClient() {
        COSCredentials cred = new BasicCOSCredentials(cosClientProperties.getAccessKey(), cosClientProperties.getSecretKey());
        Region region = new Region(cosClientProperties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        // 腾讯云文件上传
        return new COSClient(cred, clientConfig);
    }
}
