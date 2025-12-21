import { extend } from 'umi-request';
import { message } from 'antd';

const TOKEN_KEY = 'lattice_token';

const request = extend({
  prefix: '/api',
  timeout: 20000,
  errorHandler: (error) => {
    const { response } = error;
    if (response && (response.status === 401 || response.status === 403)) {
      localStorage.removeItem(TOKEN_KEY);
      message.error('登录已过期或无权限，请重新登录');
      window.location.href = '/login';
    }
    throw error;
  },
});

request.interceptors.request.use((url, options) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    options.headers = {
      ...options.headers,
      Authorization: `Bearer ${token}`,
    };
  }
  return { url, options };
});

request.interceptors.response.use(async (response) => {
  // Check for HTTP error statuses first
  if (response.status === 401 || response.status === 403) {
    const error: any = new Error(response.statusText);
    error.response = response;
    throw error;
  }

  const cloned = response.clone();
  try {
    const data = await cloned.json();
    if (data && data.code && data.code !== 'SUCCESS') {
      message.error(data.message || '请求失败');
      throw new Error(data.message || 'Request failed');
    }
    if (data && Object.prototype.hasOwnProperty.call(data, 'data')) {
      return data.data;
    }
    return data;
  } catch (e) {
    return response;
  }
});

export { TOKEN_KEY };
export default request;
