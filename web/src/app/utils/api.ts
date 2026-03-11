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
    const requestUrl = error.config?.url ?? '';
    if (error.response?.status === 401 && !requestUrl.startsWith('/api/auth/')) {
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
      emailOrUsername: credentials.emailOrUsername,
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
        username: data.username ?? null,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        role: data.role.toLowerCase() as AuthResponse['user']['role'],
      },
    };
  },

  register: async (data: RegisterData): Promise<AuthResponse> => {
    const response = await api.post<{
      token: string;
      userId: number;
      username: string | null;
      firstName: string;
      lastName: string;
      email: string;
      role: string;
    }>('/api/auth/register', {
      username: data.username || null,
      firstName: data.firstName,
      lastName: data.lastName,
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
        username: d.username ?? null,
        firstName: d.firstName,
        lastName: d.lastName,
        email: d.email,
        role: d.role.toLowerCase() as AuthResponse['user']['role'],
      },
    };
  },

  sync: async (data: { email: string; uuid: string; jwt: string; user_metadata: any; }): Promise<AuthResponse> => {
    // We send the Supabase JWT in the Authorization header to authenticate the request
    const response = await api.post<{
      userId: number;
      username: string | null;
      firstName: string;
      lastName: string;
      email: string;
      role: string;
    }>('/api/auth/register', {
      email: data.email,
      username: data.user_metadata.username || null,
      firstName: data.user_metadata.first_name || '',
      lastName: data.user_metadata.last_name || '',
      phoneNumber: data.user_metadata.phone || null,
      role: 'CUSTOMER', // Default role for now, could be passed from metadata if you allow it
    }, {
      headers: {
        Authorization: `Bearer ${data.jwt}`
      }
    });

    const d = response.data;
    localStorage.setItem('washmate_token', data.jwt);

    return {
      token: data.jwt,
      user: {
        id: d.userId,
        username: d.username ?? null,
        firstName: d.firstName,
        lastName: d.lastName,
        email: d.email,
        role: d.role.toLowerCase() as AuthResponse['user']['role'],
      },
    };
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem('washmate_token');
    localStorage.removeItem('washmate_user');
  },

  emailByUsername: async (username: string): Promise<string> => {
    const response = await api.get<{ email: string }>(`/api/auth/email-by-username?username=${encodeURIComponent(username)}`);
    return response.data.email;
  },
};

export default api;

