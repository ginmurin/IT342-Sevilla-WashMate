import { useState } from "react";
import { useNavigate } from "react-router";
import { usePayment, PaymentOrder } from "../contexts/PaymentContext";
import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import {
  Droplets,
  ArrowLeft,
  Lock,
  AlertCircle,
  CheckCircle2,
} from "lucide-react";
import { motion } from "motion/react";

export default function PaymentCheckout() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { initializePayment, addTransaction } = usePayment();

  const [serviceType, setServiceType] = useState<"wash_fold" | "wash_iron" | "dry_clean">("wash_fold");
  const [weight, setWeight] = useState(5);
  const [cardNumber, setCardNumber] = useState("");
  const [cardName, setCardName] = useState("");
  const [cardExpiry, setCardExpiry] = useState("");
  const [cardCVC, setCardCVC] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Pricing: per kilo
  const servicePricing = {
    wash_fold: 50,
    wash_iron: 80,
    dry_clean: 120,
  };

  const totalAmount = weight * servicePricing[serviceType];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!cardNumber || !cardName || !cardExpiry || !cardCVC) {
      setError("Please fill in all card details");
      return;
    }

    if (cardNumber.length < 13 || cardNumber.length > 19) {
      setError("Invalid card number");
      return;
    }

    setIsProcessing(true);

    try {
      // In a real app, call PayMongo API through your backend
      // For now, simulate the payment after a short delay
      await new Promise((resolve) => setTimeout(resolve, 2000));

      // Create order
      const order: PaymentOrder = {
        serviceType,
        weight,
        amount: totalAmount,
        description: `${weight}kg ${serviceType.replace(/_/g, " ")} laundry service`,
      };

      // Initialize payment in context
      initializePayment(order);

      // Add transaction record
      addTransaction({
        id: `TXN-${Date.now()}`,
        amount: totalAmount,
        status: "completed",
        serviceType,
        date: new Date().toISOString(),
        paymentMethod: "card",
        referenceNumber: `REF-${Math.random().toString(36).substring(7).toUpperCase()}`,
      });

      // Redirect to success
      navigate("/payment/success", {
        state: {
          orderId: `ORD-${Date.now()}`,
          amount: totalAmount,
          serviceType,
        },
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Payment processing failed");
      setIsProcessing(false);
    }
  };

  return (
    <div className="flex-1 min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-20 pb-12">
      <div className="max-w-2xl mx-auto px-4 space-y-8">
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
            <h1 className="text-3xl font-bold text-slate-900">Payment Checkout</h1>
            <p className="text-slate-500 mt-1">Complete your laundry service order</p>
          </div>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Form */}
          <div className="lg:col-span-2 space-y-6">
            {/* Service Selection */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Select Service</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {[
                    { type: "wash_fold" as const, name: "Wash & Fold", price: 50 },
                    { type: "wash_iron" as const, name: "Wash & Iron", price: 80 },
                    { type: "dry_clean" as const, name: "Dry Clean", price: 120 },
                  ].map((service) => (
                    <label
                      key={service.type}
                      className={`flex items-center p-4 border-2 rounded-lg cursor-pointer transition-all ${
                        serviceType === service.type
                          ? "border-teal-500 bg-teal-50"
                          : "border-slate-200 hover:border-slate-300"
                      }`}
                    >
                      <input
                        type="radio"
                        name="service"
                        value={service.type}
                        checked={serviceType === service.type}
                        onChange={(e) =>
                          setServiceType(e.target.value as typeof serviceType)
                        }
                        className="w-5 h-5 text-teal-600 cursor-pointer"
                      />
                      <span className="ml-3 flex-1 font-medium text-slate-900">
                        {service.name}
                      </span>
                      <span className="text-sm text-slate-500">
                        ₱{service.price}/kg
                      </span>
                    </label>
                  ))}
                </CardContent>
              </Card>
            </motion.div>

            {/* Weight Selection */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.15 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Weight</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center gap-4">
                      <input
                        type="range"
                        min="1"
                        max="50"
                        value={weight}
                        onChange={(e) => setWeight(Number(e.target.value))}
                        className="flex-1 h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer"
                        style={{
                          background: `linear-gradient(to right, #0d9488 0%, #0d9488 ${
                            ((weight - 1) / 49) * 100
                          }%, #e2e8f0 ${((weight - 1) / 49) * 100}%, #e2e8f0 100%)`,
                        }}
                      />
                      <span className="text-2xl font-bold text-teal-600 w-16 text-right">
                        {weight}kg
                      </span>
                    </div>
                    <p className="text-sm text-slate-500">
                      Adjust the weight of your laundry (1-50 kg)
                    </p>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Payment Form */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between pb-4 border-b border-slate-100">
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Lock className="w-5 h-5 text-teal-600" />
                    Payment Details
                  </CardTitle>
                  <div className="flex gap-2">
                    {["visa", "mastercard"].map((card) => (
                      <div
                        key={card}
                        className="w-10 h-6 bg-slate-100 rounded flex items-center justify-center text-xs font-semibold text-slate-600"
                      >
                        {card === "visa" ? "VISA" : "MC"}
                      </div>
                    ))}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  {error && (
                    <motion.div
                      initial={{ opacity: 0, y: -8 }}
                      animate={{ opacity: 1, y: 0 }}
                      className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3"
                    >
                      <AlertCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                      <p className="text-sm text-red-700">{error}</p>
                    </motion.div>
                  )}

                  <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Card Number
                      </label>
                      <Input
                        type="text"
                        placeholder="1234 5678 9012 3456"
                        value={cardNumber}
                        onChange={(e) => setCardNumber(e.target.value.replace(/\D/g, ""))}
                        maxLength={19}
                        className="font-mono"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Cardholder Name
                      </label>
                      <Input
                        type="text"
                        placeholder="John Doe"
                        value={cardName}
                        onChange={(e) => setCardName(e.target.value)}
                      />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-slate-700 mb-2">
                          Expiry Date
                        </label>
                        <Input
                          type="text"
                          placeholder="MM/YY"
                          value={cardExpiry}
                          onChange={(e) => setCardExpiry(e.target.value)}
                          maxLength={5}
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-slate-700 mb-2">
                          CVC
                        </label>
                        <Input
                          type="text"
                          placeholder="123"
                          value={cardCVC}
                          onChange={(e) => setCardCVC(e.target.value.replace(/\D/g, ""))}
                          maxLength={4}
                        />
                      </div>
                    </div>

                    <Button
                      type="submit"
                      disabled={isProcessing}
                      className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg"
                    >
                      {isProcessing ? (
                        <>
                          <motion.div
                            animate={{ rotate: 360 }}
                            transition={{
                              repeat: Infinity,
                              duration: 1,
                              ease: "linear",
                            }}
                            className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full"
                          />
                          Processing...
                        </>
                      ) : (
                        `Pay ₱${totalAmount.toFixed(2)}`
                      )}
                    </Button>
                  </form>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* Order Summary */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.25 }}
            className="lg:col-span-1"
          >
            <Card className="sticky top-24 border-slate-200 shadow-sm">
              <CardHeader className="bg-gradient-to-r from-teal-50 to-teal-100 pb-4 mb-4 rounded-t-xl">
                <CardTitle className="text-lg text-teal-900">Order Summary</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-3 pb-4 border-b border-slate-200">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Service</span>
                    <span className="font-medium text-slate-900">
                      {serviceType.replace(/_/g, " ")}
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Weight</span>
                    <span className="font-medium text-slate-900">{weight} kg</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-600">Rate per kg</span>
                    <span className="font-medium text-slate-900">
                      ₱{servicePricing[serviceType]}
                    </span>
                  </div>
                </div>

                <div className="flex justify-between items-center">
                  <span className="text-lg font-semibold text-slate-900">Total</span>
                  <span className="text-3xl font-bold text-teal-600">
                    ₱{totalAmount.toFixed(2)}
                  </span>
                </div>

                <div className="bg-teal-50 border border-teal-200 rounded-lg p-3 space-y-2">
                  <div className="flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-teal-600 mt-0.5 shrink-0" />
                    <p className="text-sm text-teal-900">
                      Free delivery for orders above ₱200
                    </p>
                  </div>
                  <div className="flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 text-teal-600 mt-0.5 shrink-0" />
                    <p className="text-sm text-teal-900">
                      Money-back guarantee if unsatisfied
                    </p>
                  </div>
                </div>

                <p className="text-xs text-slate-500 text-center">
                  Your payment is secure and encrypted
                </p>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
