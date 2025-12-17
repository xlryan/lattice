import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { message } from 'antd';
import { login } from '../services/lattice/auth';
import { TOKEN_KEY } from '../requestConfig';

interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  loginAction: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const existing = localStorage.getItem(TOKEN_KEY);
    if (existing) {
      setToken(existing);
    }
  }, []);

  const loginAction = async (username: string, password: string) => {
    const result = await login({ username, password });
    localStorage.setItem(TOKEN_KEY, result.token);
    setToken(result.token);
    message.success('登录成功');
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
  };

  const value = useMemo(() => ({
    token,
    isAuthenticated: Boolean(token),
    loginAction,
    logout,
  }), [token]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthState => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
};
