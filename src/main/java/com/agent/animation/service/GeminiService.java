package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.agent.animation.util.RetryUtils;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 严谨版 Gemini AI 服务类
 * 严格遵循 google-genai SDK 的复数命名规范
 */
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final Client client;
    private final AppConfig config;

    public GeminiService() {
        this.config = AppConfig.getInstance();
        String apiKey = config.getGeminiApiKey();

        // 初始化 Client，确保 API Key 正确注入
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        logger.info("GeminiService initialized for Industrial Engine");
    }

    /**
     * 严谨实现：从图像 URL 生成视频
     * 使用 SDK 规范的 GenerateVideosSource 和 GenerateVideosConfig
     */
    public String generateVideoFromImageUrl(String prompt, String imageUrl, String outputPath) throws Exception {
        return RetryUtils.executeWithRetry(() -> {
            logger.info("Initiating Veo video generation. Prompt: {}, Image: {}", prompt, imageUrl);

            // 1. 构造 Image 对象
            Image keyframeImage = Image.builder()
                    .gcsUri(imageUrl) // 确保传入的是标准的 GCS/OSS URL
                    .build();

            // 2. 构造 Source (SDK 规范: GenerateVideosSource)
            GenerateVideosSource source = GenerateVideosSource.builder()
                    .prompt(prompt)
                    .image(keyframeImage)
                    .build();

            // 3. 构造 Config (SDK 规范: GenerateVideosConfig)
            GenerateVideosConfig videoConfig = GenerateVideosConfig.builder()
                    .aspectRatio("16:9")
                    .resolution("720p")
                    .generateAudio(true)
                    .build();

            // 4. 调用 API (SDK 规范: generateVideos)
            GenerateVideosOperation operation = client.models.generateVideos(
                    "veo-2.0-generate-001",
                    source,
                    videoConfig
            );

            return waitForVideoOperation(operation, outputPath);
        });
    }

    /**
     * 轮询等待视频生成完成
     */
    private String waitForVideoOperation(GenerateVideosOperation operation, String outputPath) throws Exception {
        int pollIntervalMs = 10000; // 10秒轮询一次
        int maxWaitMs = 600000;    // 最大等待10分钟
        int elapsedMs = 0;

        while (!operation.done().isPresent()) {
            if (elapsedMs >= maxWaitMs) {
                throw new Exception("Video generation timed out.");
            }
            Thread.sleep(pollIntervalMs);
            elapsedMs += pollIntervalMs;
            logger.info("Polling Veo operation... Elapsed: {}s", elapsedMs / 1000);
            operation = client.operations.getVideosOperation(operation, null);
        }

        GenerateVideosResponse response = operation.response()
                .orElseThrow(() -> new Exception("No response from Veo API"));

        Video video = response.generatedVideos()
                .orElseThrow(() -> new Exception("No videos in response"))
                .get(0).video()
                .orElseThrow(() -> new Exception("Video object missing"));

        saveVideo(video, outputPath);
        return outputPath;
    }

    private void saveVideo(Video video, String outputPath) throws IOException {
        byte[] videoData = video.videoBytes()
                .orElseThrow(() -> new IOException("Video bytes not available"));
        FileUtils.writeByteArrayToFile(new File(outputPath), videoData);
    }
}