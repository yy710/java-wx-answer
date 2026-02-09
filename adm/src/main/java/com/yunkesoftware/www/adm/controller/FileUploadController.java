package com.yunkesoftware.www.adm.controller;

import cn.hutool.core.util.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import com.yunkesoftware.www.config.CosClientProperties;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


@Tag(name = "文件上传服务")
@RestController
@RequestMapping("/sys/file-upload")
@CrossOrigin
public class FileUploadController {

    @Resource
    private RedisTemplate<String, Map<String, Object>> redisTemplate;
    @Resource
    private COSClient cosClient;
    @Resource
    private CosClientProperties cosClientProperties;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult<String> minoUpload(@RequestPart("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            String extName = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."));

            String fileName = IdUtil.getSnowflakeNextIdStr();
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String key = dateStr + "/" + fileName + extName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientProperties.getBucket(), key, inputStream, objectMetadata);

            cosClient.putObject(putObjectRequest);
            String fileUrl = "https://" + cosClientProperties.getBucket() + ".cos." + cosClientProperties.getRegion() + ".myqcloud.com/" + key;
            return CommonResult.success(fileUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取对象存储签名数据
     */
    @Operation(summary = "获取签名和秘钥")
    @GetMapping("/cos/policy")
    public CommonResult<Map<String, Object>> getPolicy() throws IOException {

        Map<String, Object> cosPolicy = redisTemplate.opsForValue()
                .get("COS_POLICY");
        if (cosPolicy == null) {
            TreeMap<String, Object> config = new TreeMap<>();
            // 替换为您的云 api 密钥 SecretId
            config.put("secretId", cosClientProperties.getAccessKey());
            // 替换为您的云 api 密钥 SecretKey
            config.put("secretKey", cosClientProperties.getSecretKey());
            // 临时密钥有效时长，单位是秒
            int durationSeconds = 1800;
            config.put("durationSeconds", durationSeconds);
            // 换成您的 bucket
            config.put("bucket", cosClientProperties.getBucket());
            // 换成 bucket 所在地区
            config.put("region", cosClientProperties.getRegion());
            config.put("allowPrefixes", new String[]{"*"});

            String[] allowActions = new String[]{
                    // 简单上传
                    "name/cos:PutObject",
                    // 表单上传、小程序上传
                    "name/cos:PostObject",
                    // 分块上传
                    "name/cos:InitiateMultipartUpload",
                    "name/cos:ListMultipartUploads",
                    "name/cos:ListParts",
                    "name/cos:UploadPart",
                    "name/cos:CompleteMultipartUpload"
            };
            config.put("allowActions", allowActions);
            Response response = CosStsClient.getCredential(config);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("tmpSecretId", response.credentials.tmpSecretId);
            resultMap.put("tmpSecretKey", response.credentials.tmpSecretKey);
            resultMap.put("sessionToken", response.credentials.sessionToken);
            resultMap.put("startTime", response.startTime);
            resultMap.put("expiredTime", response.expiredTime);
            redisTemplate.opsForValue().set("COS_POLICY", resultMap, 1680, TimeUnit.SECONDS);
            return CommonResult.success(resultMap);
        } else {
            return CommonResult.success(cosPolicy);
        }
    }
}
