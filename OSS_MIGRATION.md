# OSS 迁移文档

**日期**: 2026-01-12  
**版本**: v1.3.0

---

## 📋 迁移概述

本次更新将项目的文件存储从 **Google Cloud Storage (GCS)** 完全迁移到 **阿里云 OSS**。

### 迁移原因

1. **统一存储方案**: 项目现在完全使用阿里云 OSS，避免混用多个云存储服务
2. **降低复杂度**: 移除 GCS 相关代码和配置，简化项目结构
3. **成本优化**: 使用单一云服务商可以更好地管理成本
4. **更好的国内访问**: 阿里云 OSS 在国内的访问速度更快

---

## 🔧 主要变更

### 1. 移除的文件

- ❌ `src/main/java/com/agent/animation/service/GCSService.java`

### 2. 更新的文件

#### 服务类
- ✅ `GeminiService.java` - 移除 GCSService 依赖，使用 OSSService
- ✅ `CharacterGenerationStep.java` - 使用 OSSService 上传角色图
- ✅ `KeyframeGenerationStep.java` - 使用 OSSService 上传关键帧
- ✅ `VideoGenerationStep.java` - 使用 OSSService 上传视频
- ✅ `VideoMergingStep.java` - 使用 OSSService 上传最终视频

#### 配置文件
- ✅ `application.properties` - 移除 GCS 配置，保留 OSS 配置

#### DTO 类
- ✅ `Character.java` - `imageUrl` 字段现在存储 OSS URL
- ✅ `Scene.java` - `keyframeUrl` 和 `videoUrl` 字段现在存储 OSS URL
- ✅ `WorkflowContext.java` - `finalVideoUrl` 字段现在存储 OSS URL

---

## 📝 代码变更详情

### 1. GeminiService

**变更前**:
```java
private final GCSService gcsService;

public GeminiService() {
    this.gcsService = new GCSService();
}

// 上传到 GCS
imageUrl = gcsService.uploadFile(keyframeImageUrl, null, false);

// 使用 GCS URI
Image keyframeImage = Image.builder()
        .gcsUri(imageUrl)
        .build();
```

**变更后**:
```java
private final OSSService ossService;

public GeminiService() {
    this.ossService = new OSSService();
}

// 上传到 OSS
imageUrl = ossService.uploadFile(keyframeImageUrl, null);

// 使用 imageBytes（因为 Vertex AI 不支持 HTTP URL）
byte[] imageBytes = Files.readAllBytes(Paths.get(keyframeImageUrl));
Image keyframeImage = Image.builder()
        .imageBytes(imageBytes)
        .mimeType("image/jpeg")
        .build();
```

### 2. CharacterGenerationStep

**变更前**:
```java
import com.agent.animation.service.GCSService;

private final GCSService gcsService;

// 上传到 GCS
String characterImageUrl = gcsService.uploadFile(characterImagePath, null, false);
logger.info("  GCS URL: {}", characterImageUrl);
```

**变更后**:
```java
import com.agent.animation.service.OSSService;

private final OSSService ossService;

// 上传到 OSS
String characterImageUrl = ossService.uploadFile(characterImagePath, null);
logger.info("  OSS URL: {}", characterImageUrl);
```

### 3. KeyframeGenerationStep

**变更前**:
```java
// 上传到 GCS
String keyframeUrl = gcsService.uploadFile(keyframePath, null, false);
```

**变更后**:
```java
// 上传到 OSS
String keyframeUrl = ossService.uploadFile(keyframePath, null);
```

### 4. VideoGenerationStep

**变更前**:
```java
// 上传视频到 GCS
String videoUrl = gcsService.uploadFile(videoPath, null, false);
```

**变更后**:
```java
// 上传视频到 OSS
String videoUrl = ossService.uploadFile(videoPath, null);
```

### 5. VideoMergingStep

**变更前**:
```java
// 上传最终视频到 GCS
String finalVideoUrl = gcsService.uploadFile(outputPath, null, false);
```

**变更后**:
```java
// 上传最终视频到 OSS
String finalVideoUrl = ossService.uploadFile(outputPath, null);
```

---

## ⚙️ 配置变更

### application.properties

**移除**:
```properties
# Google Cloud Storage Configuration
gcs.bucket.name=my-animation-assets-123
```

**保留**:
```properties
# Aliyun OSS Configuration
# All OSS configuration values are read from environment variables:
# - ALIYUN_OSS_ENDPOINT: OSS endpoint (e.g., oss-cn-hangzhou.aliyuncs.com)
# - ALIYUN_OSS_ACCESS_KEY_ID: Access Key ID
# - ALIYUN_OSS_ACCESS_KEY_SECRET: Access Key Secret
# - ALIYUN_OSS_BUCKET_NAME: Bucket name
```

---

## 🚀 使用指南

### 1. 环境变量配置

在运行项目前，需要设置以下环境变量：

```bash
export ALIYUN_OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"
export ALIYUN_OSS_ACCESS_KEY_ID="your-access-key-id"
export ALIYUN_OSS_ACCESS_KEY_SECRET="your-access-key-secret"
export ALIYUN_OSS_BUCKET_NAME="your-bucket-name"
```

### 2. OSS Bucket 配置

确保您的 OSS Bucket 已正确配置：

1. **创建 Bucket**:
   - 登录阿里云 OSS 控制台
   - 创建一个新的 Bucket
   - 选择合适的区域（建议：华东1-杭州）

