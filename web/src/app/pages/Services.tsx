import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { motion, AnimatePresence } from "motion/react";
import {
  WashingMachine,
  FoldHorizontal,
  Zap,
  Wind,
  Plus,
  Minus,
  Check,
  Crown,
  ShoppingBag,
  ArrowRight,
  Star,
  Sparkles,
  Shield,
  Clock,
  Truck,
  Headphones,
  BadgePercent,
} from "lucide-react";
import { Button } from "../components/Button";
import { getMySubscription, SubscriptionData } from "../services/subscription";

// ── Service definitions ──────────────────────────────────────────────────────
type Unit = "kg" | "piece";

interface ServiceDef {
  id: string;
  name: string;
  description: string;
  price: number;
  unit: Unit;
  icon: React.ElementType;
  color: string;
  bg: string;
  detail: string;
}

const SERVICE_LIST: ServiceDef[] = [
  {
    id: "wash",
    name: "Wash",
    description: "Deep clean using premium eco-friendly detergents sorted by fabric type and colour.",
    price: 35,
    unit: "kg",
    icon: WashingMachine,
    color: "text-blue-600",
    bg: "bg-blue-50 border-blue-200",
    detail: "Eco-friendly detergents · Colour-safe wash · Gentle spin cycle",
  },
  {
    id: "fold",
    name: "Fold",
    description: "Neatly sorted, KonMari-style folding and packaging so your wardrobe stays organised.",
    price: 20,
    unit: "kg",
    icon: FoldHorizontal,
    color: "text-teal-600",
    bg: "bg-teal-50 border-teal-200",
    detail: "KonMari fold · Packaged by outfit · Colour-sorted",
  },
  {
    id: "iron",
    name: "Iron",
    description: "Professional steam-pressing for a crisp, wrinkle-free finish every time.",
    price: 25,
    unit: "piece",
    icon: Zap,
    color: "text-amber-600",
    bg: "bg-amber-50 border-amber-200",
    detail: "Steam-pressed · Hanger included · Wrinkle-free guarantee",
  },
  {
    id: "dry_clean",
    name: "Dry Clean",
    description: "Specialist solvent-based cleaning for delicate fabrics, suits, and dress wear.",
    price: 150,
    unit: "piece",
    icon: Wind,
    color: "text-purple-600",
    bg: "bg-purple-50 border-purple-200",
    detail: "Solvent cleaning · Stain removal · Premium finishing",
  },
];

// ── Subscription plan definitions ────────────────────────────────────────────
const PLANS = [
  {
    id: "free",
    name: "Free",
    badge: null,
    price: 0,
    billing: "forever free",
    description: "Perfect for occasional laundry needs.",
    highlight: false,
    features: [
      { label: "Up to 3 orders per month", included: true },
      { label: "Standard 2–3 day service", included: true },
      { label: "Basic customer support", included: true },
      { label: "Regular pickup schedule", included: true },
      { label: "Priority same-day pickup", included: false },
      { label: "10% discount on all services", included: false },
      { label: "Dedicated account manager", included: false },
      { label: "Monthly usage report", included: false },
    ],
    cta: "Get Started Free",
    ctaVariant: "outline" as const,
  },
  {
    id: "premium",
    name: "Premium",
    badge: "Most Popular",
    price: 299,
    billing: "per month",
    description: "For busy households that need fast, reliable service every week.",
    highlight: true,
    features: [
      { label: "Unlimited orders per month", included: true },
      { label: "Priority same-day service", included: true },
      { label: "24/7 dedicated support", included: true },
      { label: "Free express pickup", included: true },
      { label: "Priority same-day pickup", included: true },
      { label: "10% discount on all services", included: true },
      { label: "Dedicated account manager", included: true },
      { label: "Monthly usage report", included: true },
    ],
    cta: "Start Premium",
    ctaVariant: "default" as const,
  },
];

