"use client";

import React, { useEffect, useState } from 'react';
import { Card, Statistic, Row, Col, Typography, message } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import { StatisticsControllerService } from '@/api';

const { Title } = Typography;

export default function StatisticsPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState({ userCount: 0, articleCount: 0 });

  const fetchStats = async () => {
    try {
      setLoading(true);
      const res = await StatisticsControllerService.getStatistics();
      if (res.code === 0 && res.data) {
        setData({
          userCount: res.data.totalUserCount || 0,
          articleCount: res.data.totalCount || 0
        });
      }
    } catch (error) {
       message.error('无法获取系统统计数据');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 p-6 md:p-12">
       <Card className="shadow-lg border-0 rounded-2xl mb-6 max-w-5xl mx-auto p-4">
         <Title level={2} style={{ color: '#52c41a', margin: '0 0 24px 0' }}>平台运营数据</Title>

         <Row gutter={16}>
            <Col span={12}>
                <Card loading={loading}>
                  <Statistic
                    title="注册用户总数"
                    value={data.userCount}
                    valueStyle={{ color: '#52c41a' }}
                    prefix={<ArrowUpOutlined />}
                  />
                </Card>
            </Col>
            <Col span={12}>
                <Card loading={loading}>
                  <Statistic
                    title="生成文章总数"
                    value={data.articleCount}
                    valueStyle={{ color: '#52c41a' }}
                    prefix={<ArrowUpOutlined />}
                  />
                </Card>
            </Col>
         </Row>
       </Card>
    </div>
  );
}
