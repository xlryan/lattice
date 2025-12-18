import React, { useState } from 'react';
import { Sidebar } from './components/Sidebar';
import { Dashboard } from './pages/Dashboard';
import { CareerNodes } from './pages/CareerNodes';
import { LatticeChat } from './pages/LatticeChat';
import { SearchPage } from './pages/Search';
import { FinancePage } from './pages/Finance';
import { DiyProjects } from './pages/DiyProjects';
import { WealthDashboard } from './pages/WealthDashboard';
import { CareerIngestPage } from './pages/CareerIngest';
import { LoginPage } from './pages/Login';
import { PageView } from './types';
import { Menu, Terminal } from 'lucide-react';
import { Button } from 'antd';
import { useAuth } from './contexts/AuthContext';

const App: React.FC = () => {
  const [currentPage, setCurrentPage] = useState<PageView>('dashboard');
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const { isAuthenticated, logout } = useAuth();
  
  // Mock user data - in a real app this would come from AuthContext or an API call
  const user = {
    name: '管理员',
    email: 'admin@lattice.local'
  };

  const renderPage = () => {
    switch (currentPage) {
      case 'dashboard':
        return <Dashboard />;
      case 'chat':
        return <LatticeChat />;
      case 'search':
        return <SearchPage />;
      case 'career':
        return <CareerNodes />;
      case 'finance':
        return <FinancePage />;
      case 'wealth':
        return <WealthDashboard />;
      case 'careerIngest':
        return <CareerIngestPage />;
      case 'diy':
        return <DiyProjects />;
      default:
        return (
          <div className="flex flex-col items-center justify-center h-full text-slate-500">
            <Terminal size={64} className="mb-4 opacity-50" />
            <h2 className="text-xl font-mono">模块: {currentPage.toUpperCase()}</h2>
            <p className="mt-2">正在开发中 / 占位符</p>
          </div>
        );
    }
  };

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  return (
    <div className="flex h-screen w-full overflow-hidden bg-background text-slate-200 font-sans">
      {/* Mobile Sidebar Toggle */}
      <button 
        className="md:hidden fixed top-4 left-4 z-50 p-2 bg-surface rounded-md border border-border"
        onClick={() => setIsSidebarOpen(!isSidebarOpen)}
      >
        <Menu size={20} />
      </button>

      {/* Sidebar */}
      <div className={`${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0 transition-transform duration-300 fixed md:relative z-40 h-full`}>
        <Sidebar currentPage={currentPage} onNavigate={setCurrentPage} user={user} />
      </div>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden bg-background relative">
        <header className="h-16 border-b border-border flex items-center px-6 justify-between bg-background/50 backdrop-blur-sm">
          <div className="flex items-center gap-2 text-slate-400">
            <span className="text-primary font-mono font-bold text-lg">LATTICE</span>
            <span className="text-slate-600">/</span>
            <span className="font-mono text-sm uppercase tracking-wider">{currentPage.toUpperCase()}</span>
          </div>
          <div className="flex items-center gap-4">
            <div className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></div>
            <span className="text-xs font-mono text-emerald-500">系统在线</span>
            <Button size="small" onClick={logout}>退出</Button>
          </div>
        </header>

        <div className="flex-1 overflow-auto p-4 md:p-6 custom-scrollbar">
          {renderPage()}
        </div>
      </main>
    </div>
  );
};

export default App;
