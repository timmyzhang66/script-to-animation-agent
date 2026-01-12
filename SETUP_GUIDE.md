# 配置指南 - Script to Animation Agent

本文档详细说明如何配置和使用标准 Vertex AI 模式。

## 重要更新（2026-01-12）

**Veo API 需要标准 Vertex AI 模式**

经过测试发现，Veo 视频生成 API 不支持 Express Mode（API Key 模式）。要使用 Veo API，必须配置标准 Vertex AI 模式，使用 GCP 项目和服务账号认证。

## 配置步骤

### 1. 创建 GCP 项目

1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 点击项目选择器，创建新项目或选择现有项目
3. 记录您的项目 ID（例如：`gen-lang-client-0352724605`）

### 2. 启用必要的 API

在 GCP Console 中启用以下 API：

1. **Vertex AI API**
   - 访问：https://console.cloud.google.com/apis/library/aiplatform.googleapis.com
   - 点击"启用"

2. **Cloud Storage API**（可选，用于存储中间文件）
   - 访问：https://console.cloud.google.com/apis/library/storage.googleapis.com
   - 点击"启用"

### 3. 创建服务账号

1. 访问 [IAM & Admin > Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts)
2. 点击 **Create Service Account**
3. 输入服务账号信息：
   - **名称**：`vertex-ai-agent`（或其他名称）
   - **描述**：`Service account for Script to Animation Agent`
4. 点击 **Create and Continue**
5. 授予以下角色：
   - `Vertex AI User` - 用于访问 Vertex AI API
   - `Storage Object Admin` - 用于访问 Cloud Storage（可选）
6. 点击 **Continue**，然后点击 **Done**

### 4. 创建服务账号密钥

1. 在服务账号列表中，找到刚创建的服务账号
2. 点击服务账号名称进入详情页
3. 切换到 **Keys** 标签
4. 点击 **Add Key** > **Create new key**
5. 选择 **JSON** 格式
6. 点击 **Create**，密钥文件将自动下载

### 5. 保存密钥文件

将下载的 JSON 密钥文件保存到项目根目录：

```bash
# 假设下载的文件名为 your-project-123456-abcdef.json
cp ~/Downloads/your-project-123456-abcdef.json ./gcp-service-account.json
```

**重要**：不要将密钥文件提交到 Git 仓库！确保 `.gitignore` 包含：

```
gcp-service-account.json
*.json
```

### 6. 设置环境变量

#### Linux/Mac

在 `~/.bashrc` 或 `~/.zshrc` 中添加：

```bash
export GCP_PROJECT_ID="your-project-id"
export GCP_LOCATION="us-central1"
export GCP_SERVICE_ACCOUNT_KEY_PATH="/path/to/gcp-service-account.json"
```

然后执行：

```bash
source ~/.bashrc  # 或 source ~/.zshrc
```

#### Windows

在 PowerShell 中：

```powershell
$env:GCP_PROJECT_ID="your-project-id"
$env:GCP_LOCATION="us-central1"
$env:GCP_SERVICE_ACCOUNT_KEY_PATH="C:\path\to\gcp-service-account.json"
```

或者在系统环境变量中设置（推荐）：
1. 右键"此电脑" > "属性" > "高级系统设置"
2. 点击"环境变量"
3. 在"用户变量"中添加上述三个变量

### 7. 验证配置

运行测试脚本验证配置是否正确：

```bash
./test_standard_vertexai.sh
```

预期输出：

```
========================================
Veo API Test - Standard Vertex AI Mode
========================================
Project ID: your-project-id
Location: us-central1

✓ Service account key file found
✓ Test image found
✓ Client initialized with standard Vertex AI mode

========================================
Test 1: Text-to-video
========================================
Generating video from text prompt...
Calling Veo API...
✓ Operation created: Optional[projects/.../operations/...]
✓ Test 1 PASSED

========================================
Test 2: Image-to-video
========================================
Generating video from image...
Image loaded: 168038 bytes, MIME type: image/jpeg
Calling Veo API...
✓ Operation created: Optional[projects/.../operations/...]
✓ Test 2 PASSED
```

