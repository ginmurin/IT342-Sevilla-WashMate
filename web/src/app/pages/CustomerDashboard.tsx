import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { ShoppingBag, Crown, Zap, Tag, Star, ChevronRight, Loader2, AlertCircle } from "lucide-react";
import { getMySubscription, type SubscriptionData } from "../services/subscription";
import { orderAPI, type OrderResponse } from "../services/order";

export function CustomerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [subscription, setSubscription] = useState<SubscriptionData | null>(null);
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(true);
  const [orderError, setOrderError] = useState<string | null>(null);

  useEffect(() => {
    getMySubscription().then(setSubscription);
  }, []);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setIsLoadingOrders(true);
        setOrderError(null);
        const ordersData = await orderAPI.getMyOrders();
        setOrders(ordersData);
      } catch (err) {
        console.error("Failed to fetch orders:", err);
        setOrderError("Failed to load orders");
      } finally {
        setIsLoadingOrders(false);
      }
    };

    fetchOrders();
  }, []);

  const isPremium = subscription?.planType === "PREMIUM";

  // Calculate active orders (not delivered or cancelled)
  const activeOrdersCount = orders.filter(
    (order) =>
      order.status &&
      !["DELIVERED", "CANCELLED"].includes(order.status.toUpperCase())
  ).length;

  // Calculate total spent (all completed/confirmed orders, excluding cancelled)
  const totalSpent = orders
    .filter((order) => order.status && order.status.toUpperCase() !== "CANCELLED" && order.status.toUpperCase() !== "PENDING")
    .reduce((sum, order) => sum + (order.totalAmount || 0), 0);

  // Get recent orders (latest 3)
  const recentOrders = orders
    .sort(
      (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
    .slice(0, 3);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  const getStatusColor = (status: string): string => {
    switch (status?.toUpperCase()) {
      case "PENDING":
        return "text-yellow-600 bg-yellow-50";
      case "CONFIRMED":
        return "text-blue-600 bg-blue-50";
      case "PICKED_UP":
        return "text-indigo-600 bg-indigo-50";
      case "IN_PROGRESS":
        return "text-purple-600 bg-purple-50";
      case "READY":
        return "text-emerald-600 bg-emerald-50";
      case "OUT_FOR_DELIVERY":
        return "text-teal-600 bg-teal-50";
      case "DELIVERED":
        return "text-slate-600 bg-slate-50";
      case "CANCELLED":
        return "text-red-600 bg-red-50";
      default:
        return "text-slate-600 bg-slate-50";
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto w-full pt-20 px-4 pb-12">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">My Laundry</h1>
          <p className="text-slate-500 mt-1">Welcome back, {user?.firstName}. Here's the status of your orders.</p>
        </div>
        <Button
          className="bg-teal-600 hover:bg-teal-700 w-full md:w-auto"
          onClick={() => navigate("/order/laundry-details")}
        >
          <ShoppingBag className="w-4 h-4 mr-2" />
          New Order
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-gradient-to-br from-teal-500 to-teal-700 text-white border-none shadow-md">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-medium opacity-90">Active Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold">{activeOrdersCount}</div>
            <p className="text-teal-100 text-sm mt-1">
              {activeOrdersCount === 0 ? "No active orders" : `${activeOrdersCount} order${activeOrdersCount !== 1 ? "s" : ""} in progress`}
            </p>
          </CardContent>
        </Card>

        {isPremium ? (
          <Card className="bg-gradient-to-br from-amber-400 via-amber-500 to-orange-500 text-white border-none shadow-lg">
            <CardHeader className="pb-2">
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg font-medium text-white/90">Subscription</CardTitle>
                <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full font-semibold tracking-wide">ACTIVE</span>
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2 mb-3">
                <Crown className="w-6 h-6 text-white drop-shadow" />
                <span className="text-2xl font-bold text-white">Premium</span>
              </div>
              <div className="space-y-1.5">
                <div className="flex items-center gap-2 text-white/90 text-sm">
                  <Tag className="w-3.5 h-3.5 shrink-0" />
                  <span>{subscription?.discountPercentage ?? 15}% off all orders</span>
                </div>
                <div className="flex items-center gap-2 text-white/90 text-sm">
                  <Zap className="w-3.5 h-3.5 shrink-0" />
                  <span>Priority order processing</span>
                </div>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card className="border-slate-200 shadow-sm">
            <CardHeader className="pb-2">
              <CardTitle className="text-lg font-medium text-slate-700">Subscription</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2 mb-1">
                <Star className="w-5 h-5 text-slate-300" />
                <span className="text-xl font-bold text-slate-900">Free</span>
              </div>
              <p className="text-slate-400 text-xs mb-3">No discounts · Standard processing</p>
              <button
                onClick={() => navigate("/subscriptions")}
                className="text-xs font-semibold text-amber-600 hover:text-amber-700 underline underline-offset-2"
              >
                Upgrade to Premium →
              </button>
            </CardContent>
          </Card>
        )}

        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-medium text-slate-700">Total Spent</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-slate-900">₱{totalSpent.toFixed(2)}</div>
            <p className="text-slate-500 text-sm mt-1">
              {totalSpent === 0 ? "No completed orders yet" : `From ${orders.filter((o) => o.status && o.status.toUpperCase() !== "CANCELLED" && o.status.toUpperCase() !== "PENDING").length} active order${orders.filter((o) => o.status && o.status.toUpperCase() !== "CANCELLED" && o.status.toUpperCase() !== "PENDING").length !== 1 ? "s" : ""}`}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="mt-8">
        <Card className="shadow-sm border-slate-200">
          <CardHeader className="border-b border-slate-100 pb-4">
            <CardTitle className="text-xl text-slate-800">Recent Orders</CardTitle>
            <CardDescription>View and track your current laundry orders.</CardDescription>
          </CardHeader>
          <CardContent>
            {isLoadingOrders ? (
              <div className="py-10 text-center">
                <Loader2 className="w-8 h-8 text-teal-600 mx-auto mb-3 animate-spin" />
                <p className="text-slate-500">Loading orders...</p>
              </div>
            ) : orderError ? (
              <div className="py-10 text-center">
                <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-3" />
                <p className="text-red-600 mb-4">{orderError}</p>
              </div>
            ) : recentOrders.length === 0 ? (
              <div className="py-10 text-center">
                <ShoppingBag className="w-12 h-12 text-slate-300 mx-auto mb-3" />
                <p className="text-slate-500 mb-4">You have no orders yet</p>
                <Button
                  className="bg-teal-600 hover:bg-teal-700 text-white"
                  onClick={() => navigate("/order/laundry-details")}
                >
                  Place Your First Order
                </Button>
              </div>
            ) : (
              <div className="space-y-3">
                {recentOrders.map((order) => (
                  <div
                    key={order.orderId}
                    className="flex items-center justify-between p-4 border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer"
                    onClick={() => navigate(`/orders/${order.orderId}`)}
                  >
                    <div className="flex-1 space-y-1">
                      <p className="font-semibold text-slate-900">Order {order.orderNumber}</p>
                      <p className="text-sm text-slate-500">{formatDate(order.createdAt)}</p>
                    </div>
                    <div className="flex items-center gap-4">
                      <span className={`text-sm font-medium px-3 py-1 rounded-full ${getStatusColor(order.status)}`}>
                        {order.status?.replace(/_/g, " ")}
                      </span>
                      <div className="text-right">
                        <p className="font-semibold text-slate-900">₱{order.totalAmount}</p>
                        <p className="text-xs text-slate-500">{order.services?.length || 0} service{order.services && order.services.length !== 1 ? "s" : ""}</p>
                      </div>
                      <ChevronRight className="w-5 h-5 text-slate-400" />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}