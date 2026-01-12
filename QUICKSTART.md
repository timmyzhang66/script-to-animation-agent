# 快速入门指南

本指南将帮助您在 5 分钟内开始使用 Script to Animation Agent。

## 前提条件

1. **Java 17+** 已安装
   ```bash
   java -version
   ```

2. **Maven 3.6+** 已安装
   ```bash
   mvn -version
   ```

3. **API Keys** 已准备
   - Google Gemini API Key
   - Gemini API Key

## 5 分钟快速开始

### 步骤 1：设置 API Keys

在终端中设置环境变量：

**Linux/Mac:**
```bash
export GEMINI_API_KEY="your-gemini-api-key-here"
export OPENAI_API_KEY="your-openai-api-key-here"
```

**Windows (CMD):**
```cmd
set GEMINI_API_KEY=your-gemini-api-key-here
set OPENAI_API_KEY=your-openai-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="your-gemini-api-key-here"
$env:OPENAI_API_KEY="your-openai-api-key-here"
```

### 步骤 2：构建项目

```bash
cd script-to-animation-agent
mvn clean package
```

等待依赖下载和编译完成（首次运行可能需要几分钟）。

### 步骤 3：运行示例

使用提供的示例脚本：

**Linux/Mac:**
```bash
./run.sh -f example_script.txt
```

**Windows:**
```cmd
java -jar target/script-to-animation-agent-1.0.0.jar -f example_script.txt
```

### 步骤 4：等待生成

系统将自动执行以下步骤：
1. ✓ 生成角色图像 (~30秒)
2. ✓ 生成分镜 (~15秒)
3. ✓ 生成关键帧 (~2分钟)
4. ✓ 生成视频片段 (~10-15分钟)
5. ✓ 合并视频 (~10秒)

### 步骤 5：查看结果

生成的视频将保存在 `output/` 目录：
```
output/animation_YYYYMMDD_HHMMSS.mp4
```

## 使用自己的脚本

### 方法 1：创建脚本文件

创建一个文本文件（例如 `my_script.txt`）：

```
Title: My Story

Scene 1:
A young wizard with a blue robe and pointed hat stands in a magical forest. 
The trees glow with mystical light.

Dialogue: "I must find the ancient spellbook!"

Scene 2:
The wizard walks through the forest, examining glowing mushrooms along the path.

Dialogue: "These mushrooms will guide my way."

Scene 3:
The wizard discovers a hidden cave entrance covered in vines.

Dialogue: "This must be the place!"
```

运行：
```bash
./run.sh -f my_script.txt
```

### 方法 2：交互式输入

直接运行程序：
```bash
./run.sh
```

按提示输入脚本内容，输入 `END` 结束。

## 脚本编写技巧

### ✅ 好的脚本示例

```
Scene 1:
A red dragon with golden scales flies over a medieval castle at sunset. 
The sky is painted in orange and purple hues. The dragon's wings are spread wide.

Dialogue: "I am the guardian of this realm!"
```

**优点**：
- 详细的视觉描述
- 明确的颜色和光照
- 具体的动作和姿态

### ❌ 不好的脚本示例

```
Scene 1:
A dragon flies.

Dialogue: "Hello."
```

**问题**：
- 描述过于简单
- 缺少视觉细节
- 难以生成高质量图像

## 配置调整

### 修改视频时长

编辑 `src/main/resources/application.properties`：

```properties
# 将每个场景的视频时长改为 10 秒
veo.duration.seconds=10
```

### 修改输出目录

```properties
storage.output.dir=./my_videos
```

### 修改 AI 模型

```properties
openai.gpt.model=gpt-4
```

## 常见问题

### Q: 构建失败怎么办？

**A:** 检查以下几点：
1. Java 版本是否为 17+
2. Maven 是否正确安装
3. 网络连接是否正常（需要下载依赖）

### Q: API 调用失败？

**A:** 检查：
1. API Keys 是否正确设置
2. API Keys 是否有效
3. 账户是否有足够的配额
4. 网络是否能访问 API 服务

### Q: 视频生成很慢？

**A:** 这是正常的：
- 每个场景的视频生成需要 2-5 分钟
- 这取决于 Gemini API 的负载
- 可以在配置中调整超时时间

### Q: 生成的视频质量不好？

**A:** 改进建议：
1. 提供更详细的场景描述
2. 明确指定颜色、光照、构图
3. 在第一个场景详细描述角色外观
4. 使用更高级的 AI 模型（如 Gemini Text Model-4）

### Q: 如何查看详细日志？

**A:** 日志文件位于 `logs/animation-agent.YYYY-MM-DD.log`

### Q: 如何清理临时文件？

**A:** 删除 `temp/` 目录：
```bash
rm -rf temp/
```

## 下一步

- 阅读完整的 [README.md](README.md) 了解详细功能
- 查看 [ARCHITECTURE.md](ARCHITECTURE.md) 了解系统架构
- 尝试编写更复杂的脚本
- 调整配置参数优化效果

## 获取帮助

查看命令行帮助：
```bash
./run.sh --help
```

或者：
```bash
java -jar target/script-to-animation-agent-1.0.0.jar --help
```

## 示例输出

成功运行后，您将看到类似的输出：

```
========================================
Animation generation completed successfully!
Output video: output/animation_20260112_143022.mp4
========================================
```

检查输出目录：
```bash
ls -lh output/
```

播放视频：
```bash
# Linux
vlc output/animation_20260112_143022.mp4

# Mac
open output/animation_20260112_143022.mp4

# Windows
start output\animation_20260112_143022.mp4
```

---

**恭喜！** 您已经成功生成了第一个 AI 动画视频！🎉
