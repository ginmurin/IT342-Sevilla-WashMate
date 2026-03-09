import axios from 'axios';
import type { AuthResponse, LoginCredentials, RegisterData } from '../types';

const BASE_URL = 'http://localhost:8080';

// Create axios instance pointing to Spring Boot backend
const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('washmate_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('washmate_token');
      localStorage.removeItem('washmate_user');
      window.location.href = '/login';
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

  register: async (data: RegisterData): Promise<{ success: boolean; message: string }> => {
    await api.post('/api/auth/register', {
      fullName: data.name,
      email: data.email,
      password: data.password,
      phoneNumber: data.phone,
      role: data.role ? data.role.toUpperCase() : 'CUSTOMER',
    });

    return {
      success: true,
      message: 'Registration successful! Please log in.',
    };
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem('washmate_token');
    localStorage.removeItem('washmate_user');
  },
};

export default api;

