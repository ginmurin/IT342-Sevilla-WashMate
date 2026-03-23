import api from '../utils/api';

export interface SubscriptionData {
  subscriptionId: number;
  planType: string;
  planPrice: number;
  ordersIncluded: number | null;
  discountPercentage: number;
  createdAt: string;
}

export interface UserSubscriptionData {
  userSubscriptionId: number;
  planType: string;
  planPrice: number;
  discountPercentage: number;
  startDate: string;
  expiryDate: string;
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  createdAt: string;
}

export async function getMySubscription(): Promise<SubscriptionData | null> {
  try {
    const res = await api.get<SubscriptionData>('/api/subscriptions/me');
    return res.data;
  } catch {
    return null;
  }
}

export async function getSubscriptionPlans(): Promise<SubscriptionData[]> {
  try {
    const res = await api.get<SubscriptionData[]>('/api/subscriptions/plans');
    return res.data || [];
  } catch (error) {
    console.error('Error fetching subscription plans:', error);
    return [];
  }
}

export async function getCurrentSubscription(): Promise<UserSubscriptionData | null> {
  try {
    const res = await api.get<UserSubscriptionData>('/api/subscriptions/me');
    return res.data;
  } catch {
    return null;
  }
}

export async function initiateUpgrade(planType: string): Promise<{
  userSubscriptionId: string;
  amount: number;
  expiryDate: string;
}> {
  try {
    const res = await api.post<{
      userSubscriptionId: string;
      planType: string;
      amount: number;
      expiryDate: string;
    }>(`/api/subscriptions/upgrade/${planType}`);
    return res.data;
  } catch (error) {
    console.error('Error initiating upgrade:', error);
    throw error;
  }
}

export async function confirmUpgrade(userSubscriptionId: string, paymentId: string): Promise<UserSubscriptionData> {
  try {
    const res = await api.post<UserSubscriptionData>(
      `/api/subscriptions/confirm-upgrade/${userSubscriptionId}/${paymentId}`
    );
    return res.data;
  } catch (error) {
    console.error('Error confirming upgrade:', error);
    throw error;
  }
}

export async function getSubscriptionHistory(): Promise<UserSubscriptionData[]> {
  try {
    const res = await api.get<UserSubscriptionData[]>('/api/subscriptions/history');
    return res.data || [];
  } catch (error) {
    console.error('Error fetching subscription history:', error);
    return [];
  }
}
