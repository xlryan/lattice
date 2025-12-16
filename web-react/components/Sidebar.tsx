import React from 'react';
import { 
  LayoutDashboard, 
  Search, 
  MessageSquare, 
  Briefcase, 
  Hammer, 
  DollarSign, 
  Settings,
  Binary
} from 'lucide-react';
import { PageView } from '../types';

interface SidebarProps {
  currentPage: PageView;
  onNavigate: (page: PageView) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentPage, onNavigate }) => {
  const menuGroups = [
    {
      title: '核心功能',
      items: [
        { id: 'dashboard', label: '仪表盘 (Dashboard)', icon: LayoutDashboard },
      ]
    },
    {
      title: '晶格大脑',
      items: [
        { id: 'search', label: '混合检索 (Search)', icon: Search },
        { id: 'chat', label: 'AI 助手 (Chat)', icon: MessageSquare },
      ]
    },
    {
      title: '数据治理',
      items: [
        { id: 'career', label: '职业生涯 (Career)', icon: Briefcase },
        { id: 'diy', label: '手工项目 (Projects)', icon: Hammer },
        { id: 'finance', label: '财务账单 (Finance)', icon: DollarSign },
      ]
    },
    {
      title: '系统管理',
      items: [
        { id: 'settings', label: '全局设置', icon: Settings },
      ]
    }
  ];

  return (
    <div className="w-64 h-full bg-surface border-r border-border flex flex-col">
      <div className="p-6 border-b border-border flex items-center gap-3">
        <div className="p-2 bg-primary/10 rounded-lg">
          <Binary className="text-primary" size={24} />
        </div>
        <div>
          <h1 className="font-bold font-mono tracking-tight text-slate-100">LATTICE</h1>
          <p className="text-[10px] text-slate-500 uppercase tracking-widest">Life OS v1.0</p>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto py-6 px-3 space-y-8">
        {menuGroups.map((group) => (
          <div key={group.title}>
            <h3 className="px-3 mb-2 text-[10px] font-bold text-slate-500 uppercase tracking-wider">
              {group.title}
            </h3>
            <div className="space-y-1">
              {group.items.map((item) => {
                const Icon = item.icon;
                const isActive = currentPage === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => onNavigate(item.id as PageView)}
                    className={`w-full flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-all duration-200 group
                      ${isActive 
                        ? 'bg-primary/10 text-primary font-medium' 
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-700/50'
                      }`}
                  >
                    <Icon size={18} className={isActive ? 'text-primary' : 'text-slate-500 group-hover:text-slate-300'} />
                    {item.label}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      <div className="p-4 border-t border-border">
        <div className="flex items-center gap-3 px-2">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-primary to-secondary"></div>
          <div className="flex-1 overflow-hidden">
            <p className="text-sm font-medium text-slate-200 truncate">管理员</p>
            <p className="text-xs text-slate-500 truncate">admin@lattice.local</p>
          </div>
        </div>
      </div>
    </div>
  );
};