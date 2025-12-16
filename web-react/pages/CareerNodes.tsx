import React, { useState } from 'react';
import { MoreHorizontal, Search, Plus, Filter, Code } from 'lucide-react';
import { CareerNode } from '../types';

// Mock Data
const MOCK_DATA: CareerNode[] = [
  {
    id: '1',
    dateRange: '2022-至今',
    company: '科技巨头集团',
    role: '高级前端架构师',
    tags: ['React', 'Architecture', 'Team Lead'],
    properties: { teamSize: 12, stack: 'Next.js, GraphQL', achievements: ['构建时间减少 50%', '发布全新设计系统'] }
  },
  {
    id: '2',
    dateRange: '2019-2022',
    company: '创新科技 (InnovateStart)',
    role: '全栈工程师',
    tags: ['Vue.js', 'Spring Boot', 'AWS'],
    properties: { teamSize: 5, stack: 'Vue 2, Java 11', exitReason: '公司被收购' }
  },
  {
    id: '3',
    dateRange: '2017-2019',
    company: 'DevHouse 外包工厂',
    role: '初级开发',
    tags: ['jQuery', 'PHP', 'MySQL'],
    properties: { legacy: true, maintenance: '维护旧系统' }
  },
];

const JsonCell: React.FC<{ data: Record<string, any> }> = ({ data }) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="relative">
      <button 
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 text-xs font-mono text-primary hover:text-emerald-400 transition-colors"
      >
        <Code size={12} />
        {Object.keys(data).length} 属性
      </button>
      
      {isOpen && (
        <>
          <div className="fixed inset-0 z-10" onClick={() => setIsOpen(false)}></div>
          <div className="absolute top-full left-0 mt-2 w-64 p-3 bg-slate-900 border border-border rounded-lg shadow-xl z-20 text-xs font-mono">
            <pre className="whitespace-pre-wrap text-slate-300">
              {JSON.stringify(data, null, 2)}
            </pre>
          </div>
        </>
      )}
    </div>
  );
};

export const CareerNodes: React.FC = () => {
  return (
    <div className="space-y-4">
      {/* Header / Actions */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-surface p-4 rounded-lg border border-border">
        <h2 className="text-lg font-semibold text-slate-100">职业生涯节点 (Career Nodes)</h2>
        <div className="flex gap-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
            <input 
              type="text" 
              placeholder="搜索节点..." 
              className="pl-9 pr-4 py-1.5 bg-slate-900 border border-slate-700 rounded text-sm text-slate-200 focus:outline-none focus:border-primary w-full sm:w-64"
            />
          </div>
          <button className="p-2 bg-slate-800 text-slate-300 rounded border border-slate-700 hover:bg-slate-700">
            <Filter size={16} />
          </button>
          <button className="flex items-center gap-2 px-3 py-1.5 bg-primary text-slate-900 font-medium rounded hover:bg-emerald-400 transition-colors">
            <Plus size={16} />
            <span className="hidden sm:inline">新建节点</span>
          </button>
        </div>
      </div>

      {/* Table Surface */}
      <div className="bg-surface rounded-lg border border-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-900/50 border-b border-border text-slate-400 font-medium uppercase text-xs tracking-wider">
              <tr>
                <th className="px-6 py-4">时间范围</th>
                <th className="px-6 py-4">公司/组织</th>
                <th className="px-6 py-4">角色/职位</th>
                <th className="px-6 py-4">技术标签</th>
                <th className="px-6 py-4">元数据 (JSON)</th>
                <th className="px-6 py-4 text-right">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {MOCK_DATA.map((node) => (
                <tr key={node.id} className="hover:bg-slate-700/20 transition-colors">
                  <td className="px-6 py-4 font-mono text-slate-400">{node.dateRange}</td>
                  <td className="px-6 py-4 font-medium text-slate-200">{node.company}</td>
                  <td className="px-6 py-4 text-slate-300">{node.role}</td>
                  <td className="px-6 py-4">
                    <div className="flex flex-wrap gap-1">
                      {node.tags.map(tag => (
                        <span key={tag} className="px-2 py-0.5 rounded-full bg-slate-800 border border-slate-700 text-xs text-slate-400">
                          {tag}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <JsonCell data={node.properties} />
                  </td>
                  <td className="px-6 py-4 text-right">
                    <button className="text-slate-500 hover:text-primary transition-colors">
                      <MoreHorizontal size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="px-6 py-4 border-t border-border flex justify-between items-center text-xs text-slate-500">
          <span>显示 3 条中的 1-3 条</span>
          <div className="flex gap-1">
            <button className="px-3 py-1 bg-slate-800 rounded border border-slate-700 disabled:opacity-50">上一页</button>
            <button className="px-3 py-1 bg-slate-800 rounded border border-slate-700">下一页</button>
          </div>
        </div>
      </div>
    </div>
  );
};