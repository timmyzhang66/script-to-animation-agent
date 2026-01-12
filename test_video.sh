#!/bin/bash

# Veo 图生视频快速测试脚本

set -e

echo "========================================"
echo "Veo Video Generation Quick Test"
echo "========================================"

# 检查 API Key
if [ -z "$GEMINI_API_KEY" ]; then
    echo "❌ Error: GEMINI_API_KEY environment variable is not set"
    echo "Please run: export GEMINI_API_KEY=\"your-api-key\""
    exit 1
fi
echo "✓ GEMINI_API_KEY is set"

# 检查测试图片
if [ ! -f "test_image.jpg" ]; then
    echo "❌ Error: test_image.jpg not found"
    echo ""
    echo "Please prepare a test image using one of these methods:"
    echo ""
    echo "Method 1: Use any existing JPEG image"
    echo "  cp /path/to/your/image.jpg test_image.jpg"
    echo ""
    echo "Method 2: Copy from generated keyframes (if you've run the main program)"
    echo "  cp temp/keyframes/scene_1_keyframe.jpg test_image.jpg"
    echo ""
    exit 1
fi
echo "✓ Test image found: test_image.jpg ($(du -h test_image.jpg | cut -f1))"

# 编译项目
echo ""
echo "Compiling project..."
mvn clean compile test-compile -q
echo "✓ Compilation successful"

# 运行测试
echo ""
echo "========================================"
echo "Running video generation test..."
echo "This may take 1-2 minutes, please wait..."
echo "========================================"
echo ""

mvn exec:java \
    -Dexec.mainClass="com.agent.animation.VeoVideoGenerationTest" \
    -Dexec.classpathScope="test" \
    -q

# 检查结果
if [ -f "test_output_video.mp4" ]; then
    echo ""
    echo "========================================"
    echo "✓ TEST PASSED!"
    echo "  Output: test_output_video.mp4 ($(du -h test_output_video.mp4 | cut -f1))"
    echo "========================================"
else
    echo ""
    echo "========================================"
    echo "❌ TEST FAILED: Output video not found"
    echo "========================================"
    exit 1
fi
