#!/bin/bash

echo "========================================="
echo "Veo API Simple Test"
echo "========================================="
echo ""

# 检查 API KEY (支持两种环境变量名)
if [ -z "$GOOGLE_API_KEY" ] && [ -z "$GEMINI_API_KEY" ]; then
    echo "Error: GOOGLE_API_KEY or GEMINI_API_KEY environment variable is not set"
    exit 1
fi

if [ -n "$GEMINI_API_KEY" ]; then
    export GOOGLE_API_KEY="$GEMINI_API_KEY"
    echo "Using GEMINI_API_KEY as GOOGLE_API_KEY"
fi

# 检查测试图片
if [ ! -f "test_image.jpg" ]; then
    echo "Error: test_image.jpg not found"
    echo "Please copy a test image to test_image.jpg"
    exit 1
fi

echo "Compiling project..."
mvn clean compile test-compile -q

if [ $? -ne 0 ]; then
    echo "Compilation failed"
    exit 1
fi

echo ""
echo "Running tests..."
echo ""

mvn exec:java \
    -Dexec.mainClass="com.agent.animation.VeoSimpleTest" \
    -Dexec.classpathScope="test" \
    -q

echo ""
echo "========================================="
echo "Test completed"
echo "========================================="
