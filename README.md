# Script to Animation Agent

一个基于 Java 的 AI Agent 程序，能够自动将用户提供的脚本转换为动画视频。该系统完全使用 Google Gemini API 进行文本分析、图像生成和视频生成。

## 功能特性

- **智能角色生成**：从脚本中自动提取主要角色并生成角色图像
- **自动分镜处理**：将脚本智能分解为多个场景
- **关键帧生成**：为每个场景生成视觉一致的关键帧图像
- **视频生成**：基于关键帧生成动画视频片段
- **视频拼接**：将所有场景视频合并为完整的动画

## 技术架构

### 核心技术栈

- **Java 17**：主要编程语言
- **Google Gen AI SDK**：用于 Gemini 文本模型、Imagen（图像生成）和 Veo（视频生成）
- **JavaCV (FFmpeg)**：用于视频处理和拼接
- **Maven**：项目构建和依赖管理

### 工作流程

```
用户输入脚本
    ↓
1. 角色生成 (Gemini Text + Gemini Imagen)
   - 提取主要角色描述
   - 生成角色参考图像
    ↓
2. 分镜处理 (Gemini Text)
   - 将脚本分解为场景
   - 生成每个场景的视觉描述
    ↓
3. 关键帧生成 (Gemini Imagen)
   - 基于场景描述生成图像
   - 保持角色视觉一致性
    ↓
4. 视频生成 (Gemini Veo)
   - 从关键帧生成动画视频
   - 每个场景 5 秒视频
    ↓
5. 视频拼接 (FFmpeg)
   - 合并所有场景视频
   - 输出最终动画
```

## 项目结构

```
script-to-animation-agent/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/agent/animation/
│   │   │       ├── agent/              # Agent 主类
│   │   │       │   └── AnimationAgent.java
│   │   │       ├── config/             # 配置管理
│   │   │       │   └── AppConfig.java
│   │   │       ├── dto/                # 数据传输对象
│   │   │       │   ├── Character.java
│   │   │       │   ├── Scene.java
│   │   │       │   ├── ScriptInput.java
│   │   │       │   └── Storyboard.java
│   │   │       ├── service/            # AI 服务层
│   │   │       │   ├── GeminiService.java
│   │   │       │   ├── GeminiTextService.java
│   │   │       │   └── VideoProcessingService.java
│   │   │       ├── workflow/           # 工作流引擎
│   │   │       │   ├── WorkflowEngine.java
│   │   │       │   ├── WorkflowStep.java
│   │   │       │   ├── WorkflowContext.java
│   │   │       │   ├── CharacterGenerationStep.java
│   │   │       │   ├── StoryboardGenerationStep.java
│   │   │       │   ├── KeyframeGenerationStep.java
│   │   │       │   ├── VideoGenerationStep.java
│   │   │       │   └── VideoMergingStep.java
│   │   │       └── Main.java           # 程序入口
│   │   └── resources/
│   │       ├── application.properties  # 应用配置
│   │       └── logback.xml            # 日志配置
│   └── test/
│       └── java/                       # 测试代码
├── pom.xml                             # Maven 配置
├── example_script.txt                  # 示例脚本
└── README.md                           # 项目文档
```

## 环境要求

- **Java 17** 或更高版本
- **Maven 3.6+**
- **API Keys**：
  - Google Gemini API Key

## 安装与配置

### 1. 克隆或下载项目

```bash
cd script-to-animation-agent
```

### 2. 配置 API Keys

设置环境变量：

```bash
export GEMINI_API_KEY="your-gemini-api-key"
```

或者在 Windows 上：

```cmd
set GEMINI_API_KEY=your-gemini-api-key
```

**注意**：本项目使用 **Vertex AI Express Mode**，支持使用 API Key 访问 Vertex AI 服务。这对于无法直接访问 Google AI Studio 的地区特别有用。

### 配置阿里云 OSS（必需）

本项目需要阿里云 OSS 来存储图片，以便 Gemini Veo API 可以通过 URL 访问图片生成视频。

```bash
export ALIYUN_OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"
export ALIYUN_OSS_ACCESS_KEY_ID="your-access-key-id"
export ALIYUN_OSS_ACCESS_KEY_SECRET="your-access-key-secret"
export ALIYUN_OSS_BUCKET_NAME="your-bucket-name"
```

详细配置步骤请参考 [OSS_SETUP.md](OSS_SETUP.md)。

### 3. 构建项目

```bash
mvn clean package
```

这将下载所有依赖并构建可执行的 JAR 文件。

## 使用方法

### 方式 1：交互式模式

