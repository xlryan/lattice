import type { ProxyOptions } from 'vite';

const proxy: Record<string, Record<string, ProxyOptions>> = {
  dev: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
  prod: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
};

export default proxy;
