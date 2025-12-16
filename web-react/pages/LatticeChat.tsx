import React, { useState, useRef, useEffect } from 'react';
import { Send, User, Bot, StopCircle } from 'lucide-react';
import { ChatMessage } from '../types';

export const LatticeChat: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    { id: '1', role: 'assistant', content: '你好。我是 Lattice AI。今天我能帮你分析哪些人生数据？', timestamp: Date.now() }
  ]);
  const [input, setInput] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || isStreaming) return;

    const userMsg: ChatMessage = { id: Date.now().toString(), role: 'user', content: input, timestamp: Date.now() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsStreaming(true);

    const assistantMsgId = (Date.now() + 1).toString();
    setMessages(prev => [...prev, { id: assistantMsgId, role: 'assistant', content: '', timestamp: Date.now() }]);

    abortControllerRef.current = new AbortController();

    try {
      // Mock streaming for Chinese demo
      const mockResponse = "我已经分析了你的职业节点。看起来你在 **向量数据库** 方面的知识图谱存在缺口。考虑到你拥有 Spring Boot 背景，我建议你探索 `pgvector` 或 `Pinecone` 的集成。\n\n需要我为你创建一个学习任务吗？";
      // Split into characters for Chinese typing effect
      const chars = mockResponse.split('');
      
      for (let i = 0; i < chars.length; i++) {
        await new Promise(resolve => setTimeout(resolve, 30)); // Faster typing for Chinese
        
        setMessages(prev => prev.map(msg => 
          msg.id === assistantMsgId ? { ...msg, content: msg.content + chars[i] } : msg
        ));
      }

    } catch (error: any) {
      if (error.name !== 'AbortError') {
        console.error("Stream error", error);
        setMessages(prev => prev.map(msg => 
          msg.id === assistantMsgId ? { ...msg, content: msg.content + "\n[错误: 连接中断]" } : msg
        ));
      }
    } finally {
      setIsStreaming(false);
      abortControllerRef.current = null;
    }
  };

  const handleStop = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      setIsStreaming(false);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] bg-surface border border-border rounded-lg overflow-hidden relative">
      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-6">
        {messages.map((msg) => (
          <div key={msg.id} className={`flex gap-4 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}>
            <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${msg.role === 'user' ? 'bg-secondary text-white' : 'bg-primary text-slate-900'}`}>
              {msg.role === 'user' ? <User size={16} /> : <Bot size={16} />}
            </div>
            <div className={`max-w-[80%] p-3 rounded-lg text-sm leading-relaxed ${msg.role === 'user' ? 'bg-slate-700 text-slate-100' : 'bg-slate-900/50 text-slate-200 border border-slate-700'}`}>
              {msg.content ? (
                <div className="whitespace-pre-wrap font-mono text-sm">{msg.content}</div>
              ) : (
                <span className="animate-pulse">_</span>
              )}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="p-4 bg-background border-t border-border">
        <div className="relative flex items-center gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSend()}
            placeholder="询问你的数据大脑..."
            disabled={isStreaming}
            className="flex-1 bg-slate-900 border border-slate-700 rounded-md py-3 pl-4 pr-12 text-sm text-slate-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary disabled:opacity-50"
          />
          <button 
            onClick={isStreaming ? handleStop : handleSend}
            className={`absolute right-2 p-1.5 rounded-md transition-colors ${
              isStreaming 
                ? 'bg-red-500/10 text-red-500 hover:bg-red-500/20' 
                : 'bg-primary text-slate-900 hover:bg-emerald-400'
            }`}
          >
            {isStreaming ? <StopCircle size={18} /> : <Send size={18} />}
          </button>
        </div>
        <p className="text-[10px] text-slate-500 mt-2 text-center">
          Lattice AI 可能会犯错。请核实重要的财务数据。
        </p>
      </div>
    </div>
  );
};