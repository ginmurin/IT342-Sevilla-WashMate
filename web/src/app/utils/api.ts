import axios from 'axios';
import type { AuthResponse, LoginCredentials, RegisterData } from '../types';

const BASE_URL = '';

// Create axios instance — requests go through Vite proxy to Spring Boot
const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to every request (skip for auth endpoints)
api.interceptors.request.use((config) => {
  const url = config.url ?? '';
  if (!url.startsWith('/api/auth/')) {
    const token = localStorage.getItem('washmate_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// Handle API errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('washmate_token');
      localStorage.removeItem('washmate_user');
      window.location.href = '/login';
      return Promise.reject(error);
    }
    // Surface the backend error message if available
    const backendMessage = error.response?.data?.error || error.response?.data?.message;
    if (backendMessage) {
      return Promise.reject(new Error(backendMessage));
    }
    return Promise.reject(error);
  }
);

// ── Auth API ───────────────────────────────────────────────────────────────────

export const authAPI = {
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/login', {
      email: credentials.email,
      password: credentials.password,
    });

    const data = response.data;
    const token: string = data.token;

    // Persist token
    localStorage.setItem('washmate_token', token);

    // Map backend response → frontend AuthResponse shape
    return {
      token,
      user: {
        id: data.userId,
        name: data.fullName,
        email: data.email,
        role: data.role.toUpperCase() as AuthResponse['user']['role'],
      },
    };
  },

  register: async (data: RegisterData): Promise<AuthResponse> => {
    const response = await api.post<{
      token: string;
      email: string;
      fullName: string;
      role: string;
      userId: number;
    }>('/api/auth/register', {
      fullName: data.name,
      email: data.email,
      password: data.password,
      phoneNumber: data.phone,
      role: data.role ? data.role.toUpperCase() : 'CUSTOMER',
    });

    const d = response.data;
    localStorage.setItem('washmate_token', d.token);

    return {
      token: d.token,
      user: {
        id: d.userId,
        name: d.fullName,
        email: d.email,
        role: d.role.toUpperCase() as AuthResponse['user']['role'],
      },
    };
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem('washmate_token');
    localStorage.removeItem('washmate_user');
  },
};

export default api;

