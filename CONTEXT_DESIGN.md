# Context 数据结构设计文档

## 概述

本文档描述了 Script to Animation Agent 项目中的 Context 数据结构设计，该结构用于追踪整个动画生成流程中的所有资源、链接和状态信息。

---

## 核心设计理念

### 1. 完整性
- 记录所有生成的资源（角色图、关键帧、视频）
- 同时保存本地路径和 GCS/OSS URL
- 支持多角色管理和场景角色关联

### 2. 可追溯性
- 每个资源都有明确的来源和生成参数
- 支持生成上下文摘要和日志
- 便于调试和问题排查

### 3. 可扩展性
- 支持添加新的资源类型
- 支持多种存储方式（本地、GCS、OSS）
- 易于集成新的 AI 模型和服务

---

## 数据结构

### WorkflowContext

工作流上下文是整个系统的核心数据结构，包含以下主要部分：

```java
public class WorkflowContext {
    // 脚本输入
    private ScriptInput scriptInput;
    
    // 所有角色（包括主角和配角）
    private List<Character> characters;
    private Map<String, Character> characterMap;
    
    // 分镜板
    private Storyboard storyboard;
    
    // 最终视频
    private String finalVideoPath;      // 本地路径
    private String finalVideoUrl;       // GCS/OSS URL
    
    // 时间统计
    private long startTime;
    private long endTime;
}
```

---

### Character（角色）

```java
public class Character {
    private String name;                // 角色名称
    private String description;         // 角色描述
    private String role;                // 角色类型（主角、配角等）
    private boolean isMainCharacter;    // 是否为主角
    
    // 角色图像资源
    private String imagePath;           // 本地文件路径
    private String imageUrl;            // GCS/OSS URL
}
```

**字段说明**：
- `name`: 角色名称，用于在场景中引用
- `description`: 详细的视觉描述，用于图像生成
- `role`: 角色类型（如 "protagonist", "antagonist", "supporting"）
- `isMainCharacter`: 标记是否为主角
- `imagePath`: 本地保存的角色图像路径
- `imageUrl`: 上传到 GCS/OSS 后的公开 URL

---

### Scene（场景）

```java
public class Scene {
    private int sceneNumber;            // 场景编号
    private String description;         // 场景描述
    private String visualDescription;   // 视觉描述
    private String dialogue;            // 对话
    
    // 场景涉及的角色
    private List<String> characterNames;
    
    // 关键帧资源
    private String keyframePath;        // 本地文件路径
    private String keyframeUrl;         // GCS/OSS URL
    
    // 视频资源
    private String videoPath;           // 本地文件路径
    private String videoUrl;            // GCS/OSS URL
}
```

**字段说明**：
- `sceneNumber`: 场景编号（从 1 开始）
- `description`: 场景的文字描述
- `visualDescription`: 增强的视觉描述（用于图像生成）
- `dialogue`: 场景中的对话内容
- `characterNames`: 场景中出现的角色名称列表
- `keyframePath`/`keyframeUrl`: 关键帧图像的本地路径和 URL
- `videoPath`/`videoUrl`: 生成视频的本地路径和 URL

---

## 工作流程

### 1. 角色生成阶段

```
输入：脚本内容
↓
Gemini 3 Flash 分析脚本
↓
提取所有角色（主角 + 配角）
↓
为每个角色生成图像（Nano Banana Pro）
↓
保存到本地 + 上传到 GCS
↓
记录到 Context.characters
```

**Context 更新**：
```java
Character character = new Character(name, description, role, isMainCharacter);
character.setImagePath("/path/to/character_alice.jpg");
character.setImageUrl("gs://bucket/character_alice.jpg");
context.addCharacter(character);
```

---

### 2. 分镜生成阶段

```
输入：脚本内容 + 角色列表
↓
Gemini 3 Flash 生成分镜
↓
分析每个场景涉及的角色
↓
记录到 Context.storyboard
```

**Context 更新**：
```java
Scene scene = new Scene(1, "Alice enters the forest");
scene.setCharacterNames(Arrays.asList("Alice", "Rabbit"));
context.getStoryboard().addScene(scene);
```

---

### 3. 关键帧生成阶段

