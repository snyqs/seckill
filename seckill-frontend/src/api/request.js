import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../store/user';

// 后端地址：优先用 VITE_API_BASE，未配置则直连本机 8080
const apiBase = import.meta.env.VITE_API_BASE || 'http://192.168.0.1:8080';

const service = axios.create({
  baseURL: apiBase,
  timeout: 10000,
});

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (typeof res === 'object' && res !== null && 'code' in res) {
      if (res.code === 200) {
        return res.data;
      }
      if (res.code !== 404) {
        ElMessage.error(res.msg || '请求失败');
      }
      if (res.code === 401) {
        const store = useUserStore();
        store.logout();
      }
      return Promise.reject(res);
    }
    return res;
  },
  (error) => {
    const msg = error.response?.data?.msg || error.message || '网络错误';
    if (error.response?.status !== 404) {
      ElMessage.error(msg);
    }
    return Promise.reject(error);
  },
);

export default service;
