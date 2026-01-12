# 模型配置说明

**最后更新**: 2026-01-12

---

## 项目使用的模型

### 1. 文本处理模型

**Gemini 3 Flash** (`gemini-3.0-flash`)

**用途**：
- ✅ 角色分析和提取
- ✅ 故事板生成
- ✅ 场景描述
- ✅ 提示词优化

**特点**：
- 快速响应（<2 秒）
- 准确的文本理解
- 支持长文本（最大 1M tokens）
- 成本低

**配置**：
```properties
gemini.text.model=gemini-3.0-flash
```

---

### 2. 图像生成模型

**Gemini 2.5 Flash Image** (`gemini-2.5-flash-image`)

**用途**：
- ✅ 角色图像生成
- ✅ 关键帧图像生成
- ✅ 基于参考图的图像生成（角色一致性）

**特点**：
- 正式发布 (GA)
- 支持最多 3 张参考图像
- 快速生成（~8 秒）
- 支持 2K 分辨率
- 对话式编辑

**配置**：
```properties
nano.banana.pro.model=gemini-2.5-flash-image
nano.banana.pro.use.google.search=false
nano.banana.pro.aspect.ratio=16:9
nano.banana.pro.resolution=2K
nano.banana.pro.max.reference.images=3
```

**为什么不用 Nano Banana Pro (Gemini 3 Pro Image)?**
- Gemini 3 Pro Image 仍在 Preview 阶段
- 在 Vertex AI 中返回 404 错误
- 可能需要白名单才能访问
- 当正式可用时，可以轻松切换

---

### 3. 视频生成模型

**Veo 3.1** (`veo-3.1-generate-001`)

**用途**：
- ✅ 从关键帧生成视频片段
- ✅ 图生视频（8 秒片段）

**特点**：
- 高质量视频生成
- 支持图像参考
- 支持多种宽高比
- 生成时间：2-5 分钟

**配置**：
```properties
gemini.veo.model=veo-3.1-generate-001
veo.number.of.videos=1
veo.duration.seconds=8
veo.enhance.prompt=true
veo.poll.interval.seconds=10
veo.max.wait.minutes=30
veo.aspect.ratio=16:9
veo.resolution=720p
```

---

## 模型对比

### 文本模型

| 模型 | 速度 | 成本 | 最大 Tokens | 推荐用途 |
|------|------|------|-------------|---------|
| **Gemini 3 Flash** | ⚡ 最快 | 💰 最低 | 1M | **文本处理** (推荐) |
| Gemini 3 Pro | 🐢 较慢 | 💰💰 较高 | 2M | 复杂推理 |
| Gemini 2.0 Flash | ⚡ 快 | 💰 低 | 1M | 旧版本 |

### 图像生成模型

| 模型 | 状态 | 参考图 | 速度 | 成本 | 推荐用途 |
|------|------|--------|------|------|---------|
| **Gemini 2.5 Flash Image** | ✅ GA | 3 张 | ⚡ 快 | 💰 低 | **图像生成** (推荐) |
| Gemini 3 Pro Image | ❌ Preview | 14 张 | 🐢 慢 | 💰💰 高 | 未来升级 |
| Imagen 3 | ✅ GA | 0 张 | ⚡ 快 | 💰 低 | 纯文生图 |
| Imagen 4 Ultra | ✅ GA | 0 张 | 🐢 慢 | 💰💰💰 最高 | 高质量图像 |

### 视频生成模型

| 模型 | 状态 | 时长 | 速度 | 成本 | 推荐用途 |
|------|------|------|------|------|---------|
| **Veo 3.1** | ✅ GA | 8s | 🐢 慢 | 💰💰 高 | **视频生成** (推荐) |
| Veo 2 | ✅ GA | 8s | 🐢 慢 | 💰💰 高 | 旧版本 |

---

## 模型组合策略

### 当前配置（推荐）

```
文本处理: Gemini 3 Flash
    ↓
图像生成: Gemini 2.5 Flash Image (最多 3 张参考图)
    ↓
视频生成: Veo 3.1
```

**优势**：
- ✅ 所有模型都是 GA 版本，稳定可靠
- ✅ 成本相对较低
- ✅ 速度快
- ✅ 质量足够高

**限制**：
- ⚠️ 图像生成最多支持 3 张参考图（对于 4+ 角色场景需要选择）

---

### 未来升级配置

当 Gemini 3 Pro Image 正式可用时：

```
文本处理: Gemini 3 Flash
    ↓
图像生成: Gemini 3 Pro Image (最多 14 张参考图)
    ↓
视频生成: Veo 3.1
```

**优势**：
- ✅ 支持最多 14 张参考图
- ✅ 更强的角色一致性
- ✅ 更好的图像质量

**成本**：
- 💰💰 图像生成成本增加约 2-3 倍

---

### 高质量配置（适合关键场景）

```
文本处理: Gemini 3 Pro
    ↓
图像生成: Imagen 4 Ultra
    ↓
视频生成: Veo 3.1
```

