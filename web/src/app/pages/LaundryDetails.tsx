import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { useOrder, ServiceType } from "../contexts/OrderContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { Settings, WashingMachine, FoldHorizontal, Zap, Wind, Check, AlertCircle, Plus, Minus, Crown } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { getMySubscription, type SubscriptionData } from "../services/subscription";

// ── Service definitions ──────────────────────────────────────────────────────
const SERVICES: {
  type: ServiceType;
  name: string;
  icon: React.ElementType;
  description: string;
  price: number;
  unit: string;
  accentBorder: string;   // border color class when selected
  accentShadow: string;   // shadow class when selected
  accentBg: string;       // card bg when selected
  accentIcon: string;     // icon bg when selected
  accentText: string;     // icon + price color when selected
  checkBg: string;        // checkmark circle bg
}[] = [
  {
    type: "wash",
    name: "Wash",
    icon: WashingMachine,
    description: "Deep clean with eco-friendly detergents, colour-sorted for fabric safety.",
    price: 35,
    unit: "kg",
    accentBorder: "border-blue-500",
    accentShadow: "shadow-blue-100",
    accentBg: "bg-blue-50",
    accentIcon: "bg-blue-100",
    accentText: "text-blue-600",
    checkBg: "bg-blue-500",
  },
  {
    type: "fold",
    name: "Fold",
    icon: FoldHorizontal,
    description: "KonMari-style neat folding and packaging, sorted by outfit.",
    price: 20,
    unit: "kg",
    accentBorder: "border-teal-500",
    accentShadow: "shadow-teal-100",
    accentBg: "bg-teal-50",
    accentIcon: "bg-teal-100",
    accentText: "text-teal-600",
    checkBg: "bg-teal-500",
  },
  {
    type: "iron",
    name: "Iron",
    icon: Zap,
    description: "Professional steam-pressing for a crisp, wrinkle-free finish.",
    price: 25,
    unit: "piece",
    accentBorder: "border-amber-500",
    accentShadow: "shadow-amber-100",
    accentBg: "bg-amber-50",
    accentIcon: "bg-amber-100",
    accentText: "text-amber-600",
    checkBg: "bg-amber-500",
  },
  {
    type: "dry_clean",
    name: "Dry Clean",
    icon: Wind,
    description: "Specialist solvent cleaning for delicate fabrics, suits and dress wear.",
    price: 150,
    unit: "piece",
    accentBorder: "border-purple-500",
    accentShadow: "shadow-purple-100",
    accentBg: "bg-purple-50",
    accentIcon: "bg-purple-100",
    accentText: "text-purple-600",
    checkBg: "bg-purple-500",
  },
];

