import api from '../utils/api';

export interface SubscriptionData {
  subscriptionId: number;
  planType: string;
  planPrice: number;
  ordersIncluded: number | null;
  discountPercentage: number;
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
