import { useState } from "react";
import { useNavigate } from "react-router";
import { usePayment } from "../contexts/PaymentContext";
import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  ArrowLeft,
  Plus,
  Wallet as WalletIcon,
  History,
  CreditCard,
  TrendingUp,
  Zap,
} from "lucide-react";
import { motion } from "motion/react";

export default function Wallet() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { walletBalance, addToWallet, transactions } = usePayment();
  const [selectedAmount, setSelectedAmount] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const presetAmounts = [500, 1000, 2000, 5000];

  const mockTransactions = [
    { id: "WAL-001", type: "load", amount: 1000, description: "Wallet load", date: "2024-01-05" },
    { id: "WAL-002", type: "deduction", amount: 250, description: "Laundry service payment", date: "2024-01-04" },
    { id: "WAL-003", type: "load", amount: 2000, description: "Wallet load", date: "2024-01-03" },
    { id: "WAL-004", type: "refund", amount: 150, description: "Order refund", date: "2024-01-02" },
  ];

  const handleLoadMoney = async (amount: number) => {
    setSelectedAmount(amount);
    setIsLoading(true);

    // Simulate adding to wallet after 1 second (would actually do payment processing)
    setTimeout(() => {
      addToWallet(amount);
      setIsLoading(false);
      setSelectedAmount(null);
    }, 1000);
  };

  const handleCustomAmount = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const amount = Number(formData.get("customAmount"));
    if (amount > 0) {
      handleLoadMoney(amount);
    }
  };

  const getTransactionIcon = (type: string) => {
    switch (type) {
      case "load":
        return <Plus className="w-5 h-5 text-emerald-600" />;
      case "deduction":
        return <CreditCard className="w-5 h-5 text-red-600" />;
      case "refund":
        return <TrendingUp className="w-5 h-5 text-blue-600" />;
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
            <h1 className="text-3xl font-bold text-slate-900">Wallet</h1>
            <p className="text-slate-500 mt-1">Manage your account balance</p>
          </div>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Wallet Card */}
          <div className="lg:col-span-2 space-y-8">
            {/* Balance Card */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card className="bg-gradient-to-br from-teal-500 to-teal-700 text-white border-none shadow-lg">
                <CardContent className="pt-8 pb-10">
                  <div className="flex items-start justify-between mb-12">
                    <div>
                      <p className="text-teal-100 text-sm mb-2">Current Balance</p>
                      <p className="text-5xl font-bold">
                        ₱{(walletBalance || 0).toFixed(2)}
                      </p>
                    </div>
                    <WalletIcon className="w-12 h-12 text-teal-100 opacity-50" />
                  </div>

                  <div className="flex items-center justify-between pt-6 border-t border-teal-400">
                    <div>
                      <p className="text-teal-100 text-sm">Account Holder</p>
                      <p className="font-semibold text-lg">
                        {user?.firstName} {user?.lastName}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-teal-100 text-sm">Status</p>
                      <p className="font-semibold text-lg text-emerald-300">Active</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Quick Load Actions */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.15 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Zap className="w-5 h-5 text-teal-600" />
                    Quick Load
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    {presetAmounts.map((amount) => (
                      <Button
                        key={amount}
                        onClick={() => handleLoadMoney(amount)}
                        disabled={isLoading}
                        variant={selectedAmount === amount && isLoading ? "default" : "outline"}
                        className={`h-16 flex flex-col items-center justify-center ${
                          selectedAmount === amount && isLoading
                            ? "bg-teal-600 text-white border-teal-600"
                            : "border-slate-300 hover:border-teal-400"
                        }`}
                      >
                        {isLoading && selectedAmount === amount ? (
                          <motion.div
                            animate={{ rotate: 360 }}
                            transition={{
                              repeat: Infinity,
                              duration: 1,
                              ease: "linear",
                            }}
                            className="w-4 h-4 border-2 border-teal-200 border-t-white rounded-full"
                          />
                        ) : (
                          <>
                            <Plus className="w-4 h-4 mb-1" />
                            <span className="font-semibold">₱{amount}</span>
                          </>
                        )}
                      </Button>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Custom Amount */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle>Custom Amount</CardTitle>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleCustomAmount} className="flex gap-3">
                    <div className="flex-1">
                      <div className="relative">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 font-medium">
                          ₱
                        </span>
                        <input
                          type="number"
                          name="customAmount"
                          placeholder="Enter amount"
                          min="100"
                          max="50000"
                          className="w-full pl-8 pr-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
                        />
                      </div>
                    </div>
                    <Button
                      type="submit"
                      disabled={isLoading}
                      className="bg-teal-600 hover:bg-teal-700 text-white px-6"
                    >
                      Load Money
                    </Button>
                  </form>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* Sidebar: Payment Methods & History */}
          <div className="lg:col-span-1 space-y-8">
            {/* Payment Methods */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.25 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <CreditCard className="w-5 h-5 text-teal-600" />
                    Payment Methods
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="p-3 border border-slate-300 rounded-lg hover:bg-slate-50 cursor-pointer transition-colors">
                    <p className="text-sm font-medium text-slate-900">Visa •••• 4242</p>
                    <p className="text-xs text-slate-500 mt-1">Expires 12/25</p>
                  </div>
                  <Button
                    variant="outline"
                    className="w-full flex items-center justify-center gap-2"
                  >
                    <Plus className="w-4 h-4" />
                    Add Payment Method
                  </Button>
                </CardContent>
              </Card>
            </motion.div>

            {/* Recent Transactions */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <History className="w-5 h-5 text-teal-600" />
                    Recent Activity
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {mockTransactions.map((txn, idx) => (
                      <motion.div
                        key={txn.id}
                        initial={{ opacity: 0, x: -16 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: 0.3 + idx * 0.05 }}
                        className="flex items-center justify-between pb-3 border-b border-slate-100 last:border-0"
                      >
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center">
                            {getTransactionIcon(txn.type)}
                          </div>
                          <div>
                            <p className="text-sm font-medium text-slate-900">
                              {txn.description}
                            </p>
                            <p className="text-xs text-slate-500">{txn.date}</p>
                          </div>
                        </div>
                        <span
                          className={`font-semibold text-sm ${
                            txn.type === "deduction"
                              ? "text-red-600"
                              : "text-emerald-600"
                          }`}
                        >
                          {txn.type === "deduction" ? "-" : "+"}₱{txn.amount}
                        </span>
                      </motion.div>
                    ))}
                  </div>
                  <Button
                    onClick={() => navigate("/payment/history")}
                    variant="ghost"
                    className="w-full mt-4 text-teal-600 hover:text-teal-700 hover:bg-teal-50"
                  >
                    View All Transactions
                  </Button>
                </CardContent>
              </Card>
            </motion.div>
          </div>
        </div>
      </div>
    </div>
  );
}
