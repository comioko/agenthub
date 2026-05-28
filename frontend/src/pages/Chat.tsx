import { useEffect, useState, useCallback, useRef } from 'react'
import { Layout, List, Input, Avatar, Card, Space, Badge, Button, Spin } from 'antd'
import { SendOutlined, PlusOutlined, RobotOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import axios from '../api/axios'
import { useWebSocket } from '../websocket/socket'
import type { Conversation, Message } from '../types'

const { Sider, Content } = Layout

const Chat = () => {
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [currentConv, setCurrentConv] = useState<Conversation | null>(null)
  const [messages, setMessages] = useState<Message[]>([])
  const [inputMsg, setInputMsg] = useState('')
  const [sending, setSending] = useState(false)
  const [loading, setLoading] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  // 处理 WebSocket 新消息
  const handleWebSocketMessage = useCallback((data: any) => {
    if (data.type === 'message' && data.conversationId === currentConv?.id) {
      const newMsg = data.message
      // 避免重复添加
      setMessages(prev => {
        if (prev.some(m => m.id === newMsg.id)) return prev
        return [...prev, newMsg]
      })
    }
  }, [currentConv])

  // 连接 WebSocket
  const { subscribe, unsubscribe } = useWebSocket(handleWebSocketMessage)

  // 初始化：检查登录状态
  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      navigate('/login')
      return
    }
    loadConversations()
  }, [])

  // 订阅当前会话
  useEffect(() => {
    if (currentConv) {
      subscribe(currentConv.id)
      loadMessages(currentConv.id)
      return () => unsubscribe(currentConv.id)
    }
  }, [currentConv, subscribe, unsubscribe])

  // 自动滚动到底部
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const loadConversations = async () => {
    try {
      const res = await axios.get('/conversations') as unknown as Conversation[]
      setConversations(res)
    } catch (error) {
      console.error('加载会话列表失败', error)
    }
  }

  const loadMessages = async (convId: number) => {
    setLoading(true)
    try {
      const res = await axios.get(`/conversations/${convId}/messages`) as unknown as Message[]
      setMessages(res)
    } catch (error) {
      console.error('加载消息失败', error)
    } finally {
      setLoading(false)
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

  const createGroupChat = async () => {
    try {
      const res = await axios.post('/conversations', {
        name: '群聊',
        type: 2  // group chat
      }) as unknown as Conversation
      setConversations([res, ...conversations])
      setCurrentConv(res)
    } catch (error) {
      console.error('创建群聊失败', error)
    }
  }

  const sendMessage = async () => {
    if (!inputMsg.trim() || !currentConv) return
    setSending(true)
    try {
      const res = await axios.post(`/conversations/${currentConv.id}/messages`, {
        content: inputMsg
      }) as unknown as Message
      // 清空输入框，消息会通过 WebSocket 接收后展示
      setInputMsg('')
      // 乐观更新：直接添加到消息列表
      setMessages(prev => [...prev, res])
    } catch (error) {
      console.error('发送消息失败', error)
    } finally {
      setSending(false)
    }
  }

  // 渲染消息
  const renderMessage = (msg: Message) => {
    const isUser = msg.senderType === 1
    const isSystem = msg.senderType === 3  // orchestrator or system

    // 根据 senderType 渲染不同的样式
    let bgColor = '#fff'
    let textColor = '#000'
    let avatarIcon = <RobotOutlined />

    if (isUser) {
      bgColor = '#1890ff'
      textColor = '#fff'
      avatarIcon = <UserOutlined />
    } else if (isSystem) {
      bgColor = '#f0f5ff'
      textColor = '#333'
      avatarIcon = <TeamOutlined />
    }

    return (
      <div
        key={msg.id}
        style={{
          display: 'flex',
          justifyContent: isUser ? 'flex-end' : 'flex-start',
          marginBottom: 12
        }}
      >
        {!isUser && (
          <Avatar icon={avatarIcon} style={{ marginRight: 8, background: isSystem ? '#722ed1' : '#52c41a' }} />
        )}
        <Card
          size="small"
          style={{
            maxWidth: '70%',
            background: bgColor,
            color: textColor,
            borderRadius: 12
          }}
          bodyStyle={{ padding: '8px 12px' }}
        >
          {isSystem && (
            <div style={{ fontSize: 12, color: '#888', marginBottom: 4 }}>
              助手
            </div>
          )}
          <pre style={{
            margin: 0,
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
            fontFamily: 'inherit'
          }}>
            {msg.content}
          </pre>
        </Card>
        {isUser && (
          <Avatar icon={avatarIcon} style={{ marginLeft: 8, background: '#1890ff' }} />
        )}
      </div>
    )
  }

  return (
    <Layout style={{ height: '100vh' }}>
      {/* 左侧会话列表 */}
      <Sider width={280} style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
        <div style={{ padding: 16 }}>
          <Space direction="vertical" style={{ width: '100%' }} size="small">
            <Button icon={<PlusOutlined />} block onClick={createConversation}>
              新建单聊
            </Button>
            <Button icon={<TeamOutlined />} block onClick={createGroupChat} type="dashed">
              新建群聊
            </Button>
          </Space>
        </div>

        <List
          dataSource={conversations}
          style={{ height: 'calc(100vh - 120px)', overflowY: 'auto' }}
          renderItem={(conv) => (
            <List.Item
              onClick={() => setCurrentConv(conv)}
              style={{
                padding: '12px 16px',
                cursor: 'pointer',
                background: currentConv?.id === conv.id ? '#e6f7ff' : 'transparent'
              }}
            >
              <List.Item.Meta
                avatar={
                  <Badge dot={false}>
                    <Avatar icon={conv.type === 1 ? <RobotOutlined /> : <TeamOutlined />} />
                  </Badge>
                }
                title={conv.name}
                description={
                  <Space size={4}>
                    <span>{conv.type === 1 ? '单聊' : '群聊'}</span>
                  </Space>
                }
              />
            </List.Item>
          )}
        />
      </Sider>

      {/* 右侧聊天区域 */}
      <Content style={{ display: 'flex', flexDirection: 'column', background: '#f5f5f5' }}>
        {currentConv ? (
          <>
            {/* 聊天头部 */}
            <div style={{
              padding: '12px 16px',
              background: '#fff',
              borderBottom: '1px solid #f0f0f0',
              display: 'flex',
              alignItems: 'center'
            }}>
              <Avatar icon={currentConv.type === 1 ? <RobotOutlined /> : <TeamOutlined />} style={{ marginRight: 8 }} />
              <span style={{ fontWeight: 500 }}>{currentConv.name}</span>
              <span style={{ marginLeft: 8, fontSize: 12, color: '#888' }}>
                {currentConv.type === 1 ? '单聊' : '群聊：@ 提及 Agent 协作'}
              </span>
            </div>

            {/* 消息列表 */}
            <div style={{ flex: 1, padding: 16, overflowY: 'auto' }}>
              {loading ? (
                <div style={{ textAlign: 'center', padding: 40 }}>
                  <Spin />
                </div>
              ) : (
                messages.map(renderMessage)
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* 输入区域 */}
            <div style={{ padding: 16, background: '#fff', borderTop: '1px solid #f0f0f0' }}>
              <Space.Compact style={{ width: '100%' }}>
                <Input
                  value={inputMsg}
                  onChange={(e) => setInputMsg(e.target.value)}
                  onPressEnter={sendMessage}
                  placeholder={currentConv.type === 2 ? "输入消息，@ 提及 Agent 如 @Code Assistant" : "输入消息..."}
                  disabled={sending}
                  style={{ borderRadius: 20 }}
                />
                <Button
                  type="primary"
                  icon={<SendOutlined />}
                  onClick={sendMessage}
                  loading={sending}
                  style={{ borderRadius: 20 }}
                >
                  发送
                </Button>
              </Space.Compact>
            </div>
          </>
        ) : (
          <div style={{
            flex: 1,
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            color: '#999',
            fontSize: 16
          }}>
            选择或创建一个会话开始聊天
          </div>
        )}
      </Content>
    </Layout>
  )
}

export default Chat
