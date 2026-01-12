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
import java.util.Base64;

/**
 * Gemini AI 服务类
 * 负责调用 Google Gemini API 进行图像生成和视频生成
 */
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final Client client;
    private final AppConfig config;
    private final OSSService ossService;
    private final GCSService gcsService;

    public GeminiService() {
        this.config = AppConfig.getInstance();
        
        // 检查是否配置了服务账号密钥文件
        String serviceAccountKeyPath = config.getGcpServiceAccountKeyPath();
        if (serviceAccountKeyPath != null && !serviceAccountKeyPath.isEmpty()) {
            // 使用标准 Vertex AI 模式（服务账号认证）
            logger.info("Using Standard Vertex AI mode with service account");
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", serviceAccountKeyPath);
            
            this.client = Client.builder()
                    .project(config.getGcpProjectId())
                    .location(config.getGcpLocation())
                    .vertexAI(true)
                    .build();
            
            logger.info("GeminiService initialized with Standard Vertex AI mode");
            logger.info("  Project: {}", config.getGcpProjectId());
            logger.info("  Location: {}", config.getGcpLocation());
        } else {
            // 使用 API Key 模式（仅支持部分 API）
            String apiKey = config.getGeminiApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Either GEMINI_API_KEY or GCP_SERVICE_ACCOUNT_KEY_PATH must be set");
            }
            
            logger.warn("Using API Key mode - Note: Veo API requires Standard Vertex AI mode");
            this.client = Client.builder()
                    .apiKey(apiKey)
                    .build();
            
            logger.info("GeminiService initialized with API Key mode");
        }
        
        // 初始化 OSS 服务（保留以供其他用途）
        this.ossService = new OSSService();
        // 初始化 GCS 服务（用于 Vertex AI 原生集成）
        this.gcsService = new GCSService();
    }

    /**
     * 生成图像（文本到图像）
     * 
     * @param prompt 文本提示
     * @param outputPath 输出文件路径
     * @return 生成的图像文件路径
     * @throws Exception 生成失败时抛出异常
     */
    public String generateImage(String prompt, String outputPath) throws Exception {
        return RetryUtils.executeWithRetry(() -> {
            logger.info("Generating image with prompt: {}", prompt);
            
            GenerateImagesConfig config = GenerateImagesConfig.builder()
                    .numberOfImages(this.config.getImagenNumberOfImages())
                    .outputMimeType(this.config.getImagenOutputMimeType())
                    .includeSafetyAttributes(true)
                    .build();

            GenerateImagesResponse response = client.models.generateImages(
                    this.config.getImagenModel(),
                    prompt,
                    config
            );

            if (response.images().isEmpty()) {
                throw new Exception("Unable to generate image for prompt: " + prompt);
            }

            Image generatedImage = response.images().get(0);
            saveImage(generatedImage, outputPath);
            
            logger.info("Image generated successfully: {}", outputPath);
            return outputPath;
        });
    }

    /**
     * 生成图像（文本到图像，带参考图像）
     * 用于生成与参考角色一致的关键帧
     * 
     * @param prompt 文本提示
     * @param referenceImagePath 参考图像路径
     * @param outputPath 输出文件路径
     * @return 生成的图像文件路径
     * @throws Exception 生成失败时抛出异常
     */
    public String generateImageWithReference(String prompt, String referenceImagePath, String outputPath) throws Exception {
        logger.info("Generating image with reference. Prompt: {}, Reference: {}", prompt, referenceImagePath);
        
        // 增强提示词，包含参考图像的描述
        String enhancedPrompt = String.format(
            "%s. Style and character should be consistent with the reference image. Maintain the same art style, color palette, and character design.",
            prompt
        );
        
        // 注意：Imagen 3.0 的参考图像功能可能需要使用 editImage 方法
        // 这里我们使用增强的提示词来保持一致性
        return generateImage(enhancedPrompt, outputPath);
    }

    /**
     * 生成视频（文本到视频）
     * 
     * @param prompt 文本提示
     * @param outputPath 输出文件路径
     * @return 生成的视频文件路径
     * @throws Exception 生成失败时抛出异常
     */
    public String generateVideoFromText(String prompt, String outputPath) throws Exception {
        return RetryUtils.executeWithRetry(() -> {
            logger.info("Generating video from text. Prompt: {}", prompt);
            
            GenerateVideosConfig config = GenerateVideosConfig.builder()
                    .numberOfVideos(this.config.getVeoNumberOfVideos())
                    .enhancePrompt(this.config.getVeoEnhancePrompt())
                    .durationSeconds(this.config.getVeoDurationSeconds())
                    .build();

            GenerateVideosOperation operation = client.models.generateVideos(
                    this.config.getVeoModel(),
                    prompt,
                    null,
                    config
            );

            return waitForVideoOperation(operation, outputPath);
        });
    }

    /**
     * 生成视频（图像到视频）
     * 
     * @param prompt 文本提示
     * @param keyframeImagePath 关键帧图像路径
     * @param outputPath 输出文件路径
     * @return 生成的视频文件路径
     * @throws Exception 生成失败时抛出异常
     */
    public String generateVideoFromImage(String prompt, String keyframeImagePath, String outputPath) throws Exception {
        return RetryUtils.executeWithRetry(() -> {
            logger.info("Generating video from image using imageBytes. Prompt: {}, Keyframe: {}", prompt, keyframeImagePath);
            
            // 读取本地图片文件为字节数组
            byte[] imageBytes = FileUtils.readFileToByteArray(new File(keyframeImagePath));
            logger.info("Read keyframe image: {} bytes", imageBytes.length);
            
            // 根据文件扩展名确定 MIME 类型
            String mimeType = "image/jpeg";
            if (keyframeImagePath.toLowerCase().endsWith(".png")) {
                mimeType = "image/png";
            }
            
            // 使用 imageBytes 和 mimeType 创建 Image 对象
            // 这是 Vertex AI 标准模式的正确用法
            Image keyframeImage = Image.builder()
                    .imageBytes(imageBytes)
                    .mimeType(mimeType)
                    .build();
            
            logger.info("Created Image object with MIME type: {}", mimeType);
            
            // 使用 GenerateVideosSource 包装 prompt 和 image
            // 这是官方 Java SDK 的正确用法
            GenerateVideosSource source = GenerateVideosSource.builder()
                    .prompt(prompt)
                    .image(keyframeImage)
                    .build();
            
            // 使用正确的配置参数：aspectRatio, resolution, generateAudio
            // 而不是 numberOfVideos, enhancePrompt, durationSeconds
            GenerateVideosConfig config = GenerateVideosConfig.builder()
                    .aspectRatio("16:9")
                    .resolution("720p")
                    .generateAudio(false)
                    .build();

            GenerateVideosOperation operation = client.models.generateVideos(
                    this.config.getVeoModel(),
                    source,
                    config
            );

            return waitForVideoOperation(operation, outputPath);
        });
    }

    /**
     * 等待视频生成操作完成
     * 
     * @param operation 视频生成操作
     * @param outputPath 输出文件路径
     * @return 生成的视频文件路径
     * @throws Exception 操作失败或超时时抛出异常
     */
    private String waitForVideoOperation(GenerateVideosOperation operation, String outputPath) throws Exception {
        int pollIntervalMs = this.config.getVeoPollIntervalSeconds() * 1000;
        int maxWaitMs = this.config.getVeoMaxWaitMinutes() * 60 * 1000;
        int elapsedMs = 0;

        logger.info("Waiting for video generation to complete...");

        while (!operation.done().isPresent()) {
            if (elapsedMs >= maxWaitMs) {
                throw new Exception("Video generation timeout after " + maxWaitMs + " ms");
            }

            try {
                Thread.sleep(pollIntervalMs);
                elapsedMs += pollIntervalMs;
                logger.debug("Polling video operation... elapsed: {} ms", elapsedMs);
                operation = client.operations.getVideosOperation(operation, null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Exception("Video generation interrupted", e);
            }
        }

        // 提取生成的视频
        if (operation.response().isPresent()) {
            GenerateVideosResponse response = operation.response().get();
            if (response.generatedVideos().isPresent() && !response.generatedVideos().get().isEmpty()) {
                Video video = response.generatedVideos().get().get(0).video().orElse(null);
                if (video != null) {
                    saveVideo(video, outputPath);
                    logger.info("Video generated successfully: {}", outputPath);
                    return outputPath;
                }
            }
        }

        throw new Exception("Failed to extract video from operation response");
    }

    /**
     * 保存图像到文件
     * 
     * @param image 图像对象
     * @param outputPath 输出文件路径
     * @throws IOException 保存失败时抛出异常
     */
    private void saveImage(Image image, String outputPath) throws IOException {
        // 获取图像字节数据
        byte[] imageData = image.imageBytes()
                .orElseThrow(() -> new IOException("Image bytes not available"));
        FileUtils.writeByteArrayToFile(new File(outputPath), imageData);
        logger.debug("Image saved to: {}", outputPath);
    }

    /**
     * 保存视频到文件
     * 
     * @param video 视频对象
     * @param outputPath 输出文件路径
     * @throws IOException 保存失败时抛出异常
     */
    private void saveVideo(Video video, String outputPath) throws IOException {
        // 获取视频字节数据
        byte[] videoData = video.videoBytes()
                .orElseThrow(() -> new IOException("Video bytes not available"));
        FileUtils.writeByteArrayToFile(new File(outputPath), videoData);
        logger.debug("Video saved to: {}", outputPath);
    }

    /**
     * 关闭客户端连接
     */
    public void close() {
        // 关闭 OSS 服务
        if (ossService != null) {
            ossService.close();
        }
        // 关闭 GCS 服务
        if (gcsService != null) {
            gcsService.close();
        }
        // 如果 Client 有关闭方法，在这里调用
        logger.info("GeminiService closed");
    }
}
