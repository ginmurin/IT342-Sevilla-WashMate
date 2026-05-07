import api from "@/features/shared/utils/api";

export interface NotificationData {
  notificationId: number;
  title: string;
  message: string;
  notificationType: 'ORDER_UPDATE' | 'PAYMENT' | 'PROMOTION' | 'SYSTEM';
  referenceType?: string;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}

export const notificationAPI = {
  // Get all notifications
  getNotifications: async (): Promise<NotificationData[]> => {
    const response = await api.get<NotificationData[]>('/api/notifications');
    return response.data;
  },

  // Get unread count
  getUnreadCount: async (): Promise<{ unreadCount: number }> => {
    const response = await api.get<{ unreadCount: number }>('/api/notifications/unread-count');
    return response.data;
  },

  // Mark single notification as read
  markAsRead: async (notificationId: number): Promise<NotificationData> => {
    const response = await api.post<NotificationData>(
      `/api/notifications/${notificationId}/read`,
      {}
    );
    return response.data;
  },

  // Mark all as read
  markAllAsRead: async (): Promise<void> => {
    await api.post('/api/notifications/mark-all-read', {});
  },

  // Delete notification
  deleteNotification: async (notificationId: number): Promise<void> => {
    await api.delete(`/api/notifications/${notificationId}`);
  },
};