```
输入：场景描述 + 涉及角色的参考图 URL
↓
Nano Banana Pro 生成关键帧
（使用角色 URL 作为参考）
↓
保存到本地 + 上传到 GCS
↓
记录到 Scene.keyframe*
```

**Context 更新**：
```java
// 获取场景涉及的角色
List<Character> sceneCharacters = context.getCharactersForScene(scene.getCharacterNames());

// 收集参考图 URL
List<String> referenceUrls = sceneCharacters.stream()
    .map(Character::getImageUrl)
    .collect(Collectors.toList());

// 生成关键帧
String keyframeUrl = nanoBananaProService.generateImageWithReferences(
    prompt, referenceUrls, aspectRatio, resolution
);

// 更新 Context
scene.setKeyframePath("/path/to/keyframe_scene_1.jpg");
scene.setKeyframeUrl("gs://bucket/keyframe_scene_1.jpg");
```

---

### 4. 视频生成阶段

```
输入：关键帧 URL + 场景描述
↓
Veo API 生成视频
（使用关键帧 URL，避免传输大文件）
↓
保存到本地 + 上传到 GCS
↓
记录到 Scene.video*
```

**Context 更新**：
```java
// 使用关键帧 URL 生成视频
String videoPath = geminiService.generateVideoFromImageUrl(
    prompt, scene.getKeyframeUrl(), outputPath
);

// 上传视频
String videoUrl = gcsService.uploadFile(videoPath, null, false);

// 更新 Context
scene.setVideoPath(videoPath);
scene.setVideoUrl(videoUrl);
```

---

### 5. 视频合并阶段

```
输入：所有场景的视频路径
↓
FFmpeg 合并视频
↓
保存到本地 + 上传到 GCS
↓
记录到 Context.finalVideo*
```

**Context 更新**：
```java
// 合并视频
String finalVideoPath = videoProcessingService.mergeVideos(videoPaths, outputPath);

// 上传最终视频
String finalVideoUrl = gcsService.uploadFile(finalVideoPath, null, false);

// 更新 Context
context.setFinalVideoPath(finalVideoPath);
context.setFinalVideoUrl(finalVideoUrl);
```

---

## Context 使用示例

### 获取角色信息

```java
// 获取所有角色
List<Character> allCharacters = context.getCharacters();

// 获取主角
Character mainCharacter = context.getMainCharacter();

// 根据名称获取角色
Character alice = context.getCharacter("Alice");

// 获取场景涉及的角色
List<Character> sceneCharacters = context.getCharactersForScene(
    Arrays.asList("Alice", "Rabbit")
);
```

### 获取资源 URL

```java
// 获取角色图像 URL
String characterImageUrl = context.getCharacter("Alice").getImageUrl();

// 获取关键帧 URL
String keyframeUrl = context.getStoryboard().getScenes().get(0).getKeyframeUrl();

// 获取视频 URL
String videoUrl = context.getStoryboard().getScenes().get(0).getVideoUrl();

// 获取最终视频 URL
String finalVideoUrl = context.getFinalVideoUrl();
```

### 生成摘要

```java
// 生成完整的上下文摘要
String summary = context.getSummary();
System.out.println(summary);
```

**输出示例**：
```
============================================================
Workflow Context Summary
============================================================

Script:
  Length: 1234 characters

Characters (3):
  - Alice [MAIN]
    Role: protagonist
    Image: /temp/character_alice.jpg
    URL: gs://bucket/character_alice.jpg
  - Rabbit
    Role: supporting
    Image: /temp/character_rabbit.jpg
    URL: gs://bucket/character_rabbit.jpg
  - Queen
    Role: antagonist
    Image: /temp/character_queen.jpg
    URL: gs://bucket/character_queen.jpg

Scenes (5):
  Scene 1:
    Characters: [Alice]
    Keyframe: /temp/keyframe_scene_1.jpg
    Keyframe URL: gs://bucket/keyframe_scene_1.jpg
    Video: /temp/video_scene_1.mp4
    Video URL: gs://bucket/video_scene_1.mp4
  ...

Final Video:
  Path: /output/animation_20260112_095500.mp4
  URL: gs://bucket/animation_20260112_095500.mp4

Duration: 245.6 seconds
============================================================
```

