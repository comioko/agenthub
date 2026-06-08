import { Card, Button, Space } from 'antd'
import { GlobalOutlined, EditOutlined, LinkOutlined } from '@ant-design/icons'
import { useState } from 'react'

interface WebPreviewCardProps {
  htmlContent?: string
  url?: string
  title?: string
}

const WebPreviewCard = ({ htmlContent, url, title }: WebPreviewCardProps) => {
  const [isEditing, setIsEditing] = useState(false)
  const [content, setContent] = useState(htmlContent || '')

  const renderPreview = () => {
    if (url) {
      return (
        <iframe
          src={url}
          title={title || '网页预览'}
          style={{
            width: '100%',
            height: 300,
            border: 'none',
            borderRadius: '0 0 8px 8px'
          }}
          sandbox="allow-scripts allow-same-origin"
        />
      )
    }

    if (htmlContent) {
      return (
        <iframe
          srcDoc={content}
          title={title || 'HTML 预览'}
          style={{
            width: '100%',
            height: 300,
            border: 'none',
            borderRadius: '0 0 8px 8px'
          }}
          sandbox="allow-scripts allow-same-origin"
        />
      )
    }

    return (
      <div style={{ padding: 40, textAlign: 'center', color: '#999' }}>
        无可预览内容
      </div>
    )
  }

  return (
    <Card
      size="small"
      title={
        <Space>
          <GlobalOutlined />
          {title || '网页预览'}
        </Space>
      }
      extra={
        <Space>
          {htmlContent && (
            <Button
              size="small"
              icon={<EditOutlined />}
              type={isEditing ? 'primary' : 'default'}
              onClick={() => setIsEditing(!isEditing)}
            >
              {isEditing ? '预览' : '编辑'}
            </Button>
          )}
          {url && (
            <Button
              size="small"
              icon={<LinkOutlined />}
              onClick={() => window.open(url, '_blank')}
            >
              新窗口
            </Button>
          )}
        </Space>
      }
      style={{ marginTop: 8, marginBottom: 8 }}
      bodyStyle={{ padding: 0 }}
    >
      {isEditing ? (
        <div style={{ padding: 12 }}>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            style={{
              width: '100%',
              height: 280,
              border: '1px solid #d9d9d9',
              borderRadius: 4,
              padding: 8,
              fontFamily: 'monospace',
              fontSize: 12,
              resize: 'vertical'
            }}
            placeholder="输入 HTML 内容..."
          />
        </div>
      ) : (
        renderPreview()
      )}
    </Card>
  )
}

export default WebPreviewCard