## 配置参数说明

### GCP_PROJECT_ID

您的 Google Cloud 项目 ID，可以在 GCP Console 的项目选择器中找到。

**示例**：`gen-lang-client-0352724605`

### GCP_LOCATION

Vertex AI 服务的区域。推荐使用 `us-central1`，因为它支持所有 Vertex AI 功能。

**可用区域**：
- `us-central1` - 美国中部（推荐）
- `us-east1` - 美国东部
- `us-west1` - 美国西部
- `europe-west4` - 欧洲西部
- `asia-southeast1` - 亚洲东南部

### GCP_SERVICE_ACCOUNT_KEY_PATH

服务账号密钥文件的绝对路径或相对路径。

**示例**：
- 相对路径：`gcp-service-account.json`
- 绝对路径：`/home/user/project/gcp-service-account.json`

## 常见问题

### Q1: 如何获取项目 ID？

A: 在 GCP Console 顶部的项目选择器中，项目名称旁边显示的就是项目 ID。

### Q2: 服务账号需要哪些权限？

A: 最低权限：
- `Vertex AI User` - 用于调用 Vertex AI API
- 如果使用 Cloud Storage：`Storage Object Admin`

### Q3: 可以使用 API Key 吗？

A: 不可以。Veo API 不支持 Express Mode（API Key 模式），必须使用标准 Vertex AI 模式。

### Q4: 如何检查 API 是否启用？

A: 访问 [API Library](https://console.cloud.google.com/apis/library)，搜索 "Vertex AI"，如果显示"管理"按钮则表示已启用。

### Q5: 密钥文件丢失怎么办？

A: 无法恢复旧密钥。需要创建新密钥：
1. 进入服务账号详情页
2. 删除旧密钥（可选）
3. 创建新密钥

### Q6: 如何限制密钥权限？

A: 建议：
1. 只授予必要的角色
2. 定期轮换密钥
3. 使用 IAM 条件限制访问
4. 启用审计日志

## 成本估算

使用标准 Vertex AI 模式的成本（2026 年 1 月定价）：

| 服务 | 价格 | 示例用量 | 成本 |
|------|------|----------|------|
| Imagen 3.0 | $0.04/图像 | 10 张关键帧 | $0.40 |
| Veo 3.1 | $0.10/秒 | 40 秒视频 | $4.00 |
| Gemini 2.0 Flash | $0.075/1M tokens | ~10K tokens | $0.001 |
| Cloud Storage | $0.02/GB/月 | 1GB | $0.02 |
| **总计** | | | **约 $4.42** |

**注意**：
- 价格可能变动，请查看 [Google Cloud 定价](https://cloud.google.com/pricing)
- 新用户可能有免费额度
- 建议设置预算提醒

## 安全最佳实践

1. **不要将密钥文件提交到版本控制**
   ```bash
   echo "gcp-service-account.json" >> .gitignore
   ```

2. **限制密钥文件权限**
   ```bash
   chmod 600 gcp-service-account.json
   ```

3. **定期轮换密钥**
   - 建议每 90 天轮换一次
   - 删除不再使用的旧密钥

4. **使用环境变量**
   - 不要在代码中硬编码密钥路径
   - 使用环境变量或配置文件

5. **启用审计日志**
   - 在 GCP Console 中启用 Cloud Audit Logs
   - 监控服务账号的使用情况

## 下一步

配置完成后，您可以：

1. 运行端到端测试：
   ```bash
   ./test_e2e.sh
   ```

2. 使用自己的脚本：
   ```bash
   java -jar target/script-to-animation-agent-1.0-SNAPSHOT.jar my_script.txt
   ```

3. 查看 [README.md](README.md) 了解更多使用方法

## 支持

如有问题，请：
1. 查看 [故障排除](#常见问题)
2. 提交 [GitHub Issue](https://github.com/timmyzhang66/script-to-animation-agent/issues)
3. 查看 [Google Cloud 文档](https://cloud.google.com/vertex-ai/docs)