2. **设置权限**:
   - Bucket 权限：私有（推荐）或公共读
   - 如果设置为私有，需要使用签名 URL 访问

3. **配置 CORS**（如果需要浏览器访问）:
   ```xml
   <CORSRule>
     <AllowedOrigin>*</AllowedOrigin>
     <AllowedMethod>GET</AllowedMethod>
     <AllowedMethod>HEAD</AllowedMethod>
     <AllowedHeader>*</AllowedHeader>
   </CORSRule>
   ```

### 3. 测试 OSS 连接

```bash
# 编译项目
mvn clean compile

# 运行测试（如果有）
mvn test

# 运行完整流程
java -jar target/script-to-animation-agent-1.0.0.jar example_script.txt
```

---

## 🔍 技术细节

### Vertex AI 与 OSS URL 的兼容性

**问题**: Vertex AI 的 Veo API 只支持 GCS URI (`gs://bucket/object`)，不支持 HTTP URL。

**解决方案**: 
1. 将图片上传到 OSS 获取 HTTP URL
2. 在调用 Veo API 时，从本地文件读取图片字节
3. 使用 `imageBytes` 而不是 `gcsUri` 创建 Image 对象

```java
// 读取本地文件
byte[] imageBytes = Files.readAllBytes(Paths.get(localPath));

// 创建 Image 对象
Image keyframeImage = Image.builder()
        .imageBytes(imageBytes)
        .mimeType("image/jpeg")
        .build();
```

### OSSService API

```java
// 上传文件
String url = ossService.uploadFile(localFilePath, objectKey);

// 上传字节数组
String url = ossService.uploadBytes(data, fileName);

// 删除文件
ossService.deleteFile(objectKey);

// 从 URL 提取 objectKey
String objectKey = ossService.extractObjectKeyFromUrl(url);

// 关闭服务
ossService.close();
```

---

## 📊 性能对比

| 指标 | GCS | OSS | 改进 |
|------|-----|-----|------|
| **国内访问速度** | 慢 | 快 | ✅ 显著提升 |
| **成本** | 高 | 中 | ✅ 降低 |
| **配置复杂度** | 高 | 中 | ✅ 简化 |
| **API 兼容性** | 原生支持 | 需要转换 | ⚠️ 需要额外处理 |

---

## ⚠️ 注意事项

### 1. 现有数据迁移

如果您之前使用 GCS 存储了数据，需要手动迁移：

```bash
# 1. 从 GCS 下载
gsutil -m cp -r gs://your-gcs-bucket/* ./backup/

# 2. 上传到 OSS
ossutil cp -r ./backup/ oss://your-oss-bucket/
```

### 2. URL 格式变化

- **GCS URL**: `gs://bucket/object` 或 `https://storage.googleapis.com/bucket/object`
- **OSS URL**: `https://bucket.oss-cn-hangzhou.aliyuncs.com/object`

### 3. 权限管理

- GCS 使用 IAM 角色和服务账号
- OSS 使用 Access Key ID 和 Access Key Secret

### 4. Vertex AI 兼容性

由于 Vertex AI 只支持 GCS URI，我们使用 `imageBytes` 方式传递图片：
- ✅ 优点：兼容所有存储服务
- ⚠️ 缺点：需要读取本地文件，增加了一次 I/O 操作

---

## 🐛 故障排查

### 问题 1: OSS 连接失败

**错误信息**:
```
IllegalStateException: ALIYUN_OSS_ENDPOINT environment variable is not set
```

**解决方案**:
```bash
# 检查环境变量
echo $ALIYUN_OSS_ENDPOINT
echo $ALIYUN_OSS_ACCESS_KEY_ID
echo $ALIYUN_OSS_ACCESS_KEY_SECRET
echo $ALIYUN_OSS_BUCKET_NAME

# 如果未设置，导出环境变量
export ALIYUN_OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"
# ... 其他变量
```

### 问题 2: 上传失败

**错误信息**:
```
Failed to upload file to OSS: Access denied
```

**解决方案**:
1. 检查 Access Key 是否正确
2. 检查 Bucket 权限设置
3. 检查 RAM 用户权限（需要 `oss:PutObject` 权限）

### 问题 3: 视频生成失败

**错误信息**:
```
cannot find symbol: method imageUrl(java.lang.String)
```

**解决方案**:
这个问题已在迁移中修复。确保使用最新代码：
```java
// 使用 imageBytes 而不是 imageUrl
Image keyframeImage = Image.builder()
        .imageBytes(imageBytes)
        .mimeType("image/jpeg")
        .build();
```

---

## 📚 相关文档

- [阿里云 OSS 文档](https://help.aliyun.com/product/31815.html)
- [Vertex AI API 文档](https://cloud.google.com/vertex-ai/docs)
- [项目 README](./README.md)
- [配置指南](./SETUP_GUIDE.md)

---

## 🎯 总结

### 完成的工作

✅ 移除所有 GCS 相关代码  
✅ 更新所有服务类使用 OSSService  
✅ 更新配置文件  
✅ 修复 Vertex AI 兼容性问题  
✅ 编译测试通过  

### 后续工作

- [ ] 运行端到端测试
- [ ] 更新用户文档
- [ ] 迁移现有数据（如果有）

---

**迁移状态**: ✅ 完成  
**编译状态**: ✅ 通过  
**测试状态**: ⏳ 待测试

---

**文档版本**: 1.0  
**最后更新**: 2026-01-12
