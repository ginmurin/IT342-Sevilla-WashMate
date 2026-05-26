import React, { useState } from 'react';
import { useNotifications } from "@/features/shared/contexts/NotificationContext";
import { Card, CardHeader, CardTitle, CardContent } from "@/features/shared/components/Card";
import { Bell, CheckCircle2, AlertCircle, Zap, Gift, Loader2, Star } from 'lucide-react';
import { motion } from 'motion/react';
import OrderFeedbackModal from '@/features/customer/components/OrderFeedbackModal';

export default function Notifications() {
  const { notifications, markAsRead, markAllAsRead, isLoading, fetchNotifications } = useNotifications();
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [isFeedbackModalOpen, setIsFeedbackModalOpen] = useState(false);

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'ORDER_UPDATE':
        return <AlertCircle className="w-5 h-5 text-blue-600" />;
      case 'PAYMENT':
        return <CheckCircle2 className="w-5 h-5 text-emerald-600" />;
      case 'PROMOTION':
        return <Gift className="w-5 h-5 text-amber-600" />;
      case 'SYSTEM':
        return <Zap className="w-5 h-5 text-slate-600" />;
      case 'FEEDBACK_REQUEST':
        return <Star className="w-5 h-5 text-amber-500" />;
      default:
        return <Bell className="w-5 h-5 text-slate-600" />;
    }
  };

  const getNotificationBgColor = (type: string) => {
    switch (type) {
      case 'ORDER_UPDATE':
        return 'bg-blue-50 border-blue-100';
      case 'PAYMENT':
        return 'bg-emerald-50 border-emerald-100';
      case 'PROMOTION':
        return 'bg-amber-50 border-amber-100';
      case 'SYSTEM':
        return 'bg-slate-50 border-slate-200';
      case 'FEEDBACK_REQUEST':
        return 'bg-amber-50 border-amber-200';
      default:
        return 'bg-slate-50 border-slate-200';
    }
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = (now.getTime() - date.getTime()) / 1000;

    if (diffInSeconds < 60) return 'Just now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const handleFeedbackClick = (notificationId: number, orderId: number) => {
    markAsRead(notificationId);
    setSelectedOrderId(orderId);
    setIsFeedbackModalOpen(true);
  };

  return (
    <div className="min-h-screen bg-slate-50 pt-20 pb-12">
      <div className="max-w-4xl mx-auto px-4">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Notifications</h1>
            <p className="text-slate-600 mt-1">View all your alerts and updates</p>
          </div>
          {notifications.some(n => !n.isRead) && (
            <button
              onClick={() => markAllAsRead()}
              className="text-sm font-medium text-teal-600 hover:text-teal-700 bg-teal-50 px-4 py-2 rounded-lg transition-colors"
            >
              Mark all as read
            </button>
          )}
        </div>

        <Card>
          <CardContent className="p-0">
            {isLoading ? (
              <div className="flex flex-col items-center justify-center h-64 text-slate-500">
                <Loader2 className="w-8 h-8 text-teal-600 animate-spin mb-4" />
                <p>Loading notifications...</p>
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-64 text-slate-500">
                <Bell className="w-12 h-12 mb-4 text-slate-300" />
                <p className="text-lg font-medium text-slate-900">No notifications yet</p>
                <p className="text-sm mt-1">When you get updates, they'll show up here.</p>
              </div>
            ) : (
              <div className="divide-y divide-slate-100">
                {notifications.map((notification) => (
                  <motion.div
                    key={notification.notificationId}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="p-6 transition-colors"
                  >
                    <div className="flex gap-4">
                      <div className="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center border">
                        {getNotificationIcon(notification.notificationType)}
                      </div>
                      <div className="flex-1 min-w-0 pt-1">
                        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-2">
                          <div>
                            <p className="text-base font-semibold text-slate-900">
                              {notification.title}
                            </p>
                            <p className="text-sm text-slate-600 mt-1">
                              {notification.message}
                            </p>
                            {notification.notificationType === 'FEEDBACK_REQUEST' && notification.referenceId && (
                               <button 
                                 onClick={() => handleFeedbackClick(notification.notificationId, notification.referenceId!)}
                                 className="mt-3 text-sm font-medium text-teal-600 hover:text-teal-700 bg-teal-50 px-3 py-1.5 rounded-lg transition-colors inline-block"
                               >
                                 Leave Feedback
                               </button>
                            )}
                          </div>
                          <div className="flex items-center gap-4 mt-2 sm:mt-0">
                            <p className="text-xs font-medium text-slate-500 whitespace-nowrap">
                              {formatTime(notification.createdAt)}
                            </p>
                            {!notification.isRead && (
                              <button
                                onClick={() => markAsRead(notification.notificationId)}
                                className="text-xs font-semibold text-teal-600 hover:text-teal-700 bg-white border border-teal-200 px-3 py-1.5 rounded-full shadow-sm transition-colors"
                              >
                                Mark read
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {isFeedbackModalOpen && selectedOrderId && (
        <OrderFeedbackModal
          isOpen={isFeedbackModalOpen}
          orderId={selectedOrderId}
          onClose={() => {
            setIsFeedbackModalOpen(false);
            setSelectedOrderId(null);
          }}
          onSuccess={() => {
            fetchNotifications();
          }}
        />
      )}
    </div>
  );
}