// ── Component ─────────────────────────────────────────────────────────────────
export default function Services() {
  const navigate = useNavigate();

  const [subscription, setSubscription] = useState<SubscriptionData | null>(null);

  useEffect(() => {
    getMySubscription().then(setSubscription);
  }, []);

  const currentPlanId = subscription?.planType?.toLowerCase();

  // selected + qty per service
  const [selected, setSelected] = useState<Record<string, number>>({});

  const toggle = (id: string) => {
    setSelected((prev) => {
      if (prev[id] !== undefined) {
        const next = { ...prev };
        delete next[id];
        return next;
      }
      return { ...prev, [id]: 1 };
    });
  };

  const adjust = (id: string, delta: number) => {
    setSelected((prev) => {
      const current = prev[id] ?? 0;
      const next = Math.max(1, current + delta);
      return { ...prev, [id]: next };
    });
  };

  const selectedServices = SERVICE_LIST.filter((s) => selected[s.id] !== undefined);

  const subtotal = selectedServices.reduce((acc, s) => {
    return acc + s.price * (selected[s.id] ?? 0);
  }, 0);

  const handleBook = () => navigate("/order/laundry-details");

  const fadeUp = {
    hidden: { opacity: 0, y: 24 },
    show: { opacity: 1, y: 0, transition: { duration: 0.5 } },
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-20 pb-20">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-20">

        {/* ── Hero ── */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="text-center pt-8"
        >
          <span className="inline-flex items-center gap-2 rounded-full bg-teal-100 text-teal-700 text-sm font-semibold px-4 py-1.5 mb-4">
            <Sparkles className="w-4 h-4" />
            WashMate Services
          </span>
          <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 mb-4 tracking-tight">
            Choose Your <span className="text-teal-600">Services</span>
          </h1>
          <p className="text-slate-500 text-lg max-w-2xl mx-auto">
            Mix and match individual laundry services — select one or more, set the quantity, and see your total instantly.
          </p>
        </motion.div>

        {/* ── Service Cards + Order Summary ── */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* ── Left: Service cards ── */}
          <div className="lg:col-span-2 space-y-4">
            <h2 className="text-xl font-bold text-slate-800 mb-2">Available Services</h2>
            {SERVICE_LIST.map((service, i) => {
              const Icon = service.icon;
              const isSelected = selected[service.id] !== undefined;
              const qty = selected[service.id] ?? 1;

              return (
                <motion.div
                  key={service.id}
                  variants={fadeUp}
                  initial="hidden"
                  animate="show"
                  transition={{ delay: i * 0.08 }}
                  className={`relative rounded-2xl border-2 p-5 transition-all duration-200 cursor-pointer select-none
                    ${isSelected
                      ? `${service.bg} shadow-md`
                      : "bg-white border-slate-200 hover:border-slate-300 hover:shadow-sm"
                    }`}
                  onClick={() => toggle(service.id)}
                >
                  {/* Selected checkmark */}
                  <AnimatePresence>
                    {isSelected && (
                      <motion.div
                        key="check"
                        initial={{ scale: 0 }}
                        animate={{ scale: 1 }}
                        exit={{ scale: 0 }}
                        className="absolute top-4 right-4 w-7 h-7 rounded-full bg-teal-600 flex items-center justify-center shadow"
                      >
                        <Check className="w-4 h-4 text-white" />
                      </motion.div>
                    )}
                  </AnimatePresence>

                  <div className="flex items-start gap-4">
                    {/* Icon */}
                    <div className={`w-14 h-14 rounded-xl flex items-center justify-center shrink-0 ${isSelected ? "bg-white shadow-sm" : "bg-slate-100"}`}>
                      <Icon className={`w-7 h-7 ${isSelected ? service.color : "text-slate-500"}`} />
                    </div>

                    {/* Info */}
                    <div className="flex-1 pr-8">
                      <div className="flex items-center gap-3 mb-1">
                        <h3 className="text-lg font-bold text-slate-900">{service.name}</h3>
                        <span className={`text-sm font-bold ${isSelected ? service.color : "text-slate-500"}`}>
                          ₱{service.price}/{service.unit}
                        </span>
                      </div>
                      <p className="text-sm text-slate-600 mb-2">{service.description}</p>
                      <p className="text-xs text-slate-400">{service.detail}</p>
                    </div>
                  </div>

                  {/* Qty controls — shown when selected */}
                  <AnimatePresence>
                    {isSelected && (
                      <motion.div
                        key="qty"
                        initial={{ opacity: 0, height: 0, marginTop: 0 }}
                        animate={{ opacity: 1, height: "auto", marginTop: 16 }}
                        exit={{ opacity: 0, height: 0, marginTop: 0 }}
                        className="overflow-hidden"
                        onClick={(e) => e.stopPropagation()}
                      >
                        <div className="flex items-center justify-between bg-white rounded-xl px-4 py-3 border border-slate-200 shadow-sm">
                          <span className="text-sm font-medium text-slate-700">
                            Quantity ({service.unit}s)
                          </span>
                          <div className="flex items-center gap-3">
                            <button
                              onClick={() => adjust(service.id, -1)}
                              className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center transition-colors"
                            >
                              <Minus className="w-4 h-4 text-slate-600" />
                            </button>
                            <span className="text-lg font-bold text-slate-900 w-8 text-center">{qty}</span>
                            <button
                              onClick={() => adjust(service.id, 1)}
                              className="w-8 h-8 rounded-full bg-teal-100 hover:bg-teal-200 flex items-center justify-center transition-colors"
                            >
                              <Plus className="w-4 h-4 text-teal-700" />
                            </button>
                            <span className="text-sm font-semibold text-teal-700 ml-2 min-w-[60px] text-right">
                              ₱{(service.price * qty).toFixed(2)}
                            </span>
                          </div>
                        </div>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </motion.div>
              );
            })}
          </div>

          {/* ── Right: Order Summary ── */}
          <div className="lg:col-span-1">
            <div className="sticky top-24">
              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.5, delay: 0.2 }}
                className="bg-white rounded-2xl border border-slate-200 shadow-md overflow-hidden"
              >
                <div className="bg-gradient-to-br from-teal-600 to-teal-700 px-6 py-5">
                  <h3 className="text-white font-bold text-lg">Order Summary</h3>
                  <p className="text-teal-100 text-sm">
                    {selectedServices.length === 0
                      ? "No services selected yet"
                      : `${selectedServices.length} service${selectedServices.length > 1 ? "s" : ""} selected`}
                  </p>
                </div>

                <div className="px-6 py-4 min-h-[160px]">
                  {selectedServices.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-32 text-center">
                      <ShoppingBag className="w-10 h-10 text-slate-200 mb-2" />
                      <p className="text-sm text-slate-400">Tap a service card to add it</p>
                    </div>
                  ) : (
                    <ul className="space-y-3 py-2">
                      {selectedServices.map((s) => (
                        <li key={s.id} className="flex items-center justify-between text-sm">
                          <div className="flex items-center gap-2">
                            <s.icon className={`w-4 h-4 ${s.color}`} />
                            <span className="text-slate-700 font-medium">{s.name}</span>
                            <span className="text-slate-400">×{selected[s.id]}</span>
                          </div>
                          <span className="font-semibold text-slate-800">
                            ₱{(s.price * (selected[s.id] ?? 0)).toFixed(2)}
                          </span>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                <div className="border-t border-slate-100 px-6 py-4 space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-slate-800 text-base">Total</span>
                    <span className="text-2xl font-extrabold text-teal-600">₱{subtotal.toFixed(2)}</span>
                  </div>

                  <Button
                    onClick={handleBook}
                    disabled={selectedServices.length === 0}
                    className="w-full bg-teal-600 hover:bg-teal-700 text-white disabled:opacity-40 disabled:cursor-not-allowed h-11 font-semibold"
                  >
                    Book Selected Services
                    <ArrowRight className="w-4 h-4 ml-2" />
                  </Button>

                  <div className="space-y-1.5 pt-1">
                    {[
                      { icon: Truck, text: "Free pickup on orders ≥ ₱200" },
                      { icon: Clock, text: "Pickup within 24 hours" },
                      { icon: Shield, text: "100% satisfaction guarantee" },
                    ].map(({ icon: Icon, text }) => (
                      <div key={text} className="flex items-center gap-2 text-xs text-slate-400">
                        <Icon className="w-3.5 h-3.5 shrink-0" />
                        {text}
                      </div>
                    ))}
                  </div>
                </div>
              </motion.div>
            </div>
          </div>
        </div>

        {/* ── Subscription Plans ── */}
        <div>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-60px" }}
            transition={{ duration: 0.6 }}
            className="text-center mb-12"
          >
            <span className="inline-flex items-center gap-2 rounded-full bg-amber-100 text-amber-700 text-sm font-semibold px-4 py-1.5 mb-4">
              <Crown className="w-4 h-4" />
              Subscription Plans
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 mb-3 tracking-tight">
              Choose Your Plan
            </h2>
            <p className="text-slate-500 max-w-xl mx-auto">
              Start for free and upgrade anytime. Premium members get exclusive discounts, priority service and more.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-3xl mx-auto">
            {PLANS.map((plan, i) => (
              <motion.div
                key={plan.id}
                initial={{ opacity: 0, y: 24 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: "-50px" }}
                transition={{ duration: 0.5, delay: i * 0.12 }}
                className={`relative rounded-2xl p-8 flex flex-col border-2 transition-all duration-300
                  ${plan.highlight
                    ? "bg-gradient-to-br from-teal-600 to-teal-700 border-teal-500 shadow-2xl shadow-teal-500/20 text-white"
                    : "bg-white border-slate-200 shadow-md text-slate-900"
                  }`}
              >
                {plan.badge && (
                  <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 bg-amber-500 text-white text-xs font-bold px-4 py-1 rounded-full shadow-lg whitespace-nowrap flex items-center gap-1">
                    <Star className="w-3 h-3 fill-white" />
                    {plan.badge}
                  </div>
                )}

                {/* Plan header */}
                <div className="mb-6">
                  <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 ${plan.highlight ? "bg-white/20" : "bg-teal-50"}`}>
                    {plan.highlight
                      ? <Crown className="w-6 h-6 text-white" />
                      : <Sparkles className="w-6 h-6 text-teal-600" />
                    }
                  </div>
                  <h3 className={`text-2xl font-extrabold mb-1 ${plan.highlight ? "text-white" : "text-slate-900"}`}>
                    {plan.name}
                  </h3>
                  <p className={`text-sm mb-4 ${plan.highlight ? "text-teal-100" : "text-slate-500"}`}>
                    {plan.description}
                  </p>
                  <div className="flex items-end gap-1">
                    <span className={`text-4xl font-extrabold ${plan.highlight ? "text-white" : "text-slate-900"}`}>
                      {plan.price === 0 ? "Free" : `₱${plan.price}`}
                    </span>
                    {plan.price > 0 && (
                      <span className={`text-sm mb-1 ${plan.highlight ? "text-teal-100" : "text-slate-400"}`}>
                        /{plan.billing}
                      </span>
                    )}
                  </div>
                </div>

                {/* Feature list */}
                <ul className="space-y-3 flex-1 mb-8">
                  {plan.features.map((feat) => (
                    <li key={feat.label} className="flex items-start gap-3">
                      <div className={`w-5 h-5 rounded-full flex items-center justify-center shrink-0 mt-0.5
                        ${feat.included
                          ? plan.highlight ? "bg-white/20" : "bg-teal-100"
                          : plan.highlight ? "bg-white/10" : "bg-slate-100"
                        }`}
                      >
                        {feat.included
                          ? <Check className={`w-3 h-3 ${plan.highlight ? "text-white" : "text-teal-600"}`} />
                          : <Minus className={`w-3 h-3 ${plan.highlight ? "text-white/40" : "text-slate-300"}`} />
                        }
                      </div>
                      <span className={`text-sm ${feat.included
                        ? plan.highlight ? "text-white" : "text-slate-700"
                        : plan.highlight ? "text-white/40" : "text-slate-300 line-through"
                      }`}>
                        {feat.label}
                      </span>
                    </li>
                  ))}
                </ul>

                {/* CTA */}
                {plan.id === currentPlanId ? (
                  <div className={`w-full h-11 flex items-center justify-center gap-2 rounded-lg font-semibold text-sm
                    ${plan.highlight
                      ? "bg-white/20 text-white border border-white/40"
                      : "bg-teal-50 text-teal-600 border border-teal-200"
                    }`}
                  >
                    <Check className="w-4 h-4" />
                    Current Plan
                  </div>
                ) : currentPlanId && plan.id === "free" ? null : (
                  <Link to="/register">
                    <Button
                      className={`w-full h-11 font-semibold transition-all duration-200
                        ${plan.highlight
                          ? "bg-white text-teal-700 hover:bg-teal-50 shadow-lg"
                          : "bg-teal-600 text-white hover:bg-teal-700"
                        }`}
                    >
                      {plan.cta}
                      <ArrowRight className="w-4 h-4 ml-2" />
                    </Button>
                  </Link>
                )}
              </motion.div>
            ))}
          </div>

          {/* Plan comparison note */}
          <motion.div
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="mt-8 text-center"
          >
            <div className="inline-flex items-center gap-6 bg-white rounded-2xl border border-slate-200 shadow-sm px-8 py-4 flex-wrap justify-center gap-y-3">
              {[
                { icon: Headphones, text: "Cancel anytime" },
                { icon: Shield, text: "No hidden fees" },
                { icon: BadgePercent, text: "Premium saves up to 10% per order" },
              ].map(({ icon: Icon, text }) => (
                <div key={text} className="flex items-center gap-2 text-sm text-slate-500">
                  <Icon className="w-4 h-4 text-teal-600" />
                  {text}
                </div>
              ))}
            </div>
          </motion.div>
        </div>

      </div>
    </div>
  );
}
