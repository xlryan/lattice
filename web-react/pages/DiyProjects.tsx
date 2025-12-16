import React from 'react';
import { Hammer, Clock, CheckCircle2, AlertCircle, MoreHorizontal } from 'lucide-react';
import { DiyProject } from '../types';

const MOCK_PROJECTS: DiyProject[] = [
  { id: '1', name: '智能家居中控改造', status: 'in-progress', progress: 65, budget: 5000, spent: 3200, deadline: '2024-05-01' },
  { id: '2', name: '阳台自动灌溉系统', status: 'planning', progress: 10, budget: 800, spent: 0, deadline: '2024-06-15' },
  { id: '3', name: '书房隔音优化', status: 'completed', progress: 100, budget: 2000, spent: 2150, deadline: '2024-02-20' },
  { id: '4', name: 'NAS 服务器升级', status: 'on-hold', progress: 40, budget: 8000, spent: 4500, deadline: '2024-04-10' },
];

const StatusBadge: React.FC<{ status: DiyProject['status'] }> = ({ status }) => {
  const styles = {
    'planning': 'bg-blue-500/10 text-blue-400 border-blue-500/20',
    'in-progress': 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    'completed': 'bg-slate-700 text-slate-400 border-slate-600',
    'on-hold': 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
  };
  
  const labels = {
    'planning': '规划中',
    'in-progress': '进行中',
    'completed': '已完成',
    'on-hold': '搁置',
  };

  return (
    <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium border ${styles[status]}`}>
      {labels[status]}
    </span>
  );
};

export const DiyProjects: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center bg-surface p-4 rounded-lg border border-border">
        <div>
          <h2 className="text-lg font-semibold text-slate-100 flex items-center gap-2">
            <Hammer className="text-primary" size={20} /> 手工与装修项目
          </h2>
          <p className="text-xs text-slate-400 mt-1">管理你的物理世界改造计划</p>
        </div>
        <button className="bg-primary text-slate-900 px-4 py-2 rounded font-medium hover:bg-emerald-400 transition-colors">
          创建新项目
        </button>
      </div>

      <div className="bg-surface border border-border rounded-lg overflow-hidden">
        <table className="w-full text-sm text-left">
          <thead className="bg-slate-900 text-slate-400 font-medium text-xs uppercase">
            <tr>
              <th className="px-6 py-4">项目名称</th>
              <th className="px-6 py-4">状态</th>
              <th className="px-6 py-4 w-1/4">进度</th>
              <th className="px-6 py-4">预算 vs 支出</th>
              <th className="px-6 py-4">截止日期</th>
              <th className="px-6 py-4 text-right">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {MOCK_PROJECTS.map(project => (
              <tr key={project.id} className="hover:bg-slate-700/20 transition-colors">
                <td className="px-6 py-4">
                  <span className="font-medium text-slate-200">{project.name}</span>
                </td>
                <td className="px-6 py-4">
                  <StatusBadge status={project.status} />
                </td>
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="flex-1 h-2 bg-slate-800 rounded-full overflow-hidden">
                      <div 
                        className={`h-full rounded-full ${project.progress === 100 ? 'bg-slate-500' : 'bg-primary'}`} 
                        style={{ width: `${project.progress}%` }}
                      ></div>
                    </div>
                    <span className="text-xs font-mono text-slate-400 w-8">{project.progress}%</span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-col text-xs font-mono">
                    <span className="text-slate-400">预算: ¥{project.budget}</span>
                    <span className={`${project.spent > project.budget ? 'text-red-400' : 'text-emerald-400'}`}>
                      支出: ¥{project.spent}
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4 font-mono text-slate-400">
                  {project.deadline}
                </td>
                <td className="px-6 py-4 text-right">
                  <button className="text-slate-500 hover:text-slate-200">
                    <MoreHorizontal size={18} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};