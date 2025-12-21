import React from 'react';
import { Button, Form, Input, ConfigProvider, theme } from 'antd';
import { useAuth } from '../contexts/AuthContext';
import { Lock, User, ArrowRight } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { loginAction } = useAuth();
  const [form] = Form.useForm();

  const handleSubmit = async () => {
    try {
      const { username, password } = await form.validateFields();
      await loginAction(username, password);
    } catch (error) {
      // Form validation error or login failed
    }
  };

  return (
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: '#10b981',
          borderRadius: 8,
        },
      }}
    >
      <div className="min-h-screen bg-[#0f172a] flex items-center justify-center relative overflow-hidden">
        {/* Ambient Background */}
        <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
          <div className="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] bg-emerald-500/10 rounded-full blur-[120px]" />
          <div className="absolute top-[40%] -right-[10%] w-[40%] h-[40%] bg-blue-500/10 rounded-full blur-[120px]" />
        </div>

        {/* Login Card */}
        <div className="w-full max-w-md p-8 relative z-10">
          <div className="bg-slate-900/50 backdrop-blur-xl border border-slate-800 rounded-2xl p-8 shadow-2xl shadow-black/50">
            <div className="mb-8 text-center">
              <div className="w-12 h-12 bg-gradient-to-tr from-emerald-400 to-cyan-500 rounded-xl mx-auto mb-4 flex items-center justify-center shadow-lg shadow-emerald-500/20">
                <div className="w-6 h-6 border-2 border-white rounded-md transform rotate-45" />
              </div>
              <h1 className="text-2xl font-bold text-white tracking-tight">Lattice</h1>
              <p className="text-slate-400 text-sm mt-2">Engineering your life data</p>
            </div>

            <Form 
              form={form} 
              onFinish={handleSubmit}
              size="large"
              layout="vertical"
              className="space-y-4"
            >
              <Form.Item 
                name="username" 
                rules={[{ required: true, message: 'Please input your username' }]}
              >
                <Input 
                  prefix={<User size={18} className="text-slate-500" />} 
                  placeholder="Username" 
                  className="!bg-slate-950/50 !border-slate-700 hover:!border-emerald-500/50 focus:!border-emerald-500 !text-slate-200"
                />
              </Form.Item>
              
              <Form.Item 
                name="password" 
                rules={[{ required: true, message: 'Please input your password' }]}
              >
                <Input.Password 
                  prefix={<Lock size={18} className="text-slate-500" />} 
                  placeholder="Password" 
                  className="!bg-slate-950/50 !border-slate-700 hover:!border-emerald-500/50 focus:!border-emerald-500 !text-slate-200"
                />
              </Form.Item>

              <Form.Item className="pt-2">
                <Button 
                  type="primary" 
                  htmlType="submit" 
                  block 
                  className="!bg-gradient-to-r !from-emerald-500 !to-emerald-600 hover:!from-emerald-400 hover:!to-emerald-500 !border-0 !h-11 !font-medium !shadow-lg !shadow-emerald-500/20 flex items-center justify-center gap-2 group"
                >
                  Sign In
                  <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform" />
                </Button>
              </Form.Item>
            </Form>
            
            <div className="mt-6 text-center">
              <p className="text-xs text-slate-500">
                Protected by Lattice Security Layer
              </p>
            </div>
          </div>
        </div>
      </div>
    </ConfigProvider>
  );
};
