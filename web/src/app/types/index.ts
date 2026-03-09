export type UserRole = 'CUSTOMER' | 'SHOP_OWNER' | 'ADMIN';

export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  phone?: string;
  avatar?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterData {
  name: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
  acceptTerms: boolean;
  role?: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  serviceType: 'wash_fold' | 'wash_iron' | 'dry_clean';
  status: 'pending' | 'processing' | 'completed' | 'cancelled';
  date: string;
  amount: number;
  customerName?: string;
  customerPhone?: string;
  pickupAddress?: string;
  pickupTime?: string;
  quantity?: number;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  time: string;
  read: boolean;
  icon: string;
}

export interface DashboardStats {
  activeOrders?: number;
  subscription?: string;
  walletBalance?: number;
  todayOrders?: number;
  inProgress?: number;
  completedToday?: number;
  revenueToday?: number;
  totalUsers?: number;
  totalRevenue?: number;
  systemHealth?: string;
}
