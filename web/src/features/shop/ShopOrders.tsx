import { useEffect, useState, useMemo } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/features/shared/components/Card";
import { Button } from "@/features/shared/components/Button";
import {
  Package,
  Search,
  ChevronDown,
  CheckCircle2,
  Clock,
  Truck,
  XCircle,
  Loader2,
  AlertCircle,
  Filter,
  ArrowUpDown,
  Eye,
  MoreHorizontal,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { shopAPI } from "@/features/shared/services/shopAPI";
import type { OrderResponse } from "@/features/shared/services/order";

const STATUS_OPTIONS = [
  "ALL",
  "PENDING",
  "CONFIRMED",
  "PICKED_UP",
  "IN_PROGRESS",
  "READY",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "CANCELLED",
] as const;

const STATUS_FLOW = [
  "PENDING",
  "CONFIRMED",
  "PICKED_UP",
  "IN_PROGRESS",
  "READY",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
] as const;

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

export default function ShopOrders() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [sortBy, setSortBy] = useState<"newest" | "oldest" | "amount">("newest");
  const [expandedOrder, setExpandedOrder] = useState<number | null>(null);
  const [updatingStatus, setUpdatingStatus] = useState<number | null>(null);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await shopAPI.getAllOrders();
      setOrders(data);
    } catch (err) {
      console.error("Failed to load orders:", err);
      setError("Failed to load orders");
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusUpdate = async (orderId: number, newStatus: string) => {
    try {
      setUpdatingStatus(orderId);
      await shopAPI.updateOrderStatus(orderId, newStatus);
      setOrders((prev) =>
        prev.map((o) => (o.orderId === orderId ? { ...o, status: newStatus } : o))
      );
    } catch (err) {
      console.error("Failed to update status:", err);
      alert("Failed to update order status. Please try again.");
    } finally {
      setUpdatingStatus(null);
    }
  };

  const getNextStatus = (current: string): string | null => {
    const idx = STATUS_FLOW.indexOf(current as any);
    if (idx === -1 || idx === STATUS_FLOW.length - 1) return null;
    return STATUS_FLOW[idx + 1];
  };

  const filteredOrders = useMemo(() => {
    let result = [...orders];

    // Filter by status
    if (statusFilter !== "ALL") {
      result = result.filter((o) => o.status?.toUpperCase() === statusFilter);
    }

    // Filter by search
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (o) =>
          o.orderNumber?.toLowerCase().includes(q) ||
          o.customerName?.toLowerCase().includes(q)
      );
    }

    // Sort
    switch (sortBy) {
      case "newest":
        result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        break;
      case "oldest":
        result.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
        break;
      case "amount":
        result.sort((a, b) => (b.totalAmount || 0) - (a.totalAmount || 0));
        break;
    }

    return result;
  }, [orders, statusFilter, searchQuery, sortBy]);

  const formatDate = (d: string) =>
    new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });

  const formatTime = (d: string) =>
    new Date(d).toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });

  const activeCount = orders.filter((o) => !["DELIVERED", "CANCELLED"].includes(o.status?.toUpperCase())).length;

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 px-4 sm:px-6 lg:px-10 pb-12 pt-20">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row md:items-center justify-between gap-4"
        >
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-slate-900">Order Management</h1>
            <p className="text-sm text-slate-500 mt-1">
              {orders.length} total orders · {activeCount} active
            </p>
          </div>
        </motion.div>

        {/* Filters */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
        >
          <Card className="border-slate-200/80 shadow-sm">
            <CardContent className="p-4">
              <div className="flex flex-col sm:flex-row gap-3">
                {/* Search */}
                <div className="relative flex-1">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    type="text"
                    placeholder="Search by order number or customer…"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500/30 focus:border-teal-400 transition-all"
                  />
                </div>

                {/* Status Filter */}
                <div className="relative">
                  <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="appearance-none pl-10 pr-10 py-2.5 rounded-lg border border-slate-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-teal-500/30 focus:border-teal-400 transition-all cursor-pointer"
                  >
                    {STATUS_OPTIONS.map((s) => (
                      <option key={s} value={s}>
                        {s === "ALL" ? "All Statuses" : s.replace(/_/g, " ")}
                      </option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                </div>

                {/* Sort */}
                <div className="relative">
                  <ArrowUpDown className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as any)}
                    className="appearance-none pl-10 pr-10 py-2.5 rounded-lg border border-slate-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-teal-500/30 focus:border-teal-400 transition-all cursor-pointer"
                  >
                    <option value="newest">Newest First</option>
                    <option value="oldest">Oldest First</option>
                    <option value="amount">Highest Amount</option>
                  </select>
                  <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Orders List */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
          className="space-y-3"
        >
          {isLoading ? (
            <Card className="border-slate-200 shadow-sm">
              <CardContent className="py-16 text-center">
                <Loader2 className="w-8 h-8 text-teal-600 mx-auto mb-3 animate-spin" />
                <p className="text-slate-500">Loading orders…</p>
              </CardContent>
            </Card>
          ) : error ? (
            <Card className="border-red-200 shadow-sm">
              <CardContent className="py-16 text-center">
                <AlertCircle className="w-10 h-10 text-red-400 mx-auto mb-3" />
                <p className="text-red-600 mb-4">{error}</p>
                <Button onClick={fetchOrders} variant="outline" className="border-red-300 text-red-600">
                  Try Again
                </Button>
              </CardContent>
            </Card>
          ) : filteredOrders.length === 0 ? (
            <Card className="border-slate-200 shadow-sm">
              <CardContent className="py-16 text-center">
                <Package className="w-10 h-10 text-slate-300 mx-auto mb-3" />
                <p className="text-slate-500">
                  {searchQuery || statusFilter !== "ALL"
                    ? "No orders match your filters."
                    : "No orders yet. They will appear here once customers start ordering."}
                </p>
              </CardContent>
            </Card>
          ) : (
            filteredOrders.map((order, i) => {
              const isExpanded = expandedOrder === order.orderId;
              const nextStatus = getNextStatus(order.status);
              const isUpdating = updatingStatus === order.orderId;

              return (
                <motion.div
                  key={order.orderId}
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.05 * Math.min(i, 10) }}
                >
                  <Card className="border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
                    {/* Order Row */}
                    <div
                      className="flex items-center justify-between px-5 py-4 cursor-pointer"
                      onClick={() => setExpandedOrder(isExpanded ? null : order.orderId)}
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
                            {order.customerName} · {formatDate(order.createdAt)}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center gap-4 shrink-0">
                        <StatusBadge status={order.status} />
                        <p className="text-sm font-bold text-slate-900 w-24 text-right hidden sm:block">
                          ₱{order.totalAmount?.toLocaleString() || "0"}
                        </p>
                        <ChevronDown
                          className={`w-4 h-4 text-slate-400 transition-transform ${
                            isExpanded ? "rotate-180" : ""
                          }`}
                        />
                      </div>
                    </div>

                    {/* Expanded Detail */}
                    <AnimatePresence>
                      {isExpanded && (
                        <motion.div
                          initial={{ height: 0, opacity: 0 }}
                          animate={{ height: "auto", opacity: 1 }}
                          exit={{ height: 0, opacity: 0 }}
                          className="overflow-hidden"
                        >
                          <div className="px-5 pb-5 border-t border-slate-100 pt-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                              {/* Services */}
                              <div>
                                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
                                  Services
                                </h4>
                                {order.services && order.services.length > 0 ? (
                                  <div className="space-y-2">
                                    {order.services.map((s) => (
                                      <div
                                        key={s.orderServiceId}
                                        className="flex justify-between items-center bg-slate-50 rounded-lg px-3 py-2"
                                      >
                                        <span className="text-sm text-slate-700">
                                          {s.serviceName}{" "}
                                          <span className="text-slate-400">×{s.quantity}</span>
                                        </span>
                                        <span className="text-sm font-medium text-slate-900">
                                          ₱{s.subtotal}
                                        </span>
                                      </div>
                                    ))}
                                  </div>
                                ) : (
                                  <p className="text-sm text-slate-400">No service details</p>
                                )}
                              </div>

                              {/* Order Details */}
                              <div className="space-y-3">
                                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
                                  Details
                                </h4>
                                <div className="grid grid-cols-2 gap-3">
                                  <div>
                                    <p className="text-xs text-slate-400">Weight</p>
                                    <p className="text-sm font-medium text-slate-900">
                                      {order.totalWeight || "N/A"} kg
                                    </p>
                                  </div>
                                  <div>
                                    <p className="text-xs text-slate-400">Total</p>
                                    <p className="text-sm font-bold text-teal-600">
                                      ₱{order.totalAmount?.toLocaleString()}
                                    </p>
                                  </div>
                                  <div>
                                    <p className="text-xs text-slate-400">Pickup</p>
                                    <p className="text-sm font-medium text-slate-900">
                                      {order.pickupSchedule
                                        ? formatDate(order.pickupSchedule)
                                        : "Not set"}
                                    </p>
                                  </div>
                                  <div>
                                    <p className="text-xs text-slate-400">Delivery</p>
                                    <p className="text-sm font-medium text-slate-900">
                                      {order.deliverySchedule
                                        ? formatDate(order.deliverySchedule)
                                        : "Not set"}
                                    </p>
                                  </div>
                                </div>

                                {/* Status Update */}
                                {nextStatus && order.status?.toUpperCase() !== "CANCELLED" && (
                                  <div className="pt-3 border-t border-slate-100">
                                    <Button
                                      size="sm"
                                      className="bg-teal-600 hover:bg-teal-700 text-white w-full gap-2"
                                      disabled={isUpdating}
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        handleStatusUpdate(order.orderId, nextStatus);
                                      }}
                                    >
                                      {isUpdating ? (
                                        <Loader2 className="w-4 h-4 animate-spin" />
                                      ) : (
                                        <CheckCircle2 className="w-4 h-4" />
                                      )}
                                      Mark as {nextStatus.replace(/_/g, " ")}
                                    </Button>
                                  </div>
                                )}
                              </div>
                            </div>
                          </div>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </Card>
                </motion.div>
              );
            })
          )}
        </motion.div>
      </div>
    </div>
  );
}



