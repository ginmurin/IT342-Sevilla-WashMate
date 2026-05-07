import axios from 'axios';
import type { AuthResponse, LoginCredentials, RegisterData, User } from '../types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Create axios instance
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const PUBLIC_AUTH_PATHS = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/verify-email',
  '/api/auth/resend-otp',
  '/api/auth/forgot-password',
  '/api/auth/reset-password',
  '/api/auth/refresh',
  '/api/auth/email-by-username',
  '/api/auth/google',
  '/api/auth/verify-redirect-code',
  '/api/auth/google/mobile',
  '/api/auth/2fa/login',
  '/api/auth/2fa/resend-login',
];

const isPublicAuthEndpoint = (url: string) =>
  PUBLIC_AUTH_PATHS.some((path) => url.startsWith(path));

const mapUser = (data: any): User => ({
  id: data.userId ?? data.id ?? 0,
  username: data.username ?? null,
  firstName: data.firstName ?? '',
  lastName: data.lastName ?? '',
  email: data.email ?? '',
  role: (data.role ?? 'CUSTOMER').toUpperCase(),
  phone: data.phoneNumber ?? data.phone ?? undefined,
  phoneNumber: data.phoneNumber ?? data.phone ?? undefined,
  emailVerified: data.emailVerified ?? undefined,
  twoFactorEnabled: data.twoFactorEnabled ?? undefined,
});

// Request interceptor: Attach JWT token to every request
api.interceptors.request.use((config) => {
  const url = config.url ?? '';

  // Don't attach token to public auth endpoints
  if (!isPublicAuthEndpoint(url)) {
    const accessToken = localStorage.getItem('washmate_access_token');
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
  }

  return config;
});

