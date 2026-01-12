# 架构设计文档

## 系统概述

Script to Animation Agent 是一个基于 Java 的智能系统，使用 AI 技术将文本脚本自动转换为动画视频。系统采用模块化设计，通过工作流引擎协调多个处理步骤。

## 核心设计原则

1. **模块化**：各功能模块独立，易于维护和扩展
2. **可扩展性**：通过工作流步骤接口支持自定义处理流程
3. **错误处理**：完善的异常处理和日志记录
4. **配置化**：关键参数通过配置文件管理

## 系统架构

### 分层架构

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│         (Main.java)                     │
│  - 命令行界面                            │
│  - 参数解析                              │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│         Agent Layer                     │
│         (AnimationAgent)                │
│  - 工作流协调                            │
│  - 资源管理                              │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│         Workflow Layer                  │
│         (WorkflowEngine)                │
│  - 步骤执行                              │
│  - 上下文管理                            │
│  - 状态跟踪                              │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│         Service Layer                   │
│  - GeminiService                        │
│  - GeminiService                        │
│  - VideoProcessingService               │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│         External APIs                   │
│  - Google Gemini API                    │
│  - Gemini API                           │
│  - FFmpeg                               │
└─────────────────────────────────────────┘
```

## 核心组件

### 1. Agent Layer

#### AnimationAgent
- **职责**：系统的主要入口点，负责初始化和协调整个动画生成流程
- **功能**：
  - 初始化工作流引擎
  - 管理系统资源
  - 执行动画生成流程
  - 处理结果和错误

### 2. Workflow Layer

#### WorkflowEngine
- **职责**：工作流执行引擎
- **功能**：
  - 管理工作流步骤
  - 按序执行步骤
  - 跟踪执行状态
  - 记录执行时间

#### WorkflowStep (接口)
- **职责**：定义工作流步骤的标准接口
- **方法**：
  - `execute(WorkflowContext)`: 执行步骤逻辑
  - `getStepName()`: 获取步骤名称

#### WorkflowContext
- **职责**：在工作流步骤间传递数据
- **内容**：
  - 脚本输入
  - 角色信息
  - 分镜数据
  - 最终视频路径
  - 执行时间统计

### 3. Workflow Steps

#### CharacterGenerationStep
- **职责**：生成主要角色
- **流程**：
  1. 使用 Gemini 提取角色描述
  2. 使用 Gemini Imagen 生成角色图像
  3. 保存角色信息到上下文

#### StoryboardGenerationStep
- **职责**：生成分镜
- **流程**：
  1. 使用 Gemini 分解脚本为场景
  2. 解析 JSON 响应
  3. 为每个场景生成视觉描述
  4. 保存分镜到上下文

#### KeyframeGenerationStep
- **职责**：生成关键帧图像
- **流程**：
  1. 遍历所有场景
  2. 使用 Gemini Imagen 生成关键帧
  3. 参考角色图像保持一致性
  4. 保存关键帧路径到场景

#### VideoGenerationStep
- **职责**：生成视频片段
- **流程**：
  1. 遍历所有场景
  2. 使用 Gemini Veo 从关键帧生成视频
  3. 轮询等待视频生成完成
  4. 保存视频路径到场景

#### VideoMergingStep
- **职责**：合并视频片段
- **流程**：
  1. 收集所有场景视频路径
  2. 使用 FFmpeg 合并视频
  3. 保存最终视频路径到上下文

### 4. Service Layer

#### GeminiService
- **职责**：封装 Google Gemini API 调用
- **功能**：
  - 文本生成图像
  - 带参考的图像生成
  - 文本生成视频
  - 图像生成视频
  - 异步操作轮询

#### GeminiService
- **职责**：封装 Gemini API 调用
- **功能**：
  - 提取角色描述
  - 生成分镜
  - 生成视觉描述
  - 文本补全

#### VideoProcessingService
- **职责**：视频处理和编辑
- **功能**：
  - 合并多个视频
  - 获取视频信息
  - 文件操作

### 5. Configuration Layer

#### AppConfig
- **职责**：配置管理
- **功能**：
  - 加载配置文件
  - 读取环境变量
  - 提供配置访问接口

### 6. Data Transfer Objects

#### ScriptInput
- **内容**：脚本内容、标题、描述

#### Character
- **内容**：角色名称、描述、图像路径

#### Scene
- **内容**：场景编号、描述、视觉描述、对话、关键帧路径、视频路径

#### Storyboard
- **内容**：场景列表

## 数据流

```
用户输入
    ↓
