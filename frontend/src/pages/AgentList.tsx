import { useEffect, useState } from 'react'
import { Card, Row, Col, Avatar, Tag, Button, Modal, Form, Input, message } from 'antd'
import { RobotOutlined, PlusOutlined, MessageOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getAgents, Agent } from '../api/agent'
import axios from '../api/axios'

const AgentList = () => {
  const [agents, setAgents] = useState<Agent[]>([])
  const [loading, setLoading] = useState(false)
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  useEffect(() => {
    loadAgents()
  }, [])

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

  const handleStartChat = async (agent: Agent) => {
    try {
      // 创建单聊会话
      await axios.post('/conversations', {
        name: `与 ${agent.name} 的对话`,
        type: 1,
        agentId: agent.id
      })
      message.success('会话创建成功')
      navigate('/chat')
    } catch (error) {
      console.error('创建会话失败', error)
      message.error('创建会话失败')
    }
  }

  const handleCreateAgent = async (values: any) => {
    try {
      await axios.post('/agents', {
        ...values,
        isPublic: false
      })
      message.success('Agent 创建成功')
      setCreateModalVisible(false)
      form.resetFields()
      loadAgents()
    } catch (error) {
      console.error('创建 Agent 失败', error)
      message.error('创建 Agent 失败')
    }
  }

  const renderAgentCard = (agent: Agent) => (
    <Col xs={24} sm={12} md={8} lg={6} key={agent.id}>
      <Card
        hoverable
        cover={
          <div style={{
            height: 120,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
          }}>
            <Avatar size={64} icon={<RobotOutlined />} style={{ background: 'rgba(255,255,255,0.2)' }} />
          </div>
        }
        actions={[
          <Button
            type="text"
            icon={<MessageOutlined />}
            onClick={() => handleStartChat(agent)}
            key="chat"
          >
            开始对话
          </Button>
        ]}
      >
        <Card.Meta
          title={agent.name}
          description={
            <div>
              <Tag color="blue">{agent.provider}</Tag>
              {agent.model && <Tag color="green">{agent.model}</Tag>}
              <p style={{ marginTop: 8, color: '#666', fontSize: 13 }}>
                {agent.description || '暂无描述'}
              </p>
            </div>
          }
        />
      </Card>
    </Col>
  )

  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>可用 Agent</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
          创建 Agent
        </Button>
      </div>

      <Row gutter={[16, 16]}>
        {agents.map(renderAgentCard)}
      </Row>

      {agents.length === 0 && !loading && (
        <div style={{ textAlign: 'center', padding: 48, color: '#999' }}>
          暂无 Agent，请创建或联系管理员添加
        </div>
      )}

      <Modal
        title="创建 Agent"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreateAgent}>
          <Form.Item
            name="name"
            label="名称"
            rules={[{ required: true, message: '请输入 Agent 名称' }]}
          >
            <Input placeholder="例如：代码助手" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea placeholder="简单描述这个 Agent 的功能..." />
          </Form.Item>

          <Form.Item
            name="systemPrompt"
            label="系统提示词"
            rules={[{ required: true, message: '请输入系统提示词' }]}
          >
            <Input.TextArea
              rows={4}
              placeholder="定义 Agent 的角色和行为，例如：你是专业的代码助手..."
            />
          </Form.Item>

          <Form.Item
            name="provider"
            label="Provider"
            initialValue="custom"
          >
            <Input disabled />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              创建
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default AgentList
