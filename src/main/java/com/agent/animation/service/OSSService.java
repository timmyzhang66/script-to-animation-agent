package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 服务类
 * 负责上传文件到阿里云 OSS 并获取公开访问 URL
 */
public class OSSService {
    private static final Logger logger = LoggerFactory.getLogger(OSSService.class);


    private final OSS ossClient;
    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;

    public OSSService() {
        AppConfig config = AppConfig.getInstance();
        this.endpoint = config.getOssEndpoint();
        this.accessKeyId = config.getOssAccessKeyId();
        this.accessKeySecret = config.getOssAccessKeySecret();
        this.bucketName = config.getOssBucketName();
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }



    /**
     * 上传文件到 OSS
     *
     * @param localFilePath 本地文件路径
     * @param objectKey OSS 对象键（可选，如果为 null 则自动生成）
     * @return 文件的公开访问 URL
     * @throws Exception 上传失败时抛出异常
     */
    public String uploadFile(String localFilePath, String objectKey) throws Exception {
        File file = new File(localFilePath);

        if (!file.exists()) {
            throw new Exception("File not found: " + localFilePath);
        }

        // 如果没有指定 objectKey，则自动生成
        if (objectKey == null || objectKey.isEmpty()) {
            String fileName = file.getName();
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
            }
            objectKey = "script-to-animation/" + UUID.randomUUID().toString() + extension;
        }

        try {
            logger.info("Uploading file to OSS: {} -> {}", localFilePath, objectKey);

            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, file);
            ossClient.putObject(putObjectRequest);

            // 生成公开访问 URL
            String url = getPublicUrl(objectKey);

            logger.info("File uploaded successfully, URL: {}", url);
            return url;

        } catch (Exception e) {
            logger.error("Failed to upload file to OSS", e);
            throw new Exception("Failed to upload file to OSS: " + e.getMessage(), e);
        }
    }

    /**
     * 上传字节数组到 OSS
     *
     * @param data 字节数组
     * @param fileName 文件名（用于生成 objectKey）
     * @return 文件的公开访问 URL
     * @throws Exception 上传失败时抛出异常
     */
    public String uploadBytes(byte[] data, String fileName) throws Exception {
        if (data == null || data.length == 0) {
            throw new Exception("Data is empty");
        }

        // 生成 objectKey
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
        }
        String objectKey = "script-to-animation/" + UUID.randomUUID().toString() + extension;

        try {
            logger.info("Uploading bytes to OSS: {} bytes -> {}", data.length, objectKey);

            // 上传字节数组
            ossClient.putObject(bucketName, objectKey, new java.io.ByteArrayInputStream(data));

            // 生成公开访问 URL
            String url = getPublicUrl(objectKey);

            logger.info("Bytes uploaded successfully, URL: {}", url);
            return url;

        } catch (Exception e) {
            logger.error("Failed to upload bytes to OSS", e);
            throw new Exception("Failed to upload bytes to OSS: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件的公开访问 URL
     *
     * @param objectKey OSS 对象键
     * @return 公开访问 URL
     */
    private String getPublicUrl(String objectKey) {
        // 构建公开访问 URL
        // 格式: https://{bucket}.{endpoint}/{objectKey}
        String domain = endpoint.replace("https://", "").replace("http://", "");
        return "https://" + bucketName + "." + domain + "/" + objectKey;
    }

    /**
     * 删除 OSS 中的文件
     *
     * @param objectKey OSS 对象键
     * @throws Exception 删除失败时抛出异常
     */
    public void deleteFile(String objectKey) throws Exception {
        try {
            logger.info("Deleting file from OSS: {}", objectKey);
            ossClient.deleteObject(bucketName, objectKey);
            logger.info("File deleted successfully");
        } catch (Exception e) {
            logger.error("Failed to delete file from OSS", e);
            throw new Exception("Failed to delete file from OSS: " + e.getMessage(), e);
        }
    }

    /**
     * 从 URL 提取 objectKey
     *
     * @param url OSS 公开访问 URL
     * @return objectKey
     */
    public String extractObjectKeyFromUrl(String url) {
        // URL 格式: https://{bucket}.{endpoint}/{objectKey}
        String domain = bucketName + "." + endpoint.replace("https://", "").replace("http://", "");
        String prefix = "https://" + domain + "/";

        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }

        // 如果无法解析，返回原始 URL
        return url;
    }

    /**
     * 关闭 OSS 客户端
     */
    public void close() {
        if (ossClient != null) {
            ossClient.shutdown();
            logger.info("OSSService closed");
        }
    }
}
