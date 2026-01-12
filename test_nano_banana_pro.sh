#!/bin/bash

echo "========================================="
echo "Nano Banana Pro Test"
echo "========================================="
echo ""

# 设置环境变量
export GCP_PROJECT_ID="gen-lang-client-0352724605"
export GCP_LOCATION="us-central1"
export GCP_SERVICE_ACCOUNT_KEY_PATH="gcp-service-account.json"

# 检查服务账号密钥文件
if [ ! -f "$GCP_SERVICE_ACCOUNT_KEY_PATH" ]; then
    echo "Error: $GCP_SERVICE_ACCOUNT_KEY_PATH not found"
    exit 1
fi

echo "Configuration:"
echo "  Project ID: $GCP_PROJECT_ID"
echo "  Location: $GCP_LOCATION"
echo "  Service Account Key: $GCP_SERVICE_ACCOUNT_KEY_PATH"
echo ""

# 编译项目
echo "Compiling project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "Compilation failed"
    exit 1
fi

echo "✓ Compilation successful"
echo ""

# 创建测试类
cat > src/test/java/com/agent/animation/NanoBananaProTest.java << 'EOF'
package com.agent.animation;

import com.agent.animation.service.NanoBananaProService;
import com.agent.animation.service.GCSService;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Nano Banana Pro 测试类
 */
public class NanoBananaProTest {
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("Test 1: Text-to-Image (No Reference)");
            System.out.println("========================================");
            
            NanoBananaProService service = new NanoBananaProService();
            GCSService gcsService = new GCSService();
            
            String prompt1 = "A young wizard named Luna with long silver hair, wearing a purple robe with star patterns, holding a magical staff, standing in a mystical forest. Professional animation style, high quality, detailed character design.";
            
            System.out.println("Generating image with prompt: " + prompt1.substring(0, Math.min(100, prompt1.length())) + "...");
            
            String base64Image1 = service.generateImage(prompt1, "16:9", "1K");
            String outputPath1 = "test_nano_banana_1.jpg";
            service.saveImageToFile(base64Image1, outputPath1);
            
            System.out.println("✓ Image generated: " + outputPath1);
            System.out.println("✓ File size: " + new File(outputPath1).length() + " bytes");
            
            // 上传到 GCS
            String imageUrl1 = gcsService.uploadFile(outputPath1, null, false);
            System.out.println("✓ Uploaded to GCS: " + imageUrl1);
            
            System.out.println("");
            System.out.println("========================================");
            System.out.println("Test 2: Text-to-Image (With Reference)");
            System.out.println("========================================");
            
            String prompt2 = "Luna the wizard casting a spell, magical energy glowing around her hands, same character as reference image. Professional animation style, cinematic lighting.";
            
            System.out.println("Generating image with reference...");
            System.out.println("Reference URL: " + imageUrl1);
            
            List<String> referenceUrls = Arrays.asList(imageUrl1);
            String base64Image2 = service.generateImageWithReferences(prompt2, referenceUrls, "16:9", "1K");
            String outputPath2 = "test_nano_banana_2.jpg";
            service.saveImageToFile(base64Image2, outputPath2);
            
            System.out.println("✓ Image generated: " + outputPath2);
            System.out.println("✓ File size: " + new File(outputPath2).length() + " bytes");
            
            String imageUrl2 = gcsService.uploadFile(outputPath2, null, false);
            System.out.println("✓ Uploaded to GCS: " + imageUrl2);
            
            System.out.println("");
            System.out.println("========================================");
            System.out.println("All tests passed!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
EOF

echo "Running Nano Banana Pro test..."
echo ""

mvn exec:java \
    -Dexec.mainClass="com.agent.animation.NanoBananaProTest" \
    -Dexec.classpathScope=test \
    -q

echo ""
echo "========================================="
echo "Test completed"
echo "========================================="
echo ""
echo "Generated files:"
echo "  - test_nano_banana_1.jpg (character reference)"
echo "  - test_nano_banana_2.jpg (with reference)"
