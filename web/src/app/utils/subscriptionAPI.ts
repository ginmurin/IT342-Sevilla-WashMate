import api from './api';

export interface SubscriptionPaymentDTO {
  paymentId: number;
  userSubscriptionId: number;
  amount: number;
  paymentMethod: string;
  paymentStatus: string;
  createdAt: string;
}

export interface UpgradeInitiationDTO {
  userSubscriptionId: number;
  amount: number;
  planType: string;
  paymentId: number;
}

export const subscriptionAPI = {
  // Initiate subscription upgrade
  initiateUpgrade: (planType: string) =>
    api.post<UpgradeInitiationDTO>(`/api/subscriptions/upgrade/${planType}`),

  // Process subscription upgrade payment (new endpoint - backend handles PayMongo)
  processUpgrade: (planType: string, paymentMethod: string) =>
    api.post(`/api/subscriptions/upgrade/${planType}/process`, { paymentMethod }),

  // Confirm subscription upgrade with payment
  confirmUpgrade: (userSubscriptionId: number, paymentId: string, paymentMethod?: string, amount?: number, paymongoPaymentIntentId?: string) =>
    api.post(`/api/subscriptions/confirm-upgrade/${userSubscriptionId}/${paymentId}`, {
      paymentMethod,
      amount,
      paymongoPaymentIntentId
    }),

  // Get all available plans
  getPlans: () =>
    api.get('/api/subscriptions/plans'),

  // Get current user's active subscription
  getCurrentSubscription: () =>
    api.get('/api/subscriptions/me'),

  // Get subscription history
  getSubscriptionHistory: () =>
    api.get('/api/subscriptions/history'),
};
