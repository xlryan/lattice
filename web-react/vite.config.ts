import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import proxy from './proxy';

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, '.', '');
    const envProxy = proxy[mode] || proxy.dev;
    return {
      server: {
        port: 3000,
        host: '0.0.0.0',
        proxy: envProxy,
      },
      plugins: [react()],
      define: {
        'process.env.API_KEY': JSON.stringify(env.GEMINI_API_KEY),
        'process.env.GEMINI_API_KEY': JSON.stringify(env.GEMINI_API_KEY)
      },
      resolve: {
        alias: {
          '@': path.resolve(__dirname, '.'),
        }
      }
    };
});
