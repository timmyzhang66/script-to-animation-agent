# 阿里云 OSS 配置说明

## 为什么需要 OSS？

在使用 Gemini Veo 进行图生视频时，API 不支持直接传递图片的字节数据，而是需要提供图片的公开访问 URL。因此，我们需要将生成的关键帧图片上传到云存储服务（如阿里云 OSS），然后将 URL 传递给 Veo API。

## 错误信息

如果没有配置 OSS，会遇到以下错误：

```
400 . Invalid resource field value in the request.
```

这是因为 Vertex AI 的 `generateVideos` API 需要通过 URL 访问图片。

## 配置步骤

### 1. 创建阿里云 OSS Bucket

1. 登录 [阿里云控制台](https://oss.console.aliyun.com/)
2. 创建一个新的 Bucket
   - **Bucket 名称**：自定义（例如：`my-animation-bucket`）
   - **地域**：选择离您最近的地域（例如：华东1（杭州））
   - **读写权限**：**公共读**（允许匿名访问图片）
   - **存储类型**：标准存储
3. 记录 Bucket 信息：
   - Bucket 名称
   - Endpoint（例如：`oss-cn-hangzhou.aliyuncs.com`）

### 2. 创建 AccessKey

1. 访问 [RAM 访问控制](https://ram.console.aliyun.com/)
2. 创建用户或使用现有用户
3. 为用户授予 OSS 权限（`AliyunOSSFullAccess`）
4. 创建 AccessKey
   - 记录 **AccessKey ID**
   - 记录 **AccessKey Secret**（只显示一次，请妥善保管）

### 3. 设置环境变量

#### Linux / macOS

```bash
export ALIYUN_OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"
export ALIYUN_OSS_ACCESS_KEY_ID="your-access-key-id"
export ALIYUN_OSS_ACCESS_KEY_SECRET="your-access-key-secret"
export ALIYUN_OSS_BUCKET_NAME="your-bucket-name"
```

#### Windows

```cmd
set ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
set ALIYUN_OSS_ACCESS_KEY_ID=your-access-key-id
set ALIYUN_OSS_ACCESS_KEY_SECRET=your-access-key-secret
set ALIYUN_OSS_BUCKET_NAME=your-bucket-name
```

#### 永久设置（Linux / macOS）

将以上命令添加到 `~/.bashrc` 或 `~/.zshrc`：

```bash
echo 'export ALIYUN_OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"' >> ~/.bashrc
echo 'export ALIYUN_OSS_ACCESS_KEY_ID="your-access-key-id"' >> ~/.bashrc
echo 'export ALIYUN_OSS_ACCESS_KEY_SECRET="your-access-key-secret"' >> ~/.bashrc
echo 'export ALIYUN_OSS_BUCKET_NAME="your-bucket-name"' >> ~/.bashrc
source ~/.bashrc
```

### 4. 验证配置

运行以下命令验证环境变量是否设置成功：

```bash
echo $ALIYUN_OSS_ENDPOINT
echo $ALIYUN_OSS_ACCESS_KEY_ID
echo $ALIYUN_OSS_BUCKET_NAME
```

## 工作流程

1. **生成关键帧图片**：使用 Gemini Imagen 生成关键帧图片，保存到本地
2. **上传到 OSS**：将图片上传到阿里云 OSS，获取公开访问 URL
3. **生成视频**：将 URL 传递给 Gemini Veo API 生成视频
4. **下载视频**：视频生成完成后下载到本地

## OSSService 功能

项目中的 `OSSService` 类提供以下功能：

### 上传文件

```java
OSSService ossService = new OSSService();
String imageUrl = ossService.uploadFile("/path/to/image.jpg", null);
System.out.println("Image URL: " + imageUrl);
```

### 上传字节数组

```java
byte[] imageData = ...;
String imageUrl = ossService.uploadBytes(imageData, "image.jpg");
```

### 删除文件

```java
String objectKey = ossService.extractObjectKeyFromUrl(imageUrl);
ossService.deleteFile(objectKey);
```

## 安全建议

1. **不要在代码中硬编码 AccessKey**：始终使用环境变量
2. **使用 RAM 子账号**：不要使用主账号的 AccessKey
3. **最小权限原则**：只授予 OSS 相关权限
4. **定期轮换 AccessKey**：建议每 90 天更换一次
5. **启用 Bucket 防盗链**：在 OSS 控制台配置 Referer 白名单

## 成本估算

阿里云 OSS 按使用量计费：

- **存储费用**：约 ¥0.12/GB/月（标准存储）
- **流量费用**：约 ¥0.50/GB（外网流出流量）
- **请求费用**：约 ¥0.01/万次（PUT 请求）

**示例**：生成 10 个场景的动画
- 图片大小：10 张 × 2MB = 20MB
- 存储费用：¥0.12 × 0.02 = ¥0.0024/月
- 上传请求：10 次 × ¥0.01/万次 ≈ ¥0.00001
- 流量费用：20MB × ¥0.50/GB ≈ ¥0.01

**总计**：约 ¥0.01（非常便宜）

## 常见问题

### Q: 为什么不使用 Google Cloud Storage？

**A**: 可以使用 GCS，但考虑到：
1. 国内访问阿里云 OSS 更快
2. 阿里云 OSS 配置更简单
3. 成本相近

如果您更喜欢 GCS，可以修改 `OSSService` 类适配 GCS SDK。

### Q: 图片上传后会一直保留吗？

**A**: 是的，图片会保留在 OSS 中。建议：
1. 定期清理不需要的图片
2. 或者配置 OSS 生命周期规则自动删除旧文件

### Q: 可以使用其他云存储服务吗？

**A**: 可以！只需实现类似的服务类：
- **AWS S3**：使用 AWS SDK for Java
- **腾讯云 COS**：使用腾讯云 SDK
- **七牛云**：使用七牛云 SDK

关键是提供公开访问的 URL。

### Q: 如何配置 OSS 生命周期规则？

**A**: 在 OSS 控制台：
1. 进入 Bucket 管理
2. 选择"基础设置" → "生命周期"
3. 添加规则：
   - 前缀：`script-to-animation/`
   - 操作：删除文件
   - 时间：30 天后

### Q: Bucket 必须设置为公共读吗？

**A**: 是的，因为 Gemini Veo API 需要能够访问图片 URL。如果担心安全问题，可以：
1. 使用 OSS 的 Referer 防盗链
2. 使用临时签名 URL（需要修改代码）
3. 使用 OSS 的 IP 白名单

## 相关链接

- [阿里云 OSS 文档](https://help.aliyun.com/product/31815.html)
- [OSS Java SDK](https://help.aliyun.com/document_detail/32008.html)
- [OSS 定价](https://www.aliyun.com/price/product#/oss/detail)

## 更新日志

- **2026-01-12**: 添加阿里云 OSS 集成
  - 创建 `OSSService` 类
  - 修改 `GeminiService` 使用 OSS URL
  - 解决图生视频的 400 错误

## 测试验证

配置完成后，运行项目应该能够正常生成视频：

```bash
# 设置所有环境变量
export GEMINI_API_KEY="your-key"
export ALIYUN_OSS_ENDPOINT="oss-cn-hangzhou.aliyuncs.com"
export ALIYUN_OSS_ACCESS_KEY_ID="your-access-key-id"
export ALIYUN_OSS_ACCESS_KEY_SECRET="your-access-key-secret"
export ALIYUN_OSS_BUCKET_NAME="your-bucket-name"

# 运行程序
./run.sh -f example_script.txt
```

如果配置正确，您应该看到类似的日志：

```
Keyframe image uploaded to OSS: https://your-bucket.oss-cn-hangzhou.aliyuncs.com/script-to-animation/xxx.jpg
Generating video from image...
```
