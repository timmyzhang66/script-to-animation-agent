#!/bin/bash

# 测试 Gemini 3 Pro Image (Nano Banana Pro) 配置

echo "=========================================="
echo "Testing Gemini 3 Pro Image Configuration"
echo "=========================================="

# 设置环境变量
export GCP_PROJECT_ID="gen-lang-client-0352724605"
export GCP_LOCATION="global"
export GCP_SERVICE_ACCOUNT_KEY_PATH="./gcp-service-account.json"

echo ""
echo "Environment variables:"
echo "  GCP_PROJECT_ID: $GCP_PROJECT_ID"
echo "  GCP_LOCATION: $GCP_LOCATION"
echo "  GCP_SERVICE_ACCOUNT_KEY_PATH: $GCP_SERVICE_ACCOUNT_KEY_PATH"
echo ""

# 编译项目
echo "Compiling project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi

echo "✅ Compilation successful"
echo ""

# 运行测试
echo "Running Gemini 3 Pro Image test..."
echo ""

mvn exec:java -Dexec.mainClass="com.agent.animation.service.NanoBananaProService" \
    -Dexec.classpathScope=compile \
    -q

echo ""
echo "=========================================="
echo "Test completed"
echo "=========================================="
