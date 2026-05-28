export interface User {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
  email?: string
}

export interface Conversation {
  id: number
  name: string
  type: number // 1=single, 2=group
  ownerId: number
  createdAt: string
  updatedAt: string
  lastMessageAt: string
}

export interface Message {
  id: number
  conversationId: number
  senderId: number
  senderType: number // 1=user, 2=agent, 3=orchestrator
  content: string
  messageType: number // 1=text, 2=artifact, 3=system
  createdAt: string
  blocks?: MessageBlock[]
}

export interface MessageBlock {
  id: number
  messageId: number
  blockType: string // code/diff/web/file/deploy
  content: string
  language?: string
  title?: string
  metadata?: Record<string, any>
}

export interface Agent {
  id: number
  name: string
  description?: string
  avatarUrl?: string
  systemPrompt: string
  provider: string
  providerAgentId?: string
  model?: string
  tools?: any[]
  ownerId?: number
  isPublic: boolean
}
