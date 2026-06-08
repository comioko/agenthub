import { useEffect, useRef, useCallback } from 'react'

type MessageHandler = (data: any) => void

class WebSocketClient {
  private ws: WebSocket | null = null
  private url: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 3000
  private handlers: Map<string, Set<MessageHandler>> = new Map()
  private isConnecting = false

  constructor() {
    const token = localStorage.getItem('token') || ''
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080'
    this.url = `${wsUrl}/ws?token=${token}`
  }

  connect() {
    if (this.isConnecting || (this.ws && this.ws.readyState === WebSocket.OPEN)) {
      return
    }

    this.isConnecting = true
    console.log('WebSocket connecting...')

    try {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        console.log('WebSocket connected')
        this.isConnecting = false
        this.reconnectAttempts = 0
        this.emit('connected', { status: 'connected' })
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('WebSocket message:', data)
          this.emit(data.type, data)
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e)
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
        this.isConnecting = false
      }

      this.ws.onclose = () => {
        console.log('WebSocket closed')
        this.isConnecting = false
        this.ws = null
        this.emit('disconnected', { status: 'disconnected' })
        this.attemptReconnect()
      }
    } catch (e) {
      console.error('WebSocket connection failed:', e)
      this.isConnecting = false
    }
  }

  private attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('Max reconnection attempts reached')
      return
    }

    this.reconnectAttempts++
    console.log(`Reconnecting... attempt ${this.reconnectAttempts}`)
    setTimeout(() => this.connect(), this.reconnectDelay)
  }

  disconnect() {
    this.maxReconnectAttempts = 0
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  subscribe(conversationId: number) {
    this.send({ type: 'subscribe', conversationId })
  }

  unsubscribe(conversationId: number) {
    this.send({ type: 'unsubscribe', conversationId })
  }

  send(data: object) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    } else {
      console.warn('WebSocket not connected, cannot send message')
    }
  }

  on(type: string, handler: MessageHandler) {
    if (!this.handlers.has(type)) {
      this.handlers.set(type, new Set())
    }
    this.handlers.get(type)!.add(handler)
  }

  off(type: string, handler: MessageHandler) {
    this.handlers.get(type)?.delete(handler)
  }

  private emit(type: string, data: any) {
    this.handlers.get(type)?.forEach(handler => handler(data))
  }
}

// Singleton instance
let socketClient: WebSocketClient | null = null

export function getSocketClient(): WebSocketClient {
  if (!socketClient) {
    socketClient = new WebSocketClient()
  }
  return socketClient
}

export function useWebSocket(onMessage: MessageHandler) {
  const clientRef = useRef(getSocketClient())

  useEffect(() => {
    const client = clientRef.current

    client.on('message', onMessage)
    client.on('connected', () => console.log('WebSocket hook: connected'))
    client.on('disconnected', () => console.log('WebSocket hook: disconnected'))

    client.connect()

    return () => {
      client.off('message', onMessage)
    }
  }, [onMessage])

  return {
    subscribe: useCallback((convId: number) => clientRef.current.subscribe(convId), []),
    unsubscribe: useCallback((convId: number) => clientRef.current.unsubscribe(convId), []),
    disconnect: useCallback(() => clientRef.current.disconnect(), [])
  }
}