// Response interceptor: Handle token refresh on 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const requestUrl = originalRequest?.url ?? '';

    // If 401 and not already retrying, try to refresh token
    if (
      error.response?.status === 401 &&
      !requestUrl.startsWith('/api/auth/') &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('washmate_refresh_token');
        if (refreshToken) {
          // Call refresh endpoint
          const response = await api.post('/api/auth/refresh', {
            refreshToken,
          });

          const newAccessToken = response.data.accessToken;
          localStorage.setItem('washmate_access_token', newAccessToken);

          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        // Clear tokens and redirect to login
        localStorage.removeItem('washmate_access_token');
        localStorage.removeItem('washmate_refresh_token');
        sessionStorage.removeItem('washmate_user');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle other 401 errors (no refresh token available)
    if (error.response?.status === 401 && !requestUrl.startsWith('/api/auth/')) {
      localStorage.removeItem('washmate_access_token');
      localStorage.removeItem('washmate_refresh_token');
      sessionStorage.removeItem('washmate_user');
      window.location.href = '/login';
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
  /**
   * Login with email/username and password
   */
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/login', {
      emailOrUsername: credentials.emailOrUsername,
      password: credentials.password,
    });

    const data = response.data;

    // Check if email verification is required
    if (data.requiresEmailVerification) {
      return {
        token: '', // No token yet
        user: {
          id: data.userId,
          email: data.email,
          username: null,
          firstName: '',
          lastName: '',
          role: 'CUSTOMER',
        },
        requiresEmailVerification: true,
        userId: data.userId,
      };
    }

    // Check if two-factor verification is required
    if (data.requiresTwoFactor) {
      return {
        token: '', // No token yet
        user: {
          id: data.userId,
          email: data.email,
          username: null,
          firstName: '',
          lastName: '',
          role: 'CUSTOMER',
        },
        requiresTwoFactor: true,
        userId: data.userId,
      };
    }

    // Store tokens
    localStorage.setItem('washmate_access_token', data.accessToken);
    localStorage.setItem('washmate_refresh_token', data.refreshToken);

    // Map backend response to frontend shape
    return {
      token: data.accessToken,
      user: mapUser(data),
    };
  },

  /**
   * Register with email, username, and password
   */
  register: async (data: RegisterData): Promise<{ userId: number; email: string; requiresEmailVerification: boolean }> => {
    const response = await api.post('/api/auth/register', {
      username: data.username || null,
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      password: data.password,
      phoneNumber: data.phone,
      role: data.role ? data.role.toUpperCase() : 'CUSTOMER',
    });

    return {
      userId: response.data.userId,
      email: response.data.email,
      requiresEmailVerification: response.data.requiresEmailVerification,
    };
  },

  /**
   * Verify email with OTP code
   */
  verifyEmail: async (userId: number, code: string): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/verify-email', {
      userId,
      code,
    });

    const data = response.data;

    // Store tokens
    localStorage.setItem('washmate_access_token', data.accessToken);
    localStorage.setItem('washmate_refresh_token', data.refreshToken);

    return {
      token: data.accessToken,
      user: mapUser(data),
    };
  },

  /**
   * Verify 2FA login code
   */
  verifyTwoFactorLogin: async (userId: number, code: string): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/2fa/login', {
      userId,
      code,
    });

    const data = response.data;

    // Store tokens
    localStorage.setItem('washmate_access_token', data.accessToken);
    localStorage.setItem('washmate_refresh_token', data.refreshToken);

    return {
      token: data.accessToken,
      user: mapUser(data),
    };
  },

  /**
   * Resend OTP code
   */
  resendOtp: async (email: string): Promise<{ message: string; expiresIn: number }> => {
    const response = await api.post('/api/auth/resend-otp', { email });
    return response.data;
  },

  /**
   * Resend 2FA login code
   */
  resendTwoFactorLogin: async (userId: number): Promise<{ message: string }> => {
    const response = await api.post('/api/auth/2fa/resend-login', { userId });
    return response.data;
  },

  /**
   * Initiate password reset
   */
  forgotPassword: async (email: string): Promise<{ message: string; expiresIn: number }> => {
    const response = await api.post('/api/auth/forgot-password', { email });
    return response.data;
  },

  /**
   * Reset password with code
   */
  resetPassword: async (email: string, code: string, newPassword: string): Promise<{ message: string }> => {
    const response = await api.post('/api/auth/reset-password', {
      email,
      code,
      newPassword,
    });
    return response.data;
  },

  /**
   * Refresh access token
   */
  refreshToken: async (refreshToken: string): Promise<string> => {
    const response = await api.post('/api/auth/refresh', {
      refreshToken,
    });
    return response.data.accessToken;
  },

  /**
   * Logout
   */
  logout: async (refreshToken?: string): Promise<void> => {
    try {
      if (refreshToken) {
        await api.post('/api/auth/logout', { refreshToken });
      }
    } finally {
      localStorage.removeItem('washmate_access_token');
      localStorage.removeItem('washmate_refresh_token');
      sessionStorage.removeItem('washmate_user');
    }
  },

  /**
   * Get email by username (for login form)
   */
  emailByUsername: async (username: string): Promise<string> => {
    const response = await api.get<{ email: string }>(`/api/auth/email-by-username?username=${encodeURIComponent(username)}`);
    return response.data.email;
  },

  /**
   * Exchange OAuth redirect code for tokens
   */
  verifyRedirectCode: async (redirectCode: string): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/verify-redirect-code', {
      redirectCode,
    });

    const data = response.data;

    // Store tokens
    localStorage.setItem('washmate_access_token', data.accessToken);
    localStorage.setItem('washmate_refresh_token', data.refreshToken);

    return {
      token: data.accessToken,
      user: mapUser(data),
    };
  },

  /**
   * Google OAuth with ID token (mobile flow)
   */
  loginWithGoogle: async (idToken: string): Promise<AuthResponse> => {
    const response = await api.post('/api/auth/google/mobile', {
      idToken,
    });

    const data = response.data;

    // Store tokens
    localStorage.setItem('washmate_access_token', data.accessToken);
    localStorage.setItem('washmate_refresh_token', data.refreshToken);

    return {
      token: data.accessToken,
      user: mapUser(data),
    };
  },

  /**
   * Get current authenticated user
   */
  me: async (): Promise<User> => {
    const response = await api.get('/api/auth/me');
    return mapUser(response.data);
  },

  /**
   * Update current user profile
   */
  updateMe: async (payload: { firstName?: string; lastName?: string; phoneNumber?: string }): Promise<User> => {
    const response = await api.put('/api/auth/me', payload);
    return mapUser(response.data);
  },

  /**
   * Send 2FA verification code
   */
  sendTwoFactorCode: async (): Promise<{ message?: string }> => {
    const response = await api.post('/api/auth/2fa/send-code');
    return response.data;
  },

  /**
   * Enable 2FA with verification code
   */
  enableTwoFactor: async (code: string): Promise<{ message?: string; twoFactorEnabled?: boolean }> => {
    const response = await api.post('/api/auth/2fa/enable', { code });
    return response.data;
  },

  /**
   * Disable 2FA
   */
  disableTwoFactor: async (): Promise<{ message?: string; twoFactorEnabled?: boolean }> => {
    const response = await api.post('/api/auth/2fa/disable');
    return response.data;
  },

  /**
   * Change password for authenticated user
   */
  changePassword: async (payload: { currentPassword: string; newPassword: string }): Promise<{ message?: string }> => {
    const response = await api.post('/api/auth/change-password', payload);
    return response.data;
  },
};

export default api;
