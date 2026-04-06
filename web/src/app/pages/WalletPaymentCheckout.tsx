import { useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import {
  ArrowLeft,
  Lock,
  AlertCircle,
  CreditCard,
  Loader2,
  Mail,
} from "lucide-react";
import { motion } from "motion/react";
import {
  createPaymentIntent,
  createCardPaymentMethod,
  attachPaymentMethod,
} from "../services/paymongo";

// ── Helpers ──────────────────────────────────────────────────────────────────
function formatCardNumber(value: string) {
  return value.replace(/\D/g, "").slice(0, 16).replace(/(.{4})/g, "$1 ").trim();
}

function formatExpiry(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 4);
  return digits.length >= 3 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
}

// ── Component ────────────────────────────────────────────────────────────────
export default function WalletPaymentCheckout() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state as {
    topUpId?: string;
    amount?: number;
    paymentId?: number;
  }) ?? {};

  const topUpId = state.topUpId ?? `TOPUP-${Date.now()}`;
  const amount = state.amount ?? 0;
  const paymentId = state.paymentId;

  const [cardNumber, setCardNumber] = useState("");
  const [cardName, setCardName] = useState("");
  const [cardEmail, setCardEmail] = useState("");
  const [cardExpiry, setCardExpiry] = useState("");
  const [cardCVC, setCardCVC] = useState("");

  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const rawNumber = cardNumber.replace(/\s/g, "");
    if (rawNumber.length < 13 || rawNumber.length > 16) {
      setError("Please enter a valid card number.");
      return;
    }
    if (!cardName.trim()) {
      setError("Please enter the cardholder name.");
      return;
    }
    if (!cardEmail.trim() || !cardEmail.includes("@")) {
      setError("Please enter a valid email address.");
      return;
    }
    const [expMonthStr, expYearStr] = cardExpiry.split("/");
    const expMonth = parseInt(expMonthStr ?? "", 10);
    const expYear = parseInt(`20${expYearStr ?? ""}`, 10);
    if (!expMonth || !expYear || expMonth < 1 || expMonth > 12) {
      setError("Please enter a valid expiry date (MM/YY).");
      return;
    }
    if (cardCVC.length < 3) {
      setError("Please enter a valid CVC.");
      return;
    }

    setIsProcessing(true);

    try {
      // 1. Create PaymentIntent
      const intentResponse = await createPaymentIntent(amount);
      const intentId = intentResponse.id;
      const clientKey = intentResponse.clientKey;

      console.log("🔑 PayMongo Intent Created:", { intentId, clientKey, amount });

      // 2. Create PaymentMethod from card details
      const paymentMethodId = await createCardPaymentMethod({
        number: rawNumber,
        expMonth,
        expYear,
        cvc: cardCVC,
        name: cardName,
        email: cardEmail,
      });

      console.log("💳 Payment Method Created:", { paymentMethodId });

      // 3. Attach — triggers 3DS if needed
      const returnUrl = `${window.location.origin}/wallet/payment-success?topUpId=${topUpId}&amount=${amount}&paymentId=${paymentId}&paymongoPaymentIntentId=${intentId}`;
      console.log("🔗 Return URL:", returnUrl);

      const attachResponse = await attachPaymentMethod(
        intentId,
        paymentMethodId,
        clientKey,
        returnUrl
      );

      const { status, redirectUrl } = attachResponse;
      console.log("📤 Attach Response:", { status, redirectUrl, intentId });

      if (status === "awaiting_next_action" && redirectUrl) {
        // Redirect to bank's 3DS authentication page
        console.log("🔄 Redirecting to 3DS:", redirectUrl);
        window.location.href = redirectUrl;
      } else {
        // succeeded or other terminal status
        console.log("✅ Direct success, navigating with state:", { topUpId, amount, paymentId, paymongoPaymentIntentId: intentId });
        navigate("/wallet/payment-success", { state: { topUpId, amount, paymentId, paymongoPaymentIntentId: intentId } });
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Payment processing failed. Please try again."
      );
      setIsProcessing(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-20 pb-12">
      <div className="max-w-lg mx-auto px-4 space-y-6">
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
            <h1 className="text-2xl font-bold text-slate-900">Card Payment</h1>
            <p className="text-slate-500 text-sm">
              Secure checkout powered by PayMongo
            </p>
          </div>
        </motion.div>

        {/* Amount card */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.05 }}
        >
          <Card className="bg-gradient-to-br from-teal-500 to-teal-700 text-white border-none shadow-lg">
            <CardContent className="pt-5 pb-5 flex items-center justify-between">
              <div>
                <p className="text-teal-100 text-sm">Top-up Reference</p>
                <p className="font-mono font-semibold text-teal-100 text-sm">
                  {topUpId}
                </p>
              </div>
              <div className="text-right">
                <p className="text-teal-100 text-sm">Amount Due</p>
                <p className="text-3xl font-bold">₱{amount.toFixed(2)}</p>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Card form */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
        >
          <Card className="border-slate-200 shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between pb-3 border-b border-slate-100">
              <CardTitle className="text-lg flex items-center gap-2">
                <Lock className="w-5 h-5 text-teal-600" /> Card Details
              </CardTitle>
              <div className="flex gap-1.5">
                {["VISA", "MC", "JCB"].map((b) => (
                  <span
                    key={b}
                    className="px-2 py-0.5 bg-slate-100 rounded text-xs font-bold text-slate-600"
                  >
                    {b}
                  </span>
                ))}
              </div>
            </CardHeader>
            <CardContent className="pt-5">
              {error && (
                <motion.div
                  initial={{ opacity: 0, y: -8 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3"
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
                  <div className="relative">
                    <CreditCard className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                    <Input
                      type="text"
                      inputMode="numeric"
                      placeholder="1234 5678 9012 3456"
                      value={cardNumber}
                      onChange={(e) =>
                        setCardNumber(formatCardNumber(e.target.value))
                      }
                      maxLength={19}
                      className="pl-9 font-mono tracking-widest"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">
                    Cardholder Name
                  </label>
                  <Input
                    type="text"
                    placeholder="JUAN DELA CRUZ"
                    value={cardName}
                    onChange={(e) =>
                      setCardName(e.target.value.toUpperCase())
                    }
                    className="uppercase tracking-wide"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-2">
                    Email Address
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                    <Input
                      type="email"
                      placeholder="juan@example.com"
                      value={cardEmail}
                      onChange={(e) => setCardEmail(e.target.value)}
                      className="pl-9"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      Expiry Date
                    </label>
                    <Input
                      type="text"
                      inputMode="numeric"
                      placeholder="MM/YY"
                      value={cardExpiry}
                      onChange={(e) =>
                        setCardExpiry(formatExpiry(e.target.value))
                      }
                      maxLength={5}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      CVC
                    </label>
                    <Input
                      type="password"
                      inputMode="numeric"
                      placeholder="•••"
                      value={cardCVC}
                      onChange={(e) =>
                        setCardCVC(
                          e.target.value.replace(/\D/g, "").slice(0, 4)
                        )
                      }
                      maxLength={4}
                    />
                  </div>
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 flex items-start gap-2">
                  <Lock className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
                  <p className="text-xs text-blue-800">
                    Your card details are encrypted and processed securely by
                    PayMongo. We never store your card information.
                  </p>
                </div>

                <Button
                  type="submit"
                  disabled={isProcessing}
                  className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg font-medium flex items-center justify-center gap-2 disabled:opacity-60"
                >
                  {isProcessing ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" /> Processing…
                    </>
                  ) : (
                    `Pay ₱${amount.toFixed(2)}`
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}
