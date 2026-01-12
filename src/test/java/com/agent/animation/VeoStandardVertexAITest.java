package com.agent.animation;

import com.google.genai.Client;
import com.google.genai.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

/**
 * Veo API 测试 - 使用标准 Vertex AI 模式（非 Express Mode）
 * 
 * 使用服务账号认证访问完整的 Vertex AI API
 */
public class VeoStandardVertexAITest {
    private static final Logger logger = LoggerFactory.getLogger(VeoStandardVertexAITest.class);
    
    private static final String PROJECT_ID = "gen-lang-client-0352724605";
    private static final String LOCATION = "us-central1";
    private static final String SERVICE_ACCOUNT_KEY_PATH = "gcp-service-account.json";
    
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("Veo API Test - Standard Vertex AI Mode");
        logger.info("========================================");
        logger.info("Project ID: {}", PROJECT_ID);
        logger.info("Location: {}", LOCATION);
        logger.info("");
        
        // 检查服务账号密钥文件
        File keyFile = new File(SERVICE_ACCOUNT_KEY_PATH);
        if (!keyFile.exists()) {
            logger.error("Service account key file not found: {}", SERVICE_ACCOUNT_KEY_PATH);
            System.exit(1);
        }
        logger.info("✓ Service account key file found");
        
        // 检查测试图片
        String testImagePath = "test_image.jpg";
        if (!new File(testImagePath).exists()) {
            logger.error("test_image.jpg not found");
            System.exit(1);
        }
        logger.info("✓ Test image found");
        
        // 设置环境变量指向服务账号密钥
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", SERVICE_ACCOUNT_KEY_PATH);
        
        try (Client client = Client.builder()
                .project(PROJECT_ID)
                .location(LOCATION)
                .vertexAI(true)
                .build()) {
            
            logger.info("✓ Client initialized with standard Vertex AI mode");
            logger.info("");
            
            // 测试 1: 纯文本生成视频
            logger.info("========================================");
            logger.info("Test 1: Text-to-video");
            logger.info("========================================");
            
            try {
                testTextToVideo(client);
                logger.info("✓ Test 1 PASSED");
            } catch (Exception e) {
                logger.error("✗ Test 1 FAILED: {}", e.getMessage());
                e.printStackTrace();
            }
            
            logger.info("");
            
            // 测试 2: 图生视频
            logger.info("========================================");
            logger.info("Test 2: Image-to-video");
            logger.info("========================================");
            
            try {
                testImageToVideo(client, testImagePath);
                logger.info("✓ Test 2 PASSED");
            } catch (Exception e) {
                logger.error("✗ Test 2 FAILED: {}", e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            logger.error("Fatal error:", e);
            System.exit(1);
        }
    }
    
    /**
     * 测试纯文本生成视频
     */
    private static void testTextToVideo(Client client) throws Exception {
        logger.info("Generating video from text prompt...");
        
        GenerateVideosSource source = GenerateVideosSource.builder()
                .prompt("A serene mountain landscape at sunset with clouds moving slowly")
                .build();
        
        GenerateVideosConfig config = GenerateVideosConfig.builder()
                .aspectRatio("16:9")
                .resolution("720p")
                .build();
        
        logger.info("Calling Veo API...");
        GenerateVideosOperation operation = client.models.generateVideos(
                "veo-3.1-generate-001",
                source,
                config
        );
        
        logger.info("✓ Operation created: {}", operation.name());
        logger.info("  Operation name can be used to check status later");
    }
    
    /**
     * 测试图生视频
     */
    private static void testImageToVideo(Client client, String imagePath) throws Exception {
        logger.info("Generating video from image...");
        
        // 读取图片并设置 MIME 类型
        byte[] imageBytes = Files.readAllBytes(new File(imagePath).toPath());
        
        // 根据文件扩展名确定 MIME 类型
        String mimeType = "image/jpeg";
        if (imagePath.toLowerCase().endsWith(".png")) {
            mimeType = "image/png";
        }
        
        Image image = Image.builder()
                .imageBytes(imageBytes)
                .mimeType(mimeType)
                .build();
        
        logger.info("Image loaded: {} bytes, MIME type: {}", imageBytes.length, mimeType);
        
        GenerateVideosSource source = GenerateVideosSource.builder()
                .prompt("Animate this scene with gentle movement and natural transitions")
                .image(image)
                .build();
        
        GenerateVideosConfig config = GenerateVideosConfig.builder()
                .aspectRatio("16:9")
                .resolution("720p")
                .build();
        
        logger.info("Calling Veo API...");
        GenerateVideosOperation operation = client.models.generateVideos(
                "veo-3.1-generate-001",
                source,
                config
        );
        
        logger.info("✓ Operation created: {}", operation.name());
        logger.info("  Operation name can be used to check status later");
    }
}
