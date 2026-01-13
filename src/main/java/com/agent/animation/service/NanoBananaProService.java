package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.agent.animation.util.RetryUtils;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Nano Banana Pro (Gemini 2.5 Flash Image) 服务
 * 使用 REST API 直接调用 Gemini 2.5 Flash Image 模型
 */
public class NanoBananaProService {
    private static final Logger logger = LoggerFactory.getLogger(NanoBananaProService.class);
    
    private final AppConfig config;
    private final HttpClient httpClient;
    private final Gson gson;
    private final GoogleCredentials credentials;
    private final String apiEndpoint;
    
    public NanoBananaProService() throws IOException {
        this.config = AppConfig.getInstance();
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        
        // 加载服务账号凭证
        String keyPath = config.getGcpServiceAccountKeyPath();
        this.credentials = GoogleCredentials.fromStream(new FileInputStream(keyPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
        
        // 构建 API 端点
        String projectId = config.getGcpProjectId();
        String location = config.getGcpLocation();
        String model = config.getNanoBananaProModel();
        
        this.apiEndpoint = String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                location, projectId, location, model
        );
        
        logger.info("NanoBananaProService initialized");
        logger.info("  Model: {}", model);
        logger.info("  Location: {}", location);
        logger.info("  Max reference images: {}", config.getNanoBananaProMaxReferenceImages());
    }
    
    /**
     * 生成图像（文本到图像，无参考图像）
     * 
     * @param prompt 文本提示
     * @param aspectRatio 宽高比（例如 "16:9", "1:1"）
     * @param resolution 分辨率（"1K", "2K", "4K"）
     * @return 生成的图像 Base64 编码
     * @throws Exception 生成失败时抛出异常
     */
    public String generateImage(String prompt, String aspectRatio, String resolution) throws Exception {
        return generateImageWithReferences(prompt, null, aspectRatio, resolution);
    }

    /**
     * 生成图像（文本到图像，带参考图像）
     * 用于保持角色一致性
     * 
     * @param prompt 文本提示
     * @param referenceImageUrls 参考图像 URL 列表（最多 3 张）
     * @param aspectRatio 宽高比
     * @param resolution 分辨率
     * @return 生成的图像 Base64 编码
     * @throws Exception 生成失败时抛出异常
     */
    public String generateImageWithReferences(
            String prompt, 
            List<String> referenceImageUrls, 
            String aspectRatio, 
            String resolution) throws Exception {
        
        logger.info("Generating image with Nano Banana Pro");
        logger.info("  Prompt: {}", prompt);
        logger.info("  Reference images: {}", referenceImageUrls != null ? referenceImageUrls.size() : 0);
        logger.info("  Aspect ratio: {}", aspectRatio);
        logger.info("  Resolution: {}", resolution);
        
        // 使用重试机制执行 API 请求
        return RetryUtils.executeWithRetry(() -> {
            // 构建请求体
            JsonObject requestBody = buildRequestBody(prompt, referenceImageUrls, aspectRatio, resolution);
            
            // 获取访问令牌
            credentials.refreshIfExpired();
            String accessToken = credentials.getAccessToken().getTokenValue();
            
            // 发送 HTTP 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiEndpoint))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();
            
            logger.debug("Sending request to: {}", apiEndpoint);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorMsg = String.format("API request failed with status %d: %s", 
                        response.statusCode(), response.body());
                logger.error(errorMsg);
                throw new Exception(errorMsg);
            }
            
            // 解析响应
            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
            return extractImageFromResponse(responseJson);
        });
    }

    /**
     * 构建请求体
     */
    private JsonObject buildRequestBody(
            String prompt, 
            List<String> referenceImageUrls, 
            String aspectRatio, 
            String resolution) {
        
        JsonObject requestBody = new JsonObject();
        
        // 构建 contents 数组
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        
        // 添加 role 字段
        content.addProperty("role", "user");
        
        // 添加文本提示
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        
        // 添加参考图像（如果有）
        if (referenceImageUrls != null && !referenceImageUrls.isEmpty()) {
            int maxImages = config.getNanoBananaProMaxReferenceImages();
            int imageCount = Math.min(referenceImageUrls.size(), maxImages);
            
            for (int i = 0; i < imageCount; i++) {
                JsonObject imagePart = new JsonObject();
                JsonObject fileData = new JsonObject();
                fileData.addProperty("fileUri", referenceImageUrls.get(i));
                fileData.addProperty("mimeType", "image/jpeg");
                imagePart.add("fileData", fileData);
                parts.add(imagePart);
            }
            
            if (referenceImageUrls.size() > maxImages) {
                logger.warn("Reference images exceed max limit ({}). Using first {} images.", 
                        maxImages, maxImages);
            }
        }
        
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        
        // 添加生成配置
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.4);
        generationConfig.addProperty("topP", 1.0);
        generationConfig.addProperty("topK", 32);
        generationConfig.addProperty("maxOutputTokens", 8192);
        
        // 添加响应模态
        JsonArray responseModalities = new JsonArray();
        responseModalities.add("IMAGE");
        generationConfig.add("responseModalities", responseModalities);
        
        // 注意：Gemini 2.5 Flash Image 不支持 imageGenerationConfig
        // aspectRatio 和 resolution 参数已经被忽略
        
        requestBody.add("generationConfig", generationConfig);
        
        return requestBody;
    }

    /**
     * 从响应中提取图像
     */
    private String extractImageFromResponse(JsonObject responseJson) throws Exception {
        try {
            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) {
                throw new Exception("No candidates in response");
            }
            
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");
            
            // 查找图像部分
            for (int i = 0; i < parts.size(); i++) {
                JsonObject part = parts.get(i).getAsJsonObject();
                if (part.has("inlineData")) {
                    JsonObject inlineData = part.getAsJsonObject("inlineData");
                    String base64Image = inlineData.get("data").getAsString();
                    String mimeType = inlineData.get("mimeType").getAsString();
                    
                    logger.info("Image generated successfully");
                    logger.info("  MIME type: {}", mimeType);
                    logger.info("  Size: {} bytes", base64Image.length());
                    
                    return base64Image;
                }
            }
            
            throw new Exception("No image found in response");
            
        } catch (Exception e) {
            logger.error("Failed to extract image from response: {}", e.getMessage());
            throw new Exception("Failed to extract image from response: " + e.getMessage(), e);
        }
    }

    /**
     * 保存 Base64 图像到文件
     */
    public void saveImageToFile(String base64Image, String outputPath) throws IOException {
        // 确保父目录存在
        Path path = Paths.get(outputPath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            logger.debug("Created directory: {}", parentDir);
        }
        
        // 解码并保存
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Files.write(path, imageBytes);
        logger.info("Image saved to: {} ({} bytes)", outputPath, imageBytes.length);
    }
}
