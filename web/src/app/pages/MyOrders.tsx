import { useNavigate } from "react-router";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  ShoppingBag,
  CheckCircle2,
  Clock,
  ChevronRight,
  Download,
} from "lucide-react";
import { motion } from "motion/react";

export default function MyOrders() {
  const navigate = useNavigate();

  const orders: {
    id: string;
    date: string;
    service: string;
    weight: number;
    status: string;
    amount: number;
    statusColor: string;
  }[] = [];

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "Ready for Pickup":
        return <CheckCircle2 className="w-5 h-5 text-emerald-600" />;
      case "In Transit":
        return <Clock className="w-5 h-5 text-blue-600" />;
      case "Delivered":
        return <CheckCircle2 className="w-5 h-5 text-slate-600" />;
      default:
        return null;
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8">
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
          {orders.length === 0 ? (
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
                key={order.id}
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
                              {order.service}
                            </p>
                            <p className="text-sm text-slate-500">
                              Order {order.id} • {order.date}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="text-2xl font-bold text-teal-600">
                              ₱{order.amount}
                            </p>
                          </div>
                        </div>

                        {/* Details Grid */}
                        <div className="grid grid-cols-3 gap-4 py-3 border-y border-slate-200">
                          <div>
                            <p className="text-xs text-slate-600 mb-1">Weight</p>
                            <p className="font-semibold text-slate-900">
                              {order.weight} kg
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-slate-600 mb-1">Service</p>
                            <p className="font-semibold text-slate-900">
                              {order.service}
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-slate-600 mb-1">Status</p>
                            <div className="flex items-center gap-1">
                              {getStatusIcon(order.status)}
                              <span className="text-sm font-medium text-slate-700">
                                {order.status}
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
                          >
                            Track
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
