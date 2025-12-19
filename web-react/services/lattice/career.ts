import request from '../../requestConfig';
import type { CareerNode, CreateCareerLogRequest } from './types';

export async function fetchCareerNodes(): Promise<CareerNode[]> {
  return request('/career/list');
}

export async function createCareerLog(data: CreateCareerLogRequest): Promise<CareerNode> {
  return request('/career/logs', {
    method: 'POST',
    data,
  });
}

export async function searchCareerNodes(q: string): Promise<CareerNode[]> {
  return request('/career/search', {
    params: { q },
  });
}
