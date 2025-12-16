export interface CareerNode {
  id: string;
  dateRange: string;
  company: string;
  role: string;
  tags: string[];
  properties: Record<string, any>; // Complex JSON data
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

export interface SearchResult {
  id: string;
  title: string;
  snippet: string;
  score: number; // 0.0 - 1.0
  source?: string;
  tags?: string[];
  date?: string;
}

export interface FinanceRecord {
  id: string;
  date: string;
  description: string;
  amount: number;
  type: 'expense' | 'income';
  category: string;
  account: string;
}

export interface DiyProject {
  id: string;
  name: string;
  status: 'planning' | 'in-progress' | 'completed' | 'on-hold';
  progress: number;
  budget: number;
  spent: number;
  deadline: string;
}

export type PageView = 'dashboard' | 'search' | 'chat' | 'career' | 'diy' | 'finance' | 'settings';
