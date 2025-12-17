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

export interface AssetAllocationItem {
  label: string;
  value: number;
}

export interface MonthlyExpenseItem {
  month: string;
  amount: number;
}
