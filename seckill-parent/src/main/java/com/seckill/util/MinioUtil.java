package com.seckill.util;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.seckill.config.MinioConfig;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Value("${minio.external-endpoint:http://8.134.151.227/:9000}")
    private String externalEndpoint;

    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查bucket是否存在失败", e);
            return false;
        }
    }

    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建bucket: {} 成功", bucketName);
            }
        } catch (Exception e) {
            log.error("创建bucket失败", e);
            throw new RuntimeException("创建bucket失败");
        }
    }

    public String uploadFile(MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            return uploadFile(minioConfig.getBucketName(), fileName, file.getInputStream(), file.getContentType());
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败");
        }
    }

    public String uploadFile(String bucketName, String fileName, InputStream inputStream, String contentType) {
        try {
            // 确保bucket存在
            createBucket(bucketName);
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
            
            log.info("文件上传成功: {}", fileName);
            return getFileUrl(bucketName, fileName);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败");
        }
    }

    public String getFileUrl(String bucketName, String fileName) {
        try {
            // 使用外部endpoint生成可直接访问的URL
            String endpoint = externalEndpoint != null && !externalEndpoint.isEmpty() ? externalEndpoint : minioConfig.getEndpoint();
            // 移除可能的尾部斜杠
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败");
        }
    }

    public void deleteFile(String bucketName, String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("文件删除成功: {}", fileName);
        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new RuntimeException("删除文件失败");
        }
    }
}