import api from '../utils/api';
import type { OrderResponse } from './order';
import type { ServiceResponse } from './service';

// ── Shop-specific Types ─────────────────────────────────────────────────────

export interface ShopStats {
  totalRevenue: number;
  totalOrders: number;
  activeOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  totalCustomers: number;
  avgOrderValue: number;
  todayRevenue: number;
  todayOrders: number;
}

export interface RevenueDataPoint {
  date: string;
  revenue: number;
  orders: number;
}

// ── Shop API ────────────────────────────────────────────────────────────────

export const shopAPI = {
  /**
   * Get all orders in the system (shop owner view).
   * Falls back to /api/orders/my-orders if no admin endpoint exists.
   */
  getAllOrders: async (): Promise<OrderResponse[]> => {
    try {
      const response = await api.get<OrderResponse[]>('/api/orders/all');
      return response.data;
    } catch {
      return [];
    }
  },

  /**
   * Update an order's status.
   */
  updateOrderStatus: async (orderId: number, status: string): Promise<OrderResponse> => {
    const response = await api.put<OrderResponse>(`/api/orders/${orderId}/status`, { status });
    return response.data;
  },

  /**
   * Get all services (for pricing management).
   */
  getAllServices: async (): Promise<ServiceResponse[]> => {
    const response = await api.get<ServiceResponse[]>('/api/services');
    return response.data;
  },

  /**
   * Update the base price of a service.
   */
  updateServicePrice: async (serviceId: number, price: number): Promise<ServiceResponse> => {
    const response = await api.put<ServiceResponse>(`/api/services/${serviceId}/price`, { price });
    return response.data;
  },

  /**
   * Update the price of a service variant.
   */
  updateVariantPrice: async (serviceId: number, variantId: number, price: number): Promise<ServiceResponse> => {
    const response = await api.put<ServiceResponse>(`/api/services/${serviceId}/variants/${variantId}/price`, { price });
    return response.data;
  },

  /**
   * Calculate dashboard stats from orders data.
   */
  calculateStats: (orders: OrderResponse[]): ShopStats => {
    const now = new Date();
    const todayStr = now.toISOString().split('T')[0];

    const completed = orders.filter(o => o.status?.toUpperCase() === 'DELIVERED');
    const active = orders.filter(o =>
      o.status && !['DELIVERED', 'CANCELLED'].includes(o.status.toUpperCase())
    );
    const cancelled = orders.filter(o => o.status?.toUpperCase() === 'CANCELLED');
    const todayOrders = orders.filter(o => o.createdAt?.startsWith(todayStr));

    const totalRevenue = orders
      .filter(o => o.status?.toUpperCase() !== 'CANCELLED')
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

    const todayRevenue = todayOrders
      .filter(o => o.status?.toUpperCase() !== 'CANCELLED')
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

    const uniqueCustomers = new Set(orders.map(o => o.customerId)).size;

    return {
      totalRevenue,
      totalOrders: orders.length,
      activeOrders: active.length,
      completedOrders: completed.length,
      cancelledOrders: cancelled.length,
      totalCustomers: uniqueCustomers,
      avgOrderValue: orders.length > 0 ? totalRevenue / orders.filter(o => o.status?.toUpperCase() !== 'CANCELLED').length : 0,
      todayRevenue,
      todayOrders: todayOrders.length,
    };
  },

  /**
   * Generate revenue data for charts from orders.
   */
  getRevenueData: (orders: OrderResponse[], days: number = 7): RevenueDataPoint[] => {
    const data: RevenueDataPoint[] = [];
    const now = new Date();
    // Normalize now to start of day for comparison
    now.setHours(0, 0, 0, 0);

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(now.getDate() - i);
      
      const label = date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });

      const dayOrders = orders.filter(o => {
        if (!o.createdAt) return false;
        const oDate = new Date(o.createdAt);
        return oDate.getFullYear() === date.getFullYear() &&
               oDate.getMonth() === date.getMonth() &&
               oDate.getDate() === date.getDate() &&
               o.status?.toUpperCase() !== 'CANCELLED';
      });

      data.push({
        date: label,
        revenue: dayOrders.reduce((sum, o) => sum + (o.totalAmount || 0), 0),
        orders: dayOrders.length,
      });
    }

    return data;
  },
};
