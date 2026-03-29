import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { useOrder, ServiceType } from "../contexts/OrderContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { Settings, WashingMachine, FoldHorizontal, Zap, Wind, Check, AlertCircle, Plus, Minus, Crown, Loader } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { getMySubscription, type SubscriptionData } from "../services/subscription";
import { getServices, type ServiceResponse } from "../services/service";

// ── Service UI definitions (icons and colors, separate from API data) ───────
const SERVICE_UI_CONFIG: Record<string, {
  icon: React.ElementType;
  description: string;
  accentBorder: string;
  accentShadow: string;
  accentBg: string;
  accentIcon: string;
  accentText: string;
  checkBg: string;
}> = {
  "wash-dry-fold": {
    icon: WashingMachine,
    description: "Deep clean with eco-friendly detergents, colour-sorted for fabric safety.",
    accentBorder: "border-blue-500",
    accentShadow: "shadow-blue-100",
    accentBg: "bg-blue-50",
    accentIcon: "bg-blue-100",
    accentText: "text-blue-600",
    checkBg: "bg-blue-500",
  },
  "iron": {
    icon: Zap,
    description: "Professional steam-pressing for a crisp, wrinkle-free finish.",
    accentBorder: "border-amber-500",
    accentShadow: "shadow-amber-100",
    accentBg: "bg-amber-50",
    accentIcon: "bg-amber-100",
    accentText: "text-amber-600",
    checkBg: "bg-amber-500",
  },
  "dry-clean": {
    icon: Wind,
    description: "Specialist solvent cleaning for delicate fabrics, suits and dress wear.",
    accentBorder: "border-purple-500",
    accentShadow: "shadow-purple-100",
    accentBg: "bg-purple-50",
    accentIcon: "bg-purple-100",
    accentText: "text-purple-600",
    checkBg: "bg-purple-500",
  },
};

