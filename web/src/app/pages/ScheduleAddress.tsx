import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router";
import { useOrder } from "../contexts/OrderContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import {
  Calendar,
  Clock,
  MapPin,
  Phone,
  Navigation,
  Search,
  X,
  Loader2,
  Map,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

const CEBU: [number, number] = [10.3157, 123.8854];

interface NominatimResult {
  place_id: string;
  display_name: string;
  lat: string;
  lon: string;
}

function osmEmbedUrl(lat: number, lng: number) {
  const delta = 0.008;
  return (
    `https://www.openstreetmap.org/export/embed.html` +
    `?bbox=${lng - delta},${lat - delta},${lng + delta},${lat + delta}` +
    `&layer=mapnik&marker=${lat},${lng}`
  );
}

// ── Main component ────────────────────────────────────────────────────────────
export default function ScheduleAddress() {
  const navigate = useNavigate();
  const { orderData, setOrderData, nextStep, prevStep } = useOrder();

  // ── Date helpers ─────────────────────────────────────────────────────────
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split("T")[0];

  const defaultPickup = new Date();
  defaultPickup.setDate(defaultPickup.getDate() + 2);
  const defaultPickupStr = defaultPickup.toISOString().split("T")[0];

  const defaultDelivery = new Date(defaultPickup);
  defaultDelivery.setDate(defaultDelivery.getDate() + 2);
  const defaultDeliveryStr = defaultDelivery.toISOString().split("T")[0];

  const timeSlots = [
    "08:00 AM – 10:00 AM",
    "10:00 AM – 12:00 PM",
    "02:00 PM – 04:00 PM",
    "04:00 PM – 06:00 PM",
  ];

  // ── Address / map state ───────────────────────────────────────────────────
  const [coords, setCoords] = useState<[number, number]>(
    orderData.lat && orderData.lng ? [orderData.lat, orderData.lng] : CEBU
  );
  const [hasPin, setHasPin] = useState(!!(orderData.lat && orderData.lng));
  const [searchQuery, setSearchQuery] = useState(orderData.address ?? "");
  const [suggestions, setSuggestions] = useState<NominatimResult[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [isLocating, setIsLocating] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const searchRef = useRef<HTMLDivElement>(null);

  // ── Close dropdown on outside click ──────────────────────────────────────
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(e.target as Node))
        setShowSuggestions(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  // ── Nominatim autocomplete (500 ms debounce, Philippines only) ───────────
  useEffect(() => {
    if (searchQuery.length < 3) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }
    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const res = await fetch(
          `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
            searchQuery
          )}&format=json&limit=5&countrycodes=ph&addressdetails=1`,
          { headers: { "Accept-Language": "en", "User-Agent": "WashMate/1.0" } }
        );
        const data: NominatimResult[] = await res.json();
        setSuggestions(data);
        setShowSuggestions(data.length > 0);
      } catch {
        setSuggestions([]);
      } finally {
        setIsSearching(false);
      }
    }, 500);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  // ── Reverse geocode lat/lng → readable address ────────────────────────────
  const reverseGeocode = useCallback(async (lat: number, lng: number) => {
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`,
        { headers: { "User-Agent": "WashMate/1.0" } }
      );
      const data = await res.json();
      return (data.display_name as string) ?? "";
    } catch {
      return "";
    }
  }, []);

  const applyLocation = useCallback(
    (lat: number, lng: number, address: string) => {
      setCoords([lat, lng]);
      setHasPin(true);
      setSearchQuery(address);
      setSuggestions([]);
      setShowSuggestions(false);
      setOrderData({ address, lat, lng });
      setErrors((prev) => ({ ...prev, address: "" }));
    },
    [setOrderData]
  );

  const handleSuggestionSelect = (result: NominatimResult) =>
    applyLocation(
      parseFloat(result.lat),
      parseFloat(result.lon),
      result.display_name
    );

  const handleUseCurrentLocation = () => {
    if (!navigator.geolocation) {
      alert("Geolocation is not supported by your browser.");
      return;
    }
    setIsLocating(true);
    navigator.geolocation.getCurrentPosition(
      async ({ coords: { latitude, longitude } }) => {
        const address = await reverseGeocode(latitude, longitude);
        applyLocation(latitude, longitude, address);
        setIsLocating(false);
      },
      () => {
        alert("Could not get your location. Please enable location access.");
        setIsLocating(false);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  // ── Validation & next ─────────────────────────────────────────────────────
  const handleNext = () => {
    const newErrors: Record<string, string> = {};
    if (!orderData.address) newErrors.address = "Please select or enter an address.";
    if (!orderData.phoneNumber) newErrors.phone = "Phone number is required.";
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    nextStep();
    navigate("/order/payment-review");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12">
      <div className="max-w-4xl mx-auto px-4 space-y-8">

        {/* ── Step header ── */}
        <motion.div initial={{ opacity: 0, y: -16 }} animate={{ opacity: 1, y: 0 }} className="space-y-2">
          <div className="flex items-center gap-4">
            <div className="flex items-center justify-center w-10 h-10 rounded-full bg-teal-600 text-white font-bold shrink-0">2</div>
            <div>
              <h1 className="text-3xl font-bold text-slate-900">Schedule &amp; Address</h1>
              <p className="text-slate-500 text-sm">Set pickup &amp; delivery times, then pin your location</p>
            </div>
          </div>
        </motion.div>

        {/* ── Stepper ── */}
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex items-center">
          {[
            { num: 1, label: "Laundry Details" },
            { num: 2, label: "Schedule & Address" },
            { num: 3, label: "Payment & Review" },
          ].map((step, i, arr) => (
            <div key={step.num} className="flex items-center flex-1 last:flex-none">
              <div className="flex flex-col items-center gap-1 shrink-0">
                <div className={`w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm border-2 transition-all ${step.num <= 2 ? "bg-teal-600 border-teal-600 text-white" : "bg-white border-slate-300 text-slate-400"}`}>
                  {step.num}
                </div>
                <span className={`text-xs font-medium whitespace-nowrap ${step.num <= 2 ? "text-teal-700" : "text-slate-400"}`}>{step.label}</span>
              </div>
              {i < arr.length - 1 && (
                <div className={`flex-1 h-0.5 mb-5 mx-2 ${step.num < 2 ? "bg-teal-500" : "bg-slate-200"}`} />
              )}
            </div>
          ))}
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">

            {/* ── Pickup Schedule ── */}
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Calendar className="w-5 h-5 text-blue-600" /> Pickup Schedule
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">Pickup Date</label>
                      <Input type="date" min={minDate} value={orderData.pickupDate || defaultPickupStr} onChange={(e) => setOrderData({ pickupDate: e.target.value })} />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">Preferred Time</label>
                      <select value={orderData.pickupTime || timeSlots[0]} onChange={(e) => setOrderData({ pickupTime: e.target.value })} className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                        {timeSlots.map((s) => <option key={s} value={s}>{s}</option>)}
                      </select>
                    </div>
                  </div>
                  <div className="bg-blue-50 rounded-lg p-3 flex items-start gap-2">
                    <Clock className="w-4 h-4 text-blue-600 mt-0.5 shrink-0" />
                    <p className="text-sm text-blue-800">Our rider will pick up within the selected time window.</p>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* ── Delivery Schedule ── */}
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }}>
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Calendar className="w-5 h-5 text-emerald-600" /> Delivery Schedule
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">Delivery Date</label>
                      <Input type="date" min={orderData.pickupDate || defaultPickupStr} value={orderData.deliveryDate || defaultDeliveryStr} onChange={(e) => setOrderData({ deliveryDate: e.target.value })} />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">Preferred Time</label>
                      <select value={orderData.deliveryTime || timeSlots[0]} onChange={(e) => setOrderData({ deliveryTime: e.target.value })} className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
                        {timeSlots.map((s) => <option key={s} value={s}>{s}</option>)}
                      </select>
                    </div>
                  </div>
                  <div className="bg-emerald-50 rounded-lg p-3 flex items-start gap-2">
                    <Clock className="w-4 h-4 text-emerald-600 mt-0.5 shrink-0" />
                    <p className="text-sm text-emerald-800">Your clean laundry will be delivered within the selected window.</p>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* ── Address + Map ── */}
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
              <Card className="border-slate-200 shadow-sm overflow-hidden">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <MapPin className="w-5 h-5 text-teal-600" /> Pickup &amp; Delivery Address
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">

                  {/* Search row */}
                  <div ref={searchRef} className="relative">
                    <div className="flex gap-2">
                      {/* Search input */}
                      <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                        <input
                          type="text"
                          placeholder="Search your address…"
                          value={searchQuery}
                          onChange={(e) => {
                            setSearchQuery(e.target.value);
                            if (!e.target.value) {
                              setOrderData({ address: "", lat: undefined, lng: undefined });
                              setHasPin(false);
                            }
                          }}
                          onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
                          className={`w-full pl-9 pr-9 py-2.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500 transition-colors ${errors.address ? "border-red-400 bg-red-50" : "border-slate-300"}`}
                        />
                        {searchQuery && !isSearching && (
                          <button
                            onClick={() => {
                              setSearchQuery("");
                              setSuggestions([]);
                              setShowSuggestions(false);
                              setHasPin(false);
                              setOrderData({ address: "", lat: undefined, lng: undefined });
                            }}
                            className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        )}
                        {isSearching && (
                          <Loader2 className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-teal-500 animate-spin pointer-events-none" />
                        )}
                      </div>

                      {/* My location button */}
                      <button
                        onClick={handleUseCurrentLocation}
                        disabled={isLocating}
                        title="Use my current location"
                        className="shrink-0 flex items-center gap-1.5 px-3 py-2.5 bg-teal-600 hover:bg-teal-700 disabled:bg-teal-400 text-white rounded-lg text-sm font-medium transition-colors"
                      >
                        {isLocating
                          ? <Loader2 className="w-4 h-4 animate-spin" />
                          : <Navigation className="w-4 h-4" />
                        }
                        <span className="hidden sm:inline">{isLocating ? "Locating…" : "My Location"}</span>
                      </button>
                    </div>

                    {/* Autocomplete dropdown */}
                    <AnimatePresence>
                      {showSuggestions && suggestions.length > 0 && (
                        <motion.ul
                          initial={{ opacity: 0, y: -4 }}
                          animate={{ opacity: 1, y: 0 }}
                          exit={{ opacity: 0, y: -4 }}
                          transition={{ duration: 0.15 }}
                          className="absolute z-[9999] top-full left-0 right-14 mt-1 bg-white border border-slate-200 rounded-xl shadow-xl overflow-hidden"
                        >
                          {suggestions.map((s) => (
                            <li key={s.place_id}>
                              <button
                                onMouseDown={(e) => e.preventDefault()}
                                onClick={() => handleSuggestionSelect(s)}
                                className="w-full flex items-start gap-3 px-4 py-3 hover:bg-teal-50 text-left transition-colors border-b border-slate-100 last:border-0"
                              >
                                <MapPin className="w-4 h-4 text-teal-500 mt-0.5 shrink-0" />
                                <span className="text-sm text-slate-700 line-clamp-2">{s.display_name}</span>
                              </button>
                            </li>
                          ))}
                        </motion.ul>
                      )}
                    </AnimatePresence>

                    {errors.address && (
                      <p className="mt-1.5 text-xs text-red-600">{errors.address}</p>
                    )}
                  </div>

                  {/* Map — OSM iframe when pinned, placeholder otherwise */}
                  <div className="rounded-xl overflow-hidden border border-slate-200" style={{ height: 260 }}>
                    {hasPin ? (
                      <iframe
                        key={`${coords[0]},${coords[1]}`}
                        src={osmEmbedUrl(coords[0], coords[1])}
                        width="100%"
                        height="100%"
                        style={{ border: 0 }}
                        loading="lazy"
                        title="Location map"
                      />
                    ) : (
                      <div className="w-full h-full bg-slate-100 flex flex-col items-center justify-center gap-3 text-slate-400">
                        <Map className="w-10 h-10" />
                        <p className="text-sm font-medium">Search or use your location to pin the map</p>
                      </div>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 flex items-center gap-1">
                    <MapPin className="w-3 h-3 shrink-0" />
                    Search an address or hit "My Location" to drop a pin
                  </p>

                  {/* Phone + Notes */}
                  <div className="pt-1 space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        <span className="flex items-center gap-1.5">
                          <Phone className="w-4 h-4 text-slate-500" /> Phone Number *
                        </span>
                      </label>
                      <div className="flex gap-2">
                        <span className="flex items-center px-3 bg-slate-100 border border-slate-300 rounded-lg text-sm text-slate-600 font-medium whitespace-nowrap">
                          +63
                        </span>
                        <Input
                          type="tel"
                          placeholder="9XX XXX XXXX"
                          value={orderData.phoneNumber || ""}
                          onChange={(e) => {
                            setOrderData({ phoneNumber: e.target.value });
                            setErrors((prev) => ({ ...prev, phone: "" }));
                          }}
                          className={errors.phone ? "border-red-400 bg-red-50" : ""}
                        />
                      </div>
                      {errors.phone && <p className="mt-1 text-xs text-red-600">{errors.phone}</p>}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Additional Notes <span className="font-normal text-slate-400">(Optional)</span>
                      </label>
                      <textarea
                        placeholder="E.g., gate code, landmark, floor number…"
                        value={orderData.notes || ""}
                        onChange={(e) => setOrderData({ notes: e.target.value })}
                        className="w-full p-3 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none h-20"
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* ── Sidebar ── */}
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25 }} className="lg:col-span-1">
            <div className="sticky top-24 space-y-4">
              <Card className="border-teal-200 shadow-sm bg-gradient-to-br from-teal-50 to-teal-100">
                <CardHeader className="pb-3">
                  <CardTitle className="text-lg text-teal-900">Timeline</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex gap-3">
                    <div className="flex flex-col items-center shrink-0">
                      <div className="w-3 h-3 rounded-full bg-blue-600" />
                      <div className="w-0.5 h-12 bg-teal-300 my-1" />
                    </div>
                    <div>
                      <p className="font-semibold text-slate-900 text-sm">Pickup</p>
                      <p className="text-xs text-slate-600">{orderData.pickupDate || defaultPickupStr}</p>
                      <p className="text-xs text-slate-500">{orderData.pickupTime || timeSlots[0]}</p>
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <div className="shrink-0 mt-0.5">
                      <div className="w-3 h-3 rounded-full bg-emerald-600" />
                    </div>
                    <div>
                      <p className="font-semibold text-slate-900 text-sm">Delivery</p>
                      <p className="text-xs text-slate-600">{orderData.deliveryDate || defaultDeliveryStr}</p>
                      <p className="text-xs text-slate-500">{orderData.deliveryTime || timeSlots[0]}</p>
                    </div>
                  </div>

                  {hasPin && orderData.address && (
                    <div className="bg-white rounded-xl p-3 border border-teal-200">
                      <p className="text-xs font-semibold text-teal-700 flex items-center gap-1 mb-1">
                        <MapPin className="w-3 h-3" /> Location pinned
                      </p>
                      <p className="text-xs text-slate-600 line-clamp-2">{orderData.address}</p>
                    </div>
                  )}

                  <div className="border-t border-teal-200 pt-4">
                    <p className="text-xs text-teal-700 font-medium">Estimated Cost</p>
                    <p className="text-3xl font-bold text-teal-700">
                      ₱{(orderData.estimatedPrice || 0).toFixed(2)}
                    </p>
                  </div>
                </CardContent>
              </Card>

              <Button onClick={handleNext} className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg font-medium">
                Continue to Payment
              </Button>
              <Button onClick={() => { prevStep(); navigate("/order/laundry-details"); }} variant="outline" className="w-full border-slate-300">
                Back
              </Button>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
