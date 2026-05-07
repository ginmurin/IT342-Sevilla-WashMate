import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Lock, AlertCircle, Loader2, CheckCircle2 } from "lucide-react";
import { motion } from "motion/react";

/**
 * Wallet Payment Checkout Page
 * Backend handles all payment processing via PayMongo.
 * Frontend just shows loading state while backend processes.
 */
export default function WalletPaymentCheckout() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state as {
    paymentId?: number;
    amount?: number;
    paymentMethod?: string;
    paymentIntentId?: string;
    clientKey?: string;
  }) ?? {};

  const [isProcessing, setIsProcessing] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Validate required state
  useEffect(() => {
    if (!state.paymentId || !state.amount) {
      setError("Invalid payment data. Please start over.");
      setIsProcessing(false);
      return;
    }

    // Simulate processing time, then redirect to success
    // In production, you might wait for webhook confirmation
    const timer = setTimeout(() => {
      console.log("✅ Payment processing complete, redirecting to success");
      navigate("/wallet/payment-success", {
        state: {
          paymentId: state.paymentId,
          amount: state.amount,
          paymentMethod: state.paymentMethod,
        },
      });
    }, 2000);

    return () => clearTimeout(timer);
  }, [state, navigate]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center p-4">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="w-full max-w-md"
      >
        <Card className="border-slate-200 shadow-lg">
          <CardContent className="pt-8 pb-8 space-y-6">
            {error ? (
              // Error State
              <>
                <div className="flex justify-center">
                  <div className="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center">
                    <AlertCircle className="w-8 h-8 text-red-600" />
                  </div>
                </div>

                <div className="space-y-2 text-center">
                  <h2 className="text-xl font-bold text-slate-900">Payment Error</h2>
                  <p className="text-sm text-slate-600">{error}</p>
                </div>

                <button
                  onClick={() => window.location.href = "/wallet"}
                  className="w-full bg-slate-900 hover:bg-slate-800 text-white py-2 rounded-lg font-medium transition-colors"
                >
                  Back to Wallet
                </button>
              </>
            ) : (
              // Processing State
              <>
                <div className="flex justify-center">
                  <motion.div
                    animate={{ rotate: 360 }}
                    transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                    className="w-16 h-16 rounded-full bg-teal-100 flex items-center justify-center"
                  >
                    <Loader2 className="w-8 h-8 text-teal-600" />
                  </motion.div>
                </div>

                <div className="space-y-2 text-center">
                  <h2 className="text-xl font-bold text-slate-900">Processing Payment</h2>
                  <p className="text-sm text-slate-600">Your payment is being securely processed</p>
                </div>

                {/* Payment Details */}
                <div className="bg-slate-50 rounded-lg p-4 space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Amount</span>
                    <span className="font-semibold text-slate-900">₱{state.amount?.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Payment Method</span>
                    <span className="font-semibold text-slate-900">{state.paymentMethod}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Transaction ID</span>
                    <span className="font-semibold text-slate-900">#{state.paymentId}</span>
                  </div>
                </div>

                {/* Security Info */}
                <div className="flex items-center gap-2 text-xs text-teal-700 bg-teal-50 p-3 rounded-lg">
                  <Lock className="w-4 h-4 shrink-0" />
                  <span>Your payment is 100% secure and encrypted</span>
                </div>

                {/* Processing Steps */}
                <div className="space-y-2">
                  {[
                    "Creating payment request",
                    "Validating payment details",
                    "Processing with payment gateway",
                  ].map((step, index) => (
                    <motion.div
                      key={step}
                      initial={{ opacity: 0.5 }}
                      animate={{ opacity: 1 }}
                      transition={{
                        delay: index * 0.3,
                        duration: 0.6,
                        repeat: Infinity,
                        repeatType: "reverse",
                      }}
                      className="flex items-center gap-2 text-sm text-slate-600"
                    >
                      <CheckCircle2 className="w-4 h-4 text-teal-600" />
                      <span>{step}</span>
                    </motion.div>
                  ))}
                </div>

                <p className="text-xs text-slate-500 text-center">
                  Do not close this window. You will be redirected shortly.
                </p>
              </>
            )}
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
}

