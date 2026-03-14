import { useState } from "react";
import { useNavigate } from "react-router";
import { usePayment } from "../contexts/PaymentContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  ArrowLeft,
  Download,
  Eye,
  Calendar,
  Filter,
  CheckCircle2,
  Clock,
  AlertCircle,
} from "lucide-react";
import { motion } from "motion/react";

export default function PaymentHistory() {
  const navigate = useNavigate();
  const { transactions } = usePayment();
  const [statusFilter, setStatusFilter] = useState<"all" | "completed" | "pending" | "failed">("all");

  const mockTransactions = [
    {
      id: "TXN-1704067200000",
      orderId: "ORD-1704067200000",
      amount: 350,
      status: "completed" as const,
      serviceType: "Wash & Iron",
      date: "2024-01-01T08:00:00Z",
      paymentMethod: "card",
      referenceNumber: "REF-ABC123XYZ",
    },
    {
      id: "TXN-1704153600000",
      orderId: "ORD-1704153600000",
      amount: 200,
      status: "completed" as const,
      serviceType: "Wash & Fold",
      date: "2024-01-02T14:30:00Z",
      paymentMethod: "card",
      referenceNumber: "REF-DEF456UVW",
    },
    {
      id: "TXN-1704240000000",
      orderId: "ORD-1704240000000",
      amount: 560,
      status: "completed" as const,
      serviceType: "Dry Clean",
      date: "2024-01-03T10:15:00Z",
      paymentMethod: "card",
      referenceNumber: "REF-GHI789RST",
    },
    {
      id: "TXN-1704326400000",
      orderId: "ORD-1704326400000",
      amount: 150,
      status: "pending" as const,
      serviceType: "Wash & Fold",
      date: "2024-01-04T16:45:00Z",
      paymentMethod: "card",
      referenceNumber: "REF-JKL012MNO",
    },
    {
      id: "TXN-1704412800000",
      orderId: "ORD-1704412800000",
      amount: 300,
      status: "failed" as const,
      serviceType: "Wash & Iron",
      date: "2024-01-05T09:20:00Z",
      paymentMethod: "card",
      referenceNumber: "REF-PQR345LMK",
    },
  ];

  const allTransactions = [...transactions, ...mockTransactions];

  const filteredTransactions =
    statusFilter === "all"
      ? allTransactions
      : allTransactions.filter((t) => t.status === statusFilter);

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed":
        return { bg: "bg-emerald-50", border: "border-emerald-200", text: "text-emerald-700", icon: "text-emerald-600" };
      case "pending":
        return { bg: "bg-amber-50", border: "border-amber-200", text: "text-amber-700", icon: "text-amber-600" };
      case "failed":
        return { bg: "bg-red-50", border: "border-red-200", text: "text-red-700", icon: "text-red-600" };
      default:
        return { bg: "bg-slate-50", border: "border-slate-200", text: "text-slate-700", icon: "text-slate-600" };
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed":
        return <CheckCircle2 className="w-5 h-5" />;
      case "pending":
        return <Clock className="w-5 h-5" />;
      case "failed":
        return <AlertCircle className="w-5 h-5" />;
      default:
        return null;
    }
  };

  return (
    <div className="flex-1 min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-20 pb-12">
      <div className="max-w-5xl mx-auto px-4 space-y-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex items-center gap-4"
        >
          <button
            onClick={() => navigate(-1)}
            className="p-2 hover:bg-white rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5 text-slate-600" />
          </button>
          <div>
            <h1 className="text-3xl font-bold text-slate-900">Payment History</h1>
            <p className="text-slate-500 mt-1">View all your completed transactions</p>
          </div>
        </motion.div>

        {/* Filters */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="flex gap-2 flex-wrap"
        >
          {["all", "completed", "pending", "failed"].map((status) => (
            <Button
              key={status}
              onClick={() => setStatusFilter(status as any)}
              variant={statusFilter === status ? "default" : "outline"}
              className={`flex items-center gap-2 ${
                statusFilter === status
                  ? "bg-teal-600 hover:bg-teal-700 text-white"
                  : "border-slate-300 text-slate-700"
              }`}
            >
              <Filter className="w-4 h-4" />
              {status.charAt(0).toUpperCase() + status.slice(1)}
            </Button>
          ))}
        </motion.div>

        {/* Summary Stats */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-4"
        >
          <Card className="border-slate-200 shadow-sm">
            <CardContent className="pt-6">
              <div className="text-center">
                <p className="text-sm text-slate-600 mb-2">Total Transactions</p>
                <p className="text-3xl font-bold text-slate-900">{allTransactions.length}</p>
              </div>
            </CardContent>
          </Card>

          <Card className="border-emerald-200 shadow-sm">
            <CardContent className="pt-6">
              <div className="text-center">
                <p className="text-sm text-emerald-600 mb-2">Completed Orders</p>
                <p className="text-3xl font-bold text-emerald-700">
                  ₱{allTransactions
                    .filter((t) => t.status === "completed")
                    .reduce((sum, t) => sum + t.amount, 0)
                    .toFixed(2)}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card className="border-slate-200 shadow-sm">
            <CardContent className="pt-6">
              <div className="text-center">
                <p className="text-sm text-slate-600 mb-2">Pending/Failed</p>
                <p className="text-3xl font-bold text-slate-900">
                  {allTransactions.filter((t) => t.status !== "completed").length}
                </p>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Transactions Table */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <Card className="border-slate-200 shadow-sm overflow-hidden">
            <CardHeader className="bg-gradient-to-r from-slate-50 to-slate-100 pb-4 mb-0 border-b border-slate-200 rounded-t-xl">
              <CardTitle>Recent Transactions</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              {filteredTransactions.length === 0 ? (
                <div className="p-12 text-center">
                  <AlertCircle className="w-12 h-12 text-slate-300 mx-auto mb-3" />
                  <p className="text-slate-500">No transactions found</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b border-slate-200 bg-slate-50">
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Order ID</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Service</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Amount</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Date</th>
                        <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Status</th>
                        <th className="px-6 py-3 text-right text-sm font-semibold text-slate-700">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredTransactions.map((transaction, idx) => {
                        const colors = getStatusColor(transaction.status);
                        const date = new Date(transaction.date);
                        return (
                          <motion.tr
                            key={transaction.id}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: idx * 0.05 }}
                            className="border-b border-slate-100 hover:bg-slate-50 transition-colors"
                          >
                            <td className="px-6 py-4">
                              <code className="text-sm font-mono text-teal-600">
                                {transaction.orderId || transaction.id}
                              </code>
                            </td>
                            <td className="px-6 py-4">
                              <span className="text-sm font-medium text-slate-900">
                                {transaction.serviceType}
                              </span>
                            </td>
                            <td className="px-6 py-4">
                              <span className="text-sm font-semibold text-slate-900">
                                ₱{transaction.amount.toFixed(2)}
                              </span>
                            </td>
                            <td className="px-6 py-4">
                              <span className="text-sm text-slate-600">
                                {date.toLocaleDateString()} · {date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                              </span>
                            </td>
                            <td className="px-6 py-4">
                              <span
                                className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-medium ${colors.bg} ${colors.text} border ${colors.border}`}
                              >
                                <span className={colors.icon}>
                                  {getStatusIcon(transaction.status)}
                                </span>
                                {transaction.status.charAt(0).toUpperCase() +
                                  transaction.status.slice(1)}
                              </span>
                            </td>
                            <td className="px-6 py-4 text-right">
                              <button className="inline-flex items-center gap-2 text-teal-600 hover:text-teal-700 font-medium text-sm">
                                <Download className="w-4 h-4" />
                                Invoice
                              </button>
                            </td>
                          </motion.tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>
        </motion.div>

        {/* Back Button */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.25 }}
          className="flex justify-center"
        >
          <Button
            onClick={() => navigate("/customer")}
            variant="outline"
            className="px-8"
          >
            Back to Dashboard
          </Button>
        </motion.div>
      </div>
    </div>
  );
}