直接运行程序，按提示输入脚本：

```bash
java -jar target/script-to-animation-agent-1.0.0.jar
```

### 方式 2：从文件读取

```bash
java -jar target/script-to-animation-agent-1.0.0.jar -f example_script.txt
```

### 方式 3：直接提供脚本

```bash
java -jar target/script-to-animation-agent-1.0.0.jar "Your script content here"
```

### 查看帮助

```bash
java -jar target/script-to-animation-agent-1.0.0.jar --help
```

## 配置说明

配置文件位于 `src/main/resources/application.properties`，可以调整以下参数：

### AI 模型配置

```properties
# Gemini 模型
gemini.imagen.model=imagen-3.0-generate-002
gemini.veo.model=veo-2.0-generate-001

```

### 图像生成配置

```properties
imagen.number.of.images=1
imagen.output.mime.type=image/jpeg
```

### 视频生成配置

```properties
veo.number.of.videos=1
veo.duration.seconds=5
veo.enhance.prompt=true
veo.poll.interval.seconds=10
veo.max.wait.minutes=30
```

### 存储配置

```properties
storage.temp.dir=./temp
storage.output.dir=./output
```

### 视频处理配置

```properties
video.output.format=mp4
video.frame.rate=30
video.codec=h264
```

## 输出说明

- **临时文件**：存储在 `./temp` 目录
  - 角色图像：`character_main.jpg`
  - 关键帧图像：`keyframe_scene_N.jpg`
  - 场景视频：`video_scene_N.mp4`

- **最终输出**：存储在 `./output` 目录
  - 格式：`animation_YYYYMMDD_HHMMSS.mp4`

- **日志文件**：存储在 `./logs` 目录
  - 格式：`animation-agent.YYYY-MM-DD.log`

## 示例脚本

项目包含一个示例脚本 `example_script.txt`，描述了一个小机器人的冒险故事。您可以使用此脚本测试系统：

```bash
java -jar target/script-to-animation-agent-1.0.0.jar -f example_script.txt
```

## 脚本编写指南

为了获得最佳效果，建议按以下格式编写脚本：

```
Title: Your Story Title

Scene 1:
[场景描述，包括环境、角色外观、动作等]

Dialogue: "[对话内容]"

Scene 2:
[下一个场景的描述]

Dialogue: "[对话内容]"

...
```

### 脚本编写要点

1. **详细的视觉描述**：包括颜色、光照、构图等细节
2. **角色一致性**：在第一个场景中详细描述主要角色的外观
3. **场景分明**：每个场景应该是一个独立的视觉单元
4. **适当长度**：建议 3-10 个场景，每个场景 5 秒视频

## 性能说明

- **角色生成**：约 10-30 秒
- **分镜处理**：约 5-15 秒
- **关键帧生成**：每个场景约 10-30 秒
- **视频生成**：每个场景约 2-5 分钟（取决于 API 负载）
- **视频拼接**：约 5-15 秒

总体时间取决于场景数量和 API 响应速度。

## 故障排除

### API 调用失败

- 检查 API Keys 是否正确设置
- 确认 API 配额是否充足
- 检查网络连接

### 视频生成超时

- 增加 `veo.max.wait.minutes` 配置值
- 检查 Gemini API 服务状态

### 内存不足

- 增加 JVM 堆内存：`java -Xmx4g -jar ...`
- 减少同时处理的场景数量

### 视频拼接失败

- 确保所有场景视频都已成功生成
- 检查临时目录的磁盘空间

## 依赖说明

主要依赖库：

- `com.google.genai:google-genai:1.34.0` - Google Gen AI SDK

- `org.bytedeco:javacv-platform:1.5.10` - JavaCV (FFmpeg)
- `com.google.code.gson:gson:2.10.1` - JSON 处理
- `ch.qos.logback:logback-classic:1.4.11` - 日志框架

## 扩展开发

### 添加自定义工作流步骤

```java
public class CustomStep implements WorkflowStep {
    @Override
    public void execute(WorkflowContext context) throws Exception {
        // 自定义逻辑
    }

    @Override
    public String getStepName() {
        return "Custom Step";
    }
}

// 在 AnimationAgent 中添加
agent.addWorkflowStep(new CustomStep());
```

### 自定义 AI 模型

修改 `application.properties` 中的模型配置，或在代码中直接指定模型名称。

## 许可证

Apache License 2.0

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系。

---

**注意**：本项目需要有效的 Google Gemini API Key。视频生成是计算密集型操作，可能产生 API 使用费用。请合理使用并监控您的 API 配额。
