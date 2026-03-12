package com.wfh.aipassagecreator.service;

import com.wfh.aipassagecreator.common.ErrorCode;
import com.wfh.aipassagecreator.config.RustFsClientConfig;
import com.wfh.aipassagecreator.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @Title: MinioManager
 * @Author wangfenghuan
 * @Package com.wfh.drawio.manager
 * @Date 2025/12/22 18:59
 * @description:
 */
@Component
public class S3Service {

    @Resource
    private RustFsClientConfig clientConfig;

    @Resource
    private S3Client s3Client;

    public String putObject(String objectName, InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(clientConfig.getBucketName())
                            .key(objectName)
                            .contentType("application/octet-stream")
                            .build(),
                    RequestBody.fromBytes(bytes)
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
        String encodedPath = UriUtils.encodePath(objectName, StandardCharsets.UTF_8);
        // 拼接最终 URL
        return "https://oss.intellidraw.top" + "/" + clientConfig.getBucketName() + "/" + encodedPath;
    }
}
