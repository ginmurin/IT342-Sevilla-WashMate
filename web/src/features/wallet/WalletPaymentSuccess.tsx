import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams, useLocation } from "react-router";
import { usePayment } from "@/features/shared/contexts/PaymentContext";
import { useWallet } from "@/features/wallet/WalletContext";
import { useNotifications } from "@/features/shared/contexts/NotificationContext";
import { walletAPI } from "./walletAPI";
import { Card, CardContent } from "@/features/shared/components/Card";
import { Button } from "@/features/shared/components/Button";
import {
  CheckCircle2,
  Home,
  ArrowRight,
  Copy,
  Wallet as WalletIcon,
} from "lucide-react";
import { motion } from "motion/react";

export default function WalletPaymentSuccess() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { refetchWalletData } = usePayment();
  const { topUpData } = useWallet();
  const { fetchNotifications } = useNotifications();
  const hasConfirmedPayment = useRef(false);

  // Handle both state (from navigate) and query params (from 3DS redirect)
  const state = (location.state as {
    topUpId?: string;
    amount?: number;
    paymentId?: number;
    paymongoPaymentIntentId?: string;
  }) ?? {};

  const topUpId = searchParams.get("topUpId") || state.topUpId || `TOPUP-${Date.now()}`;
  const paymentId = searchParams.get("paymentId") || state.paymentId?.toString();
  const [amount] = useState(() => {
    const queryAmount = Number(searchParams.get("amount"));
    if (Number.isFinite(queryAmount) && queryAmount > 0) {
      return queryAmount;
    }

    if (typeof state.amount === "number" && state.amount > 0) {
      return state.amount;
    }

    const savedAmount = localStorage.getItem("walletTopUpAmount");
    if (savedAmount) {
      const parsedAmount = Number(savedAmount);
      if (Number.isFinite(parsedAmount) && parsedAmount > 0) {
        return parsedAmount;
      }
    }

    return topUpData.amount > 0 ? topUpData.amount : 0;
  });
  const paymongoPaymentIntentId = searchParams.get("paymongoPaymentIntentId") || state.paymongoPaymentIntentId;

  // Debug logging on mount
  useEffect(() => {
    console.log("📍 WalletPaymentSuccess Mounted:", {
      fromSearchParams: {
        topUpId: searchParams.get("topUpId"),
        paymentId: searchParams.get("paymentId"),
        amount: searchParams.get("amount"),
        paymongoPaymentIntentId: searchParams.get("paymongoPaymentIntentId"),
      },
      fromState: state,
      resolved: {
        topUpId,
        paymentId,
        amount,
        paymongoPaymentIntentId,
      },
    });
  }, []);

  useEffect(() => {
    // Confirm wallet top-up with backend only once
    if (paymentId && !hasConfirmedPayment.current) {
      hasConfirmedPayment.current = true;

      console.log("🔍 Confirming payment:", {
        paymentId,
        amount,
        paymongoPaymentIntentId,
      });

      walletAPI
        .confirmTopup(Number(paymentId), amount, paymongoPaymentIntentId || undefined)
        .then((response) => {
          console.log("✅ Wallet top-up confirmed successfully", response.data);
          // Refetch wallet data to get updated balance
          refetchWalletData();
          // Force fetch notifications to show the new top-up immediately
          fetchNotifications();
          localStorage.removeItem("walletTopUpAmount");
        })
        .catch((error) => {
          console.error("❌ Failed to confirm wallet top-up:", error);
          console.error("Error status:", error.response?.status);
          console.error("Error details:", error.response?.data || error.message);
        });
    }
  }, [paymentId, amount, paymongoPaymentIntentId]);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(topUpId);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-50 pt-20 pb-12 flex items-center">
      <div className="max-w-2xl mx-auto px-4 w-full">
        {/* Success Animation */}
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2, duration: 0.4 }}
          className="text-center mb-8"
        >
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center mx-auto mb-6 shadow-lg">
              <CheckCircle2 className="w-10 h-10 text-white" />
            </div>
          </motion.div>
        </motion.div>

        {/* Success Message */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="text-center mb-8"
        >
          <h1 className="text-4xl font-bold text-slate-900 mb-2">
            Payment Successful!
          </h1>
          <p className="text-lg text-slate-600">
            Your wallet has been credited successfully
          </p>
        </motion.div>

        {/* Details Card */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="mb-8"
        >
          <Card className="border-emerald-200 bg-white shadow-lg">
            <CardContent className="pt-8 pb-8 space-y-6">
              {/* Top-up Details */}
              <div className="space-y-4">
                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Reference Number</span>
                  <div className="flex items-center gap-2">
                    <code className="font-mono font-semibold text-slate-900 text-sm">
                      {topUpId}
                    </code>
                    <button
                      onClick={copyToClipboard}
                      className="p-1 hover:bg-slate-100 rounded transition-colors"
                      title="Copy reference"
                    >
                      <Copy className="w-4 h-4 text-slate-500" />
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Amount Added</span>
                  <span className="text-2xl font-bold text-emerald-600">
                    ₱{amount.toFixed(2)}
                  </span>
                </div>

                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Status</span>
                  <span className="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm font-medium">
                    <CheckCircle2 className="w-4 h-4" />
                    Completed
                  </span>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-slate-600">Date & Time</span>
                  <span className="text-slate-900 font-medium">
                    {new Date().toLocaleString()}
                  </span>
                </div>
              </div>

              {/* Wallet Info Box */}
              <div className="bg-gradient-to-br from-teal-50 to-emerald-50 rounded-lg p-4 flex items-start gap-3 border border-teal-200">
                <WalletIcon className="w-5 h-5 text-teal-600 shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-slate-900">
                    Your wallet balance has been updated
                  </p>
                  <p className="text-xs text-slate-600 mt-1">
                    You can now use this balance to pay for laundry orders or
                    future transactions
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Action Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="grid grid-cols-1 md:grid-cols-2 gap-3"
        >
          <Button
            onClick={() => navigate("/wallet")}
            className="bg-emerald-600 hover:bg-emerald-700 text-white h-12 rounded-lg font-medium flex items-center justify-center gap-2"
          >
            <WalletIcon className="w-4 h-4" />
            View Wallet
          </Button>
          <Button
            onClick={() => navigate("/")}
            variant="outline"
            className="border-slate-300 h-12 rounded-lg font-medium flex items-center justify-center gap-2"
          >
            <Home className="w-4 h-4" />
            Go Home
          </Button>
        </motion.div>

        {/* Order Laundry Button */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
          className="mt-3"
        >
          <Button
            onClick={() => navigate("/order/laundry-details")}
            className="w-full bg-teal-600 hover:bg-teal-700 text-white h-12 rounded-lg font-medium flex items-center justify-center gap-2"
          >
            Order Laundry Now
            <ArrowRight className="w-4 h-4" />
          </Button>
        </motion.div>

        {/* Info Box */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.7 }}
          className="mt-8 text-center"
        >
          <p className="text-sm text-slate-600">
            A receipt has been sent to your registered email address
          </p>
        </motion.div>
      </div>
    </div>
  );
}




