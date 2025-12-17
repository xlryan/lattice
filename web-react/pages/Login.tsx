import React from 'react';
import { Button, Card, Form, Input, Typography } from 'antd';
import { useAuth } from '../contexts/AuthContext';

export const LoginPage: React.FC = () => {
  const { loginAction } = useAuth();
  const [form] = Form.useForm();

  const handleSubmit = async () => {
    const { username, password } = await form.validateFields();
    await loginAction(username, password);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center">
      <Card style={{ width: 360 }}>
        <Typography.Title level={4} style={{ textAlign: 'center' }}>Lattice 登录</Typography.Title>
        <Form layout="vertical" form={form} onFinish={handleSubmit}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="admin" autoComplete="username" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="••••••" autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            登录
          </Button>
        </Form>
      </Card>
    </div>
  );
};
