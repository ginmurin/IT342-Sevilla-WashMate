import api from '../utils/api';

export interface OrderService {
  serviceId: number;
  quantity: number;
  selectedVariantId?: number;  // For services with variants (e.g., Dry Clean)
}

export interface CreateOrderRequest {
  services: OrderService[];                 // Multiple services per order
  totalWeight?: number;
  specialInstructions?: string;
  deliveryFee?: number;                     // Calculated delivery fee (0 or 50)
  pickupSchedule?: string;
  deliverySchedule?: string;
  isRushOrder?: boolean;
  pickupAddressId?: number;
  deliveryAddressId?: number;
  // Address information from frontend (for creating new addresses)
  pickupAddressString?: string;
  pickupLatitude?: number;
  pickupLongitude?: number;
  deliveryAddressString?: string;
  deliveryLatitude?: number;
  deliveryLongitude?: number;
}

export interface OrderServiceResponse {
  orderServiceId: number;
  serviceId: number;
  serviceName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderResponse {
  orderId: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  totalWeight?: number;
  services: OrderServiceResponse[];         // All services in this order
  customerId: number;
  customerName: string;
  pickupSchedule?: string;
  deliverySchedule?: string;
  isRushOrder?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentProcessResponse {
  paymentId: number;
  orderId: number;
  amount: number;
  paymentMethod: string;
  paymentIntentId?: string;  // For card payments
  clientKey?: string;        // For card payments
  checkoutUrl?: string;      // For e-wallet payments
  sourceId?: string;         // For e-wallet payments
  walletPayment?: boolean;   // For wallet payments
  error?: string;
}

export const orderAPI = {
  createOrder: async (request: CreateOrderRequest): Promise<OrderResponse> => {
    const response = await api.post<OrderResponse>('/api/orders/create', request);
    return response.data;
  },

  processPayment: async (orderId: number, paymentMethod: string): Promise<PaymentProcessResponse> => {
    const response = await api.post<PaymentProcessResponse>(`/api/orders/${orderId}/payment/process`, { paymentMethod });
    return response.data;
  },

  confirmPayment: async (orderId: number, paymentId: string, amount?: number, paymongoPaymentIntentId?: string): Promise<OrderResponse> => {
    const response = await api.post<OrderResponse>(`/api/orders/${orderId}/payment/confirm/${paymentId}`, { amount, paymongoPaymentIntentId });
    return response.data;
  },

  getMyOrders: async (): Promise<OrderResponse[]> => {
    const response = await api.get<OrderResponse[]>('/api/orders/my-orders');
    return response.data;
  },

  getOrderById: async (orderId: number): Promise<OrderResponse> => {
    const response = await api.get<OrderResponse>(`/api/orders/${orderId}`);
    return response.data;
  }
};
