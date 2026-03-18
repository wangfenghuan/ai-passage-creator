import type { ThemeConfig } from 'antd';

const theme: ThemeConfig = {
  token: {
    colorPrimary: '#52c41a', // 绿色主题
    colorInfo: '#52c41a',
    borderRadius: 6,
    // 以下覆盖可能产生蓝紫色渐变的属性，强制其变为绿色或中性色
    colorPrimaryHover: '#73d13d',
    colorPrimaryActive: '#389e0d',
    colorLink: '#52c41a',
    colorLinkHover: '#73d13d',
    colorLinkActive: '#389e0d',
  },
  components: {
    Button: {
      colorPrimary: '#52c41a',
      colorPrimaryHover: '#73d13d',
      colorPrimaryActive: '#389e0d',
      primaryShadow: '0 2px 0 rgba(82, 196, 26, 0.1)',
    },
  },
};

export default theme;
