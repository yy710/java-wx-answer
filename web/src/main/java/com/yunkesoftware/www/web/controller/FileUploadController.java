package com.yunkesoftware.www.web.controller;

import cn.hutool.core.util.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.yunkesoftware.www.config.CosClientProperties;
import com.yunkesoftware.www.result.CommonResult;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/wx/file-upload")
public class FileUploadController {
    @Resource
    private COSClient cosClient;
    @Resource
    private CosClientProperties cosClientProperties;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult<String> minoUpload(@RequestPart("file") MultipartFile file) {
        try {
            if (StringUtils.hasLength(file.getOriginalFilename())) {
                String extName = file.getOriginalFilename()
                        .substring(file.getOriginalFilename().lastIndexOf("."));
                String fileName = IdUtil.getSnowflakeNextIdStr();
                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String key = dateStr + "/" + fileName + extName;
                InputStream inputStream = file.getInputStream();
                ObjectMetadata objectMetadata = new ObjectMetadata();
                PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientProperties.getBucket(), key, inputStream, objectMetadata);

                cosClient.putObject(putObjectRequest);
                String fileUrl = "https://" + cosClientProperties.getBucket() + ".cos." + cosClientProperties.getRegion() + ".myqcloud.com/" + key;
                return CommonResult.success(fileUrl);
            }
            return CommonResult.failed("上传失败");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
