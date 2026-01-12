#!/bin/bash

echo "========================================="
echo "End-to-End Test - Script to Animation"
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

# 创建测试脚本文件
cat > test_script.txt << 'EOF'
In a magical forest, a young wizard named Luna discovers a hidden portal. 
She steps through and finds herself in a world of floating islands and glowing crystals.
Luna meets a friendly dragon who offers to guide her on an adventure.
Together they soar through the clouds, exploring the wonders of this enchanted realm.
EOF

echo "Test script created: test_script.txt"
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

# 运行端到端测试
echo "Running end-to-end test..."
echo ""

mvn exec:java \
    -Dexec.mainClass="com.agent.animation.Main" \
    -Dexec.args="test_script.txt" \
    -q

echo ""
echo "========================================="
echo "Test completed"
echo "========================================="
echo ""
echo "Check the output directory for generated files:"
echo "  - Characters: ./output/characters/"
echo "  - Storyboard: ./output/storyboard/"
echo "  - Keyframes: ./output/keyframes/"
echo "  - Videos: ./output/videos/"
echo "  - Final video: ./output/final_video.mp4"
