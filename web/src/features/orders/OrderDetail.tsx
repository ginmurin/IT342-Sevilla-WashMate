import { useNavigate, useParams } from "react-router";
import { useState, useEffect } from "react";
import { Card, CardContent } from "@/features/shared/components/Card";
import { Button } from "@/features/shared/components/Button";
import {
  MapPin,
  Calendar,
  Clock,
  Package,
  CheckCircle2,
  AlertCircle,
  Loader2,
  ArrowLeft,
  Truck,
  Home,
  Download,
} from "lucide-react";
import { motion } from "motion/react";
import { orderAPI, type OrderResponse } from "@/features/shared/services/order";

export default function OrderDetail() {
  const navigate = useNavigate();
  const { orderId } = useParams();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrder = async () => {
      if (!orderId) {
        setError("Order ID not found");
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError(null);
        const orderData = await orderAPI.getOrderById(parseInt(orderId));
        setOrder(orderData);
      } catch (err) {
        console.error("Failed to fetch order:", err);
        setError(
          err instanceof Error ? err.message : "Failed to load order details"
        );
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusColor = (status: string): string => {
    switch (status.toLowerCase()) {
      case "pending":
        return "text-yellow-600";
      case "confirmed":
        return "text-blue-600";
      case "picked_up":
        return "text-indigo-600";
      case "in_progress":
        return "text-purple-600";
      case "ready":
        return "text-emerald-600";
      case "out_for_delivery":
        return "text-teal-600";
      case "delivered":
        return "text-slate-600";
      case "cancelled":
        return "text-red-600";
      default:
        return "text-slate-600";
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case "pending":
      case "confirmed":
        return <Clock className="w-6 h-6 text-blue-600" />;
      case "picked_up":
        return <Package className="w-6 h-6 text-indigo-600" />;
      case "in_progress":
        return <Clock className="w-6 h-6 text-purple-600" />;
      case "ready":
        return <CheckCircle2 className="w-6 h-6 text-emerald-600" />;
      case "out_for_delivery":
        return <Truck className="w-6 h-6 text-teal-600" />;
      case "delivered":
        return <CheckCircle2 className="w-6 h-6 text-slate-600" />;
      case "cancelled":
        return <AlertCircle className="w-6 h-6 text-red-600" />;
      default:
        return <Clock className="w-6 h-6 text-slate-400" />;
    }
  };

  const getStatusBgColor = (status: string): string => {
    switch (status.toLowerCase()) {
      case "pending":
        return "bg-yellow-50";
      case "confirmed":
        return "bg-blue-50";
      case "picked_up":
        return "bg-indigo-50";
      case "in_progress":
        return "bg-purple-50";
      case "ready":
        return "bg-emerald-50";
      case "out_for_delivery":
        return "bg-teal-50";
      case "delivered":
        return "bg-slate-50";
      case "cancelled":
        return "bg-red-50";
      default:
        return "bg-slate-50";
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <Card className="border-slate-200 shadow-sm">
          <CardContent className="pt-12 pb-12 text-center">
            <Loader2 className="w-8 h-8 text-teal-600 mx-auto mb-3 animate-spin" />
            <p className="text-slate-600">Loading order details...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6"
        >
          <Button
            onClick={() => navigate("/my-orders")}
            variant="outline"
            className="gap-2 border-slate-300"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Orders
          </Button>
        </motion.div>

        <Card className="border-red-200 shadow-sm">
          <CardContent className="pt-12 pb-12 text-center">
            <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-3" />
            <p className="text-red-600 mb-4">
              {error || "Order not found"}
            </p>
            <Button
              onClick={() => navigate("/my-orders")}
              variant="outline"
              className="border-red-300 text-red-600"
            >
              Return to Orders
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8 pt-20 px-4 pb-12">
      {/* Back Button */}
      <motion.div
        initial={{ opacity: 0, y: -16 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <Button
          onClick={() => navigate("/my-orders")}
          variant="outline"
          className="gap-2 border-slate-300"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Orders
        </Button>
      </motion.div>

      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="space-y-2"
      >
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">
              Order {order.orderNumber}
            </h1>
            <p className="text-slate-600">
              Placed on {formatDate(order.createdAt)}
            </p>
          </div>
          <div className={`px-4 py-3 rounded-lg ${getStatusBgColor(order.status)}`}>
            <div className="flex items-center gap-2">
              {getStatusIcon(order.status)}
              <span className={`text-lg font-semibold ${getStatusColor(order.status)}`}>
                {order.status.replace(/_/g, " ")}
              </span>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Main Content */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="space-y-6"
      >
        {/* Services Card */}
        <Card className="border-slate-200 shadow-sm">
          <CardContent className="p-6 space-y-4">
            <div className="flex items-center gap-2 mb-4">
              <Package className="w-5 h-5 text-teal-600" />
              <h2 className="text-xl font-semibold text-slate-900">Services</h2>
            </div>

            <div className="space-y-3">
              {order.services?.map((service) => (
                <div
                  key={service.orderServiceId}
                  className="flex items-center justify-between p-3 bg-slate-50 rounded-lg"
                >
                  <div className="flex-1">
                    <p className="font-medium text-slate-900">
                      {service.serviceName}
                    </p>
                    <p className="text-sm text-slate-600">
                      Quantity: {service.quantity}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-slate-900">
                      ₱{service.unitPrice}
                    </p>
                    <p className="text-sm text-slate-600">
                      Subtotal: ₱{service.subtotal}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {order.totalWeight && (
              <div className="pt-3 border-t border-slate-200">
                <p className="text-slate-700">
                  <span className="font-medium">Total Weight:</span> {order.totalWeight} kg
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Schedule & Address Card */}
        <Card className="border-slate-200 shadow-sm">
          <CardContent className="p-6 space-y-6">
            {/* Pickup */}
            {order.pickupSchedule && (
              <div>
                <div className="flex items-center gap-2 mb-3">
                  <Home className="w-5 h-5 text-blue-600" />
                  <h3 className="font-semibold text-slate-900">Pickup</h3>
                </div>
                <div className="space-y-2 ml-7">
                  <div className="flex items-center gap-2 text-slate-700">
                    <Calendar className="w-4 h-4 text-slate-500" />
                    <span>{formatDate(order.pickupSchedule)}</span>
                  </div>
                </div>
              </div>
            )}

            {/* Delivery */}
            {order.deliverySchedule && (
              <div>
                <div className="flex items-center gap-2 mb-3">
                  <Truck className="w-5 h-5 text-teal-600" />
                  <h3 className="font-semibold text-slate-900">Delivery</h3>
                </div>
                <div className="space-y-2 ml-7">
                  <div className="flex items-center gap-2 text-slate-700">
                    <Calendar className="w-4 h-4 text-slate-500" />
                    <span>{formatDate(order.deliverySchedule)}</span>
                  </div>
                </div>
              </div>
            )}

            {/* Rush Order Badge */}
            {order.isRushOrder && (
              <div className="bg-orange-50 border border-orange-200 rounded-lg p-3">
                <p className="text-orange-700 font-medium">⚡ Rush Order</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Payment Summary Card */}
        <Card className="border-slate-200 shadow-sm bg-gradient-to-br from-slate-50 to-slate-100/50">
          <CardContent className="p-6 space-y-4">
            <h2 className="text-xl font-semibold text-slate-900">Summary</h2>

            <div className="space-y-3 border-t border-slate-200 pt-4">
              <div className="flex justify-between items-center">
                <span className="text-slate-700">Subtotal</span>
                <span className="font-medium text-slate-900">
                  ₱{order.services?.reduce((sum, s) => sum + s.subtotal, 0).toFixed(2)}
                </span>
              </div>

              <div className="flex justify-between items-center pb-3 border-b border-slate-200">
                <span className="text-slate-700">Delivery Fee</span>
                <span className="font-medium text-slate-900">TBD</span>
              </div>

              <div className="flex justify-between items-center pt-3">
                <span className="text-lg font-semibold text-slate-900">
                  Total Amount
                </span>
                <span className="text-2xl font-bold text-teal-600">
                  ₱{order.totalAmount}
                </span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Actions */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="flex gap-3 flex-wrap"
        >
          <Button
            className="flex items-center gap-2 border-slate-300"
            variant="outline"
          >
            <Download className="w-4 h-4" />
            Download Invoice
          </Button>
          <Button className="flex items-center gap-2 border-slate-300" variant="outline">
            <Package className="w-4 h-4" />
            Reorder
          </Button>
        </motion.div>
      </motion.div>
    </div>
  );
}




