import { useState } from "react";
import { useNavigate } from "react-router";
import { useOrder } from "../contexts/OrderContext";
import { orderAPI } from "../services/order";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { CreditCard, Smartphone, CheckCircle2, AlertCircle, Loader2, Zap, Wallet } from "lucide-react";
import { motion } from "motion/react";
import { useEffect } from "react";

export default function PaymentReview() {
  const navigate = useNavigate();
  const { orderData, setOrderData, prevStep, submitOrder, resetOrder } = useOrder();
  const [isProcessing, setIsProcessing] = useState(false);
  const [agreeToTerms, setAgreeToTerms] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);
  const [selectedMethod, setSelectedMethod] = useState<string>("");

  // Restore orderId from localStorage if it was lost (e.g. page refresh)
  useEffect(() => {
    console.log('📋 PaymentReview mounted. orderData.orderId:', orderData.orderId);

    if (!orderData.orderId) {
      // Try to restore from localStorage
      const savedOrderId = localStorage.getItem('currentOrderId');
      const savedOrderData = localStorage.getItem('currentOrderData');

      console.log('📦 Checking localStorage - orderId:', savedOrderId, 'orderData:', savedOrderData);

      if (savedOrderData) {
        try {
          const parsedData = JSON.parse(savedOrderData);
          // Convert selectedServices Object back to Map
          if (parsedData.selectedServices && typeof parsedData.selectedServices === 'object') {
            parsedData.selectedServices = new Map(Object.entries(parsedData.selectedServices));
          }
          console.log('✅ Restored order data from localStorage:', parsedData);
          setOrderData(parsedData);
        } catch (e) {
          console.error('❌ Failed to parse stored order data:', e);
        }
      } else if (savedOrderId) {
        console.log('✅ Restored orderId from localStorage:', savedOrderId);
        setOrderData({ orderId: parseInt(savedOrderId) });
      }
    }
  }, []);

  const handleSubmit = async () => {
    if (!agreeToTerms) {
      setPaymentError("Please agree to the terms and conditions.");
      return;
    }
    if (!selectedMethod) {
      setPaymentError("Please select a payment method.");
      return;
    }
    setPaymentError(null);
    setIsProcessing(true);

    const amount = orderData.estimatedPrice || 0;

    try {
      // Get orderId from orderData, localStorage, or create the order now.
      let orderId = orderData.orderId;

      if (!orderId) {
        // Try to get from localStorage (in case of page refresh)
        const savedOrderId = localStorage.getItem('currentOrderId');
        if (savedOrderId) {
          orderId = parseInt(savedOrderId);
        }
      }

      if (!orderId) {
        const createdOrder = await submitOrder(false);
        orderId = createdOrder?.orderId;
      }

      if (!orderId) {
        throw new Error("Order ID not found - please go back and resubmit your order");
      }

      console.log("📝 Using order ID:", orderId, "Amount:", amount);

      // 2. Process payment with backend (backend handles PayMongo)
      const paymentResponse = await orderAPI.processPayment(orderId, selectedMethod);
      console.log("💳 Payment process response:", paymentResponse);

      const paymentId = paymentResponse?.paymentId;

      if (!paymentId) {
        console.error("❌ No paymentId in response:", paymentResponse);
        throw new Error("Failed to process payment - no paymentId returned from backend");
      }

      console.log("💳 Payment processing initiated:", { paymentId, orderId, method: selectedMethod });

      // 3. Route based on payment method
      if (selectedMethod === "CARD") {
        // For card: backend returns paymentIntentId and clientKey
        const paymentIntentId = paymentResponse?.paymentIntentId;
        const clientKey = paymentResponse?.clientKey;

        console.log("💳 Routing to card checkout with intent:", paymentIntentId);
        // Clear the cart so the user can't accidentally double pay via the back button
        localStorage.removeItem('currentOrderId');
        localStorage.removeItem('currentOrderData');
        resetOrder();
        const actualAmount = paymentResponse?.amount || amount;
        navigate("/payment/checkout", {
          state: { orderId, amount: actualAmount, paymentId, paymentMethod: "CARD", paymentIntentId, clientKey },
        });
        return;
      }

      if (selectedMethod === "WALLET") {
        console.log("💰 Routing to wallet success (direct payment)");
        // Clear the stored order data after successful payment
        localStorage.removeItem('currentOrderId');
        localStorage.removeItem('currentOrderData');
        const actualAmount = paymentResponse?.amount || amount;
        resetOrder();
        navigate("/payment/success", {
          state: { orderId, amount: actualAmount, paymentId, paymentMethod: "WALLET" },
        });
        return;
      }

      // E-wallet methods (GCASH, PAYMAYA, GRAB_PAY)
      // Backend returns checkout URL
      const checkoutUrl = paymentResponse?.checkoutUrl;

      if (!checkoutUrl) {
        throw new Error("No checkout URL returned from backend for e-wallet payment");
      }

      console.log("✅ Redirecting to e-wallet checkout:", checkoutUrl);
      // Clear the stored order data before redirecting
      localStorage.removeItem('currentOrderId');
      localStorage.removeItem('currentOrderData');
      resetOrder();
      window.location.href = checkoutUrl;

    } catch (err) {
      console.error("❌ Payment error:", err);
      setPaymentError(err instanceof Error ? err.message : "Payment failed. Please try again.");
      setIsProcessing(false);
    }
  };

  const paymentMethods = [
    {
      id: "GCASH",
      name: "GCash",
      icon: Smartphone,
      description: "Pay using GCash mobile wallet",
      color: "from-blue-500 to-cyan-500",
    },
    {
      id: "PAYMAYA",
      name: "Maya",
      icon: Smartphone,
      description: "Pay using Maya mobile wallet",
      color: "from-purple-500 to-pink-500",
    },
    {
      id: "CARD",
      name: "Credit/Debit Card",
      icon: CreditCard,
      description: "Visa, Mastercard, or other cards",
      color: "from-orange-500 to-red-500",
    },
    {
      id: "GRAB_PAY",
      name: "GrabPay",
      icon: Smartphone,
      description: "Pay through GrabPay",
      color: "from-green-500 to-emerald-500",
    },
    {
      id: "WALLET",
      name: "Wallet",
      icon: Wallet,
      description: "Pay using your WashMate wallet balance",
      color: "from-teal-500 to-cyan-500",
    },
  ];

  const serviceNames: Record<string, string> = {
    wash: "Wash",
    fold: "Fold",
    iron: "Iron",
    dry_clean: "Dry Clean",
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12">
      <div className="max-w-4xl mx-auto px-4 space-y-8">
        {/* Step Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-2"
        >
          <div className="flex items-center gap-4">
            <div className="flex items-center justify-center w-10 h-10 rounded-full bg-teal-600 text-white font-bold">
              3
            </div>
            <h1 className="text-3xl font-bold text-slate-900">Payment & Review</h1>
          </div>
          <p className="text-slate-600 ml-14">Confirm your order and choose payment method</p>
        </motion.div>

        {/* Progress Bar */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex gap-2"
        >
          {[1, 2, 3].map((step) => (
            <div key={step} className="flex-1">
              <div className="h-2 rounded-full bg-teal-600" />
            </div>
          ))}
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Order Summary */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Order Summary</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  {/* Service Details */}
                  <div className="bg-slate-50 rounded-lg p-4 space-y-3">
                    <div className="flex justify-between">
                      <span className="text-sm text-slate-600">Services</span>
                      <span className="font-semibold text-slate-900 text-right">
                        {Array.from(orderData.selectedServices?.keys() ?? []).length > 0
                          ? Array.from(orderData.selectedServices?.keys() ?? []).join(" + ")
                          : "—"}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-slate-600">Weight</span>
                      <span className="font-semibold text-slate-900">
                        {orderData.weight || 5} kg
                      </span>
                    </div>
                    {orderData.specialInstructions && (
                      <div className="flex justify-between">
                        <span className="text-sm text-slate-600">Special Instructions</span>
                        <span className="text-sm text-slate-900">
                          {orderData.specialInstructions}
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Schedule Details */}
                  <div className="border-t border-slate-200 pt-4 space-y-3">
                    <p className="font-semibold text-slate-900 text-sm">Schedule</p>
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-600">Pickup</span>
                      <span className="font-medium text-slate-900 text-right">
                        {orderData.pickupDate ?? "—"}{orderData.pickupTime ? ` at ${orderData.pickupTime}` : ""}
                      </span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-600">Delivery</span>
                      <span className="font-medium text-slate-900 text-right">
                        {orderData.deliveryDate ?? "—"}{orderData.deliveryTime ? ` at ${orderData.deliveryTime}` : ""}
                      </span>
                    </div>
                  </div>

                  {/* Address */}
                  <div className="border-t border-slate-200 pt-4 space-y-2">
                    <p className="font-semibold text-slate-900 text-sm">Delivery Address</p>
                    <p className="text-sm text-slate-600">{orderData.address}</p>
                    <p className="text-sm text-slate-600">Ph: {orderData.phoneNumber}</p>
                  </div>
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
                          onClick={() => setSelectedMethod(method.id)}
                          whileHover={{ scale: 1.02 }}
                          whileTap={{ scale: 0.98 }}
                          className={`p-4 rounded-lg border-2 transition-all text-left ${
                            selectedMethod === method.id
                            ? "border-teal-500 bg-teal-50"
                            : "border-slate-200 hover:border-slate-300 bg-white"
                          }`}
                        >
                          <div
                            className={`w-10 h-10 rounded-lg flex items-center justify-center mb-2 bg-gradient-to-br ${
                              method.color
                            } text-white`}
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
                        <a href="#" className="text-teal-600 hover:underline font-medium">
                          Terms of Service
                        </a>{" "}
                        and{" "}
                        <a href="#" className="text-teal-600 hover:underline font-medium">
                          Privacy Policy
                        </a>
                      </p>
                      <p className="text-xs text-slate-500 mt-1">
                        By placing this order, you acknowledge and agree to our policies
                      </p>
                    </div>
                  </label>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* Cost Summary Sidebar */}
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
                      <span className="text-white/80">Laundry Service</span>
                      <span className="font-semibold">
                        ₱{(orderData.subtotal ?? orderData.estimatedPrice ?? 0).toFixed(2)}
                      </span>
                    </div>
                    {(orderData.discountPercentage ?? 0) > 0 && (
                      <div className="flex justify-between text-sm">
                        <span className="flex items-center gap-1 text-amber-300">
                          <Zap className="w-3.5 h-3.5" />
                          Premium Discount ({orderData.discountPercentage}%)
                        </span>
                        <span className="font-semibold text-amber-300">
                          −₱{(orderData.discountAmount ?? 0).toFixed(2)}
                        </span>
                      </div>
                    )}
                  </div>

                  <div className="border-t border-teal-400 pt-4 flex justify-between items-center">
                    <span className="font-semibold">Total Amount</span>
                    <span className="text-3xl font-bold">
                      ₱{(orderData.estimatedPrice || 0).toFixed(2)}
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
                disabled={isProcessing || !agreeToTerms || !selectedMethod}
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
                onClick={() => { prevStep(); navigate("/order/schedule-address"); }}
                variant="outline"
                className="w-full border-slate-300"
              >
                Back
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
