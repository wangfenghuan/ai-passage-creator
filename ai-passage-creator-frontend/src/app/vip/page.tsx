"use client";

import React, { useEffect } from 'react';
import { Card, Button, Typography, Space, message, Spin, Result } from 'antd';
import { useRouter, useSearchParams } from 'next/navigation';
import { PaymentControllerService } from '@/api';

const { Title, Paragraph } = Typography;

function VipContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [loading, setLoading] = React.useState(false);

  useEffect(() => {
    // 处理支付回调
    const success = searchParams.get('success');
    const canceled = searchParams.get('canceled');

    if (success) {
      message.success('支付成功！感谢您对 AI Passage Creator 的支持');
      // 可以调用获取个人信息的接口刷新权限缓存
    }

    if (canceled) {
      message.warning('支付已取消');
    }
  }, [searchParams]);

  const handleSubscribe = async () => {
    try {
      setLoading(true);
      // 调用支付接口，如果后端返回了 session url 则跳转
      const res = await PaymentControllerService.createVipPaymentSession();
      if (res.code === 0 && res.data) {
        window.location.href = res.data;
      } else {
        message.error(res.message || '获取支付链接失败');
      }
    } catch (error: any) {
        if (error?.response?.status === 401 || error.message?.includes('401')) {
          message.warning('请登录后再试');
          router.push('/login');
        } else {
          message.error('请求支付服务异常');
        }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
      <Card className="max-w-md w-full text-center shadow-lg border-0 rounded-2xl py-8">
        <Title level={2} style={{ color: '#52c41a', margin: 0 }}>解锁高级创作功能</Title>
        <Paragraph className="text-gray-500 mt-4 mb-8 text-lg">
          开通高级会员，畅享无限制的每日生成数量、独特的 Flux 配图和快速响应优先权
        </Paragraph>
        
        <div className="bg-green-50 rounded-xl p-6 mb-8 text-left">
           <ul className="space-y-3 text-gray-700">
              <li>✨ 无限制的文章与大纲生成</li>
              <li>🎨 优先处理各种高清无版权或 AI 绘图请求</li>
              <li>🚀 更快的云端大模型响应速度</li>
              <li>💬 专属的客服对接群</li>
           </ul>
        </div>

        <Button 
           type="primary" 
           size="large" 
           block 
           onClick={handleSubscribe} 
           loading={loading}
           style={{ backgroundColor: '#52c41a', height: '54px', fontSize: '18px' }}
        >
          立即赞助 (￥19.9/月)
        </Button>

        <div className="mt-4">
           <Button type="link" onClick={() => router.push('/')} className="text-gray-500">以后再说，返回首页</Button>
        </div>
      </Card>
    </div>
  );
}

export default function VipPage() {
  return (
    <React.Suspense fallback={<div className="min-h-screen flex items-center justify-center bg-gray-50 p-6"><Spin size="large" tip="加载中..." /></div>}>
      <VipContent />
    </React.Suspense>
  );
}