// ── Component ─────────────────────────────────────────────────────────────────
export default function LaundryDetails() {
  const navigate = useNavigate();
  const { orderData, setOrderData, nextStep } = useOrder();

  const [localWeight, setLocalWeight] = useState(orderData.weight ?? 5);
  const [pieceQty, setPieceQty] = useState<Record<string, number>>(
    orderData.serviceQuantities ?? {}
  );
  const [showError, setShowError] = useState(false);
  const [subscription, setSubscription] = useState<SubscriptionData | null>(null);

  useEffect(() => {
    getMySubscription().then(setSubscription);
  }, []);

  const isPremium = subscription?.planType === "PREMIUM";
  const discountPct = isPremium ? (subscription?.discountPercentage ?? 15) : 0;

  const adjustPiece = (type: string, delta: number) => {
    setPieceQty((prev) => ({
      ...prev,
      [type]: Math.max(1, (prev[type] ?? 1) + delta),
    }));
  };

  // ── Toggle a service in the selectedServices array ─────────────────────────
  const selected = orderData.selectedServices ?? [];

  const toggleService = (type: ServiceType) => {
    setShowError(false);
    const next = selected.includes(type)
      ? selected.filter((s) => s !== type)
      : [...selected, type];
    setOrderData({ selectedServices: next });
  };

  // ── Compute totals ─────────────────────────────────────────────────────────
  const lineItems = SERVICES.filter((s) => selected.includes(s.type)).map((s) => {
    const qty = s.unit === "kg" ? localWeight : (pieceQty[s.type] ?? 1);
    return { ...s, qty, subtotal: s.price * qty };
  });

  const subtotalAmount = lineItems.reduce((sum, li) => sum + li.subtotal, 0);
  const discountAmount = Math.round(subtotalAmount * discountPct / 100);
  const estimatedPrice = subtotalAmount - discountAmount;

  // ── Proceed to next step ───────────────────────────────────────────────────
  const handleNext = () => {
    if (selected.length === 0) {
      setShowError(true);
      return;
    }
    setOrderData({
      weight: localWeight,
      serviceQuantities: pieceQty,
      estimatedPrice,
      subtotal: subtotalAmount,
      discountPercentage: discountPct,
      discountAmount,
      isRushOrder: isPremium,
    });
    nextStep();
    navigate("/order/schedule-address");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12">
      <div className="max-w-4xl mx-auto px-4 space-y-8">

        {/* ── Step header ── */}
        <motion.div initial={{ opacity: 0, y: -16 }} animate={{ opacity: 1, y: 0 }} className="space-y-2">
          <div className="flex items-center gap-4">
            <div className="flex items-center justify-center w-10 h-10 rounded-full bg-teal-600 text-white font-bold shrink-0">
              1
            </div>
            <div>
              <h1 className="text-3xl font-bold text-slate-900">Laundry Details</h1>
              <p className="text-slate-500 text-sm">Select one or more services, then set the weight</p>
            </div>
          </div>
        </motion.div>

        {/* ── Step stepper ── */}
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex items-center">
          {[
            { num: 1, label: "Laundry Details" },
            { num: 2, label: "Schedule & Address" },
            { num: 3, label: "Payment & Review" },
          ].map((step, i, arr) => (
            <div key={step.num} className="flex items-center flex-1 last:flex-none">
              <div className="flex flex-col items-center gap-1 shrink-0">
                <div
                  className={`w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm border-2 transition-all
                    ${step.num === 1
                      ? "bg-teal-600 border-teal-600 text-white shadow-md shadow-teal-200"
                      : "bg-white border-slate-300 text-slate-400"
                    }`}
                >
                  {step.num}
                </div>
                <span className={`text-xs font-medium whitespace-nowrap ${step.num === 1 ? "text-teal-700" : "text-slate-400"}`}>
                  {step.label}
                </span>
              </div>
              {i < arr.length - 1 && (
                <div className="flex-1 h-0.5 mb-5 mx-2 bg-slate-200" />
              )}
            </div>
          ))}
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* ── Left column ── */}
          <div className="lg:col-span-2 space-y-6">

            {/* Service selection */}
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
              <Card className="border-slate-200 shadow-sm">
                <CardHeader className="pb-3">
                  <CardTitle className="text-lg">Select Services</CardTitle>
                  <p className="text-sm text-slate-500">Choose one or more — click a card to select it</p>
                </CardHeader>
                <CardContent className="space-y-3">
                  {/* Validation error */}
                  <AnimatePresence>
                    {showError && (
                      <motion.div
                        initial={{ opacity: 0, y: -8 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -8 }}
                        className="flex items-center gap-2 bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2.5 text-sm"
                      >
                        <AlertCircle className="w-4 h-4 shrink-0" />
                        Please select at least one service before continuing.
                      </motion.div>
                    )}
                  </AnimatePresence>

                  {SERVICES.map((service) => {
                    const Icon = service.icon;
                    const isSelected = selected.includes(service.type);

                    return (
                      <motion.button
                        key={service.type}
                        onClick={() => toggleService(service.type)}
                        whileTap={{ scale: 0.985 }}
                        className={`
                          w-full p-4 rounded-xl text-left transition-all duration-150 relative
                          ${isSelected
                            ? `border-[3px] ${service.accentBorder} ${service.accentBg} shadow-lg ${service.accentShadow}`
                            : "border-2 border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm"
                          }
                        `}
                      >
                        {/* Selected check badge */}
                        <AnimatePresence>
                          {isSelected && (
                            <motion.span
                              key="badge"
                              initial={{ scale: 0, opacity: 0 }}
                              animate={{ scale: 1, opacity: 1 }}
                              exit={{ scale: 0, opacity: 0 }}
                              transition={{ type: "spring", stiffness: 400, damping: 20 }}
                              className={`absolute top-3 right-3 w-6 h-6 rounded-full ${service.checkBg} flex items-center justify-center shadow-sm`}
                            >
                              <Check className="w-3.5 h-3.5 text-white" strokeWidth={3} />
                            </motion.span>
                          )}
                        </AnimatePresence>

                        <div className="flex items-center gap-4 pr-8">
                          {/* Icon */}
                          <div
                            className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 transition-colors duration-150
                              ${isSelected ? service.accentIcon : "bg-slate-100"}`}
                          >
                            <Icon
                              className={`w-6 h-6 transition-colors duration-150
                                ${isSelected ? service.accentText : "text-slate-500"}`}
                            />
                          </div>

                          {/* Text */}
                          <div className="flex-1">
                            <div className="flex items-baseline gap-2 mb-0.5">
                              <span className={`font-bold text-base transition-colors duration-150 ${isSelected ? "text-slate-900" : "text-slate-700"}`}>
                                {service.name}
                              </span>
                              <span className={`text-sm font-semibold transition-colors duration-150 ${isSelected ? service.accentText : "text-slate-400"}`}>
                                ₱{service.price}/{service.unit}
                              </span>
                            </div>
                            <p className="text-sm text-slate-500 leading-snug">{service.description}</p>
                          </div>
                        </div>

                        {/* Piece quantity controls — only for piece-based services */}
                        {isSelected && service.unit === "piece" && (
                          <div
                            className="mt-3 flex items-center justify-between bg-white rounded-xl px-4 py-2.5 border border-slate-200"
                            onPointerDown={(e) => e.stopPropagation()}
                            onClick={(e) => e.stopPropagation()}
                          >
                            <span className="text-sm font-medium text-slate-700">Quantity (pieces)</span>
                            <div className="flex items-center gap-3">
                              <div
                                role="button"
                                tabIndex={0}
                                onPointerDown={(e) => e.stopPropagation()}
                                onClick={(e) => { e.stopPropagation(); adjustPiece(service.type, -1); }}
                                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center cursor-pointer transition-colors"
                              >
                                <Minus className="w-4 h-4 text-slate-600" />
                              </div>
                              <span className="text-lg font-bold text-slate-900 w-6 text-center">
                                {pieceQty[service.type] ?? 1}
                              </span>
                              <div
                                role="button"
                                tabIndex={0}
                                onPointerDown={(e) => e.stopPropagation()}
                                onClick={(e) => { e.stopPropagation(); adjustPiece(service.type, 1); }}
                                className="w-8 h-8 rounded-full bg-teal-100 hover:bg-teal-200 flex items-center justify-center cursor-pointer transition-colors"
                              >
                                <Plus className="w-4 h-4 text-teal-700" />
                              </div>
                              <span className="text-sm font-semibold text-teal-700 min-w-[56px] text-right">
                                ₱{(service.price * (pieceQty[service.type] ?? 1)).toFixed(2)}
                              </span>
                            </div>
                          </div>
                        )}
                      </motion.button>
                    );
                  })}

                  {/* Selected summary pill */}
                  <AnimatePresence>
                    {selected.length > 0 && (
                      <motion.p
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="text-xs text-slate-500 text-center pt-1"
                      >
                        {selected.length} service{selected.length > 1 ? "s" : ""} selected
                        {" — "}
                        {SERVICES.filter((s) => selected.includes(s.type)).map((s) => s.name).join(" + ")}
                      </motion.p>
                    )}
                  </AnimatePresence>
                </CardContent>
              </Card>
            </motion.div>

            {/* Weight selection */}
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }}>
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Laundry Weight</CardTitle>
                  <p className="text-sm text-slate-500 mt-0.5">For Wash &amp; Fold — priced per kg</p>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <label className="text-sm font-medium text-slate-700">Estimated Weight</label>
                      <span className="text-2xl font-bold text-teal-600">{localWeight} kg</span>
                    </div>
                    <input
                      type="range"
                      min="1"
                      max="50"
                      value={localWeight}
                      onChange={(e) => {
                        setLocalWeight(Number(e.target.value));
                        setOrderData({ weight: Number(e.target.value) });
                      }}
                      className="w-full h-2 rounded-lg appearance-none cursor-pointer"
                      style={{
                        background: `linear-gradient(to right, #0d9488 0%, #0d9488 ${((localWeight - 1) / 49) * 100}%, #e2e8f0 ${((localWeight - 1) / 49) * 100}%, #e2e8f0 100%)`,
                      }}
                    />
                    <p className="text-xs text-slate-500 mt-2">1 – 50 kg available</p>
                  </div>

                  <div className="bg-blue-50 rounded-lg p-4 space-y-1.5">
                    <p className="text-sm font-medium text-blue-900">Need help estimating?</p>
                    <ul className="text-xs text-blue-700 space-y-1">
                      <li>• T-shirt: ~0.2 kg</li>
                      <li>• Jeans: ~0.5 kg</li>
                      <li>• Bed sheet: ~1 kg</li>
                      <li>• Full washer load: ~3–5 kg</li>
                    </ul>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Special instructions */}
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Settings className="w-5 h-5 text-teal-600" />
                    Special Instructions
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <textarea
                    placeholder="E.g., Handle delicate items carefully, use mild detergent, etc."
                    value={orderData.specialInstructions ?? ""}
                    onChange={(e) => setOrderData({ specialInstructions: e.target.value })}
                    className="w-full p-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none h-24 text-sm"
                  />
                  <p className="text-xs text-slate-500 mt-2">Optional: Add special care instructions for your items</p>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* ── Order Summary sidebar ── */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.25 }}
            className="lg:col-span-1"
          >
            <div className="sticky top-24 space-y-4">
              <Card className="border-teal-200 shadow-sm bg-gradient-to-br from-teal-50 to-teal-100">
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg text-teal-900">Order Summary</CardTitle>
                    {isPremium && (
                      <span className="flex items-center gap-1 text-xs font-semibold text-amber-700 bg-amber-50 border border-amber-200 rounded-full px-2 py-0.5">
                        <Crown className="w-3 h-3" /> Premium
                      </span>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  {lineItems.length === 0 ? (
                    <p className="text-sm text-teal-700 italic">No services selected yet</p>
                  ) : (
                    <ul className="space-y-2 pb-3 border-b-2 border-teal-200">
                      {lineItems.map((li) => {
                        const Icon = li.icon;
                        return (
                          <li key={li.type} className="flex items-center justify-between text-sm">
                            <span className="flex items-center gap-2 text-teal-800">
                              <Icon className={`w-4 h-4 ${li.accentText}`} />
                              {li.name}
                              <span className="text-teal-600 text-xs">× {li.qty} {li.unit}{li.unit === "piece" && li.qty !== 1 ? "s" : ""}</span>
                            </span>
                            <span className="font-semibold text-teal-900">₱{li.subtotal.toFixed(2)}</span>
                          </li>
                        );
                      })}
                    </ul>
                  )}

                  {isPremium && discountAmount > 0 && (
                    <div className="space-y-1.5 pb-2 border-b border-teal-200">
                      <div className="flex justify-between text-sm text-teal-800">
                        <span>Subtotal</span>
                        <span>₱{subtotalAmount.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between text-sm font-medium text-amber-700">
                        <span className="flex items-center gap-1">
                          <Zap className="w-3.5 h-3.5" />
                          Premium Discount ({discountPct}%)
                        </span>
                        <span>−₱{discountAmount.toFixed(2)}</span>
                      </div>
                    </div>
                  )}

                  <div className="flex justify-between items-center">
                    <span className="font-semibold text-teal-900">Total Estimate</span>
                    <span className={`text-3xl font-bold ${lineItems.length > 0 ? "text-teal-700" : "text-teal-400"}`}>
                      ₱{estimatedPrice.toFixed(2)}
                    </span>
                  </div>

                  {isPremium && (
                    <div className="flex items-center gap-2 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 text-xs font-semibold text-amber-700">
                      <Zap className="w-3.5 h-3.5 shrink-0" />
                      Priority order processing included
                    </div>
                  )}

                  <div className="bg-white rounded-lg p-3 space-y-1 text-xs text-slate-600">
                    <p>✓ Free delivery for orders above ₱200</p>
                    <p>✓ Pickup within 24 hours</p>
                    <p>✓ Delivery in 2–3 days</p>
                  </div>
                </CardContent>
              </Card>

              <Button
                onClick={handleNext}
                className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg font-medium"
              >
                Continue to Schedule
              </Button>

              <Button
                onClick={() => navigate("/customer")}
                variant="outline"
                className="w-full border-slate-300"
              >
                Cancel
              </Button>
            </div>
          </motion.div>

        </div>
      </div>
    </div>
  );
}
