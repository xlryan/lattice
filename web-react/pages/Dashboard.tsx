import React, { useEffect, useState } from 'react';
import { Activity, TrendingUp, DollarSign, Database } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { fetchDashboardStats } from '../services/lattice/dashboard';
import { DashboardStats } from '../services/lattice/types';
import { Skeleton } from 'antd';

const StatCard: React.FC<{ title: string; value: string | number; trend: string; icon: React.ReactNode }> = ({ title, value, trend, icon }) => (
  <div className="bg-surface border border-border rounded-lg p-6">
    <div className="flex justify-between items-start mb-4">
      <div>
        <p className="text-slate-400 text-xs uppercase tracking-wider font-semibold">{title}</p>
        <h3 className="text-2xl font-bold text-slate-100 mt-1">{value}</h3>
      </div>
      <div className="p-2 bg-slate-800 rounded-md text-slate-400">
        {icon}
      </div>
    </div>
    <div className="flex items-center text-xs">
      <span className="text-emerald-500 font-medium flex items-center gap-1">
        <TrendingUp size={12} /> {trend}
      </span>
      <span className="text-slate-500 ml-2">较上月</span>
    </div>
  </div>
);

export const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardStats()
      .then(setStats)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="p-6"><Skeleton active /></div>;
  }

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="净资产 (Net Worth)" value={stats?.netWorth || '¥0'} trend="+12.5%" icon={<DollarSign size={20} />} />
        <StatCard title="知识节点 (Nodes)" value={stats?.nodeCount || 0} trend="+5.2%" icon={<Database size={20} />} />
        <StatCard title="活跃项目 (Projects)" value={stats?.activeProjects || 0} trend="+1" icon={<Activity size={20} />} />
        <StatCard title="健康评分 (Health)" value={`${stats?.healthScore || 0}/100`} trend="+2.4%" icon={<Activity size={20} />} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-surface border border-border rounded-lg p-6">
          <div className="flex justify-between items-center mb-6">
            <h3 className="font-semibold text-slate-200">财务流向 (Firefly)</h3>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={stats?.monthlyFlow || []}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                <XAxis dataKey="name" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(value) => `¥${value}`} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#e2e8f0' }}
                  itemStyle={{ color: '#e2e8f0' }}
                  cursor={{ fill: '#334155', opacity: 0.2 }}
                />
                <Bar dataKey="income" name="收入" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="expense" name="支出" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-surface border border-border rounded-lg p-6">
          <h3 className="font-semibold text-slate-200 mb-4">最近更新</h3>
          <div className="space-y-4">
            {(stats?.recentUpdates || []).map((update) => (
              <div key={update.id} className="flex gap-3 pb-3 border-b border-border last:border-0 last:pb-0">
                <div className="h-2 w-2 mt-2 rounded-full bg-primary flex-shrink-0"></div>
                <div>
                  <p className="text-sm text-slate-300">{update.content}</p>
                  <p className="text-xs text-slate-500 mt-1">{update.time}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};