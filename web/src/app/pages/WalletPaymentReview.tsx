import { useState } from "react";
import { useNavigate } from "react-router";
import { useWallet } from "../contexts/WalletContext";
import { usePayment } from "../contexts/PaymentContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  CreditCard,
  Smartphone,
  CheckCircle2,
  AlertCircle,
  Loader2,
  ArrowLeft,
} from "lucide-react";
import { motion } from "motion/react";
import { createSource, type SourceType } from "../services/paymongo";

export default function WalletPaymentReview() {
  const navigate = useNavigate();
  const { topUpData, setTopUpData, submitTopUp } = useWallet();
  const { addToWallet } = usePayment();
  const [isProcessing, setIsProcessing] = useState(false);
  const [agreeToTerms, setAgreeToTerms] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  const sourceTypeMap: Record<string, SourceType> = {
    gcash: "gcash",
    maya: "paymaya",
    grab_pay: "grab_pay",
  };

  const handleSubmit = async () => {
    if (!agreeToTerms) {
      setPaymentError("Please agree to the terms and conditions.");
      return;
    }
    setPaymentError(null);
    setIsProcessing(true);

    const amount = topUpData.amount;
    const topUpId = `TOPUP-${Date.now()}`;

    try {
      // Step 1: Initiate wallet top-up (creates Payment record in backend)
      const paymentData = await submitTopUp();
      const paymentId = paymentData?.paymentId;

      if (!paymentId) {
        setPaymentError("Failed to initiate payment. Please try again.");
        setIsProcessing(false);
        return;
      }

      if (topUpData.paymentMethod === "card") {
        navigate("/wallet/payment-checkout", {
          state: { topUpId, amount, paymentId, paymentMethod: "card" },
        });
        return;
      }

      const sourceType = sourceTypeMap[topUpData.paymentMethod!];
      if (!sourceType) {
        setPaymentError("Please select a payment method.");
        setIsProcessing(false);
        return;
      }

      const successUrl = `${window.location.origin}/wallet/payment-success?topUpId=${topUpId}&amount=${amount}&paymentId=${paymentId}`;
      const failedUrl = `${window.location.origin}/wallet/payment-error`;

      const checkoutUrl = await createSource(
        sourceType,
        amount,
        successUrl,
        failedUrl
      );
      window.location.href = checkoutUrl;
    } catch (err) {
      setPaymentError(
        err instanceof Error ? err.message : "Payment failed. Please try again."
      );
      setIsProcessing(false);
    }
  };

  const paymentMethods = [
    {
      id: "gcash",
      name: "GCash",
      icon: Smartphone,
      description: "Pay using GCash mobile wallet",
      color: "from-blue-500 to-cyan-500",
    },
    {
      id: "maya",
      name: "Maya",
      icon: Smartphone,
      description: "Pay using Maya mobile wallet",
      color: "from-purple-500 to-pink-500",
    },
    {
      id: "card",
      name: "Credit/Debit Card",
      icon: CreditCard,
      description: "Visa, Mastercard, or other cards",
      color: "from-orange-500 to-red-500",
    },
    {
      id: "grab_pay",
      name: "GrabPay",
      icon: Smartphone,
      description: "Pay through GrabPay",
      color: "from-green-500 to-emerald-500",
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12">
      <div className="max-w-4xl mx-auto px-4 space-y-8">
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
            <h1 className="text-3xl font-bold text-slate-900">
              Add Money to Wallet
            </h1>
            <p className="text-slate-600 mt-1">Choose payment method</p>
          </div>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Amount Summary */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Top-up Amount</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="bg-slate-50 rounded-lg p-6 flex items-center justify-between">
                    <span className="text-slate-600 font-medium">
                      Amount to Add
                    </span>
                    <span className="text-4xl font-bold text-teal-600">
                      ₱{topUpData.amount.toFixed(2)}
                    </span>
                  </div>
                  <Button
                    onClick={() => navigate("/wallet")}
                    variant="outline"
                    className="w-full border-slate-300"
                  >
                    Edit Amount
                  </Button>
                </CardContent>
              </Card>
            </motion.div>

            {/* Payment Method Selection */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.15 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Select Payment Method</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-3">
                    {paymentMethods.map((method) => {
                      const Icon = method.icon;
                      return (
                        <motion.button
                          key={method.id}
                          onClick={() =>
                            setTopUpData({
                              paymentMethod: method.id as any,
                            })
                          }
                          whileHover={{ scale: 1.02 }}
                          whileTap={{ scale: 0.98 }}
                          className={`p-4 rounded-lg border-2 transition-all text-left ${
                            topUpData.paymentMethod === method.id
                              ? "border-teal-500 bg-teal-50"
                              : "border-slate-200 hover:border-slate-300 bg-white"
                          }`}
                        >
                          <div
                            className={`w-10 h-10 rounded-lg flex items-center justify-center mb-2 bg-gradient-to-br ${method.color} text-white`}
                          >
                            <Icon className="w-5 h-5" />
                          </div>
                          <p className="font-semibold text-slate-900 text-sm">
                            {method.name}
                          </p>
                          <p className="text-xs text-slate-600 mt-1">
                            {method.description}
                          </p>
                        </motion.button>
                      );
                    })}
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Terms & Conditions */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardContent className="pt-6">
                  <label className="flex items-start gap-3 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={agreeToTerms}
                      onChange={(e) => setAgreeToTerms(e.target.checked)}
                      className="w-5 h-5 mt-0.5 rounded border-slate-300 text-teal-600 focus:ring-teal-500"
                    />
                    <div>
                      <p className="text-sm text-slate-700">
                        I agree to the{" "}
                        <a
                          href="#"
                          className="text-teal-600 hover:underline font-medium"
                        >
                          Terms of Service
                        </a>{" "}
                        and{" "}
                        <a
                          href="#"
                          className="text-teal-600 hover:underline font-medium"
                        >
                          Privacy Policy
                        </a>
                      </p>
                      <p className="text-xs text-slate-500 mt-1">
                        By adding money to your wallet, you acknowledge and
                        agree to our policies
                      </p>
                    </div>
                  </label>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* Amount Summary Sidebar */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.25 }}
            className="lg:col-span-1"
          >
            <div className="sticky top-24 space-y-4">
              <Card className="bg-gradient-to-br from-teal-500 to-teal-700 text-white border-none shadow-lg">
                <CardContent className="pt-6 space-y-4">
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-white/80">Top-up Amount</span>
                      <span className="font-semibold">
                        ₱{topUpData.amount.toFixed(2)}
                      </span>
                    </div>
                  </div>

                  <div className="border-t border-teal-400 pt-4 flex justify-between items-center">
                    <span className="font-semibold">Total</span>
                    <span className="text-3xl font-bold">
                      ₱{topUpData.amount.toFixed(2)}
                    </span>
                  </div>

                  <div className="bg-white/10 rounded-lg p-3 space-y-2 text-xs text-white/80">
                    <div className="flex items-start gap-2">
                      <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                      <span>100% secure payment</span>
                    </div>
                    <div className="flex items-start gap-2">
                      <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                      <span>Money-back guarantee</span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {paymentError && (
                <div className="flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-lg">
                  <AlertCircle className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
                  <p className="text-sm text-red-700">{paymentError}</p>
                </div>
              )}

              <Button
                onClick={handleSubmit}
                disabled={
                  isProcessing ||
                  !agreeToTerms ||
                  !topUpData.paymentMethod ||
                  topUpData.amount <= 0
                }
                className="w-full bg-emerald-600 hover:bg-emerald-700 text-white h-11 rounded-lg font-medium disabled:opacity-50 flex items-center justify-center gap-2"
              >
                {isProcessing ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Processing…
                  </>
                ) : (
                  "Confirm & Pay"
                )}
              </Button>

              <Button
                onClick={() => navigate("/wallet")}
                variant="outline"
                className="w-full border-slate-300"
              >
                Cancel
              </Button>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 flex items-start gap-2">
                <AlertCircle className="w-4 h-4 text-blue-600 mt-0.5 shrink-0" />
                <p className="text-xs text-blue-800">
                  You will be redirected to the payment gateway after confirming
                </p>
              </div>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
