import React from 'react';
import { Activity, TrendingUp, DollarSign, Database } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const data = [
  { name: '周一', assets: 4000, liab: 2400 },
  { name: '周二', assets: 3000, liab: 1398 },
  { name: '周三', assets: 2000, liab: 9800 },
  { name: '周四', assets: 2780, liab: 3908 },
  { name: '周五', assets: 1890, liab: 4800 },
  { name: '周六', assets: 2390, liab: 3800 },
  { name: '周日', assets: 3490, liab: 4300 },
];

const StatCard: React.FC<{ title: string; value: string; trend: string; icon: React.ReactNode }> = ({ title, value, trend, icon }) => (
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
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="净资产 (Net Worth)" value="¥824,592" trend="+12.5%" icon={<DollarSign size={20} />} />
        <StatCard title="知识节点 (Nodes)" value="1,204" trend="+5.2%" icon={<Database size={20} />} />
        <StatCard title="活跃项目 (Projects)" value="8" trend="+1" icon={<Activity size={20} />} />
        <StatCard title="健康评分 (Health)" value="92/100" trend="+2.4%" icon={<Activity size={20} />} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-surface border border-border rounded-lg p-6">
          <div className="flex justify-between items-center mb-6">
            <h3 className="font-semibold text-slate-200">财务流向 (Firefly)</h3>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                <XAxis dataKey="name" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(value) => `¥${value}`} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#e2e8f0' }}
                  itemStyle={{ color: '#e2e8f0' }}
                  cursor={{ fill: '#334155', opacity: 0.2 }}
                />
                <Bar dataKey="assets" name="收入" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="liab" name="支出" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-surface border border-border rounded-lg p-6">
          <h3 className="font-semibold text-slate-200 mb-4">最近更新</h3>
          <div className="space-y-4">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="flex gap-3 pb-3 border-b border-border last:border-0 last:pb-0">
                <div className="h-2 w-2 mt-2 rounded-full bg-primary flex-shrink-0"></div>
                <div>
                  <p className="text-sm text-slate-300">更新了职业生涯节点 "高级工程师"</p>
                  <p className="text-xs text-slate-500 mt-1">2 小时前</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};