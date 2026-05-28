import { useState, useEffect, useRef } from 'react'
import { Popover, Avatar, List, Typography } from 'antd'
import { RobotOutlined } from '@ant-design/icons'
import { getAgents, Agent } from '../api/agent'

const { Text } = Typography

interface MentionAutocompleteProps {
  visible: boolean
  position: { top: number; left: number }
  onSelect: (agentName: string) => void
  onClose: () => void
}

const MentionAutocomplete = ({ visible, position, onSelect, onClose }: MentionAutocompleteProps) => {
  const [agents, setAgents] = useState<Agent[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (visible) {
      loadAgents()
    }
  }, [visible])

  const loadAgents = async () => {
    setLoading(true)
    try {
      const data = await getAgents()
      setAgents(data)
    } catch (error) {
      console.error('加载 Agent 列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  if (!visible) return null

  return (
    <div
      style={{
        position: 'fixed',
        top: position.top,
        left: position.left,
        zIndex: 1000,
        background: '#fff',
        borderRadius: 8,
        boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        maxHeight: 300,
        overflow: 'auto',
        minWidth: 250
      }}
    >
      <List
        loading={loading}
        dataSource={agents}
        renderItem={(agent) => (
          <List.Item
            onClick={() => onSelect(`"${agent.name}" `)}
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
    </div>
  )
}

export default MentionAutocomplete
