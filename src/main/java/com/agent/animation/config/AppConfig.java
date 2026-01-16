package com.agent.animation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Properties;

/**
 * 全局配置管理类
 * 建议开发阶段直接在此处硬编码，生产环境再切换为环境变量
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static AppConfig instance;
    private final Properties properties = new Properties();

    private AppConfig() {
        loadProperties();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            logger.warn("Could not load application.properties, using defaults/env.");
        }
    }

    private String getSetting(String key, String envVar, String defaultValue) {
        // 优先级 1: 检查是否在代码里写死了（如果有特定前缀或者不是默认值）
        // 优先级 2: 检查 application.properties
        // 优先级 3: 检查环境变量
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(envVar);
        }
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    // --- Gemini 配置 ---
    // TODO: 在这里直接替换你的 Key
    public String getGeminiApiKey() {
        return getSetting("gemini.api.key", "GEMINI_API_KEY", "YOUR_ACTUAL_API_KEY_HERE");
    }

    public String getTextModel() {
        return getSetting("text.model", "TEXT_MODEL", "gemini-2.0-flash");
    }

    public String getImageModel() {
        return getSetting("image.model", "IMAGE_MODEL", "imagen-3.0-generate-001");
    }

    // --- 阿里云 OSS 配置 ---
    // TODO: 在这里填入你的 OSS 信息
    public String getOssAccessKeyId() {
        return getSetting("aliyun.oss.access.key.id", "OSS_ACCESS_KEY_ID", "YOUR_OSS_KEY_ID");
    }

    public String getOssAccessKeySecret() {
        return getSetting("aliyun.oss.access.key.secret", "OSS_ACCESS_KEY_SECRET", "YOUR_OSS_SECRET");
    }

    public String getOssEndpoint() {
        return getSetting("aliyun.oss.endpoint", "OSS_ENDPOINT", "oss-cn-hangzhou.aliyuncs.com");
    }

    public String getOssBucketName() {
        return getSetting("aliyun.oss.bucket.name", "OSS_BUCKET_NAME", "YOUR_BUCKET_NAME");
    }

    // --- 本地暂存配置 ---
    public String getTempDir() {
        String tempDir = getSetting("temp.dir", "TEMP_DIR", "temp");
        java.io.File file = new java.io.File(tempDir);
        if (!file.exists()) file.mkdirs();
        return tempDir;
    }

    // --- Nano Banana Pro (如有使用) ---
    public String getNanoBananaProAspectRatio() { return "16:9"; }
    public String getNanoBananaProResolution() { return "1280x720"; }
}