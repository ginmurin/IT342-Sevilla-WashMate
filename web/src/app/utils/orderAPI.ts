import api from './api';
import type { AxiosResponse } from 'axios';

export interface OrderServiceDTO {
  orderServiceId?: number;
  serviceId: number;
  serviceName: string;
  quantity: number;
  unitPrice?: number;
  subtotal?: number;
}

export interface OrderDTO {
  orderId: number;
  orderNumber: string;
  customerId: number;
  customerName: string;
  services: OrderServiceDTO[];
  pickupAddressId: number;
  deliveryAddressId: number;
  totalWeight: number;
  totalAmount: number;
  status: string;
  specialInstructions?: string;
  pickupSchedule: string;
  deliverySchedule: string;
  isRushOrder: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OrderPaymentDTO {
  paymentId: number;
  orderId: number;
  amount: number;
  paymentMethod: string;
  paymentStatus: string;
  createdAt: string;
}

export interface PaymentProcessResponse {
  paymentId: number;
  orderId: number;
  amount: number;
  paymentMethod: string;
  paymentIntentId?: string;
  clientKey?: string;
  checkoutUrl?: string;
  sourceId?: string;
  walletPayment?: boolean;
  error?: string;
}

export const orderAPI = {
  // Create a new order
  createOrder: (request: any): Promise<AxiosResponse<OrderDTO>> =>
    api.post<OrderDTO>('/api/orders/create', request),

  // Initiate payment for an order (old endpoint - kept for compatibility)
  initiatePayment: (orderId: number, paymentMethod: string) =>
    api.post<OrderPaymentDTO>(`/api/orders/${orderId}/payment/initiate`, { paymentMethod }),

  // Process payment for an order (new endpoint - backend handles PayMongo)
  processPayment: (orderId: number, paymentMethod: string): Promise<AxiosResponse<PaymentProcessResponse>> =>
    api.post<PaymentProcessResponse>(`/api/orders/${orderId}/payment/process`, { paymentMethod }),

  // Confirm payment for an order
  confirmPayment: (orderId: number, paymentId: string, amount?: number, paymongoPaymentIntentId?: string): Promise<AxiosResponse<OrderDTO>> =>
    api.post<OrderDTO>(`/api/orders/${orderId}/payment/confirm/${paymentId}`, { amount, paymongoPaymentIntentId }),

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
