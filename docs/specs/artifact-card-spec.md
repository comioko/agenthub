# Artifact 卡片规范

## 作用
定义所有富媒体卡片的类型、结构和渲染方式。

## 适用范围
MessageBlock 实体、ArtifactCard 组件、Chat 渲染逻辑。

## MessageBlock 类型

| 类型 | 值 | 组件 | 说明 |
|------|---|------|------|
| code | 1 | CodeCard | 代码块 |
| diff | 2 | DiffCard | 代码差异 |
| web | 3 | WebPreviewCard | 网页预览 |
| file | 4 | FileCard | 文件附件 |
| deploy | 5 | DeployCard | 部署状态 |

## MessageBlock 结构

```java
public class MessageBlock {
    Long id;
    Long messageId;       // 关联消息ID
    String blockType;     // 卡片类型
    String content;       // 内容
    String language;      // 语言（code类型用）
    String title;         // 标题
    Object metadata;     // 额外数据（JSON）
}
```

## 数据库表结构

```sql
CREATE TABLE message_block (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    block_type VARCHAR(50) NOT NULL COMMENT 'code/diff/web/file/deploy',
    content LONGTEXT NOT NULL,
    language VARCHAR(50) DEFAULT NULL,
    title VARCHAR(200) DEFAULT NULL,
    metadata JSON DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_message (message_id)
);
```

## CodeCard 结构

```typescript
interface CodeCardProps {
  code: string           // 代码内容
  language?: string     // 语言：java, python, js, etc.
  title?: string        // 可选标题
}
```

## DiffCard 结构

```typescript
interface DiffCardProps {
  oldCode?: string      // 原始代码
  newCode?: string     // 新代码
  language?: string     // 语言
  title?: string        // 可选标题
}
```

## 渲染规则

1. blocks 数组顺序即为渲染顺序
2. 每个 block 之间有 8px 间距
3. 卡片最大宽度为消息气泡的 100%
4. 代码块最大高度 400px，超出可滚动

## 组件文件位置

```
frontend/src/components/ArtifactCard/
├── index.ts           # 导出入口
├── CodeCard.tsx      # 代码块
├── DiffCard.tsx      # 差异对比
├── WebPreviewCard.tsx # 网页预览（待实现）
├── FileCard.tsx       # 文件附件（待实现）
└── DeployCard.tsx     # 部署状态（待实现）
```

## 新增卡片类型 Checklist

- [ ] 在本文档定义 block_type 常量
- [ ] 创建 {Type}Card.tsx 组件
- [ ] 在 MessageBlockRepository 中添加查询方法（如需）
- [ ] 在 Chat.tsx 中添加渲染逻辑
- [ ] 添加单元测试
- [ ] 更新本文档卡片类型表格
