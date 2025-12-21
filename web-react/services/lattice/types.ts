export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  traceId: string;
  timestamp: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface CareerNode {
  id: string;
  type: string;
  rawContent: string;
  structuredData: Record<string, any>;
  createdAt: string;
  tags: string[];
}

export interface CreateCareerLogRequest {
  rawText: string;
  type: string;
  tags?: string[];
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

export interface AgentResponse {
  intent: string;
  message: string;
  sessionId?: string;
}

export interface FinanceRecord {
  id: string;
  date: string;
  description: string;
  amount: number;
  type: 'income' | 'expense' | 'withdrawal' | 'deposit' | 'transfer';
  category: string;
  account: string;
}

export interface AssetAllocationItem {
  label: string;
  value: number;
}

export interface MonthlyExpenseItem {
  month: string;
  amount: number;
}

export interface DashboardStats {
  netWorth: string;
  nodeCount: number;
  activeProjects: number;
  healthScore: number;
  monthlyFlow: Array<{ name: string; income: number; expense: number }>;
  recentUpdates: Array<{ id: string; content: string; time: string }>;
}
