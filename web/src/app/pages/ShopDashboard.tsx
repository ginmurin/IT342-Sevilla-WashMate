import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  Store,
  DollarSign,
  ShoppingBag,
  Users,
  TrendingUp,
  Clock,
  CheckCircle2,
  XCircle,
  ChevronRight,
  Loader2,
  AlertCircle,
  ArrowUpRight,
  ArrowDownRight,
  Package,
  BarChart3,
} from "lucide-react";
import { motion } from "motion/react";
import { shopAPI, type ShopStats, type RevenueDataPoint } from "../services/shopAPI";
import type { OrderResponse } from "../services/order";

// ── Mini bar-chart component ────────────────────────────────────────────────
function MiniBarChart({ data }: { data: RevenueDataPoint[] }) {
  const maxRevenue = Math.max(...data.map((d) => d.revenue), 1);

  return (
    <div className="flex items-end gap-1.5 h-32 w-full">
      {data.map((d, i) => {
        const height = (d.revenue / maxRevenue) * 100;
        return (
          <div key={i} className="flex-1 flex flex-col items-center gap-1 h-full">
            <div className="w-full relative group flex-1 flex items-end">
              {/* Tooltip */}
              <div className="absolute -top-10 left-1/2 -translate-x-1/2 bg-slate-800 text-white text-xs px-2 py-1 rounded-md whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10">
                ₱{d.revenue.toLocaleString()} · {d.orders} orders
              </div>
              <motion.div
                initial={{ height: 0 }}
                animate={{ height: `${Math.max(height, 4)}%` }}
                transition={{ delay: 0.3 + i * 0.05, duration: 0.4, ease: "easeOut" }}
                className={`w-full rounded-t-md ${
                  i === data.length - 1
                    ? "bg-gradient-to-t from-teal-500 to-teal-400"
                    : "bg-teal-200 hover:bg-teal-300"
                } transition-colors cursor-pointer`}
                style={{ minHeight: "4px" }}
              />
            </div>
            <span className="text-[10px] text-slate-400 leading-none h-3">{d.date.split(" ")[1]}</span>
          </div>
        );
      })}
    </div>
  );
}

