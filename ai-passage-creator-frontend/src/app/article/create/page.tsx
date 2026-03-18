"use client";

import React, { useState } from 'react';
import { Card, Form, Input, Select, Button, Typography, message, Space } from 'antd';
import { useRouter } from 'next/navigation';
import { Service, ArticleCreateRequest } from '@/api';

const { Title, Paragraph } = Typography;
const { Option } = Select;
const { TextArea } = Input;

export default function CreateArticlePage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    try {
      setLoading(true);
      const req: ArticleCreateRequest = {
        topic: values.topic,
        style: values.style || 'long',
        enabledImageMethods: values.enabledImageMethods || [],
      };
      
      const res = await Service.create(req);
      
      if (res.code === 0 && res.data) {
        message.success('创作任务已提交');
        // 跳转到文章生成过程页面或编辑器页面
        router.push(`/article/editor/${res.data}`);
      } else {
        message.error(res.message || '任务提交失败');
      }
    } catch (error: any) {
      if (error?.response?.status === 401 || error.message?.includes('401')) {
        message.warning('登录已过期');
        router.push('/login');
      } else {
        message.error('请求异常，请稍后再试');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <Card className="max-w-xl w-full shadow-lg rounded-xl border-0">
        <Typography className="text-center mb-6">
          <Title level={2} style={{ color: '#52c41a', margin: 0 }}>创建新文章</Title>
          <Paragraph className="text-gray-500 mt-2">输入选题并配置参数，AI 将为您生成优质文章</Paragraph>
        </Typography>

        <Form
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ style: 'long', enabledImageMethods: [] }}
          size="large"
        >
          <Form.Item
            name="topic"
            label="文章选题"
            rules={[{ required: true, message: '请输入您的文章选题或核心思想' }]}
          >
            <TextArea 
              rows={4} 
              placeholder="例如：探讨人工智能在医疗领域的未来发展..." 
              maxLength={500}
              showCount
            />
          </Form.Item>

          <Form.Item name="style" label="文章风格">
            <Select placeholder="选择生成的风格类型">
              <Option value="long">长篇深度剖析</Option>
              <Option value="short">短小精悍资讯</Option>
              <Option value="humorous">幽默风趣段子</Option>
              <Option value="academic">严谨学术报告</Option>
            </Select>
          </Form.Item>

          <Form.Item name="enabledImageMethods" label="配图方式 (功能测试版)">
            <Select mode="multiple" placeholder="选择你要如何生成配图 (可多选)">
              <Option value="NANO_BANANA">Nano Banana 引擎</Option>
              <Option value="FLUX">Flux AI</Option>
              <Option value="PEXELS">Pexels 免版权图库搜索</Option>
              <Option value="MEME">表情包抓取</Option>
            </Select>
          </Form.Item>

          <Form.Item className="mt-8 mb-0">
            <Space className="w-full justify-between">
              <Button type="default" onClick={() => router.back()}>
                返回
              </Button>
              <Button type="primary" htmlType="submit" loading={loading} style={{ backgroundColor: '#52c41a' }}>
                开始生成
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
