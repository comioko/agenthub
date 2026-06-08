import axios from '../api/axios'

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

export async function getAgents(): Promise<Agent[]> {
  return axios.get('/agents') as unknown as Agent[]
}

export async function createAgent(agent: Partial<Agent>): Promise<Agent> {
  return axios.post('/agents', agent) as unknown as Agent
}
