import React, { useState, useRef, useEffect } from 'react';
import { Send, User, Bot, StopCircle, MessageSquare, Plus, Clock } from 'lucide-react';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import ReactMarkdown from 'react-markdown';
import { message as antdMessage } from 'antd';
import { ChatMessage, ChatSession } from '../types';
import { useAuth } from '../contexts/AuthContext';
import request from '../requestConfig';

export const LatticeChat: React.FC = () => {
  const { token } = useAuth();
  
  // State
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [currentSessionId, setCurrentSessionId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  
  // Refs
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const abortControllerRef = useRef<AbortController | null>(null);
  const newSessionIdRef = useRef<string | null>(null);

  // Initial Load
  useEffect(() => {
    fetchSessions();
  }, []);

  // Fetch messages when session changes
  useEffect(() => {
    if (currentSessionId) {
      fetchMessages(currentSessionId);
    } else {
      // New Chat State
      setMessages([
        {
          id: 'welcome',
          role: 'assistant',
          content: '你好。我是 Lattice AI。今天我能帮你分析哪些人生数据？',
          timestamp: Date.now()
        }
      ]);
    }
  }, [currentSessionId]);

  // Scroll on messages change
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const fetchSessions = async () => {
    try {
      const data = await request<ChatSession[]>('/chat/sessions');
      setSessions(data);
    } catch (e) {
      console.error("Failed to fetch sessions", e);
    }
  };

  const fetchMessages = async (sessionId: string) => {
    try {
      const data = await request<ChatMessage[]>(`/chat/sessions/${sessionId}/messages`);
      setMessages(data);
    } catch (e) {
      console.error("Failed to fetch messages", e);
    }
  };

  const handleNewChat = () => {
    setCurrentSessionId(null);
  };

  const handleSend = async () => {
    if (!input.trim() || isStreaming) return;

    const userMsg: ChatMessage = { id: Date.now().toString(), role: 'user', content: input, timestamp: Date.now() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsStreaming(true);

    const assistantMsgId = (Date.now() + 1).toString();
    setMessages(prev => [...prev, { id: assistantMsgId, role: 'assistant', content: '', timestamp: Date.now() }]);

    newSessionIdRef.current = null;
    abortControllerRef.current = new AbortController();

    try {
      const controller = new AbortController();
      abortControllerRef.current = controller;
      const formData = new FormData();
      formData.append('message', userMsg.content);
      
      // Append sessionId if continuing a chat
      let url = '/api/chat/stream';
      if (currentSessionId) {
        url += `?sessionId=${currentSessionId}`;
      }

      await fetchEventSource(url, {
        method: 'POST',
        body: formData,
        headers: {
          Authorization: token ? `Bearer ${token}` : '',
        },
        signal: controller.signal,
        async onmessage(event) {
          if (event.data) {
            try {
              const payload = JSON.parse(event.data);
              
              if (payload.code === 'SUCCESS' && payload.data) {
                // Capture session ID if provided (for new chats)
                if (payload.data.sessionId) {
                  newSessionIdRef.current = payload.data.sessionId;
                }

                if (payload.data.message) {
                  setMessages(prev => prev.map(msg =>
                    msg.id === assistantMsgId ? { ...msg, content: payload.data.message } : msg
                  ));
                }
              }
            } catch (e) {
              // Fallback
              setMessages(prev => prev.map(msg =>
                msg.id === assistantMsgId ? { ...msg, content: msg.content + event.data } : msg
              ));
            }
          }
        },
        onerror(err) {
          console.error("Stream error", err);
          controller.abort();
          setIsStreaming(false);
          throw err;
        },
      });

    } catch (error: any) {
      if (error.name !== 'AbortError') {
        console.error("Stream exception", error);
        setMessages(prev => prev.map(msg => 
          msg.id === assistantMsgId ? { ...msg, content: msg.content + "\n[错误: 连接中断]" } : msg
        ));
      }
    } finally {
      setIsStreaming(false);
      abortControllerRef.current = null;
      
      // If a new session was created during this turn, switch to it
      if (!currentSessionId && newSessionIdRef.current) {
        setCurrentSessionId(newSessionIdRef.current);
        await fetchSessions(); // Refresh sidebar
      } else if (currentSessionId) {
        // Just refresh session list to update timestamp/order
        fetchSessions();
      }
    }
  };

  const handleStop = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      setIsStreaming(false);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="flex h-[calc(100vh-8rem)] bg-surface border border-border rounded-lg overflow-hidden">
      
      {/* Sidebar - Session List */}
      <div className="w-64 border-r border-border bg-slate-900/30 flex flex-col">
        <div className="p-4 border-b border-border">
          <button 
            onClick={handleNewChat}
            className="w-full flex items-center justify-center gap-2 bg-primary text-slate-900 py-2 rounded-md font-medium hover:bg-emerald-400 transition-colors"
          >
            <Plus size={18} />
            New Chat
          </button>
        </div>
        <div className="flex-1 overflow-y-auto p-2 space-y-1">
          {sessions.map(session => (
            <button
              key={session.id}
              onClick={() => setCurrentSessionId(session.id)}
              className={`w-full text-left p-3 rounded-md text-sm transition-colors group ${ 
                currentSessionId === session.id 
                  ? 'bg-slate-800 text-slate-100' 
                  : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
              }`}
            >
              <div className="font-medium truncate mb-1 flex items-center gap-2">
                <MessageSquare size={14} className="flex-shrink-0" />
                <span className="truncate">{session.title}</span>
              </div>
              <div className="text-[10px] text-slate-600 flex items-center gap-1">
                <Clock size={10} />
                {formatDate(session.updatedAt)}
              </div>
            </button>
          ))}
          {sessions.length === 0 && (
            <div className="text-center text-slate-600 text-xs mt-10">
              No history yet
            </div>
          )}
        </div>
      </div>

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col relative">
        {/* Messages Area */}
        <div className="flex-1 overflow-y-auto p-4 space-y-6">
          {messages.map((msg) => (
            <div key={msg.id} className={`flex gap-4 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${msg.role === 'user' ? 'bg-secondary text-white' : 'bg-primary text-slate-900'}`}>
                {msg.role === 'user' ? <User size={16} /> : <Bot size={16} />}
              </div>
              <div className={`max-w-[80%] p-3 rounded-lg text-sm leading-relaxed ${msg.role === 'user' ? 'bg-slate-700 text-slate-100' : 'bg-slate-900/50 text-slate-200 border border-slate-700'}`}>
                {msg.content ? (
                  msg.role === 'assistant' ? <ReactMarkdown>{msg.content}</ReactMarkdown> : <div className="whitespace-pre-wrap font-mono text-sm">{msg.content}</div>
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
              placeholder="Ask your digital twin..."
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
             Lattice AI can make mistakes. Verify important financial data.
          </p>
        </div>
      </div>
    </div>
  );
};