// ── Status badge ────────────────────────────────────────────────────────────
function StatusBadge({ status }: { status: string }) {
  const config: Record<string, { bg: string; text: string; dot: string }> = {
    PENDING: { bg: "bg-amber-50", text: "text-amber-700", dot: "bg-amber-500" },
    CONFIRMED: { bg: "bg-blue-50", text: "text-blue-700", dot: "bg-blue-500" },
    PICKED_UP: { bg: "bg-indigo-50", text: "text-indigo-700", dot: "bg-indigo-500" },
    IN_PROGRESS: { bg: "bg-purple-50", text: "text-purple-700", dot: "bg-purple-500" },
    READY: { bg: "bg-emerald-50", text: "text-emerald-700", dot: "bg-emerald-500" },
    OUT_FOR_DELIVERY: { bg: "bg-teal-50", text: "text-teal-700", dot: "bg-teal-500" },
    DELIVERED: { bg: "bg-slate-100", text: "text-slate-600", dot: "bg-slate-400" },
    CANCELLED: { bg: "bg-red-50", text: "text-red-600", dot: "bg-red-500" },
  };
  const c = config[status?.toUpperCase()] || config.PENDING;

  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${c.bg} ${c.text}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${c.dot}`} />
      {status?.replace(/_/g, " ")}
    </span>
  );
}

// ── Main Dashboard ──────────────────────────────────────────────────────────
export function ShopDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const ordersData = await shopAPI.getAllOrders();
        setOrders(ordersData);
      } catch (err) {
        console.error("Failed to load shop data:", err);
        setError("Failed to load dashboard data");
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const stats: ShopStats = useMemo(() => shopAPI.calculateStats(orders), [orders]);
  const revenueData = useMemo(() => shopAPI.getRevenueData(orders, 7), [orders]);

  const recentOrders = useMemo(
    () =>
      [...orders]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5),
    [orders]
  );

  const formatCurrency = (n: number) =>
    `₱${n.toLocaleString("en-PH", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

  const formatDate = (d: string) =>
    new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });

  const formatTime = (d: string) =>
    new Date(d).toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });

  if (isLoading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-10 h-10 text-teal-600 mx-auto mb-4 animate-spin" />
          <p className="text-slate-500 font-medium">Loading dashboard…</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 px-4 sm:px-6 lg:px-10 pb-12 pt-20">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* ─── Header ───────────────────────────────────────────── */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row md:items-center justify-between gap-4"
        >
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-teal-500 to-teal-700 flex items-center justify-center shadow-lg shadow-teal-200">
              <Store className="w-7 h-7 text-white" />
            </div>
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-slate-900">
                Shop Dashboard
              </h1>
              <p className="text-sm text-slate-500">
                Welcome back,{" "}
                <span className="font-medium text-slate-700">{user?.firstName}</span>
                . Here's today's overview.
              </p>
            </div>
          </div>
          <div className="flex gap-3">
            <Button
              variant="outline"
              className="border-slate-300 gap-2"
              onClick={() => navigate("/shop/orders")}
            >
              <Package className="w-4 h-4" />
              All Orders
            </Button>
            <Button
              className="bg-teal-600 hover:bg-teal-700 text-white gap-2"
              onClick={() => navigate("/shop/services")}
            >
              <BarChart3 className="w-4 h-4" />
              Manage Pricing
            </Button>
          </div>
        </motion.div>

        {/* ─── Stat Cards ───────────────────────────────────────── */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5"
        >
          {[
            {
              label: "Total Revenue",
              value: formatCurrency(stats.totalRevenue),
              icon: DollarSign,
              color: "from-emerald-500 to-emerald-600",
              shadowColor: "shadow-emerald-200",
              trend: stats.todayRevenue > 0,
              sub: `${formatCurrency(stats.todayRevenue)} today`,
            },
            {
              label: "Active Orders",
              value: stats.activeOrders.toString(),
              icon: Clock,
              color: "from-blue-500 to-blue-600",
              shadowColor: "shadow-blue-200",
              trend: null,
              sub: `${stats.totalOrders} total`,
            },
            {
              label: "Completed",
              value: stats.completedOrders.toString(),
              icon: CheckCircle2,
              color: "from-teal-500 to-teal-600",
              shadowColor: "shadow-teal-200",
              trend: true,
              sub: `${stats.cancelledOrders} cancelled`,
            },
            {
              label: "Customers",
              value: stats.totalCustomers.toString(),
              icon: Users,
              color: "from-purple-500 to-purple-600",
              shadowColor: "shadow-purple-200",
              trend: null,
              sub: `Avg. ${formatCurrency(stats.avgOrderValue)}/order`,
            },
          ].map((stat, i) => {
            const Icon = stat.icon;
            return (
              <motion.div
                key={stat.label}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.15 + i * 0.05 }}
              >
                <Card className="border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
                  <CardContent className="p-5">
                    <div className="flex items-start justify-between mb-3">
                      <div className={`w-11 h-11 rounded-xl bg-gradient-to-br ${stat.color} flex items-center justify-center shadow-md ${stat.shadowColor}`}>
                        <Icon className="w-5 h-5 text-white" />
                      </div>
                      {stat.trend !== null && (
                        <span className={`inline-flex items-center gap-0.5 text-xs font-medium ${stat.trend ? "text-emerald-600" : "text-red-500"}`}>
                          {stat.trend ? <ArrowUpRight className="w-3.5 h-3.5" /> : <ArrowDownRight className="w-3.5 h-3.5" />}
                        </span>
                      )}
                    </div>
                    <p className="text-2xl font-bold text-slate-900">{stat.value}</p>
                    <p className="text-xs text-slate-500 mt-1">{stat.sub}</p>
                    <p className="text-xs text-slate-400 mt-0.5">{stat.label}</p>
                  </CardContent>
                </Card>
              </motion.div>
            );
          })}
        </motion.div>

        {/* ─── Chart + Recent Orders ─────────────────────────── */}
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          {/* Revenue Chart */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.25 }}
            className="lg:col-span-3"
          >
            <Card className="border-slate-200/80 shadow-sm h-full">
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-lg text-slate-800">Revenue Overview</CardTitle>
                    <p className="text-xs text-slate-400 mt-0.5">Last 7 days</p>
                  </div>
                  <div className="flex items-center gap-1.5 text-emerald-600">
                    <TrendingUp className="w-4 h-4" />
                    <span className="text-sm font-semibold">
                      {formatCurrency(revenueData.reduce((s, d) => s + d.revenue, 0))}
                    </span>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="pt-4">
                {revenueData.every((d) => d.revenue === 0) ? (
                  <div className="h-32 flex items-center justify-center text-slate-400 text-sm">
                    No revenue data for this period
                  </div>
                ) : (
                  <MiniBarChart data={revenueData} />
                )}
              </CardContent>
            </Card>
          </motion.div>

          {/* Order Status Summary */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="lg:col-span-2"
          >
            <Card className="border-slate-200/80 shadow-sm h-full">
              <CardHeader className="pb-2">
                <CardTitle className="text-lg text-slate-800">Order Summary</CardTitle>
                <p className="text-xs text-slate-400 mt-0.5">Current status breakdown</p>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {[
                    { label: "Pending", count: orders.filter((o) => o.status?.toUpperCase() === "PENDING").length, color: "bg-amber-500" },
                    { label: "Confirmed", count: orders.filter((o) => o.status?.toUpperCase() === "CONFIRMED").length, color: "bg-blue-500" },
                    { label: "In Progress", count: orders.filter((o) => ["PICKED_UP", "IN_PROGRESS"].includes(o.status?.toUpperCase())).length, color: "bg-purple-500" },
                    { label: "Ready", count: orders.filter((o) => o.status?.toUpperCase() === "READY").length, color: "bg-emerald-500" },
                    { label: "Delivered", count: orders.filter((o) => o.status?.toUpperCase() === "DELIVERED").length, color: "bg-slate-400" },
                    { label: "Cancelled", count: orders.filter((o) => o.status?.toUpperCase() === "CANCELLED").length, color: "bg-red-500" },
                  ]
                    .filter((s) => s.count > 0)
                    .map((s) => {
                      const pct = orders.length > 0 ? (s.count / orders.length) * 100 : 0;
                      return (
                        <div key={s.label} className="flex items-center gap-3">
                          <span className={`w-2.5 h-2.5 rounded-full ${s.color} shrink-0`} />
                          <span className="text-sm text-slate-600 flex-1">{s.label}</span>
                          <span className="text-sm font-semibold text-slate-800">{s.count}</span>
                          <div className="w-16 h-1.5 bg-slate-100 rounded-full overflow-hidden">
                            <div className={`h-full rounded-full ${s.color}`} style={{ width: `${pct}%` }} />
                          </div>
                        </div>
                      );
                    })}
                  {orders.length === 0 && (
                    <p className="text-sm text-slate-400 text-center py-4">No orders yet</p>
                  )}
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </div>

        {/* ─── Recent Orders ─────────────────────────────────── */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35 }}
        >
          <Card className="border-slate-200/80 shadow-sm">
            <CardHeader className="border-b border-slate-100 pb-4">
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle className="text-lg text-slate-800">Recent Orders</CardTitle>
                  <p className="text-xs text-slate-400 mt-0.5">Latest customer orders</p>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-teal-600 hover:text-teal-700 gap-1"
                  onClick={() => navigate("/shop/orders")}
                >
                  View All <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </CardHeader>
            <CardContent className="p-0">
              {error ? (
                <div className="py-12 text-center">
                  <AlertCircle className="w-10 h-10 text-red-400 mx-auto mb-3" />
                  <p className="text-red-600 text-sm">{error}</p>
                </div>
              ) : recentOrders.length === 0 ? (
                <div className="py-12 text-center">
                  <ShoppingBag className="w-10 h-10 text-slate-300 mx-auto mb-3" />
                  <p className="text-slate-500 text-sm">No orders yet. They will appear here once customers start ordering.</p>
                </div>
              ) : (
                <div className="divide-y divide-slate-100">
                  {recentOrders.map((order) => (
                    <div
                      key={order.orderId}
                      className="flex items-center justify-between px-6 py-4 hover:bg-slate-50/80 transition-colors cursor-pointer"
                      onClick={() => navigate(`/shop/orders`)}
                    >
                      <div className="flex items-center gap-4 flex-1 min-w-0">
                        <div className="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center shrink-0">
                          <Package className="w-5 h-5 text-slate-500" />
                        </div>
                        <div className="min-w-0">
                          <p className="text-sm font-semibold text-slate-900 truncate">
                            Order {order.orderNumber}
                          </p>
                          <p className="text-xs text-slate-500">
                            {order.customerName} · {formatDate(order.createdAt)} at {formatTime(order.createdAt)}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-4 shrink-0">
                        <StatusBadge status={order.status} />
                        <p className="text-sm font-bold text-slate-900 w-24 text-right">
                          {formatCurrency(order.totalAmount)}
                        </p>
                        <ChevronRight className="w-4 h-4 text-slate-400" />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}
