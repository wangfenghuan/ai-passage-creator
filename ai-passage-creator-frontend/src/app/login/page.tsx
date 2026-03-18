"use client";

import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, message, Tabs } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { OpenAPI, UserControllerService, UserLoginRequest, UserRegisterRequest } from '@/api';

const { Title } = Typography;

export default function LoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('login');

  const onLogin = async (values: UserLoginRequest) => {
    try {
      setLoading(true);
      const res = await UserControllerService.userLogin(values);
      if (res.code === 0) {
        message.success('登录成功');
        router.push('/'); // 跳转到首页
      } else {
        message.error(res.message || '登录失败');
      }
    } catch (error: any) {
      message.error(error.message || '网络或接口异常');
    } finally {
      setLoading(false);
    }
  };

  const onRegister = async (values: UserRegisterRequest) => {
    try {
      setLoading(true);
      const res = await UserControllerService.userRegister(values);
      if (res.code === 0) {
        message.success('注册成功，请登录');
        setActiveTab('login');
      } else {
        message.error(res.message || '注册失败');
      }
    } catch (error: any) {
      message.error(error.message || '网络或接口异常');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 flex-col">
      <div className="mb-8 text-center">
        <Title level={2} style={{ color: '#52c41a', margin: 0 }}>AI Passage Creator</Title>
        <p className="text-gray-500 mt-2">智能辅助文章创作平台</p>
      </div>

      <Card className="w-full max-w-md shadow-lg rounded-lg border-0">
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          centered
          items={[
            {
              key: 'login',
              label: '账号登录',
              children: (
                <Form
                  name="loginForm"
                  onFinish={onLogin}
                  layout="vertical"
                  size="large"
                >
                  <Form.Item
                    name="userAccount"
                    rules={[{ required: true, message: '请输入账号' }]}
                  >
                    <Input prefix={<UserOutlined />} placeholder="账号" />
                  </Form.Item>
                  <Form.Item
                    name="userPassWord"
                    rules={[{ required: true, message: '请输入密码' }]}
                  >
                    <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                  </Form.Item>
                  <Form.Item>
                    <Button type="primary" htmlType="submit" className="w-full" loading={loading} style={{ backgroundColor: '#52c41a' }}>
                      登录
                    </Button>
                  </Form.Item>
                </Form>
              ),
            },
            {
              key: 'register',
              label: '新用户注册',
              children: (
                <Form
                  name="registerForm"
                  onFinish={onRegister}
                  layout="vertical"
                  size="large"
                >
                  <Form.Item
                    name="userAccount"
                    rules={[
                      { required: true, message: '请输入账号' },
                      { min: 4, message: '账号长度不少于4位' }
                    ]}
                  >
                    <Input prefix={<UserOutlined />} placeholder="请输入账号" />
                  </Form.Item>
                  <Form.Item
                    name="userPassWord"
                    rules={[
                      { required: true, message: '请输入密码' },
                      { min: 8, message: '密码长度不少于8位' }
                    ]}
                  >
                    <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
                  </Form.Item>
                  <Form.Item
                    name="checkPassword"
                    dependencies={['userPassWord']}
                    rules={[
                      { required: true, message: '请确认密码' },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue('userPassWord') === value) {
                            return Promise.resolve();
                          }
                          return Promise.reject(new Error('两次输入的密码不匹配!'));
                        },
                      }),
                    ]}
                  >
                    <Input.Password prefix={<LockOutlined />} placeholder="请再次输入密码" />
                  </Form.Item>
                  <Form.Item>
                    <Button type="primary" htmlType="submit" className="w-full" loading={loading} style={{ backgroundColor: '#52c41a' }}>
                      注册
                    </Button>
                  </Form.Item>
                </Form>
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}
