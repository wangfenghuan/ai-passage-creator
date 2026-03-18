"use client";

import React, { useEffect, useState, use } from 'react';
import { Card, Steps, Button, Typography, message, Skeleton, Tag, Space, Divider, Radio, Input } from 'antd';
import { useRouter } from 'next/navigation';
import { 
  Service, 
  ArticleVO, 
  ArticleConfirmTitleRequest, 
  ArticleConfirmOutlineRequest 
} from '@/api';

const { Title, Paragraph } = Typography;
const { TextArea } = Input;

interface EditorPageProps {
  params: Promise<{ taskId: string }>;
}

export default function EditorPage(props: EditorPageProps) {
  const params = use(props.params);
  const { taskId } = params;
  const router = useRouter();

  const [loading, setLoading] = useState(true);
  const [article, setArticle] = useState<ArticleVO | null>(null);
  const [currentStep, setCurrentStep] = useState(0);

  // 阶段 1：确认标题相关
  const [selectedMainTitle, setSelectedMainTitle] = useState('');
  const [selectedSubTitle, setSelectedSubTitle] = useState('');
  const [description, setDescription] = useState('');

  // 阶段 2：确认大纲相关
  const [editedOutline, setEditedOutline] = useState('');

  useEffect(() => {
    if (!taskId) return;
    loadArticle();
    
    const sse = new EventSource(`/api/article/progress/${taskId}`, {
      withCredentials: true,
    });

    sse.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('SSE update:', data);
        if (['PHASE1_FINISHED', 'PHASE2_FINISHED', 'FINISHED'].includes(data.state || data.phase)) {
           loadArticle();
        }
      } catch (e) {
        console.error('Failed to parse SSE data', e);
      }
    };

    sse.onerror = (e) => {
      console.error('SSE Error:', e);
      sse.close();
    };

    return () => {
      sse.close();
    };
  }, [taskId]);

  const loadArticle = async () => {
    try {
      const res = await Service.getArticle(taskId);
      if (res.code === 0 && res.data) {
        setArticle(res.data);
        const currentPhase = res.data.phase || res.data.status;
        updateStepByState(currentPhase);
        
        if (currentPhase === 'PHASE1_FINISHED' && res.data.titleOptions) {
            if (res.data.titleOptions?.length > 0 && !selectedMainTitle) {
               setSelectedMainTitle(res.data.titleOptions[0].mainTitle || '');
               setSelectedSubTitle(res.data.titleOptions[0].subTitle || '');
            }
        }
        
        if (currentPhase === 'PHASE2_FINISHED' && res.data.outline) {
            if (!editedOutline) {
                const outlineText = res.data.outline.map((sec: any) => 
                   `${sec.order}. ${sec.title}\n   ${sec.content}\n`
                ).join('\n');
                setEditedOutline(outlineText);
            }
        }
      }
    } catch (error) {
      message.error('获取文章信息失败');
    } finally {
      setLoading(false);
    }
  };

  const updateStepByState = (state?: string) => {
    switch (state) {
      case 'PENDING':
        setCurrentStep(0); break;
      case 'PHASE1_FINISHED':
        setCurrentStep(1); break;
      case 'PHASE2_FINISHED':
        setCurrentStep(2); break;
      case 'FINISHED':
        setCurrentStep(3); break;
      default:
        setCurrentStep(0);
    }
  };

  const onConfirmTitle = async () => {
    try {
      const req: ArticleConfirmTitleRequest = {
        taskId,
        selectedMainTitle,
        selectedSubTitle,
        userDescription: description,
      };
      const res = await Service.confirmTitle(req);
      if (res.code === 0) {
        message.success('标题已确认，请等待大纲生成');
        setCurrentStep(2);
        loadArticle();
      }
    } catch (e) {
      message.error('操作失败');
    }
  };

  const onConfirmOutline = async () => {
    try {
      const sections = editedOutline.split('\n\n').filter(s => s.trim().length > 0).map((part, i) => {
          const lines = part.split('\n');
          return {
              order: i + 1,
              title: lines[0],
              content: lines.slice(1).join(' ')
          };
      });

      const req: ArticleConfirmOutlineRequest = {
        taskId,
        outline: sections,
      };
      const res = await Service.confirmOutline(req);
      if (res.code === 0) {
        message.success('大纲已确认，正在生成正文及配图');
        setCurrentStep(3);
        loadArticle();
      }
    } catch (e) {
        message.error('操作失败');
    }
  };

  const renderContent = () => {
    if (loading || !article) return <Skeleton active paragraph={{ rows: 8 }} />;

    const currentPhase = article.phase || article.status;

    if (currentPhase === 'PENDING') {
      return (
        <div className="text-center py-10">
          <Title level={4}>AI 正在分析您的选题并为您生成标题候选</Title>
          <p className="text-gray-500">（通常需要 10-30 秒，请耐心等待）</p>
        </div>
      );
    }

    if (currentPhase === 'PHASE1_FINISHED') {
      return (
        <div className="py-4">
          <Title level={4}>请选择一个满意的标题</Title>
          <div className="mb-6">
            <Title level={5}>候选主标题：</Title>
            <Radio.Group onChange={(e) => setSelectedMainTitle(e.target.value)} value={selectedMainTitle}>
              <Space direction="vertical">
                {article.titleOptions?.map((t: any, idx: number) => (
                  <Radio key={`main-${idx}`} value={t.mainTitle}>
                    {t.mainTitle}
                  </Radio>
                ))}
              </Space>
            </Radio.Group>
          </div>

          {selectedMainTitle && (
            <div className="mb-6 bg-gray-50 p-4 rounded-md">
              <Title level={5}>请选择该主标题下的副标题方案：</Title>
              <Radio.Group onChange={(e) => setSelectedSubTitle(e.target.value)} value={selectedSubTitle}>
                <Space direction="vertical">
                  {article.titleOptions?.filter((t: any) => t.mainTitle === selectedMainTitle && t.subTitle).map((t: any, idx: number) => (
                    <Radio key={`sub-${idx}`} value={t.subTitle}>
                      {t.subTitle}
                    </Radio>
                  ))}
                </Space>
              </Radio.Group>
            </div>
          )}

          <div className="mb-8">
            <Title level={5}>您可以补充一些额外需求：</Title>
            <TextArea 
               rows={3} 
               value={description}
               onChange={(e) => setDescription(e.target.value)}
               placeholder="例如：希望文章侧重点在技术实现机制上..." />
          </div>

          <Button type="primary" onClick={onConfirmTitle} disabled={!selectedMainTitle || !selectedSubTitle} style={{ backgroundColor: '#52c41a' }}>
            确定标题并生成大纲
          </Button>
        </div>
      );
    }

    if (currentPhase === 'PHASE2_FINISHED') {
        return (
            <div className="py-4">
                <Title level={4}>文章大纲已生成，您可以进行调整</Title>
                <div className="mb-6">
                  <TextArea 
                    rows={12} 
                    value={editedOutline}
                    onChange={(e) => setEditedOutline(e.target.value)}
                  />
                  <p className="text-sm text-gray-500 mt-2">（暂时采用纯文本格式确认大纲结构）</p>
                </div>
                <Space>
                    <Button type="primary" onClick={onConfirmOutline} style={{ backgroundColor: '#52c41a' }}>
                    确认大纲并生成正文
                    </Button>
                    <Button>
                    使用 AI 自动优化大纲
                    </Button>
                </Space>
            </div>
        );
    }

    if (currentPhase === 'FINISHED') {
        return (
             <div className="py-4">
                <Title level={3}>{article.mainTitle}</Title>
                <Title level={4} type="secondary">{article.subTitle}</Title>
                <Divider />
                <div 
                   className="prose prose-lg max-w-none text-gray-800"
                   dangerouslySetInnerHTML={{ __html: article.content || '<p>内容为空</p>' }}
                />
            </div>
        );
    }

    return null;
  };

  const items = [
    { title: '分析选题' },
    { title: '确认标题' },
    { title: '规划大纲' },
    { title: '正文生成' },
  ];

  return (
    <div className="min-h-screen bg-gray-50 p-6 md:p-12">
       <div className="max-w-4xl mx-auto">
         <Card className="shadow-lg border-0 rounded-2xl mb-6">
            <div className="flex justify-between items-center mb-6">
               <Space>
                   <Title level={2} style={{ margin: 0, color: '#52c41a' }}>AI 文章创作室</Title>
                   <Tag color="green">TaskId: {taskId.substring(0, 8)}...</Tag>
               </Space>
               <Button onClick={() => router.push('/article/history')}>返回列表</Button>
            </div>
            
            <Steps current={currentStep} items={items} className="mb-10" />

            {renderContent()}
         </Card>
       </div>
    </div>
  );
}
