import React, { useEffect, useState } from 'react';
import { DollarSign, ArrowUpRight, ArrowDownRight, Wallet, CreditCard, PieChart } from 'lucide-react';
import { FinanceRecord } from '../services/lattice/types';
import { fetchTransactions } from '../services/lattice/wealth';
import { Skeleton, message } from 'antd';

export const FinancePage: React.FC = () => {
  const [records, setRecords] = useState<FinanceRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTransactions()
      .then(setRecords)
      .catch(() => message.error('获取交易记录失败'))
      .finally(() => setLoading(false));
  }, []);

  const monthlyIncome = records
    .filter(r => r.amount > 0)
    .reduce((sum, r) => sum + r.amount, 0);
  
  const monthlyExpense = records
    .filter(r => r.amount < 0)
    .reduce((sum, r) => sum + Math.abs(r.amount), 0);

  return (
    <div className="space-y-6">
      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-emerald-500/10 border border-emerald-500/20 rounded-lg p-4 flex items-center justify-between">
          <div>
            <p className="text-emerald-400 text-xs font-bold uppercase tracking-wider">本月收入</p>
            <h3 className="text-2xl font-mono text-emerald-100 mt-1">
              {loading ? '...' : `+¥${monthlyIncome.toLocaleString()}`}
            </h3>
          </div>
          <div className="bg-emerald-500/20 p-2 rounded-full text-emerald-400"><ArrowUpRight size={24} /></div>
        </div>
        <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-4 flex items-center justify-between">
          <div>
            <p className="text-blue-400 text-xs font-bold uppercase tracking-wider">本月支出</p>
            <h3 className="text-2xl font-mono text-blue-100 mt-1">
              {loading ? '...' : `-¥${monthlyExpense.toLocaleString()}`}
            </h3>
          </div>
          <div className="bg-blue-500/20 p-2 rounded-full text-blue-400"><ArrowDownRight size={24} /></div>
        </div>
        <div className="bg-surface border border-border rounded-lg p-4 flex items-center justify-between">
          <div>
            <p className="text-slate-400 text-xs font-bold uppercase tracking-wider">结余</p>
            <h3 className="text-2xl font-mono text-slate-100 mt-1">
              {loading ? '...' : `¥${(monthlyIncome - monthlyExpense).toLocaleString()}`}
            </h3>
          </div>
          <div className="bg-slate-700 p-2 rounded-full text-slate-400"><Wallet size={24} /></div>
        </div>
      </div>

      {/* Transaction Table */}
      <div className="bg-surface border border-border rounded-lg overflow-hidden">
        <div className="p-4 border-b border-border flex justify-between items-center bg-slate-900/50">
          <h3 className="font-semibold text-slate-200 flex items-center gap-2">
            <CreditCard size={18} /> 最近交易记录
          </h3>
          <button className="text-xs bg-primary text-slate-900 px-3 py-1.5 rounded font-medium hover:bg-emerald-400">
            同步 Firefly
          </button>
        </div>
        <div className="overflow-x-auto">
          {loading ? (
            <div className="p-10"><Skeleton active /></div>
          ) : (
            <table className="w-full text-sm text-left">
              <thead className="bg-slate-900 text-slate-400 font-medium text-xs uppercase">
                <tr>
                  <th className="px-6 py-3">日期</th>
                  <th className="px-6 py-3">描述</th>
                  <th className="px-6 py-3">账户</th>
                  <th className="px-6 py-3">分类</th>
                  <th className="px-6 py-3 text-right">金额</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {records.map(record => (
                  <tr key={record.id} className="hover:bg-slate-700/20 transition-colors group">
                    <td className="px-6 py-4 font-mono text-slate-400">{new Date(record.date).toLocaleDateString()}</td>
                    <td className="px-6 py-4 text-slate-200 font-medium">{record.description}</td>
                    <td className="px-6 py-4 text-slate-400 text-xs">{record.account}</td>
                    <td className="px-6 py-4">
                      <span className="bg-slate-800 text-slate-300 border border-slate-700 px-2 py-0.5 rounded text-xs">
                        {record.category || '未分类'}
                      </span>
                    </td>
                    <td className={`px-6 py-4 text-right font-mono font-medium ${
                      record.amount > 0 ? 'text-emerald-400' : 'text-slate-300'
                    }`}>
                      {record.amount > 0 ? '+' : ''}{record.amount.toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};