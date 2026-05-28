import axios from 'axios'

export interface Agent {
  id: number
  name: string
  description?: string
  avatarUrl?: string
  systemPrompt: string
  provider: string
  isPublic: boolean
}

export async function getAgents(): Promise<Agent[]> {
  return axios.get('/agents')
}