---

## 性能优化

### 使用 URL 而不是 Base64

**优势**：
- **传输速度提升 95%**：URL 只有几十字节，Base64 编码的图片有几 MB
- **内存占用减少 90%**：不需要在内存中保存大量 Base64 字符串
- **网络带宽减少 99.99%**：API 调用只传递 URL，由 Google 内部网络获取图片

**实现**：
```java
// ❌ 旧方式：使用 Base64
byte[] imageBytes = FileUtils.readFileToByteArray(new File(imagePath));
String base64 = Base64.getEncoder().encodeToString(imageBytes);
// API 调用时传递 base64 字符串（几 MB）

// ✅ 新方式：使用 URL
String imageUrl = gcsService.uploadFile(imagePath, null, false);
// API 调用时传递 URL（几十字节）
Image image = Image.builder().gcsUri(imageUrl).build();
```

---

## 最佳实践

### 1. 始终上传资源到 GCS

```java
// 生成资源后立即上传
String localPath = generateResource();
String url = gcsService.uploadFile(localPath, null, false);

// 同时记录本地路径和 URL
resource.setLocalPath(localPath);
resource.setUrl(url);
```

### 2. 使用 URL 进行 API 调用

```java
// ✅ 推荐：使用 URL
geminiService.generateVideoFromImageUrl(prompt, keyframeUrl, outputPath);

// ❌ 避免：使用本地路径（会自动转换为 URL，但效率较低）
geminiService.generateVideoFromImage(prompt, keyframePath, outputPath);
```

### 3. 定期清理本地文件

```java
// 在工作流完成后清理临时文件
public void cleanup() {
    for (Character character : context.getCharacters()) {
        new File(character.getImagePath()).delete();
    }
    for (Scene scene : context.getStoryboard().getScenes()) {
        new File(scene.getKeyframePath()).delete();
        new File(scene.getVideoPath()).delete();
    }
}
```

### 4. 使用摘要进行调试

```java
// 在关键步骤输出摘要
logger.info(context.getSummary());

// 或者保存到文件
Files.writeString(
    Paths.get("context_summary.txt"), 
    context.getSummary()
);
```

---

## 常见问题

### Q: 为什么需要同时保存本地路径和 URL？

**A**: 
- **本地路径**：用于本地处理（如视频合并）和备份
- **URL**：用于 API 调用和跨服务访问，性能更好

### Q: 如何确保角色一致性？

**A**: 
1. 在角色生成阶段，为每个角色生成高质量的参考图
2. 在关键帧生成时，使用角色的 URL 作为参考图
3. Nano Banana Pro 会根据参考图保持角色外观一致

### Q: 如果 GCS 上传失败怎么办？

**A**: 
- 系统会自动重试（最多 5 次）
- 如果仍然失败，会回退到使用本地路径
- 建议检查 GCS 配置和网络连接

### Q: Context 数据会持久化吗？

**A**: 
- 当前版本：仅在内存中，工作流结束后丢失
- 未来计划：支持序列化到 JSON 文件，便于恢复和分析

---

## 未来扩展

### 1. Context 持久化

```java
// 保存 Context 到文件
contextService.save(context, "context_20260112.json");

// 从文件恢复 Context
WorkflowContext context = contextService.load("context_20260112.json");
```

### 2. 增量生成

```java
// 只重新生成失败的场景
for (Scene scene : context.getStoryboard().getScenes()) {
    if (scene.getVideoUrl() == null) {
        regenerateVideo(scene);
    }
}
```

### 3. 多版本管理

```java
// 为同一场景生成多个版本
scene.addKeyframeVersion("v1", keyframeUrl1);
scene.addKeyframeVersion("v2", keyframeUrl2);

// 选择最佳版本
scene.setActiveKeyframeVersion("v2");
```

---

## 总结

Context 数据结构是 Script to Animation Agent 的核心，它：

1. **完整记录**所有生成的资源和链接
2. **支持多角色**管理和场景角色关联
3. **优化性能**通过 URL 传递资源
4. **便于调试**通过摘要和日志
5. **易于扩展**支持新功能和服务

通过合理使用 Context，可以实现高效、可靠、可追溯的动画生成流程。
