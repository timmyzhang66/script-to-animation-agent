package com.agent.animation;

import com.agent.animation.config.AppConfig;
import com.agent.animation.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Veo 图生视频功能测试
 * 
 * 这个测试类用于验证 Veo API 的图生视频功能是否正常工作
 * 
 * 使用方法：
 * 1. 确保设置了 GEMINI_API_KEY 环境变量
 * 2. 准备一张测试图片（例如 test_image.jpg）
 * 3. 运行此测试类
 * 
 * @author Agent
 */
public class VeoVideoGenerationTest {
    private static final Logger logger = LoggerFactory.getLogger(VeoVideoGenerationTest.class);
    
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("Veo Video Generation Test");
        logger.info("========================================");
        
        try {
            // 1. 检查环境变量
            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("GEMINI_API_KEY environment variable is not set!");
                System.exit(1);
            }
            logger.info("✓ GEMINI_API_KEY is set");
            
            // 2. 初始化配置和服务
            AppConfig config = new AppConfig();
            logger.info("✓ AppConfig initialized");
            logger.info("  - Veo Model: {}", config.getVeoModel());
            
            GeminiService geminiService = new GeminiService(config);
            logger.info("✓ GeminiService initialized");
            
            // 3. 检查测试图片
            String testImagePath = "test_image.jpg";
            File testImageFile = new File(testImagePath);
            
            if (!testImageFile.exists()) {
                logger.error("Test image not found: {}", testImagePath);
                logger.error("Please create a test image first.");
                logger.error("You can use any JPEG image, for example:");
                logger.error("  - Download an image from the internet");
                logger.error("  - Or generate one using Imagen");
                System.exit(1);
            }
            logger.info("✓ Test image found: {} ({} bytes)", testImagePath, testImageFile.length());
            
            // 4. 生成视频
            String prompt = "A beautiful scene with natural movement and smooth transitions";
            String outputPath = "test_output_video.mp4";
            
            logger.info("========================================");
            logger.info("Starting video generation...");
            logger.info("  - Input image: {}", testImagePath);
            logger.info("  - Prompt: {}", prompt);
            logger.info("  - Output: {}", outputPath);
            logger.info("========================================");
            
            long startTime = System.currentTimeMillis();
            
            String videoPath = geminiService.generateVideoFromImage(prompt, testImagePath, outputPath);
            
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            
            // 5. 验证结果
            File outputFile = new File(videoPath);
            if (outputFile.exists()) {
                logger.info("========================================");
                logger.info("✓ Video generation SUCCESSFUL!");
                logger.info("  - Output file: {}", videoPath);
                logger.info("  - File size: {} bytes", outputFile.length());
                logger.info("  - Generation time: {} seconds", duration);
                logger.info("========================================");
            } else {
                logger.error("Video generation failed: output file not found");
                System.exit(1);
            }
            
            // 6. 清理资源
            geminiService.close();
            logger.info("✓ Resources cleaned up");
            
        } catch (Exception e) {
            logger.error("Test failed with exception:", e);
            System.exit(1);
        }
    }
}
