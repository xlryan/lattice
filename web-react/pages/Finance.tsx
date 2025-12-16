import React from 'react';
import { DollarSign, ArrowUpRight, ArrowDownRight, Wallet, CreditCard, PieChart } from 'lucide-react';
import { FinanceRecord } from '../types';

const MOCK_RECORDS: FinanceRecord[] = [
  { id: '1', date: '2024-03-15', description: 'TechCorp 薪资发放', amount: 35000.00, type: 'income', category: '薪水', account: '招商银行 (6789)' },
  { id: '2', date: '2024-03-14', description: 'Apple Store - MacBook Pro', amount: -18999.00, type: 'expense', category: '电子产品', account: '信用卡 (Visa)' },
  { id: '3', date: '2024-03-14', description: '星巴克咖啡', amount: -38.00, type: 'expense', category: '餐饮', account: '微信支付' },
  { id: '4', date: '2024-03-12', description: 'AWS 云服务账单', amount: -450.20, type: 'expense', category: '基础设施', account: 'PayPal' },
  { id: '5', date: '2024-03-10', description: '理财收益 - 基金', amount: 1204.50, type: 'income', category: '投资', account: '蚂蚁财富' },
];

export const FinancePage: React.FC = () => {
  return (
    <div className="space-y-6">
      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-emerald-500/10 border border-emerald-500/20 rounded-lg p-4 flex items-center justify-between">
          <div>
            <p className="text-emerald-400 text-xs font-bold uppercase tracking-wider">本月收入</p>
            <h3 className="text-2xl font-mono text-emerald-100 mt-1">+¥36,204.50</h3>
          </div>
          <div className="bg-emerald-500/20 p-2 rounded-full text-emerald-400"><ArrowUpRight size={24} /></div>
        </div>
        <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-4 flex items-center justify-between">
          <div>
            <p className="text-blue-400 text-xs font-bold uppercase tracking-wider">本月支出</p>
            <h3 className="text-2xl font-mono text-blue-100 mt-1">-¥19,487.20</h3>
          </div>
          <div className="bg-blue-500/20 p-2 rounded-full text-blue-400"><ArrowDownRight size={24} /></div>
        </div>
        <div className="bg-surface border border-border rounded-lg p-4 flex items-center justify-between">
          <div>
            <p className="text-slate-400 text-xs font-bold uppercase tracking-wider">结余</p>
            <h3 className="text-2xl font-mono text-slate-100 mt-1">¥16,717.30</h3>
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
              {MOCK_RECORDS.map(record => (
                <tr key={record.id} className="hover:bg-slate-700/20 transition-colors group">
                  <td className="px-6 py-4 font-mono text-slate-400">{record.date}</td>
                  <td className="px-6 py-4 text-slate-200 font-medium">{record.description}</td>
                  <td className="px-6 py-4 text-slate-400 text-xs">{record.account}</td>
                  <td className="px-6 py-4">
                    <span className="bg-slate-800 text-slate-300 border border-slate-700 px-2 py-0.5 rounded text-xs">
                      {record.category}
                    </span>
                  </td>
                  <td className={`px-6 py-4 text-right font-mono font-medium ${
                    record.type === 'income' ? 'text-emerald-400' : 'text-slate-300'
                  }`}>
                    {record.amount > 0 ? '+' : ''}{record.amount.toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};