import type { User, AuthResponse } from '../types';

export const getStoredAuth = (): { token: string | null; user: User | null } => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : null;
  return { token, user };
};

export const setStoredAuth = (authData: AuthResponse): void => {
  localStorage.setItem('token', authData.token);
  localStorage.setItem('user', JSON.stringify(authData.user));
};

export const clearStoredAuth = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

export const isAuthenticated = (): boolean => {
  const { token } = getStoredAuth();
  return !!token;
};

export const getCurrentUser = (): User | null => {
  const { user } = getStoredAuth();
  return user;
};

export const getDashboardPath = (role: string): string => {
  switch (role) {
    case 'CUSTOMER':
      return '/customer/dashboard';
    case 'SHOP_OWNER':
      return '/shop/dashboard';
    case 'ADMIN':
      return '/admin/dashboard';
    default:
      return '/login';
  }
};
