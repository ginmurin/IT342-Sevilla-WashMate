import { useNavigate } from "react-router";
import { Card, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  AlertCircle,
  Home,
  ArrowLeft,
  RefreshCw,
  Wallet as WalletIcon,
} from "lucide-react";
import { motion } from "motion/react";

export default function WalletPaymentError() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-red-50 to-orange-50 pt-20 pb-12 flex items-center">
      <div className="max-w-2xl mx-auto px-4 w-full">
        {/* Error Icon */}
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2, duration: 0.4 }}
          className="text-center mb-8"
        >
          <motion.div
            animate={{ shake: [0, -4, 4, -2, 2, 0] }}
            transition={{
              duration: 0.5,
              delay: 0.2,
              repeat: 1,
              repeatDelay: 1,
            }}
          >
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-red-400 to-orange-500 flex items-center justify-center mx-auto mb-6 shadow-lg">
              <AlertCircle className="w-10 h-10 text-white" />
            </div>
          </motion.div>
        </motion.div>

        {/* Error Message */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="text-center mb-8"
        >
          <h1 className="text-4xl font-bold text-slate-900 mb-2">
            Payment Failed
          </h1>
          <p className="text-lg text-slate-600">
            We were unable to process your wallet top-up at this time
          </p>
        </motion.div>

        {/* Details Card */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="mb-8"
        >
          <Card className="border-red-200 bg-white shadow-lg">
            <CardContent className="pt-8 pb-8 space-y-6">
              {/* Error Details */}
              <div className="space-y-4">
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start gap-3">
                  <AlertCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-slate-900 text-sm">
                      Transaction Declined
                    </p>
                    <p className="text-xs text-slate-600 mt-1">
                      Your payment could not be processed. This may be due to
                      insufficient funds, incorrect card details, or a temporary
                      issue with your bank.
                    </p>
                  </div>
                </div>

                {/* Troubleshooting Tips */}
                <div className="border-t border-slate-100 pt-4">
                  <p className="font-medium text-slate-900 text-sm mb-3">
                    What you can try:
                  </p>
                  <ul className="space-y-2">
                    <li className="flex gap-2 text-sm text-slate-600">
                      <span className="text-red-600">•</span>
                      <span>Check your card details and try again</span>
                    </li>
                    <li className="flex gap-2 text-sm text-slate-600">
                      <span className="text-red-600">•</span>
                      <span>Verify your card has sufficient balance</span>
                    </li>
                    <li className="flex gap-2 text-sm text-slate-600">
                      <span className="text-red-600">•</span>
                      <span>Try a different payment method</span>
                    </li>
                    <li className="flex gap-2 text-sm text-slate-600">
                      <span className="text-red-600">•</span>
                      <span>Contact your bank if the issue persists</span>
                    </li>
                  </ul>
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
            onClick={() => navigate("/wallet/payment-review")}
            className="bg-orange-600 hover:bg-orange-700 text-white h-12 rounded-lg font-medium flex items-center justify-center gap-2"
          >
            <RefreshCw className="w-4 h-4" />
            Try Again
          </Button>
          <Button
            onClick={() => navigate("/wallet")}
            variant="outline"
            className="border-slate-300 h-12 rounded-lg font-medium flex items-center justify-center gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Wallet
          </Button>
        </motion.div>

        {/* Go Home Button */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
          className="mt-3"
        >
          <Button
            onClick={() => navigate("/")}
            className="w-full border-slate-300 h-12 rounded-lg font-medium flex items-center justify-center gap-2"
            variant="outline"
          >
            <Home className="w-4 h-4" />
            Go Home
          </Button>
        </motion.div>

        {/* Support Info */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.7 }}
          className="mt-8 text-center"
        >
          <p className="text-sm text-slate-600">
            Need help?{" "}
            <a href="#" className="text-teal-600 hover:underline font-medium">
              Contact Support
            </a>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