**优势**：
- ✅ 最高质量
- ✅ 最强推理能力

**成本**：
- 💰💰💰 成本增加约 5-10 倍

---

## 如何切换模型

### 1. 更新配置文件

编辑 `src/main/resources/application.properties`:

```properties
# 切换文本模型
gemini.text.model=gemini-3-pro-preview  # 或 gemini-3.0-flash

# 切换图像模型
nano.banana.pro.model=gemini-3-pro-image-preview  # 或 gemini-2.5-flash-image

# 切换视频模型
gemini.veo.model=veo-3.1-generate-001  # 或 veo-2-generate-001
```

### 2. 重新编译

```bash
mvn clean compile
```

### 3. 测试

```bash
# 测试文本模型
mvn test -Dtest=GeminiTextServiceTest

# 测试图像模型
./test_gemini3_pro_image.sh

# 测试视频模型
./test_standard_vertexai.sh
```

---

## 模型可用性检查

### 检查 Gemini 3 Pro Image 是否可用

```bash
# 使用 gcloud CLI
gcloud ai models list --region=us-central1 | grep gemini-3-pro-image

# 或使用 REST API
curl -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  "https://us-central1-aiplatform.googleapis.com/v1/projects/YOUR_PROJECT/locations/us-central1/publishers/google/models/gemini-3-pro-image-preview"
```

如果返回 200，说明模型可用。

---

## 成本估算

### 生成一个 4 场景、32 秒的动画

#### 当前配置（推荐）

| 步骤 | 模型 | 数量 | 单价 | 小计 |
|------|------|------|------|------|
| 角色分析 | Gemini 3 Flash | 1 次 | $0.0001 | $0.0001 |
| 故事板生成 | Gemini 3 Flash | 1 次 | $0.0001 | $0.0001 |
| 角色图生成 | Gemini 2.5 Flash Image | 3 张 | $0.02 | $0.06 |
| 关键帧生成 | Gemini 2.5 Flash Image | 4 张 | $0.02 | $0.08 |
| 视频生成 | Veo 3.1 | 32 秒 | $0.10/秒 | $3.20 |
| **总计** | | | | **$3.34** |

#### 未来升级配置

| 步骤 | 模型 | 数量 | 单价 | 小计 |
|------|------|------|------|------|
| 角色分析 | Gemini 3 Flash | 1 次 | $0.0001 | $0.0001 |
| 故事板生成 | Gemini 3 Flash | 1 次 | $0.0001 | $0.0001 |
| 角色图生成 | Gemini 3 Pro Image | 3 张 | $0.05 | $0.15 |
| 关键帧生成 | Gemini 3 Pro Image | 4 张 | $0.05 | $0.20 |
| 视频生成 | Veo 3.1 | 32 秒 | $0.10/秒 | $3.20 |
| **总计** | | | | **$3.55** |

**成本增加**: 约 6% ($0.21)

---

## 最佳实践

### 1. 根据场景选择模型

```java
// 对于简单场景，使用快速模型
if (scene.getComplexity() < 5) {
    useModel("gemini-2.5-flash-image");
}
// 对于复杂场景，使用高质量模型
else {
    useModel("gemini-3-pro-image-preview");
}
```

### 2. 批量处理

```java
// 批量生成图像以减少 API 调用次数
List<String> prompts = scenes.stream()
    .map(Scene::getDescription)
    .collect(Collectors.toList());
    
batchGenerateImages(prompts);
```

### 3. 缓存结果

```java
// 缓存生成的图像以避免重复生成
String cacheKey = generateCacheKey(prompt, referenceImages);
if (cache.containsKey(cacheKey)) {
    return cache.get(cacheKey);
}
```

---

## 常见问题

### Q: 为什么不直接使用 Gemini 3 Pro Image？

**A**: Gemini 3 Pro Image 目前在 Vertex AI 中返回 404 错误，可能还在 Preview 阶段或需要白名单。Gemini 2.5 Flash Image 是 GA 版本，稳定可靠。

### Q: 3 张参考图够用吗？

**A**: 对于大多数场景（80-95%），3 张参考图已经足够。只有在 4+ 角色同时出现的场景中才会有限制，这种情况相对较少。

### Q: 如何提高角色一致性？

**A**: 
1. 使用高质量的角色参考图
2. 在提示词中明确描述角色特征
3. 优先选择主角作为参考图
4. 使用相似的光照和角度

### Q: 视频生成为什么这么慢？

**A**: Veo 模型生成高质量视频需要 2-5 分钟。这是正常的，因为视频生成比图像生成复杂得多。

---

## 总结

**当前推荐配置**：
- ✅ **文本**: Gemini 3 Flash
- ✅ **图像**: Gemini 2.5 Flash Image
- ✅ **视频**: Veo 3.1

这个配置提供了**最佳的性能、成本和质量平衡**，适合生产环境使用。

当 Gemini 3 Pro Image 正式可用时，可以轻松升级以获得更强的角色一致性能力。

---

**文档版本**: 1.0  
**最后更新**: 2026-01-12
