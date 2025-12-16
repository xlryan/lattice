import React from 'react';
import { Copy, Terminal } from 'lucide-react';

const CodeBlock: React.FC<{ title: string; code: string; lang?: string }> = ({ title, code, lang = 'bash' }) => (
  <div className="my-6 rounded-lg overflow-hidden border border-border">
    <div className="bg-slate-900 px-4 py-2 border-b border-border flex justify-between items-center">
      <span className="text-xs font-mono text-slate-400">{title}</span>
      <Copy size={14} className="text-slate-500 cursor-pointer hover:text-slate-300" />
    </div>
    <pre className="bg-[#0f172a] p-4 overflow-x-auto">
      <code className="text-sm font-mono text-emerald-400">{code}</code>
    </pre>
  </div>
);

export const SetupGuide: React.FC = () => {
  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-12">
      <div className="border-b border-border pb-6">
        <h1 className="text-3xl font-bold text-slate-100 mb-2">Project Setup & Configuration</h1>
        <p className="text-slate-400">Step-by-step guide to initializing the actual Ant Design Pro project for Lattice.</p>
      </div>

      <section>
        <h2 className="text-xl font-semibold text-primary mb-4 flex items-center gap-2">
          <span className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center text-sm">1</span>
          Initialization & Cleaning
        </h2>
        <p className="text-slate-300 mb-4">Run the following command to initialize UmiJS with Ant Design Pro V6 Simple template.</p>
        <CodeBlock 
          title="Terminal" 
          code="pnpm create umi lattice-frontend"
        />
        <ul className="list-disc pl-5 space-y-2 text-slate-300 text-sm">
          <li>Select <strong className="text-slate-100">Simple App</strong> (Recommended for clean slate).</li>
          <li>Select <strong className="text-slate-100">Ant Design Pro</strong>.</li>
          <li>Select <strong className="text-slate-100">TypeScript</strong>.</li>
          <li>Select <strong className="text-slate-100">pnpm</strong>.</li>
        </ul>
        
        <div className="mt-4 p-4 bg-slate-900/50 rounded-lg border border-slate-700">
          <h4 className="text-sm font-bold text-slate-200 mb-2">Files to Clean:</h4>
          <ul className="text-xs font-mono text-slate-400 space-y-1">
            <li className="text-red-400">- src/locales/** (If you don't need i18n)</li>
            <li className="text-red-400">- src/e2e/** (Remove sample tests)</li>
            <li className="text-red-400">- src/pages/Welcome.tsx (Default page)</li>
            <li className="text-yellow-400">! Update src/app.tsx to remove 'login' redirection logic if using custom auth.</li>
          </ul>
        </div>
      </section>

      <section>
        <h2 className="text-xl font-semibold text-primary mb-4 flex items-center gap-2">
          <span className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center text-sm">2</span>
          Route Configuration
        </h2>
        <p className="text-slate-300 mb-4">Paste this into <code className="text-emerald-400 bg-slate-900 px-1 py-0.5 rounded">config/routes.ts</code>.</p>
        <CodeBlock 
          title="config/routes.ts" 
          lang="typescript"
          code={`export default [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    name: 'Dashboard',
    path: '/dashboard',
    icon: 'dashboard',
    component: './Dashboard',
  },
  {
    name: 'Lattice Brain',
    icon: 'api',
    path: '/brain',
    routes: [
      {
        name: 'Search',
        path: 'search',
        component: './Brain/Search',
      },
      {
        name: 'AI Chat',
        path: 'chat',
        component: './Brain/Chat',
      },
    ],
  },
  {
    name: 'Data Governance',
    icon: 'database',
    path: '/data',
    routes: [
      {
        name: 'Career Nodes',
        path: 'career',
        component: './Data/Career',
      },
      {
        name: 'DIY Projects',
        path: 'diy',
        component: './Data/Diy',
      },
      {
        name: 'Finance',
        path: 'finance',
        component: './Data/Finance',
      },
    ],
  },
  {
    name: 'Settings',
    path: '/settings',
    icon: 'setting',
    component: './Settings',
  },
  {
    component: './404',
  },
];`}
        />
      </section>

      <section>
        <h2 className="text-xl font-semibold text-primary mb-4 flex items-center gap-2">
          <span className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center text-sm">3</span>
          Proxy Setup
        </h2>
        <p className="text-slate-300 mb-4">Configure the proxy in <code className="text-emerald-400 bg-slate-900 px-1 py-0.5 rounded">config/proxy.ts</code> to forward API requests to Spring Boot.</p>
        <CodeBlock 
          title="config/proxy.ts" 
          lang="typescript"
          code={`/**
 * @name Proxy Config
 * @see https://umijs.org/docs/guides/proxy
 */
export default {
  // Local development proxy
  dev: {
    '/api/': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      pathRewrite: { '^': '' }, 
      // If backend is strictly /api/v1, remove pathRewrite or adjust accordingly
    },
  },
};`}
        />
      </section>
    </div>
  );
};