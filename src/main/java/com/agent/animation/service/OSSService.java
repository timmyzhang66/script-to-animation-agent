package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

public class OSSService {
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
    }
    // ... upload 方法保持不变 ...
}