# 修复 429 Rate Limit 错误

## 问题描述

在生成关键帧时遇到 429 错误：

```
code: 429
message: Resource exhausted. Please try again later.
status: RESOURCE_EXHAUSTED
```

这表示 API 请求速率超过了配额限制。

---

## 根本原因

### 请求频率过高

项目在短时间内发送了多个 API 请求：

1. **角色生成**: 2 个角色 → 2 个图像生成请求
2. **关键帧生成**: 4 个场景 → 4 个图像生成请求
3. **视频生成**: 4 个场景 → 4 个视频生成请求

总共：**10 个 API 请求**，在几秒钟内连续发送。

### Vertex AI 配额限制

Gemini 2.5 Flash Image 的默认配额：
- **每分钟请求数**: 60 RPM
- **每天请求数**: 1000 RPD

虽然理论上 60 RPM 足够，但实际上可能存在：
- **突发限制**: 短时间内请求过多
- **并发限制**: 同时处理的请求数量
- **区域限制**: 特定区域的配额可能更低

---

## 解决方案

### 1. 添加重试机制

在 `NanoBananaProService.generateImageWithReferences()` 中添加重试逻辑：

```java
// 使用重试机制执行 API 请求
return RetryUtils.executeWithRetry(() -> {
    // 构建请求体
    JsonObject requestBody = buildRequestBody(prompt, referenceImageUrls, aspectRatio, resolution);
    
    // 获取访问令牌
    credentials.refreshIfExpired();
    String accessToken = credentials.getAccessToken().getTokenValue();
    
    // 发送 HTTP 请求
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiEndpoint))
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build();
    
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    
    if (response.statusCode() != 200) {
        String errorMsg = String.format("API request failed with status %d: %s", 
                response.statusCode(), response.body());
        throw new Exception(errorMsg);
    }
    
    // 解析响应
    JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
    return extractImageFromResponse(responseJson);
});
```

### 2. 重试配置

`RetryUtils` 使用指数退避策略：

```properties
# application.properties
max.retry.attempts=5
initial.retry.delay.ms=2000
max.retry.delay.ms=60000
```

**重试时间表**：
- 第 1 次失败：等待 2 秒后重试
- 第 2 次失败：等待 4 秒后重试
- 第 3 次失败：等待 8 秒后重试
- 第 4 次失败：等待 16 秒后重试
- 第 5 次失败：等待 32 秒后重试

**总重试时间**: 最多 62 秒

---

## 修复效果

### 修复前

```
2026-01-13 11:43:49 [main] ERROR c.a.a.w.KeyframeGenerationStep - Failed to generate keyframe for scene 2
java.lang.Exception: API request failed with status 429
```

### 修复后

```
2026-01-13 11:50:00 [main] WARN  c.a.a.util.RetryUtils - API Rate limit exceeded (429). Attempt 1/5 failed. Retrying in 2000 ms...
2026-01-13 11:50:02 [main] INFO  c.a.a.s.NanoBananaProService - Image generated successfully
```

---

## 额外优化建议

### 1. 增加请求间隔

在每个 API 请求之间添加固定延迟：

```java
// 在 KeyframeGenerationStep 中
Thread.sleep(1000); // 1 秒延迟
```

### 2. 使用批处理

将多个请求合并为批处理请求（如果 API 支持）。

### 3. 增加配额

联系 Google Cloud 支持，申请增加 API 配额：
- 访问 [Google Cloud Console](https://console.cloud.google.com/apis/api/aiplatform.googleapis.com/quotas)
- 选择 Vertex AI API
- 请求增加配额

---

## 相关文档

- [Vertex AI Error Code 429](https://cloud.google.com/vertex-ai/generative-ai/docs/error-code-429)
- [Vertex AI Quotas](https://cloud.google.com/vertex-ai/docs/quotas)

---

**修复状态**: ✅ 完成  
**日期**: 2026-01-13
