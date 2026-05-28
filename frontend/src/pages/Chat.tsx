import { useEffect, useState } from 'react'
import { Layout, List, Input, Avatar, Card, Space, Badge, Button } from 'antd'
import { SendOutlined, PlusOutlined, RobotOutlined, UserOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import type { Conversation, Message } from '../types'

const { Sider, Content } = Layout

const Chat = () => {
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [currentConv, setCurrentConv] = useState<Conversation | null>(null)
  const [messages, setMessages] = useState<Message[]>([])
  const [inputMsg, setInputMsg] = useState('')
  const [sending, setSending] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      navigate('/login')
      return
    }
    loadConversations()
  }, [])

  useEffect(() => {
    if (currentConv) {
      loadMessages(currentConv.id)
    }
  }, [currentConv])

  const loadConversations = async () => {
    try {
      const res = await axios.get('/conversations') as unknown as Conversation[]
      setConversations(res)
    } catch (error) {
      console.error('加载会话列表失败', error)
    }
  }

  const loadMessages = async (convId: number) => {
    try {
      const res = await axios.get(`/conversations/${convId}/messages`) as unknown as Message[]
      setMessages(res)
    } catch (error) {
      console.error('加载消息失败', error)
    }
  }

  const createConversation = async () => {
    try {
      const res = await axios.post('/conversations', {
        name: '新会话',
        type: 1
      }) as unknown as Conversation
      setConversations([res, ...conversations])
      setCurrentConv(res)
    } catch (error) {
      console.error('创建会话失败', error)
    }
  }

  const sendMessage = async () => {
    if (!inputMsg.trim() || !currentConv) return
    setSending(true)
    try {
      const res = await axios.post(`/conversations/${currentConv.id}/messages`, {
        content: inputMsg
      }) as unknown as Message
      setMessages([...messages, res])
      setInputMsg('')
      loadConversations()
    } catch (error) {
      console.error('发送消息失败', error)
    } finally {
      setSending(false)
    }
  }

  const renderMessage = (msg: Message) => {
    const isUser = msg.senderType === 1
    return (
      <div style={{ display: 'flex', justifyContent: isUser ? 'flex-end' : 'flex-start', marginBottom: 16 }}>
        {!isUser && <Avatar icon={<RobotOutlined />} style={{ marginRight: 8 }} />}
        <Card size="small" style={{ maxWidth: '70%', background: isUser ? '#1890ff' : '#fff', color: isUser ? '#fff' : '#000' }}>
          <p>{msg.content}</p>
        </Card>
        {isUser && <Avatar icon={<UserOutlined />} style={{ marginLeft: 8 }} />}
      </div>
    )
  }

  return (
    <Layout style={{ height: '100vh' }}>
      <Sider width={300} style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
        <div style={{ padding: 16 }}>
          <Button icon={<PlusOutlined />} block onClick={createConversation}>
            新建会话
          </Button>
        </div>
        <List
          dataSource={conversations}
          renderItem={(conv) => (
            <List.Item
              onClick={() => setCurrentConv(conv)}
              style={{ padding: '12px 16px', cursor: 'pointer', background: currentConv?.id === conv.id ? '#f0f5ff' : 'transparent' }}
            >
              <List.Item.Meta
                avatar={<Badge count={0}><Avatar icon={<RobotOutlined />} /></Badge>}
                title={conv.name}
                description={<Space>{conv.type === 1 ? '单聊' : '群聊'}</Space>}
              />
            </List.Item>
          )}
        />
      </Sider>
      <Content style={{ display: 'flex', flexDirection: 'column' }}>
        {currentConv ? (
          <>
            <div style={{ flex: 1, padding: 16, overflowY: 'auto' }}>
              {messages.map(renderMessage)}
            </div>
            <div style={{ padding: 16, borderTop: '1px solid #f0f0f0' }}>
              <Space.Compact style={{ width: '100%' }}>
                <Input
                  value={inputMsg}
                  onChange={(e) => setInputMsg(e.target.value)}
                  onPressEnter={sendMessage}
                  placeholder="输入消息..."
                  disabled={sending}
                />
                <Button type="primary" icon={<SendOutlined />} onClick={sendMessage} loading={sending}>
                  发送
                </Button>
              </Space.Compact>
            </div>
          </>
        ) : (
          <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', color: '#999' }}>
            选择或创建一个会话开始聊天
          </div>
        )}
      </Content>
    </Layout>
  )
}

export default Chat
