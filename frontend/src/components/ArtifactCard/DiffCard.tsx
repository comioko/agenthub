import { Card, Tag } from 'antd'
import { PlusOutlined, MinusOutlined } from '@ant-design/icons'

interface DiffCardProps {
  oldCode?: string
  newCode?: string
  language?: string
  title?: string
}

const DiffCard = ({ oldCode, newCode, language = 'diff', title }: DiffCardProps) => {
  const renderDiffLine = (line: string, index: number) => {
    const isAdd = line.startsWith('+')
    const isRemove = line.startsWith('-')
    const bgColor = isAdd ? '#e6ffec' : isRemove ? '#fff1f0' : 'transparent'
    const textColor = isAdd ? '#22863a' : isRemove ? '#cb2431' : '#333'

    return (
      <div
        key={index}
        style={{
          background: bgColor,
          color: textColor,
          padding: '2px 8px',
          fontFamily: 'monospace',
          fontSize: 13,
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-all'
        }}
      >
        {isAdd && <PlusOutlined style={{ marginRight: 4 }} />}
        {isRemove && <MinusOutlined style={{ marginRight: 4 }} />}
        {line}
      </div>
    )
  }

  const removeLines = (oldCode || '').split('\n').map((line) => ({ content: line, type: 'remove' as const }))
  const addLines = (newCode || '').split('\n').map((line) => ({ content: line, type: 'add' as const }))
  const lines: Array<{ content: string; type: 'remove' | 'add' }> = [...removeLines, ...addLines]

  return (
    <Card
      size="small"
      title={title || '代码差异'}
      extra={<Tag color="purple">{language}</Tag>}
      style={{ marginTop: 8, marginBottom: 8 }}
      bodyStyle={{ padding: 0, maxHeight: 400, overflow: 'auto' }}
    >
      {lines.map((line, idx) => renderDiffLine(line.content, idx))}
    </Card>
  )
}

export default DiffCard
