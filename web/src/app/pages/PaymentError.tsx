import { useLocation, useNavigate } from "react-router";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { AlertCircle, Home, RotateCcw, Mail } from "lucide-react";
import { motion } from "motion/react";

export default function PaymentError() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as any;

  const { errorMessage, errorCode } = state || {
    errorMessage: "Payment processing failed. Please try again.",
    errorCode: "ERR_UNKNOWN",
  };

  const errorDetails: Record<string, { title: string; description: string }> = {
    ERR_CARD_DECLINED: {
      title: "Card Declined",
      description: "Your card was declined. Please verify your card details and try again.",
    },
    ERR_INSUFFICIENT_FUNDS: {
      title: "Insufficient Funds",
      description: "Your account does not have enough balance. Please add funds and try again.",
    },
    ERR_INVALID_CARD: {
      title: "Invalid Card",
      description: "The card details provided are invalid. Please check and try again.",
    },
    ERR_EXPIRED_CARD: {
      title: "Card Expired",
      description: "Your card has expired. Please use another payment method.",
    },
    ERR_NETWORK: {
      title: "Network Error",
      description: "There was a network issue. Please check your connection and try again.",
    },
    ERR_UNKNOWN: {
      title: "Payment Failed",
      description: "An unexpected error occurred. Please try again later.",
    },
  };

  const details = errorDetails[errorCode] || errorDetails.ERR_UNKNOWN;

  return (
    <div className="flex-1 min-h-screen bg-gradient-to-br from-red-50 via-slate-50 to-orange-50 pt-20 pb-12">
      <div className="max-w-2xl mx-auto px-4 space-y-8">
        {/* Error Icon */}
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{
            type: "spring",
            stiffness: 100,
            damping: 15,
            delay: 0.2,
          }}
          className="flex justify-center"
        >
          <div className="relative">
            <div className="absolute inset-0 bg-red-200 rounded-full animate-pulse" />
            <div className="relative w-24 h-24 bg-gradient-to-br from-red-400 to-red-600 rounded-full flex items-center justify-center">
              <AlertCircle className="w-12 h-12 text-white" />
            </div>
          </div>
        </motion.div>

        {/* Error Message */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="text-center space-y-2"
        >
          <h1 className="text-4xl font-bold text-slate-900">{details.title}</h1>
          <p className="text-lg text-slate-600">{details.description}</p>
        </motion.div>

        {/* Error Details Card */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
        >
          <Card className="border-red-200 shadow-lg">
            <CardHeader className="bg-gradient-to-r from-red-50 to-orange-50 pb-4 mb-4 rounded-t-xl border-b border-red-200">
              <CardTitle className="text-red-900">Error Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Error Code */}
              <div className="bg-slate-50 rounded-lg p-4 border border-slate-200">
                <p className="text-sm text-slate-600 mb-2">Error Code</p>
                <p className="font-mono text-sm font-bold text-slate-700">{errorCode}</p>
              </div>

              {/* Custom Message */}
              {errorMessage && (
                <div className="bg-orange-50 border border-orange-200 rounded-lg p-4">
                  <p className="text-sm text-orange-900">{errorMessage}</p>
                </div>
              )}

              {/* Troubleshooting Tips */}
              <div className="border-t border-b border-slate-200 py-4 space-y-3">
                <h3 className="font-semibold text-slate-900">Try the following:</h3>
                <ul className="space-y-2 text-sm text-slate-700">
                  <li className="flex items-start gap-3">
                    <span className="font-bold text-teal-600">1.</span>
                    <span>Check your card details (number, expiry, CVC)</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="font-bold text-teal-600">2.</span>
                    <span>Ensure your card has sufficient balance</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="font-bold text-teal-600">3.</span>
                    <span>Try a different payment method</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="font-bold text-teal-600">4.</span>
                    <span>Contact your bank if the issue persists</span>
                  </li>
                </ul>
              </div>

              {/* Actions */}
              <div className="flex gap-3 pt-4 flex-col sm:flex-row">
                <Button
                  onClick={() => navigate("/payment/checkout")}
                  className="flex-1 flex items-center justify-center gap-2 bg-teal-600 hover:bg-teal-700 text-white"
                >
                  <RotateCcw className="w-4 h-4" />
                  Try Again
                </Button>

                <Button
                  onClick={() => navigate("/customer")}
                  variant="outline"
                  className="flex-1 flex items-center justify-center gap-2"
                >
                  <Home className="w-4 h-4" />
                  Go to Dashboard
                </Button>
              </div>

              {/* Support */}
              <Button
                onClick={() => {}}
                variant="outline"
                className="w-full flex items-center justify-center gap-2 border-slate-300"
              >
                <Mail className="w-4 h-4" />
                Contact Support
              </Button>
            </CardContent>
          </Card>
        </motion.div>

        {/* Help Section */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
          className="text-center space-y-2 text-sm text-slate-600"
        >
          <p>Still having issues?</p>
          <a href="mailto:support@washmate.ph" className="text-teal-600 hover:underline font-medium">
            Email us at support@washmate.ph
          </a>
        </motion.div>
      </div>
    </div>
  );
}
