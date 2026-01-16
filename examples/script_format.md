# 脚本格式说明

## 📝 脚本格式

脚本按照以下方式组织：

```
Scene X:
[场景描述 - 包含环境、人物动作等]
Dialogue:
[说话人]: "[对话内容]"
[说话人]: "[对话内容]"
...

Scene X+1:
[场景描述]
Dialogue:
[说话人]: "[对话内容]"
...
```

---

## 🎬 格式详解

### 1. 场景标题

```
Scene 1:
Scene 2:
Scene 3:
...
```

- **格式**: `Scene` + 空格 + 数字 + 冒号
- **大小写**: 不区分大小写（`Scene`, `scene`, `SCENE` 都可以）
- **数字**: 必须是连续的整数（1, 2, 3, ...）

---

### 2. 场景描述

场景标题后面，`Dialogue:` 之前的所有内容都是场景描述。

**包含内容**:
- 环境描述（时间、地点、氛围）
- 人物位置和动作
- 视觉细节
- 表情和姿态

**示例**:
```
Scene 1:
The scene opens in a warm, dimly lit modern living room. 
Wife is lounging provocatively on a velvet sofa, wearing a sheer silk lace nightgown. 
Husband sits on the floor beside the sofa, looking up at her with a pitiful expression.
```

**注意**:
- 可以多行
- 会自动合并为一段
- 用于生成关键帧图像

---

### 3. 对话部分

```
Dialogue:
[说话人]: "[对话内容]"
[说话人]: "[对话内容]"
```

**格式要求**:
- 以 `Dialogue:` 开头（不区分大小写）
- 每行一个对话
- 格式: `说话人: "对话内容"` 或 `说话人："对话内容"`
- 对话内容必须用引号包裹（支持中英文引号）

**示例**:
```
Dialogue:
Husband："老婆我没钱抽烟了，可怜可怜我呗"
Wife："现在给你个机会，一分钟一块钱怎么样"
```

**说话人匹配**:
- 说话人会与 `characters.json` 中的角色名称匹配
- 匹配是宽松的（不需要完全一致）
- 如果没有匹配的角色，会作为临时角色处理

---

## 📋 完整示例

```
Scene 1:
The scene opens in a warm, dimly lit modern living room. Wife is lounging provocatively on a velvet sofa, wearing a sheer silk lace nightgown that accentuates her figure. Husband sits on the floor beside the sofa, looking up at her with a pitiful, begging expression.
Dialogue:
Husband："老婆我没钱抽烟了，可怜可怜我呗"

Scene 2:
The Wife sets down her glass of wine and leans forward, her gaze seductive and playful. She reaches out a slender finger to tilt the man's chin up, a mischievous smirk playing on her lips.
Dialogue:
Wife："现在给你个机会，一分钟一块钱怎么样"

Scene 3:
The Husband's eyes light up with a mix of hope and desperation. He nods eagerly, his hands clasped together in a pleading gesture.
Dialogue:
Husband："好的好的，谢谢老婆！"

Scene 4:
The Wife leans back on the sofa with a satisfied smile, crossing her legs elegantly. She picks up her wine glass and takes a slow sip, watching the Husband with amusement.
Dialogue:
Wife："那你就好好表现吧"
```

---

## 🎯 工作流程

### 1. 解析脚本

系统会自动解析脚本，提取：
- 场景编号
- 场景描述
- 对话内容
- 涉及的角色

### 2. 生成关键帧

每个 Scene 会生成一个关键帧图像：
- 使用场景描述作为 Prompt
- 如果场景中有定义的角色，会使用角色参考图像

### 3. 生成视频

每个 Scene 会生成一个视频片段：
- 使用关键帧图像作为起始帧
- 使用场景描述和对话作为 Prompt
- Veo 3.1 会自动生成音频和口型同步

---

## 💡 最佳实践

### 1. 场景描述

✅ **好的描述**:
```
The scene opens in a warm, dimly lit modern living room. 
Wife is lounging provocatively on a velvet sofa, wearing a sheer silk lace nightgown. 
Husband sits on the floor beside the sofa, looking up at her with a pitiful expression.
```

❌ **不好的描述**:
```
They are in a room.
```

**建议**:
- 详细描述环境
- 描述人物位置和动作
- 包含视觉细节
- 描述表情和情绪

---

### 2. 对话格式

✅ **正确格式**:
```
Dialogue:
Husband："老婆我没钱抽烟了"
Wife："现在给你个机会"
```

❌ **错误格式**:
```
Dialogue:
Husband: 老婆我没钱抽烟了（缺少引号）
Wife "现在给你个机会"（缺少冒号）
```

---

### 3. 角色名称

**在 characters.json 中定义**:
```json
{
  "characters": [
    {
      "name": "Husband",
      "description": "...",
      "role": "Protagonist",
      "isMainCharacter": true
    },
    {
      "name": "Wife",
      "description": "...",
      "role": "Protagonist",
      "isMainCharacter": true
    }
  ]
}
```

**在脚本中使用**:
```
Dialogue:
Husband："..."
Wife："..."
```

**匹配规则**:
- 优先精确匹配
- 支持部分匹配（如 "The Husband" 可以匹配 "Husband"）
- 不区分大小写

---

## 🔍 常见问题

### Q: 场景编号必须从 1 开始吗？

A: 是的，场景编号必须从 1 开始，并且连续递增。

---

### Q: 可以没有对话吗？

A: 可以。如果场景没有对话，可以省略 `Dialogue:` 部分。

示例：
```
Scene 1:
A beautiful sunset over the ocean. Waves gently crash on the shore.

Scene 2:
...
```

---

### Q: 对话可以有多轮吗？

A: 可以。每行一个对话即可。

示例：
```
Dialogue:
Husband："第一句话"
Wife："第二句话"
Husband："第三句话"
Wife："第四句话"
```

---

### Q: 说话人必须在 characters.json 中定义吗？

A: 不是必须的。如果说话人没有在 characters.json 中定义，系统会将其作为临时角色处理（不会生成角色参考图像）。

---

### Q: 场景描述可以多长？

A: 没有严格限制，但建议控制在 200-500 字之间，过长可能影响图像生成质量。

---

## 🚀 如何使用

### 1. 创建脚本文件

```bash
nano my_script.txt
```

按照上述格式编写脚本。

### 2. 创建角色文件（可选）

```bash
nano my_characters.json
```

定义脚本中的主要角色。

### 3. 运行

```bash
# 使用角色文件
./run.sh -f my_script.txt -c my_characters.json

# 不使用角色文件
./run.sh -f my_script.txt
```

---

## 📚 相关文档

- `examples/example_script.txt` - 示例脚本
- `examples/characters.json` - 示例角色定义
- `examples/characters_format.md` - 角色文件格式说明

---

**文档版本**: 1.0  
**更新时间**: 2026-01-15
