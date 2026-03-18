"use client";

import React, { useEffect, useState } from 'react';
import { Card, Table, Typography, Button, Space, message, Popconfirm } from 'antd';
import { useRouter } from 'next/navigation';
import { Service, ArticleVO, DeleteRequest, ArticleQueryRequest } from '@/api';

const { Title } = Typography;

export default function HistoryPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<ArticleVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });

  const loadData = async (current: number, pageSize: number) => {
    setLoading(true);
    try {
      const requestBody: ArticleQueryRequest = {
        current,
        pageSize,
        sortField: 'createTime',
        sortOrder: 'descend',
      };
      
      const res = await Service.listArticle(requestBody);
      
      if (res.code === 0 && res.data) {
        setData(res.data.records || []);
        setTotal(res.data.totalRow || 0);
      } else {
        message.error(res.message || '获取文章列表失败');
      }
    } catch (error: any) {
      if (error?.response?.status === 401 || error.message?.includes('401')) {
        message.warning('请先登录');
        router.push('/login');
      } else {
        message.error('加载历史记录失败');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(pagination.current, pagination.pageSize);
  }, [pagination.current, pagination.pageSize]);

  const handleDelete = async (id: string) => {
    try {
      const req: DeleteRequest = { id: Number(id) }; // 需注意类型的匹配，如果是string转number
      const res = await Service.deleteArticle(req);
      if (res.code === 0) {
        message.success('删除成功');
        loadData(pagination.current, pagination.pageSize);
      } else {
        message.error(res.message || '删除失败');
      }
    } catch (error) {
      message.error('删除异常');
    }
  };

  const columns = [
    {
      title: '生成主题',
      dataIndex: 'topic',
      key: 'topic',
    },
    {
      title: '主标题',
      dataIndex: 'mainTitle',
      key: 'mainTitle',
    },
    {
      title: '任务状态',
      key: 'currentState',
      render: (_: any, record: ArticleVO) => {
        const stateMap: Record<string, string> = {
          'PENDING': '等待中',
          'PHASE1_FINISHED': '待选标题',
          'PHASE2_FINISHED': '待审大纲',
          'FINISHED': '已完成',
          'FAILED': '失败'
        };
        const st = record.phase || record.status || 'UNKNOWN';
        return stateMap[st] || st;
      }
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (time: string) => time ? new Date(time).toLocaleString() : '-'
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: ArticleVO) => (
        <Space size="middle">
          <Button type="link" onClick={() => router.push(`/article/editor/${record.taskId}`)}>
            查看详情
          </Button>
          <Popconfirm
            title="确定要删除这篇文章吗?"
            onConfirm={() => handleDelete(String(record.id))}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const handleTableChange = (pag: any) => {
    setPagination({
      current: pag.current,
      pageSize: pag.pageSize,
    });
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <Card title={<Title level={3} style={{ margin: 0, color: '#52c41a' }}>历史文章</Title>} className="shadow-sm rounded-lg border-0">
        <Table 
          columns={columns} 
          dataSource={data} 
          rowKey="id" 
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: total,
            showSizeChanger: true,
          }}
          onChange={handleTableChange}
        />
      </Card>
    </div>
  );
}
