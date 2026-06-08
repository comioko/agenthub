# Skill: 新增消息卡片类型

## 适用场景
需要新增 Artifact 卡片类型（如图表卡片、思维导图卡片）

## 输入
- 卡片类型名称（如 chart, mindmap）
- 数据结构
- 渲染需求

## 输出
完整的卡片组件 + 后端存储逻辑

## 执行步骤

### 1. 定义卡片类型

在 `artifact-card-spec.md` 中添加：

```markdown
| chart | 6 | ChartCard | 图表展示 |
```

### 2. 创建卡片组件

```tsx
// frontend/src/components/ArtifactCard/ChartCard.tsx
import { Card } from 'antd'

interface ChartCardProps {
  data: any           // 图表数据
  chartType?: string // line, bar, pie
  title?: string
}

const ChartCard = ({ data, chartType = 'line', title }: ChartCardProps) => {
  return (
    <Card title={title} size="small">
      {/* 渲染图表 */}
    </Card>
  )
}

export default ChartCard
```

### 3. 在 Chat.tsx 中添加渲染逻辑

```tsx
import ChartCard from '../components/ArtifactCard/ChartCard'

const renderBlock = (block: MessageBlock) => {
  switch (block.blockType) {
    case 'code':
      return <CodeCard {...block} />
    case 'diff':
      return <DiffCard {...block} />
    case 'chart':
      return <ChartCard {...block} />
    default:
      return null
  }
}
```

### 4. 添加单元测试（如需）

```tsx
import { render } from '@testing-library/react'
import ChartCard from './ChartCard'

test('renders chart card', () => {
  const { getByText } = render(
    <ChartCard data={{}} chartType="line" title="测试图表" />
  )
  expect(getByText('测试图表')).toBeInTheDocument()
})
```

## 注意事项

1. **组件放在 `frontend/src/components/ArtifactCard/` 目录**
2. **必须定义清晰的 Props 接口**
3. **考虑响应式布局**
4. **更新 `index.ts` 导出**
5. **更新 `artifact-card-spec.md`**
