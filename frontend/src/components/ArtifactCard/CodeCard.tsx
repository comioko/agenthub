import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism'
import { Card, Tag } from 'antd'

interface CodeCardProps {
  code: string
  language?: string
  title?: string
}

const CodeCard = ({ code, language = 'javascript', title }: CodeCardProps) => {
  return (
    <Card
      size="small"
      title={title || '代码'}
      extra={<Tag color="blue">{language}</Tag>}
      style={{ marginTop: 8, marginBottom: 8 }}
      bodyStyle={{ padding: 0 }}
    >
      <SyntaxHighlighter
        language={language}
        style={vscDarkPlus}
        customStyle={{
          margin: 0,
          borderRadius: 0,
          fontSize: 13
        }}
        showLineNumbers
      >
        {code}
      </SyntaxHighlighter>
    </Card>
  )
}

export default CodeCard
