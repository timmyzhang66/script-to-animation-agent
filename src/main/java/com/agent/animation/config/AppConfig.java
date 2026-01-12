package com.agent.animation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用配置管理类
 * 负责加载和管理应用程序的配置参数
 */
public class AppConfig {
    private static AppConfig instance;
    private final Properties properties;

    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
            
            // 从环境变量加载敏感配置
            String geminiKey = System.getenv("GEMINI_API_KEY");
            if (geminiKey != null) {
                properties.setProperty("gemini.api.key", geminiKey);
            }
            
            // 加载 GCP 配置
            String gcpProjectId = System.getenv("GCP_PROJECT_ID");
            if (gcpProjectId != null) {
                properties.setProperty("gcp.project.id", gcpProjectId);
            }
            
            String gcpLocation = System.getenv("GCP_LOCATION");
            if (gcpLocation != null) {
                properties.setProperty("gcp.location", gcpLocation);
            }
            
            String gcpServiceAccountKeyPath = System.getenv("GCP_SERVICE_ACCOUNT_KEY_PATH");
            if (gcpServiceAccountKeyPath != null) {
                properties.setProperty("gcp.service.account.key.path", gcpServiceAccountKeyPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application properties", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    // Gemini 配置
    public String getGeminiApiKey() {
        return getProperty("gemini.api.key");
    }

    public String getImagenModel() {
        return getProperty("gemini.imagen.model", "imagen-3.0-generate-002");
    }

    public String getVeoModel() {
        return getProperty("gemini.veo.model", "veo-2.0-generate-001");
    }

    public String getTextModel() {
        return getProperty("gemini.text.model", "gemini-2.0-flash-exp");
    }

    // 图像生成配置
    public int getImagenNumberOfImages() {
        return getIntProperty("imagen.number.of.images", 1);
    }

    public String getImagenOutputMimeType() {
        return getProperty("imagen.output.mime.type", "image/jpeg");
    }

    // 视频生成配置
    public int getVeoNumberOfVideos() {
        return getIntProperty("veo.number.of.videos", 1);
    }

    public int getVeoDurationSeconds() {
        return getIntProperty("veo.duration.seconds", 5);
    }

    public boolean getVeoEnhancePrompt() {
        return getBooleanProperty("veo.enhance.prompt", true);
    }

    public int getVeoPollIntervalSeconds() {
        return getIntProperty("veo.poll.interval.seconds", 10);
    }

    public int getVeoMaxWaitMinutes() {
        return getIntProperty("veo.max.wait.minutes", 30);
    }

    // 重试配置
    public int getMaxRetryAttempts() {
        return getIntProperty("retry.max.attempts", 5);
    }

    public long getInitialRetryDelayMs() {
        String value = getProperty("retry.initial.delay.ms");
        return value != null ? Long.parseLong(value) : 5000L;
    }

    public long getMaxRetryDelayMs() {
        String value = getProperty("retry.max.delay.ms");
        return value != null ? Long.parseLong(value) : 60000L;
    }

    // 存储配置
    public String getTempDir() {
        return getProperty("storage.temp.dir", "./temp");
    }

    public String getOutputDir() {
        return getProperty("storage.output.dir", "./output");
    }

    // 视频处理配置
    public String getVideoOutputFormat() {
        return getProperty("video.output.format", "mp4");
    }

    public int getVideoFrameRate() {
        return getIntProperty("video.frame.rate", 30);
    }

    public String getVideoCodec() {
        return getProperty("video.codec", "h264");
    }
    
    // GCP 配置
    public String getGcpProjectId() {
        return getProperty("gcp.project.id");
    }
    
    public String getGcpLocation() {
        return getProperty("gcp.location", "us-central1");
    }
    
    public String getGcpServiceAccountKeyPath() {
        return getProperty("gcp.service.account.key.path");
    }
    
    // Nano Banana Pro 配置
    public String getNanoBananaProModel() {
        return getProperty("nano.banana.pro.model", "gemini-3-pro-image-preview");
    }
    
    public boolean getNanoBananaProUseGoogleSearch() {
        return getBooleanProperty("nano.banana.pro.use.google.search", false);
    }
    
    public String getNanoBananaProAspectRatio() {
        return getProperty("nano.banana.pro.aspect.ratio", "16:9");
    }
    
    public String getNanoBananaProResolution() {
        return getProperty("nano.banana.pro.resolution", "2K");
    }
    
    // Gemini 3 Flash 配置
    public String getGemini3FlashModel() {
        return getProperty("gemini.3.flash.model", "gemini-3.0-flash");
    }
}
