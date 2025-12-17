import request from '../../requestConfig';
import type { LoginRequest, LoginResponse } from './types';

export async function login(req: LoginRequest): Promise<LoginResponse> {
  return request('/auth/login', {
    method: 'POST',
    data: req,
  });
}
