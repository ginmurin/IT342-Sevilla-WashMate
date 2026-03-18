import { useLocation, useNavigate } from "react-router";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import {
  CheckCircle2,
  Download,
  Home,
  Clock,
  MapPin,
  DollarSign,
} from "lucide-react";
import { motion } from "motion/react";

export default function PaymentSuccess() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as any;

  const { orderId, amount, serviceType } = state || {
    orderId: "ORD-" + Date.now(),
    amount: 250,
    serviceType: "wash_fold",
  };

  // Confetti animation component
  const Confetti = () => (
    <div className="fixed inset-0 pointer-events-none overflow-hidden">
      {[...Array(30)].map((_, i) => (
        <motion.div
          key={i}
          initial={{
            opacity: 1,
            y: -20,
            x: Math.random() * window.innerWidth,
          }}
          animate={{
            opacity: 0,
            y: window.innerHeight,
          }}
          transition={{
            duration: 2,
            delay: Math.random() * 0.3,
            ease: "easeIn",
          }}
          className={`absolute w-2 h-2 rounded-full ${
            ["bg-teal-500", "bg-teal-400", "bg-blue-500", "bg-emerald-500"][
              i % 4
            ]
          }`}
        />
      ))}
    </div>
  );

  return (
    <div className="flex-1 min-h-screen bg-gradient-to-br from-teal-50 via-slate-50 to-blue-50 pt-20 pb-12">
      <Confetti />

      <div className="max-w-2xl mx-auto px-4 space-y-8">
        {/* Success Icon */}
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
            <div className="absolute inset-0 bg-teal-200 rounded-full animate-pulse" />
            <div className="relative w-24 h-24 bg-gradient-to-br from-teal-400 to-teal-600 rounded-full flex items-center justify-center">
              <CheckCircle2 className="w-12 h-12 text-white" />
            </div>
          </div>
        </motion.div>

        {/* Success Message */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="text-center space-y-2"
        >
          <h1 className="text-4xl font-bold text-slate-900">Payment Successful!</h1>
          <p className="text-lg text-slate-600">
            Your laundry order has been confirmed
          </p>
        </motion.div>

        {/* Order Details Card */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
        >
          <Card className="border-slate-200 shadow-lg">
            <CardHeader className="bg-gradient-to-r from-teal-50 to-blue-50 pb-4 mb-4 rounded-t-xl border-b border-slate-100">
              <CardTitle>Order Confirmation</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Order ID */}
              <div className="bg-slate-50 rounded-lg p-4">
                <p className="text-sm text-slate-600 mb-1">Order ID</p>
                <p className="text-2xl font-mono font-bold text-teal-600">{orderId}</p>
              </div>

              {/* Details Grid */}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <DollarSign className="w-4 h-4 text-teal-600" />
                    <span className="text-sm text-slate-600">Amount Paid</span>
                  </div>
                  <p className="text-2xl font-bold text-slate-900 ml-6">
                    ₱{amount.toFixed(2)}
                  </p>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4 text-blue-600" />
                    <span className="text-sm text-slate-600">Status</span>
                  </div>
                  <p className="text-2xl font-bold text-emerald-600 ml-6">Confirmed</p>
                </div>
              </div>

              {/* Service Details */}
              <div className="border-t border-b border-slate-200 py-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-slate-600">Service Type</span>
                  <span className="font-medium text-slate-900">
                    {serviceType.replace(/_/g, " ")}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-600">Estimated Delivery</span>
                  <span className="font-medium text-slate-900">2-3 days</span>
                </div>
              </div>

              {/* Next Steps */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 space-y-2">
                <h3 className="font-semibold text-blue-900">What comes next?</h3>
                <ul className="space-y-2 text-sm text-blue-800">
                  <li className="flex items-start gap-2">
                    <span className="font-bold">1.</span>
                    <span>We'll pick up your laundry within 24 hours</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="font-bold">2.</span>
                    <span>You'll receive updates via email and SMS</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="font-bold">3.</span>
                    <span>Your clean clothes will be delivered in 2-3 days</span>
                  </li>
                </ul>
              </div>

              {/* Actions */}
              <div className="flex gap-3 pt-4">
                <Button
                  onClick={() => {
                    const element = document.createElement("a");
                    element.href = "#";
                    element.download = `invoice-${orderId}.pdf`;
                    element.click();
                  }}
                  variant="outline"
                  className="flex-1 flex items-center justify-center gap-2"
                >
                  <Download className="w-4 h-4" />
                  Download Invoice
                </Button>

                <Button
                  onClick={() => navigate("/payment/history")}
                  className="flex-1 bg-teal-600 hover:bg-teal-700 text-white"
                >
                  View All Orders
                </Button>
              </div>

              <Button
                onClick={() => navigate("/customer")}
                variant="outline"
                className="w-full flex items-center justify-center gap-2"
              >
                <Home className="w-4 h-4" />
                Back to Dashboard
              </Button>
            </CardContent>
          </Card>
        </motion.div>

        {/* Support Section */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
          className="text-center text-sm text-slate-600"
        >
          <p>
            Need help? Contact our support team at{" "}
            <a href="mailto:support@washmate.ph" className="text-teal-600 hover:underline font-medium">
              support@washmate.ph
            </a>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
