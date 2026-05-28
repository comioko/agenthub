export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
}

export interface CreateConversationRequest {
  name: string
  type: number
}

export interface SendMessageRequest {
  content: string
}
