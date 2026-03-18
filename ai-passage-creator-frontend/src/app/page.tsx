"use client";

import { Button, Typography, Space, Divider } from "antd";
import { useRouter } from "next/navigation";

const { Title, Paragraph } = Typography;

export default function Home() {
  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-8 sm:p-20 font-[family-name:var(--font-geist-sans)] bg-gray-50">
      <main className="flex flex-col gap-8 items-center text-center max-w-2xl bg-white p-12 rounded-2xl shadow-xl w-full">
        <Title level={1} style={{ color: '#52c41a', margin: 0 }}>AI Passage Creator</Title>
        <Paragraph className="text-lg text-gray-500">
          一款使用人工智能辅助文章创作的先进平台。
        </Paragraph>
        
        <Divider />
        
        <div className="flex gap-4 items-center flex-col sm:flex-row w-full justify-center">
          <Button type="primary" size="large" onClick={() => router.push('/article/create')} style={{ backgroundColor: '#52c41a', minWidth: '150px' }}>
            开始创作
          </Button>
          <Button size="large" onClick={() => router.push('/article/history')} style={{ minWidth: '150px' }}>
            历史文章
          </Button>
        </div>

        <Space className="mt-8 text-gray-400" split={<Divider type="vertical" />}>
          <Button type="link" onClick={() => router.push('/statistics')}>运营数据</Button>
          <Button type="link" onClick={() => router.push('/vip')}>充值中心</Button>
          <Button type="link" onClick={() => router.push('/login')}>账号登录</Button>
        </Space>
      </main>
    </div>
  );
}
