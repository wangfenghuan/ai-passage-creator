import { OpenAPI } from '@/api';
import { message } from 'antd';
import axios from 'axios';

// 配置 OpenAPI 基础设置
OpenAPI.BASE = '/api'; // 依赖 Next.js 的 rewrite 或直接映射到后端域名
OpenAPI.WITH_CREDENTIALS = true;

// 我们还可以直接修改 OpenAPI 生成的默认 client 实例
// 获取 axios 实例
const axiosInstance = axios.create({
  baseURL: OpenAPI.BASE,
  withCredentials: true,
  timeout: 60000,
});

axiosInstance.interceptors.request.use(
  (config) => {
    // 可以在这里统一添加 Token
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 如果返回的自定义错误码表示失败
    if (res && res.code && res.code !== 0) {
      message.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return response;
  },
  (error) => {
    if (error.response) {
      if (error.response.status === 401) {
        message.error('未登录或会话已过期，请重新登录');
        // 可选：重定向到登录页
      } else {
        message.error(`请求失败: ${error.response.status}`);
      }
    } else {
      message.error('网络异常，请稍后重试');
    }
    return Promise.reject(error);
  }
);

// OpenAPI 生成工具默认可以通过重写 axios 请求来进行拦截控制。
// 这里我们也可以导出一个挂载好拦截器的实例备用
export default axiosInstance;
