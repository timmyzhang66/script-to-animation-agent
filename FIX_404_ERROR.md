# 修复 Gemini 2.5 Flash Image 404 错误

## 问题描述

```
2026-01-13 11:17:11 [main] ERROR c.a.a.w.CharacterGenerationStep - Failed to generate image for character: Cat
java.lang.Exception: API request failed with status 404
The requested URL /v1/projects/gen-lang-client-0352724605/locations/global/publishers/google/models/gemini-2.5-flash-image:generateContent was not found on this server.
```

## 根本原因

错误的 URL 中使用了 `locations/global`，但 **Gemini 2.5 Flash Image 不支持 `global` 区域**。

根据 [Vertex AI 文档](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash-image)，Gemini 2.5 Flash Image 支持的区域包括：
- ✅ `us-central1`
- ✅ `us-east1`
- ✅ `us-west1`
- ✅ `europe-west1`
- ❌ **不支持 `global`**

## 修复方案

### 1. 更新配置文件

确保 `application.properties` 中使用正确的区域：

```properties
# Google Cloud Platform Configuration
gcp.project.id=gen-lang-client-0352724605
gcp.location=us-central1  # ✅ 使用具体区域，不要使用 global
gcp.service.account.key.path=./gcp-service-account.json
```

### 2. API 端点格式

正确的 API 端点格式：
```
https://us-central1-aiplatform.googleapis.com/v1/projects/PROJECT_ID/locations/us-central1/publishers/google/models/gemini-2.5-flash-image:generateContent
```

错误的格式（使用 global）：
```
https://global-aiplatform.googleapis.com/v1/projects/PROJECT_ID/locations/global/publishers/google/models/gemini-2.5-flash-image:generateContent
```

### 3. 验证步骤

1. **检查配置**：
   ```bash
   grep "gcp.location" src/main/resources/application.properties
   ```
   应该显示：`gcp.location=us-central1`

2. **重新编译**：
   ```bash
   mvn clean package -DskipTests
   ```

3. **运行测试**：
   ```bash
   java -jar target/script-to-animation-agent-1.0.0-shaded.jar example_script.txt
   ```

## Gemini 2.5 Flash Image 特性

根据官方文档：

| 特性 | 值 |
|------|-----|
| **模型 ID** | `gemini-2.5-flash-image` |
| **输入** | 文本、图像 |
| **输出** | 文本和图像 |
| **最大参考图像** | 3 张 |
| **支持的宽高比** | 1:1, 3:2, 2:3, 3:4, 4:3, 4:5, 5:4, 9:16, 16:9, 21:9 |
| **支持的区域** | us-central1, us-east1, us-west1, europe-west1 等 |
| **API 端点** | `:generateContent` |

## 常见问题

### Q: 为什么不能使用 `global` 区域？

A: Gemini 2.5 Flash Image 是一个较新的模型，目前只在特定区域部署。`global` 端点不支持这个模型。

### Q: 如何选择区域？

A: 选择离您最近的区域以获得最佳性能：
- 美国用户：`us-central1` 或 `us-west1`
- 欧洲用户：`europe-west1`
- 亚洲用户：目前需要使用美国或欧洲区域

### Q: 是否需要更改其他配置？

A: 不需要。只需确保 `gcp.location` 设置为支持的区域即可。

## 参考文档

- [Gemini 2.5 Flash Image 文档](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash-image)
- [Vertex AI 区域列表](https://cloud.google.com/vertex-ai/docs/general/locations)
- [Gemini API 参考](https://cloud.google.com/vertex-ai/docs/generative-ai/model-reference/gemini)

---

**修复日期**: 2026-01-13  
**状态**: ✅ 已修复
