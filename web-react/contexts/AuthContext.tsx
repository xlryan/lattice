import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { message } from 'antd';
import { login } from '../services/lattice/auth';
import { TOKEN_KEY } from '../requestConfig';

interface AuthState {
  token: string | null;
  username: string | null;
  isAuthenticated: boolean;
  loginAction: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(null);
  const [username, setUsername] = useState<string | null>(null);

  useEffect(() => {
    const existing = localStorage.getItem(TOKEN_KEY);
    const existingUser = localStorage.getItem('lattice_username');
    if (existing) {
      setToken(existing);
      setUsername(existingUser);
    }
  }, []);

  const loginAction = async (username: string, password: string) => {
    const result = await login({ username, password });
    localStorage.setItem(TOKEN_KEY, result.token);
    localStorage.setItem('lattice_username', username);
    setToken(result.token);
    setUsername(username);
    message.success('登录成功');
    window.location.href = '/';
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem('lattice_username');
    setToken(null);
    setUsername(null);
  };

  const value = useMemo(() => ({
    token,
    username,
    isAuthenticated: Boolean(token),
    loginAction,
    logout,
  }), [token, username]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthState => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
};
