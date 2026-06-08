import { useState, useEffect } from 'react'
import { Avatar, List, Typography, Spin } from 'antd'
import { RobotOutlined } from '@ant-design/icons'
import { getAgents, Agent } from '../api/agent'

const { Text } = Typography

interface MentionAutocompleteProps {
  visible: boolean
  position: { top: number; left: number }
  onSelect: (agentName: string) => void
  onClose: () => void
}

const MentionAutocomplete = ({ visible, position, onSelect }: MentionAutocompleteProps) => {
  const [agents, setAgents] = useState<Agent[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (visible) {
      loadAgents()
    }
  }, [visible])

  const loadAgents = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getAgents()
      setAgents(data)
    } catch (err) {
      console.error('Error loading agents:', err)
      setError('加载失败')
    } finally {
      setLoading(false)
    }
  }

  if (!visible) return null

  return (
    <div
      data-testid="mention-autocomplete"
      style={{
        position: 'fixed',
        top: position.top || 100,
        left: position.left || 0,
        zIndex: 10000,
        background: '#fff',
        borderRadius: 8,
        boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        maxHeight: 300,
        overflow: 'auto',
        minWidth: 300
      }}
    >
      {loading && (
        <div style={{ padding: 20, textAlign: 'center' }}>
          <Spin size="small" />
        </div>
      )}
      {error && (
        <div style={{ padding: 20, textAlign: 'center', color: 'red' }}>
          {error}
        </div>
      )}
      {!loading && !error && agents.length === 0 && (
        <div style={{ padding: 20, textAlign: 'center', color: '#999' }}>
          暂无 Agent
        </div>
      )}
      {!loading && !error && agents.length > 0 && (
        <List
          dataSource={agents}
          renderItem={(agent) => (
            <List.Item
              onClick={() => {
                onSelect(`"${agent.name}" `)
              }}
              style={{
                padding: '8px 12px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center'
              }}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLElement).style.background = '#f5f5f5'
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLElement).style.background = '#fff'
              }}
            >
              <Avatar
                icon={<RobotOutlined />}
                style={{ marginRight: 8, background: '#52c41a' }}
              />
              <div>
                <Text strong>{agent.name}</Text>
                <br />
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {agent.description || agent.provider}
                </Text>
              </div>
            </List.Item>
          )}
        />
      )}
    </div>
  )
}

export default MentionAutocomplete
