package com.agent.animation;

import com.google.genai.Client;
import com.google.genai.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Veo API 简化测试 - 尝试不同的调用方式
 * 
 * 目的：找出导致 400 错误的具体原因
 */
public class VeoSimpleTest {
    private static final Logger logger = LoggerFactory.getLogger(VeoSimpleTest.class);
    
    public static void main(String[] args) {
        // 支持两种环境变量名称
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("GOOGLE_API_KEY or GEMINI_API_KEY not set");
            System.exit(1);
        }
        logger.info("Using API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
        
        String testImagePath = "test_image.jpg";
        if (!new File(testImagePath).exists()) {
            logger.error("test_image.jpg not found");
            System.exit(1);
        }
        
        try (Client client = Client.builder()
                .apiKey(apiKey)
                .vertexAI(true)
                .build()) {
            
            logger.info("========================================");
            logger.info("Test 1: Text-to-video (no image)");
            logger.info("========================================");
            
            try {
                testTextToVideo(client);
                logger.info("✓ Test 1 PASSED");
            } catch (Exception e) {
                logger.error("✗ Test 1 FAILED: {}", e.getMessage());
                e.printStackTrace();
            }
            
            logger.info("");
            logger.info("========================================");
            logger.info("Test 2: Image-to-video with fromFile");
            logger.info("========================================");
            
            try {
                testImageToVideoWithFromFile(client, testImagePath);
                logger.info("✓ Test 2 PASSED");
            } catch (Exception e) {
                logger.error("✗ Test 2 FAILED: {}", e.getMessage());
                e.printStackTrace();
            }
            
            logger.info("");
            logger.info("========================================");
            logger.info("Test 3: Image-to-video with imageBytes");
            logger.info("========================================");
            
            try {
                testImageToVideoWithBytes(client, testImagePath);
                logger.info("✓ Test 3 PASSED");
            } catch (Exception e) {
                logger.error("✗ Test 3 FAILED: {}", e.getMessage());
                e.printStackTrace();
            }
            
            logger.info("");
            logger.info("========================================");
            logger.info("Test 4: Image-to-video with minimal config");
            logger.info("========================================");
            
            try {
                testImageToVideoMinimal(client, testImagePath);
                logger.info("✓ Test 4 PASSED");
            } catch (Exception e) {
                logger.error("✗ Test 4 FAILED: {}", e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            logger.error("Fatal error:", e);
            System.exit(1);
        }
    }
    
    /**
     * 测试 1：纯文本生成视频（不使用图片）
     */
    private static void testTextToVideo(Client client) throws Exception {
        logger.info("Generating video from text prompt only...");
        
        GenerateVideosSource source = GenerateVideosSource.builder()
                .prompt("A beautiful mountain landscape")
                .build();
        
        GenerateVideosConfig config = GenerateVideosConfig.builder()
                .aspectRatio("16:9")
                .resolution("720p")
                .build();
        
        GenerateVideosOperation operation = client.models.generateVideos(
                "veo-3.1-generate-001",
                source,
                config
        );
        
        logger.info("Operation created: {}", operation.name());
    }
    
    /**
     * 测试 2：使用 Image.fromFile()
     */
    private static void testImageToVideoWithFromFile(Client client, String imagePath) throws Exception {
        logger.info("Using Image.fromFile()...");
        
        Image image = Image.fromFile(imagePath, "image/jpeg");
        
        GenerateVideosSource source = GenerateVideosSource.builder()
                .prompt("Animate this scene")
                .image(image)
                .build();
        
        GenerateVideosConfig config = GenerateVideosConfig.builder()
                .aspectRatio("16:9")
                .resolution("720p")
                .build();
        
        GenerateVideosOperation operation = client.models.generateVideos(
                "veo-3.1-generate-001",
                source,
                config
        );
        
        logger.info("Operation created: {}", operation.name());
    }
    
    /**
     * 测试 3：使用 imageBytes
     */
    private static void testImageToVideoWithBytes(Client client, String imagePath) throws Exception {
        logger.info("Using Image.builder().imageBytes()...");
        
        byte[] imageBytes = Files.readAllBytes(new File(imagePath).toPath());
        
        Image image = Image.builder()
                .imageBytes(imageBytes)
                .build();
        
        GenerateVideosSource source = GenerateVideosSource.builder()
                .prompt("Animate this scene")
                .image(image)
                .build();
        
        GenerateVideosConfig config = GenerateVideosConfig.builder()
                .aspectRatio("16:9")
                .resolution("720p")
                .build();
        
        GenerateVideosOperation operation = client.models.generateVideos(
                "veo-3.1-generate-001",
                source,
                config
        );
        
        logger.info("Operation created: {}", operation.name());
    }
    
    /**
     * 测试 4：最小化配置
     */
    private static void testImageToVideoMinimal(Client client, String imagePath) throws Exception {
        logger.info("Using minimal configuration...");
        
        byte[] imageBytes = Files.readAllBytes(new File(imagePath).toPath());
        
        Image image = Image.builder()
                .imageBytes(imageBytes)
                .build();
        
        GenerateVideosSource source = GenerateVideosSource.builder()
                .prompt("Animate this scene")
                .image(image)
                .build();
        
        // 不设置任何配置，使用默认值
        GenerateVideosOperation operation = client.models.generateVideos(
                "veo-3.1-generate-001",
                source,
                null  // 尝试不传配置
        );
        
        logger.info("Operation created: {}", operation.name());
    }
}
