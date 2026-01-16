# Characters JSON Format

## 文件格式说明

`characters.json` 文件用于定义动画中的角色，避免 AI 分析角色时出现错误。

---

## JSON 结构

```json
{
  "characters": [
    {
      "name": "角色名称",
      "description": "详细的角色外观描述",
      "role": "角色类型（如 Protagonist, Supporting Character, Antagonist 等）",
      "isMainCharacter": true/false
    }
  ]
}
```

---

## 字段说明

### `characters` (必需)
- **类型**: 数组
- **说明**: 包含所有角色定义的列表

### `name` (必需)
- **类型**: 字符串
- **说明**: 角色的名称
- **示例**: `"Detective John Smith"`, `"Sarah Johnson"`

### `description` (必需)
- **类型**: 字符串
- **说明**: 角色的详细外观描述，用于生成角色图像
- **建议**: 
  - 包含年龄、身高、体型等基本信息
  - 描述面部特征（眼睛、头发、表情等）
  - 描述服装和配饰
  - 描述姿态和气质
  - 越详细越好，有助于生成一致的角色图像

**示例**:
```
"A tall detective in his 40s with sharp, observant eyes and a weathered face. 
He wears a dark gray trench coat over a crisp white shirt and black tie. 
His short, salt-and-pepper hair is neatly combed. 
He has a serious, determined expression and carries himself with quiet confidence."
```

### `role` (必需)
- **类型**: 字符串
- **说明**: 角色在故事中的类型/角色
- **常见值**:
  - `"Protagonist"` - 主角
  - `"Antagonist"` - 反派
  - `"Supporting Character"` - 配角
  - `"Mentor"` - 导师
  - `"Comic Relief"` - 喜剧角色
  - 或其他自定义角色类型

### `isMainCharacter` (必需)
- **类型**: 布尔值 (true/false)
- **说明**: 是否为主要角色
- **用途**: 主要角色会在更多场景中出现，系统会更注重保持其一致性

---

## 完整示例

```json
{
  "characters": [
    {
      "name": "Detective John Smith",
      "description": "A tall detective in his 40s with sharp, observant eyes and a weathered face that tells stories of countless cases. He wears a dark gray trench coat over a crisp white shirt and black tie. His short, salt-and-pepper hair is neatly combed. He has a serious, determined expression and carries himself with quiet confidence. His posture is upright and alert, always ready for action.",
      "role": "Protagonist",
      "isMainCharacter": true
    },
    {
      "name": "Sarah Johnson",
      "description": "A young journalist in her late 20s with long, flowing brown hair and bright, curious green eyes. She wears casual but professional attire: a light blue blazer over a white blouse and dark jeans. She has an energetic, enthusiastic demeanor and often carries a notebook and pen. Her expression is inquisitive and friendly, with a slight smile that suggests optimism and determination.",
      "role": "Supporting Character",
      "isMainCharacter": false
    },
    {
      "name": "Professor Michael Chen",
      "description": "An elderly professor in his 60s with gray hair, round glasses, and a kind, wise expression. He wears a brown tweed jacket with elbow patches over a cream-colored sweater. His posture is slightly hunched from years of studying, but his eyes sparkle with intelligence and warmth. He often gestures expressively when speaking, conveying his passion for knowledge.",
      "role": "Supporting Character",
      "isMainCharacter": false
    }
  ]
}
```

---

## 使用方法

### 命令行方式

```bash
# 使用 characters.json 文件
./run.sh -f my_script.txt -c characters.json

# 或使用完整路径
./run.sh -f scripts/my_script.txt -c examples/characters.json
```

### 不使用 characters.json

如果不提供 `-c` 参数，系统会使用 AI 自动分析脚本中的角色：

```bash
# AI 自动分析角色
./run.sh -f my_script.txt
```

---

## 最佳实践

### 1. 详细的描述

❌ **不好的描述**:
```
"A detective"
```

✅ **好的描述**:
```
"A tall detective in his 40s with sharp, observant eyes and a weathered face. 
He wears a dark gray trench coat over a crisp white shirt and black tie. 
His short, salt-and-pepper hair is neatly combed."
```

### 2. 一致的风格

确保所有角色的描述风格一致，使用相似的细节层次。

### 3. 避免模糊的术语

❌ 避免: "好看的", "帅气的", "漂亮的"  
✅ 使用: 具体的特征描述，如 "高颧骨", "深邃的眼睛", "优雅的姿态"

### 4. 包含关键特征

- 年龄范围
- 身高体型
- 发型和发色
- 眼睛颜色和表情
- 服装风格
- 标志性配饰
- 姿态和气质

---

## 注意事项

1. **JSON 格式必须正确**: 使用 JSON 验证工具检查格式
2. **必须包含 `characters` 字段**: 这是顶层必需字段
3. **所有角色字段都是必需的**: `name`, `description`, `role`, `isMainCharacter`
4. **描述要详细**: 越详细的描述，生成的角色图像越准确
5. **文件编码**: 使用 UTF-8 编码保存文件

---

## 故障排除

### 错误: "Characters file not found"
- 检查文件路径是否正确
- 确保文件存在于指定位置

### 错误: "Invalid characters file format: missing 'characters' field"
- 检查 JSON 文件是否包含顶层 `characters` 字段
- 使用 JSON 验证工具检查格式

### 错误: JSON 解析失败
- 检查 JSON 语法（逗号、引号、括号等）
- 确保所有字符串都用双引号包裹
- 确保布尔值使用 `true`/`false`（小写，无引号）

---

**创建时间**: 2026-01-14  
**版本**: 1.0
