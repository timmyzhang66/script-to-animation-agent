package com.agent.animation;

import com.agent.animation.service.NanoBananaProService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 测试 Gemini 3 Pro Image (Nano Banana Pro) 配置
 */
public class Gemini3ProImageTest {
    private static final Logger logger = LoggerFactory.getLogger(Gemini3ProImageTest.class);

    public static void main(String[] args) {
        try {
            logger.info("========================================");
            logger.info("Gemini 3 Pro Image Configuration Test");
            logger.info("========================================");
            
            // 初始化服务
            logger.info("Initializing NanoBananaProService...");
            NanoBananaProService service = new NanoBananaProService();
            logger.info("✅ Service initialized successfully");
            
            // 测试 1: 简单文本生成图像
            logger.info("");
            logger.info("Test 1: Generate image from text prompt");
            logger.info("  Prompt: A cute cat sitting on a chair");
            logger.info("  Aspect Ratio: 16:9");
            logger.info("  Resolution: 2K");
            
            String imageBase64 = service.generateImage(
                "A cute cat sitting on a chair",
                "16:9",
                "2K"
            );
            
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                logger.info("✅ Test 1 PASSED - Image generated successfully");
                logger.info("  Image size: {} bytes", Base64.getDecoder().decode(imageBase64).length);
                
                // 保存图像到文件
                String outputPath = "./temp/test_gemini3_cat.jpg";
                Files.createDirectories(Paths.get("./temp"));
                Files.write(Paths.get(outputPath), Base64.getDecoder().decode(imageBase64));
                logger.info("  Saved to: {}", outputPath);
            } else {
                logger.error("❌ Test 1 FAILED - No image generated");
            }
            
            logger.info("");
            logger.info("========================================");
            logger.info("All tests completed");
            logger.info("========================================");
            
        } catch (Exception e) {
            logger.error("❌ Test failed with exception", e);
            System.exit(1);
        }
    }
}
