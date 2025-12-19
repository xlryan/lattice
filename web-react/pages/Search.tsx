import React, { useState } from 'react';
import { Search, Database, FileText, Tag, Calendar, Zap } from 'lucide-react';
import { SearchResult } from '../types';
import { hybridSearch } from '../lib/api';

export const SearchPage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [domain, setDomain] = useState<'CAREER' | 'WEALTH' | 'BUILD' | 'KNOWLEDGE' | 'INTEL' | 'INBOX'>('CAREER');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [took, setTook] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!query.trim()) return;
    setIsLoading(true);
    setError(null);
    try {
      const payload = await hybridSearch(query, domain);
      setResults(payload.results);
      setTook(payload.took);
    } catch (err) {
      console.error(err);
      setError('检索服务暂时不可用，请稍后再试。');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-surface p-8 rounded-lg border border-border flex flex-col items-center gap-6">
        <h2 className="text-2xl font-mono font-bold text-slate-100 flex items-center gap-2">
          <Database className="text-primary" />
          混合检索中心
        </h2>
        <div className="w-full max-w-2xl relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
          <input 
            type="text" 
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                handleSearch();
              }
            }}
            placeholder="输入关键词或自然语言问题 (例如: 去年在服务器上花了多少钱?)" 
            className="w-full bg-slate-900 border border-slate-700 rounded-lg py-4 pl-12 pr-4 text-slate-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary shadow-lg"
          />
          <div className="absolute right-3 top-1/2 -translate-y-1/2 flex gap-2">
            <select
              value={domain}
              onChange={(e) => setDomain(e.target.value as typeof domain)}
              className="bg-slate-800 text-slate-200 text-xs border border-slate-700 rounded px-2 py-1 focus:outline-none"
            >
              <option value="CAREER">CAREER</option>
              <option value="WEALTH">WEALTH</option>
              <option value="BUILD">BUILD</option>
              <option value="KNOWLEDGE">KNOWLEDGE</option>
              <option value="INTEL">INTEL</option>
              <option value="INBOX">INBOX</option>
            </select>
            <button
              onClick={handleSearch}
              disabled={isLoading}
              className="text-[10px] bg-primary text-slate-900 px-2 py-1 rounded border border-primary/50 disabled:opacity-50"
            >
              {isLoading ? '检索中...' : 'Hybrid Search'}
            </button>
          </div>
        </div>
        <div className="flex gap-4 text-sm text-slate-500">
          <span>结果数量: <b className="text-slate-300">{results.length}</b></span>
          <span>向量维度: <b className="text-slate-300">1536</b></span>
          <span>耗时: <b className="text-emerald-500">{took ?? '--'}ms</b></span>
        </div>
        {error && <p className="text-red-400 text-xs">{error}</p>}
      </div>

      <div className="space-y-4">
        <h3 className="text-sm font-bold text-slate-500 uppercase tracking-wider px-2">检索结果 ({results.length})</h3>
        {results.length === 0 && !isLoading && (
          <div className="text-center text-slate-500 border border-dashed border-slate-700 rounded-lg py-10">
            还没有结果，试试“最近一次 DIY 成本” 或 “Firefly 餐饮支出”。
          </div>
        )}
        
        {results.map(result => (
          <div key={result.id} className="bg-surface border border-border rounded-lg p-5 hover:border-primary/50 transition-colors group cursor-pointer">
            <div className="flex justify-between items-start mb-2">
              <h4 className="text-lg font-medium text-secondary group-hover:underline decoration-secondary/50 underline-offset-4">
                {result.title}
              </h4>
              <div className="flex items-center gap-1.5 bg-slate-900 px-2 py-1 rounded border border-slate-700">
                <Zap size={12} className={result.score > 0.9 ? "text-yellow-400" : "text-slate-400"} />
                <span className="text-xs font-mono font-bold text-slate-300">{result.score.toFixed(2)}</span>
              </div>
            </div>
            
            <p className="text-slate-400 text-sm leading-relaxed mb-3 font-mono">
              {result.snippet}
            </p>

            <div className="flex items-center gap-4 text-xs text-slate-500">
              <div className="flex items-center gap-1">
                <FileText size={12} />
                <span>{result.source ?? 'Lattice Node'}</span>
              </div>
              <div className="flex items-center gap-1">
                <Calendar size={12} />
                <span>{result.date ?? ''}</span>
              </div>
              <div className="flex gap-2 ml-auto">
                {(result.tags ?? []).map(tag => (
                  <span key={tag} className="flex items-center gap-1 bg-slate-800 px-2 py-0.5 rounded text-slate-400">
                    <Tag size={10} />
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </div>
        ))}
        {isLoading && (
          <div className="text-center text-slate-400 text-sm">正在检索，请稍候...</div>
        )}
      </div>
    </div>
  );
};
