# Veo 图生视频功能测试指南

## 快速测试步骤

### 1. 准备测试图片

您有两种方式准备测试图片：

#### 方式 A：使用任意现有图片
将任意 JPEG 图片重命名为 `test_image.jpg` 并放在项目根目录。

#### 方式 B：从项目生成的图片中选择
如果您已经运行过主程序，可以从 `temp/` 目录中复制一张关键帧图片：

```bash
# 查看已生成的图片
ls -lh temp/keyframes/

# 复制一张作为测试图片
cp temp/keyframes/scene_1_keyframe.jpg test_image.jpg
```

### 2. 编译测试类

```bash
mvn clean compile test-compile
```

### 3. 运行测试

```bash
# 确保设置了 API Key
export GEMINI_API_KEY="your-api-key"

# 运行测试
mvn exec:java -Dexec.mainClass="com.agent.animation.VeoVideoGenerationTest" -Dexec.classpathScope="test"
```

### 4. 检查结果

如果测试成功，您将看到：
- ✓ 日志显示 "Video generation SUCCESSFUL!"
- ✓ 项目根目录下生成 `test_output_video.mp4`
- ✓ 显示视频文件大小和生成时间

## 预期输出

```
========================================
Veo Video Generation Test
========================================
✓ GEMINI_API_KEY is set
✓ AppConfig initialized
  - Veo Model: veo-3.1-generate-001
✓ GeminiService initialized
✓ Test image found: test_image.jpg (245678 bytes)
========================================
Starting video generation...
  - Input image: test_image.jpg
  - Prompt: A beautiful scene with natural movement and smooth transitions
  - Output: test_output_video.mp4
========================================
Generating video from image using imageBytes...
[等待 1-2 分钟...]
========================================
✓ Video generation SUCCESSFUL!
  - Output file: test_output_video.mp4
  - File size: 1234567 bytes
  - Generation time: 95 seconds
========================================
✓ Resources cleaned up
```

## 故障排查

### 错误：Test image not found
**解决**：确保 `test_image.jpg` 在项目根目录。

### 错误：GEMINI_API_KEY environment variable is not set
**解决**：运行 `export GEMINI_API_KEY="your-key"`。

### 错误：400 . Invalid resource field value
**解决**：确保代码已更新到最新版本（提交 687f401 或更新）。

### 错误：429 . Quota exceeded
**解决**：等待几分钟后重试，或检查您的 API 配额。

## 清理测试文件

测试完成后，您可以删除测试文件：

```bash
rm test_image.jpg test_output_video.mp4
```