ScriptInput
    ↓
WorkflowContext
    ↓
CharacterGenerationStep → Character + Image
    ↓
StoryboardGenerationStep → Storyboard (Scenes)
    ↓
KeyframeGenerationStep → Scenes + Keyframe Images
    ↓
VideoGenerationStep → Scenes + Videos
    ↓
VideoMergingStep → Final Video
    ↓
输出路径
```

## 错误处理策略

### 1. 异常传播
- 每个步骤抛出异常时，工作流引擎捕获并记录
- 失败的步骤导致整个工作流停止

### 2. 日志记录
- 使用 SLF4J + Logback
- 不同级别的日志：DEBUG, INFO, WARN, ERROR
- 日志文件按日期滚动

### 3. 资源清理
- 使用 try-catch-finally 确保资源释放
- 临时文件在失败时保留用于调试

## 性能优化

### 1. 异步处理
- 视频生成使用轮询机制
- 可配置的轮询间隔和超时时间

### 2. 资源管理
- 临时文件存储在独立目录
- 最终输出存储在输出目录
- 支持手动清理临时文件

### 3. 并发控制
- 当前实现为串行处理
- 未来可扩展为并行处理多个场景

## 扩展点

### 1. 自定义工作流步骤
实现 `WorkflowStep` 接口并添加到工作流引擎：

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
```

### 2. 自定义 AI 模型
修改服务类中的模型调用逻辑，支持不同的 AI 提供商。

### 3. 自定义视频处理
扩展 `VideoProcessingService`，添加更多视频编辑功能（如转场效果、字幕等）。

### 4. 批处理支持
扩展 `AnimationAgent`，支持批量处理多个脚本。

## 配置管理

### 配置文件结构

```properties
# AI 模型配置
gemini.api.key=${GEMINI_API_KEY}
openai.api.key=${OPENAI_API_KEY}
gemini.imagen.model=imagen-3.0-generate-002
gemini.veo.model=veo-2.0-generate-001
openai.gpt.model=gpt-4.1-mini

# 生成参数
imagen.number.of.images=1
veo.duration.seconds=5

# 存储配置
storage.temp.dir=./temp
storage.output.dir=./output

# 视频处理
video.output.format=mp4
video.frame.rate=30
```

### 环境变量优先级
1. 环境变量（最高优先级）
2. 配置文件
3. 默认值

## 安全考虑

### 1. API Key 管理
- 通过环境变量传递
- 不在代码中硬编码
- 不提交到版本控制

### 2. 输入验证
- 验证脚本内容非空
- 验证文件路径有效性
- 防止路径遍历攻击

### 3. 资源限制
- 配置最大等待时间
- 限制生成的视频数量
- 监控磁盘空间使用

## 测试策略

### 1. 单元测试
- 测试各个服务类的方法
- 模拟 API 响应

### 2. 集成测试
- 测试工作流步骤的集成
- 使用测试 API Keys

### 3. 端到端测试
- 使用示例脚本完整测试
- 验证输出视频质量

## 部署建议

### 1. 环境准备
- Java 17 运行时
- 足够的磁盘空间（至少 10GB）
- 稳定的网络连接

### 2. 资源配置
- 推荐内存：4GB+
- 推荐 CPU：4 核+
- 推荐磁盘：SSD

### 3. 监控
- 监控 API 调用次数
- 监控磁盘使用情况
- 监控处理时间

## 未来改进方向

1. **并行处理**：支持多场景并行生成
2. **缓存机制**：缓存角色图像和关键帧
3. **增量生成**：支持修改部分场景后重新生成
4. **Web 界面**：提供 Web UI 替代命令行
5. **云部署**：支持容器化部署
6. **多语言支持**：支持多种语言的脚本
7. **高级编辑**：添加转场效果、背景音乐、字幕等
8. **质量控制**：自动评估生成内容质量并重试