// ── Component ─────────────────────────────────────────────────────────────────
export default function LaundryDetails() {
  const navigate = useNavigate();
  const { orderData, setOrderData, nextStep } = useOrder();

  const [services, setServices] = useState<ServiceResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [localWeight, setLocalWeight] = useState(orderData.weight ?? 5);
  const [pieceQty, setPieceQty] = useState<Record<string, number>>(
    orderData.serviceQuantities ?? {}
  );
  const [selectedVariants, setSelectedVariants] = useState<Record<number, number>>(
    orderData.selectedVariants ?? {}
  );
  const [showError, setShowError] = useState(false);
  const [subscription, setSubscription] = useState<SubscriptionData | null>(null);

  // Fetch services from API on mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [servicesData, subData] = await Promise.all([
          getServices(),
          getMySubscription(),
        ]);
        setServices(servicesData);
        setSubscription(subData);

        // Store services in OrderContext for later use in submitOrder
        setOrderData({ availableServices: servicesData });

        // Auto-select services marked as auto-selected
        const autoSelected = new Map<string, number>();
        servicesData.forEach((svc) => {
          if (svc.isAutoSelected) {
            const qty = svc.unitType === "kg" ? localWeight : 1;
            autoSelected.set(svc.serviceName, qty);
          }
        });
        if (autoSelected.size > 0) {
          setOrderData({ selectedServices: autoSelected });
        }
      } catch (error) {
        console.error("Failed to fetch services:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const isPremium = subscription?.planType === "PREMIUM";
  const discountPct = isPremium ? (subscription?.discountPercentage ?? 15) : 0;

  const adjustPiece = (serviceName: string, delta: number) => {
    const newQty = Math.max(1, (pieceQty[serviceName] ?? 1) + delta);
    const updated = {
      ...pieceQty,
      [serviceName]: newQty,
    };
    setPieceQty(updated);

    // Also update selectedServices Map (this is what gets submitted to backend)
    const updatedSelected = new Map(selected);
    if (updatedSelected.has(serviceName)) {
      updatedSelected.set(serviceName, newQty);
    }

    // Persist both to OrderContext
    setOrderData({
      serviceQuantities: updated,
      selectedServices: updatedSelected
    });
  };

  // ── Determine UI display name for service ─────────────────────────────────────
  const getDisplayName = (service: ServiceResponse): string => {
    if (service.serviceName === "Wash & Dry & Fold") {
      return "Wash & Dry & Fold";
    }
    return service.serviceName;
  };

  // ── Get UI config for service ────────────────────────────────────────────────
  const getServiceUI = (service: ServiceResponse) => {
    if (service.serviceName === "Wash & Dry & Fold") {
      return SERVICE_UI_CONFIG["wash-dry-fold"];
    } else if (service.serviceName === "Iron") {
      return SERVICE_UI_CONFIG["iron"];
    } else if (service.serviceName === "Dry Clean") {
      return SERVICE_UI_CONFIG["dry-clean"];
    }
    return SERVICE_UI_CONFIG["wash-dry-fold"]; // Default fallback
  };

  // ── Toggle service selection (prevent auto-selected from being deselected) ────
  const selected = orderData.selectedServices ?? new Map();

  const toggleService = (service: ServiceResponse) => {
    setShowError(false);

    // Prevent deselecting auto-selected services
    if (service.isAutoSelected && selected.has(service.serviceName)) {
      return;
    }

    const newSelected = new Map(selected);
    const newVariants = { ...selectedVariants };

    if (newSelected.has(service.serviceName)) {
      // Deselecting the service
      newSelected.delete(service.serviceName);
      // Clear associated variants when service is deselected
      if (service.hasVariants) {
        delete newVariants[service.serviceId];
      }
    } else {
      // Selecting the service
      const qty = service.unitType === "kg" ? localWeight : 1;
      newSelected.set(service.serviceName, qty);
      // Auto-select first variant if service has variants
      if (service.hasVariants && service.variants && service.variants.length > 0) {
        newVariants[service.serviceId] = service.variants[0].variantId;
      }
    }

    setOrderData({ selectedServices: newSelected });
    if (Object.keys(newVariants).length > 0 || service.hasVariants) {
      setSelectedVariants(newVariants);
    }
  };

  // ── Calculate line items from selected services ────────────────────────────────
  const lineItems = services
    .filter((s) => selected.has(s.serviceName))
    .map((s) => {
      const qty = s.unitType === "kg" ? localWeight : (pieceQty[s.serviceName] ?? 1);

      // Determine price (base or variant)
      let price = s.basePricePerUnit;
      if (s.hasVariants && selectedVariants[s.serviceId]) {
        const selectedVariant = s.variants?.find(
          (v) => v.variantId === selectedVariants[s.serviceId]
        );
        if (selectedVariant) {
          price = selectedVariant.variantPrice;
        }
      }

      return {
        serviceName: s.serviceName,
        unitType: s.unitType,
        qty,
        price,
        subtotal: price * qty,
        icon: getServiceUI(s).icon,
        accentText: getServiceUI(s).accentText,
      };
    });

  // ── Calculate delivery fee (conditional) ────────────────────────────────────
  // Exclude delivery from subtotal for fee calculation
  const subtotalWithoutDelivery = lineItems
    .filter(li => li.serviceName !== "Delivery")
    .reduce((sum, li) => sum + li.subtotal, 0);

  // Free delivery if: premium user OR order >= ₱400
  const isEligibleForFreeDelivery = isPremium || subtotalWithoutDelivery >= 400;
  const deliveryFee = isEligibleForFreeDelivery ? 0 : 50;

  // Calculate final totals
  const subtotalAmount = subtotalWithoutDelivery + deliveryFee;
  const discountAmount = Math.round(subtotalAmount * discountPct / 100);
  const estimatedPrice = subtotalAmount - discountAmount;

  // ── Validation: at least one service ────────────────────────────────────────
  // Variants are now auto-selected when service is selected, so no need to validate them separately

  // ── Proceed to next step ───────────────────────────────────────────────────
  const handleNext = () => {
    if (selected.size === 0) {
      setShowError(true);
      return;
    }
    setOrderData({
      weight: localWeight,
      serviceQuantities: pieceQty,
      selectedVariants,
      estimatedPrice,
      subtotal: subtotalAmount,
      deliveryFee,  // Include calculated delivery fee
      discountPercentage: discountPct,
      discountAmount,
      isRushOrder: isPremium,
    });
    nextStep();
    navigate("/order/schedule-address");
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12 flex items-center justify-center">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
        >
          <Loader className="w-8 h-8 text-teal-600" />
        </motion.div>
      </div>
    );
  }

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

                  {services.map((service) => {
                    const ui = getServiceUI(service);
                    const Icon = ui.icon;
                    const isSelected = selected.has(service.serviceName);
                    const isAutoSelected = service.isAutoSelected;

                    return (
                      <motion.button
                        key={service.serviceId}
                        onClick={() => toggleService(service)}
                        whileTap={{ scale: 0.985 }}
                        disabled={isAutoSelected && isSelected}
                        className={`
                          w-full p-4 rounded-xl text-left transition-all duration-150 relative
                          ${isSelected
                            ? `border-[3px] ${ui.accentBorder} ${ui.accentBg} shadow-lg ${ui.accentShadow}`
                            : "border-2 border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm"
                          }
                          ${isAutoSelected && isSelected ? "cursor-default" : "cursor-pointer"}
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
                              className={`absolute top-3 right-3 w-6 h-6 rounded-full ${ui.checkBg} flex items-center justify-center shadow-sm`}
                            >
                              <Check className="w-3.5 h-3.5 text-white" strokeWidth={3} />
                            </motion.span>
                          )}
                        </AnimatePresence>

                        <div className="flex items-center gap-4 pr-8">
                          {/* Icon */}
                          <div
                            className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 transition-colors duration-150
                              ${isSelected ? `${ui.accentIcon}` : "bg-slate-100"}`}
                          >
                            <Icon
                              className={`w-6 h-6 transition-colors duration-150
                                ${isSelected ? ui.accentText : "text-slate-500"}`}
                            />
                          </div>

                          {/* Text */}
                          <div className="flex-1">
                            <div className="flex items-baseline gap-2 mb-0.5">
                              <span className={`font-bold text-base transition-colors duration-150 ${isSelected ? "text-slate-900" : "text-slate-700"}`}>
                                {getDisplayName(service)}
                              </span>
                              <span className={`text-sm font-semibold transition-colors duration-150 ${isSelected ? ui.accentText : "text-slate-400"}`}>
                                {service.hasVariants ? (
                                  selectedVariants[service.serviceId] ? (
                                    `₱${service.variants?.find(v => v.variantId === selectedVariants[service.serviceId])?.variantPrice.toFixed(2)}/piece`
                                  ) : (
                                    "Select variant for pricing"
                                  )
                                ) : (
                                  `₱${service.basePricePerUnit.toFixed(2)}/${service.unitType}`
                                )}
                              </span>
                            </div>
                            <p className="text-sm text-slate-500 leading-snug">{ui.description}</p>
                            {isAutoSelected && (
                              <p className="text-xs text-teal-600 font-medium mt-1">Auto-selected with subscription</p>
                            )}
                          </div>
                        </div>

                        {/* Piece quantity controls — only for piece-based services */}
                        {isSelected && service.unitType === "piece" && (
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
                                onClick={(e) => { e.stopPropagation(); adjustPiece(service.serviceName, -1); }}
                                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center cursor-pointer transition-colors"
                              >
                                <Minus className="w-4 h-4 text-slate-600" />
                              </div>
                              <span className="text-lg font-bold text-slate-900 w-6 text-center">
                                {pieceQty[service.serviceName] ?? 1}
                              </span>
                              <div
                                role="button"
                                tabIndex={0}
                                onPointerDown={(e) => e.stopPropagation()}
                                onClick={(e) => { e.stopPropagation(); adjustPiece(service.serviceName, 1); }}
                                className="w-8 h-8 rounded-full bg-teal-100 hover:bg-teal-200 flex items-center justify-center cursor-pointer transition-colors"
                              >
                                <Plus className="w-4 h-4 text-teal-700" />
                              </div>
                              <span className="text-sm font-semibold text-teal-700 min-w-[56px] text-right">
                                ₱{((() => {
                                  // Determine price: use variant if available, otherwise base price
                                  if (service.hasVariants && selectedVariants[service.serviceId]) {
                                    const variant = service.variants?.find(v => v.variantId === selectedVariants[service.serviceId]);
                                    return (variant?.variantPrice ?? service.basePricePerUnit) * (pieceQty[service.serviceName] ?? 1);
                                  }
                                  return service.basePricePerUnit * (pieceQty[service.serviceName] ?? 1);
                                })()).toFixed(2)}
                              </span>
                            </div>
                          </div>
                        )}

                        {/* Variant selector for Dry Clean */}
                        {isSelected && service.hasVariants && service.variants && service.variants.length > 0 && (
                          <div
                            className="mt-3 bg-white rounded-xl px-4 py-3 border border-slate-200"
                            onPointerDown={(e) => e.stopPropagation()}
                            onClick={(e) => e.stopPropagation()}
                          >
                            <p className="text-sm font-medium text-slate-700 mb-2">Select item type (required)</p>
                            <div className="space-y-2">
                              {service.variants.map((variant) => (
                                <label key={variant.variantId} className="flex items-center gap-3 cursor-pointer p-2 rounded-lg hover:bg-slate-50 transition-colors">
                                  <input
                                    type="radio"
                                    name={`variant-${service.serviceId}`}
                                    checked={selectedVariants[service.serviceId] === variant.variantId}
                                    onChange={() => {
                                      const updated = {
                                        ...selectedVariants,
                                        [service.serviceId]: variant.variantId,
                                      };
                                      setSelectedVariants(updated);
                                      // Persist variant selection to OrderContext
                                      setOrderData({ selectedVariants: updated });
                                    }}
                                    className="w-4 h-4 text-teal-600"
                                  />
                                  <span className="flex-1 text-sm text-slate-700">{variant.variantName}</span>
                                  <span className="text-sm font-semibold text-teal-700">₱{variant.variantPrice.toFixed(2)}</span>
                                </label>
                              ))}
                            </div>
                          </div>
                        )}
                      </motion.button>
                    );
                  })}

                  {/* Selected summary pill */}
                  <AnimatePresence>
                    {selected.size > 0 && (
                      <motion.p
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="text-xs text-slate-500 text-center pt-1"
                      >
                        {selected.size} service{selected.size > 1 ? "s" : ""} selected
                        {" — "}
                        {Array.from(selected.keys()).join(" + ")}
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
                  <p className="text-sm text-slate-500 mt-0.5">For Wash &amp; Dry &amp; Fold — priced per kg</p>
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
                        const newWeight = Number(e.target.value);
                        setLocalWeight(newWeight);

                        // CRITICAL: Update selectedServices in OrderContext using orderData directly
                        // (not the closure-captured 'selected' which is stale)
                        const updatedSelected = new Map(orderData.selectedServices ?? new Map());
                        if (updatedSelected.has("Wash & Dry & Fold")) {
                          updatedSelected.set("Wash & Dry & Fold", newWeight);
                        }

                        // Single call to ensure both weight and selectedServices are updated together
                        setOrderData({
                          weight: newWeight,
                          selectedServices: updatedSelected
                        });
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
                          <li key={li.serviceName} className="flex items-center justify-between text-sm">
                            <span className="flex items-center gap-2 text-teal-800">
                              <Icon className={`w-4 h-4 ${li.accentText}`} />
                              {li.serviceName}
                              <span className="text-teal-600 text-xs">× {li.qty} {li.unitType}{li.unitType === "piece" && li.qty !== 1 ? "s" : ""}</span>
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

                  {/* Delivery fee info and upsell message */}
                  {lineItems.length > 0 && (
                    <div className="space-y-2">
                      {/* Show delivery fee status */}
                      <div className="flex justify-between text-sm text-slate-600 px-2">
                        <span>Delivery Fee:</span>
                        <span className={deliveryFee === 0 ? "text-teal-600 font-semibold" : "text-slate-700"}>
                          {deliveryFee === 0 ? "FREE ✓" : `₱${deliveryFee.toFixed(2)}`}
                        </span>
                      </div>

                      {/* Upsell message when applicable */}
                      {!isPremium && subtotalWithoutDelivery > 0 && subtotalWithoutDelivery < 400 && (
                        <motion.div
                          initial={{ opacity: 0, y: -8 }}
                          animate={{ opacity: 1, y: 0 }}
                          className="bg-teal-50 border border-teal-200 rounded-lg px-3 py-2 text-xs font-medium text-teal-700"
                        >
                          Add ₱{(400 - subtotalWithoutDelivery).toFixed(2)} more to get FREE delivery
                        </motion.div>
                      )}
                    </div>
                  )}

                  {isPremium && (
                    <div className="flex items-center gap-2 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 text-xs font-semibold text-amber-700">
                      <Zap className="w-3.5 h-3.5 shrink-0" />
                      Priority order processing included
                    </div>
                  )}

                  <div className="bg-white rounded-lg p-3 space-y-1 text-xs text-slate-600">
                    <p>✓ {isPremium ? "FREE delivery (Premium)" : "Free delivery for orders above ₱400"}</p>
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
