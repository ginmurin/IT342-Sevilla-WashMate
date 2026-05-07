import { useNavigate } from "react-router";
import { useState, useEffect } from "react";
import { Card, CardContent } from "@/features/shared/components/Card";
import { Button } from "@/features/shared/components/Button";
import {
  ShoppingBag,
  CheckCircle2,
  Clock,
  ChevronRight,
  Download,
  Loader2,
  AlertCircle,
} from "lucide-react";
import { motion } from "motion/react";
import { orderAPI, type OrderResponse } from "@/features/shared/services/order";

export default function MyOrders() {
  const navigate = useNavigate();

  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const orderData = await orderAPI.getMyOrders();
        setOrders(orderData);
      } catch (err) {
        console.error('Failed to fetch orders:', err);
        setError(err instanceof Error ? err.message : 'Failed to load orders');
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrders();
  }, []);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getStatusColor = (status: string): string => {
    switch (status.toLowerCase()) {
      case 'pending': return 'text-yellow-600';
      case 'confirmed': return 'text-blue-600';
      case 'picked_up': return 'text-indigo-600';
      case 'in_process': return 'text-purple-600';
      case 'ready': return 'text-emerald-600';
      case 'delivered': return 'text-slate-600';
      case 'cancelled': return 'text-red-600';
      default: return 'text-slate-600';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
      case 'confirmed':
        return <Clock className="w-5 h-5 text-blue-600" />;
      case 'picked_up':
      case 'in_process':
        return <Clock className="w-5 h-5 text-purple-600" />;
      case 'ready':
        return <CheckCircle2 className="w-5 h-5 text-emerald-600" />;
      case 'delivered':
        return <CheckCircle2 className="w-5 h-5 text-slate-600" />;
      case 'cancelled':
        return <AlertCircle className="w-5 h-5 text-red-600" />;
      default:
        return <Clock className="w-5 h-5 text-slate-400" />;
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8 pt-20 px-4 pb-12">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -16 }}
        animate={{ opacity: 1, y: 0 }}
        className="space-y-2"
      >
        <h1 className="text-3xl font-bold text-slate-900">My Orders</h1>
        <p className="text-slate-600">Track and manage your laundry orders</p>
      </motion.div>

      {/* Quick Actions */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="flex gap-3 flex-wrap"
      >
        <Button
          onClick={() => navigate("/order/laundry-details")}
          className="bg-teal-600 hover:bg-teal-700 text-white gap-2"
        >
          <ShoppingBag className="w-4 h-4" />
          New Order
        </Button>
        <Button variant="outline" className="border-slate-300">
          All Orders
        </Button>
        <Button variant="outline" className="border-slate-300">
          Active
        </Button>
      </motion.div>

      {/* Orders List */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15 }}
      >
        <div className="grid grid-cols-1 gap-4">
          {isLoading ? (
            <Card className="border-slate-200 shadow-sm">
              <CardContent className="pt-12 pb-12 text-center">
                <Loader2 className="w-8 h-8 text-teal-600 mx-auto mb-3 animate-spin" />
                <p className="text-slate-600">Loading your orders...</p>
              </CardContent>
            </Card>
          ) : error ? (
            <Card className="border-red-200 shadow-sm">
              <CardContent className="pt-12 pb-12 text-center">
                <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-3" />
                <p className="text-red-600 mb-4">{error}</p>
                <Button
                  onClick={() => window.location.reload()}
                  variant="outline"
                  className="border-red-300 text-red-600"
                >
                  Try Again
                </Button>
              </CardContent>
            </Card>
          ) : orders.length === 0 ? (
            <Card className="border-slate-200 shadow-sm">
              <CardContent className="pt-12 pb-12 text-center">
                <ShoppingBag className="w-12 h-12 text-slate-300 mx-auto mb-3" />
                <p className="text-slate-600 mb-4">No orders yet</p>
                <Button
                  onClick={() => navigate("/order/laundry-details")}
                  className="bg-teal-600 hover:bg-teal-700 text-white"
                >
                  Create First Order
                </Button>
              </CardContent>
            </Card>
          ) : (
            orders.map((order, idx) => (
              <motion.div
                key={order.orderId}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.15 + idx * 0.05 }}
              >
                <Card className="border-slate-200 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 space-y-3">
                        {/* Order ID and Date */}
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="font-semibold text-slate-900 text-lg">
                              {order.services && order.services.length > 0
                                ? order.services.length === 1
                                  ? order.services[0].serviceName
                                  : order.services.map(s => s.serviceName).slice(0, 2).join(", ") + (order.services.length > 2 ? ` +${order.services.length - 2} more` : "")
                                : 'Order'}
                            </p>
                            <p className="text-sm text-slate-500">
                              Order {order.orderNumber} • {formatDate(order.createdAt)}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="text-2xl font-bold text-teal-600">
                              ₱{order.totalAmount}
                            </p>
                          </div>
                        </div>

                        {/* Services List */}
                        {order.services && order.services.length > 0 && (
                          <div className="bg-slate-50 rounded-lg p-3 space-y-2">
                            {order.services.map((service) => (
                              <div key={service.orderServiceId} className="flex justify-between items-center text-sm">
                                <span className="text-slate-700">
                                  {service.serviceName} <span className="text-slate-500">x{service.quantity}</span>
                                </span>
                                <span className="font-medium text-slate-900">₱{service.subtotal}</span>
                              </div>
                            ))}
                          </div>
                        )}

                        {/* Details Grid */}
                        <div className="grid grid-cols-3 gap-4 py-3 border-y border-slate-200">
                          <div>
                            <p className="text-xs text-slate-600 mb-1">Weight</p>
                            <p className="font-semibold text-slate-900">
                              {order.totalWeight || 'N/A'} kg
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-slate-600 mb-1">Services</p>
                            <p className="font-semibold text-slate-900">
                              {order.services?.length || 0}
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-slate-600 mb-1">Status</p>
                            <div className="flex items-center gap-1">
                              {getStatusIcon(order.status)}
                              <span className={`text-sm font-medium ${getStatusColor(order.status)}`}>
                                {order.status.replace('_', ' ')}
                              </span>
                            </div>
                          </div>
                        </div>

                        {/* Actions */}
                        <div className="flex gap-2 pt-2">
                          <Button
                            variant="outline"
                            size="sm"
                            className="flex items-center gap-2 border-slate-300 text-sm"
                          >
                            <Download className="w-3 h-3" />
                            Invoice
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="flex items-center gap-2 border-slate-300 text-sm"
                            onClick={() => navigate(`/orders/${order.orderId}`)}
                          >
                            View Details
                          </Button>
                        </div>
                      </div>

                      <ChevronRight className="w-5 h-5 text-slate-400 mt-1 shrink-0" />
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            ))
          )}
        </div>
      </motion.div>
    </div>
  );
}




