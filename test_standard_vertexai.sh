#!/bin/bash

echo "========================================="
echo "Veo API Test - Standard Vertex AI Mode"
echo "========================================="
echo ""

# 检查服务账号密钥文件
if [ ! -f "gcp-service-account.json" ]; then
    echo "Error: gcp-service-account.json not found"
    exit 1
fi

# 检查测试图片
if [ ! -f "test_image.jpg" ]; then
    echo "Error: test_image.jpg not found"
    exit 1
fi

echo "Compiling project..."
mvn clean compile test-compile -q

if [ $? -ne 0 ]; then
    echo "Compilation failed"
    exit 1
fi

echo ""
echo "Running test with standard Vertex AI mode..."
echo ""

# 设置环境变量
export GOOGLE_APPLICATION_CREDENTIALS="gcp-service-account.json"

mvn exec:java \
    -Dexec.mainClass="com.agent.animation.VeoStandardVertexAITest" \
    -Dexec.classpathScope="test" \
    -q

echo ""
echo "========================================="
echo "Test completed"
echo "========================================="
