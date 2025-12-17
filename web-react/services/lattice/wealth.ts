import request from '../../requestConfig';
import type { AssetAllocationItem, MonthlyExpenseItem } from './types';

export async function fetchAssetAllocation(): Promise<AssetAllocationItem[]> {
  return request('/wealth/assets');
}

export async function fetchMonthlyExpenses(): Promise<MonthlyExpenseItem[]> {
  return request('/wealth/expenses/monthly');
}
