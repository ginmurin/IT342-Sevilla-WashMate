export type UserRole = 'CUSTOMER' | 'ADMIN' | 'SHOP_OWNER';

export interface User {
  id: number;
  username: string | null;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  status?: string;
  phone?: string;
  phoneNumber?: string;
  emailVerified?: boolean;
  twoFactorEnabled?: boolean;
  createdAt?: string;
  walletBalance?: number;
}

export interface AuthResponse {
  token: string;
  user: User;
  requiresEmailVerification?: boolean;
  requiresTwoFactor?: boolean;
  userId?: number;
}

export interface LoginCredentials {
  emailOrUsername: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterData {
  username?: string;
  firstName: string;
  lastName: string;
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
