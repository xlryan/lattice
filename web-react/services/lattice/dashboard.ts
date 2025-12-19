import request from '../../requestConfig';
import type { DashboardStats } from './types';

export async function fetchDashboardStats(): Promise<DashboardStats> {
  return request('/dashboard/stats');
}
