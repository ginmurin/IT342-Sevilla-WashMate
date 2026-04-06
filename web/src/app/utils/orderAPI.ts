import api from './api';

export interface OrderPaymentDTO {
  paymentId: number;
  orderId: number;
  amount: number;
  paymentMethod: string;
  paymentStatus: string;
  createdAt: string;
}

export const orderAPI = {
  // Initiate payment for an order
  initiatePayment: (orderId: number, paymentMethod: string) =>
    api.post<OrderPaymentDTO>(`/api/orders/${orderId}/payment/initiate`, { paymentMethod }),

  // Confirm payment for an order
  confirmPayment: (orderId: number, paymentId: string, amount?: number, paymongoPaymentIntentId?: string) =>
    api.post(`/api/orders/${orderId}/payment/confirm/${paymentId}`, { amount, paymongoPaymentIntentId }),

  // Get order by ID
  getOrder: (orderId: number) =>
    api.get(`/api/orders/${orderId}`),

  // Get current user's orders
  getMyOrders: () =>
    api.get('/api/orders/my-orders'),

  // Get order payments
  getOrderPayments: (orderId: number) =>
    api.get(`/api/orders/${orderId}/payments`),
};
