import { useEffect, useState } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/features/shared/components/Card";
import { Button } from "@/features/shared/components/Button";
import {
  Tag,
  Edit3,
  Check,
  X,
  Loader2,
  AlertCircle,
  ShoppingBag,
  DollarSign,
  Layers,
  Save,
  RotateCcw,
  Wind,
  Shirt,
  Droplets,
} from "lucide-react";
import { motion } from "motion/react";
import { shopAPI } from "@/features/shared/services/shopAPI";
import type { ServiceResponse, ServiceVariantResponse } from "@/features/shared/services/service";

interface EditingPrice {
  serviceId: number;
  variantId?: number;
  originalPrice: number;
  newPrice: string;
}

export default function ShopServices() {
  const [services, setServices] = useState<ServiceResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingPrices, setEditingPrices] = useState<Map<string, EditingPrice>>(new Map());
  const [savingIds, setSavingIds] = useState<Set<string>>(new Set());

  useEffect(() => {
    fetchServices();
  }, []);

  const fetchServices = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await shopAPI.getAllServices();
      setServices(data);
    } catch (err) {
      console.error("Failed to load services:", err);
      setError("Failed to load services");
    } finally {
      setIsLoading(false);
    }
  };

  const getEditKey = (serviceId: number, variantId?: number) =>
    variantId ? `${serviceId}-${variantId}` : `${serviceId}`;

  const startEditing = (serviceId: number, currentPrice: number, variantId?: number) => {
    const key = getEditKey(serviceId, variantId);
    setEditingPrices((prev) => {
      const next = new Map(prev);
      next.set(key, {
        serviceId,
        variantId,
        originalPrice: currentPrice,
        newPrice: currentPrice.toString(),
      });
      return next;
    });
  };

  const cancelEditing = (serviceId: number, variantId?: number) => {
    const key = getEditKey(serviceId, variantId);
    setEditingPrices((prev) => {
      const next = new Map(prev);
      next.delete(key);
      return next;
    });
  };

  const updateEditingPrice = (key: string, value: string) => {
    setEditingPrices((prev) => {
      const next = new Map(prev);
      const existing = next.get(key);
      if (existing) {
        next.set(key, { ...existing, newPrice: value });
      }
      return next;
    });
  };

  const savePrice = async (key: string) => {
    const editing = editingPrices.get(key);
    if (!editing) return;

    const newPrice = parseFloat(editing.newPrice);
    if (isNaN(newPrice) || newPrice < 0) {
      alert("Please enter a valid price.");
      return;
    }

    setSavingIds((prev) => new Set(prev).add(key));

    try {
      if (editing.variantId) {
        await shopAPI.updateVariantPrice(editing.serviceId, editing.variantId, newPrice);
      } else {
        await shopAPI.updateServicePrice(editing.serviceId, newPrice);
      }

      setServices((prev) =>
        prev.map((s) => {
          if (s.serviceId === editing.serviceId) {
            if (editing.variantId && s.variants) {
              return {
                ...s,
                variants: s.variants.map((v) =>
                  v.variantId === editing.variantId
                    ? { ...v, variantPrice: newPrice }
                    : v
                ),
              };
            }
            return { ...s, basePricePerUnit: newPrice };
          }
          return s;
        })
      );

      cancelEditing(editing.serviceId, editing.variantId);
    } catch (err) {
      console.error("Failed to save price:", err);
      alert("Failed to save price. Please try again.");
    } finally {
      setSavingIds((prev) => {
        const next = new Set(prev);
        next.delete(key);
        return next;
      });
    }
  };

  const getServiceIcon = (name: string) => {
    const lower = name.toLowerCase();
    if (lower.includes("dry")) return <Wind className="w-6 h-6 text-slate-500" />;
    if (lower.includes("iron") || lower.includes("press")) return <Shirt className="w-6 h-6 text-slate-500" />;
    if (lower.includes("wash")) return <Droplets className="w-6 h-6 text-slate-500" />;
    if (lower.includes("fold")) return <Layers className="w-6 h-6 text-slate-500" />;
    return <ShoppingBag className="w-6 h-6 text-slate-500" />;
  };

  if (isLoading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-10 h-10 text-teal-600 mx-auto mb-4 animate-spin" />
          <p className="text-slate-500 font-medium">Loading services…</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 px-4 sm:px-6 lg:px-10 pb-12 pt-20">
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row md:items-center justify-between gap-4"
        >
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-slate-900">Service Pricing</h1>
            <p className="text-sm text-slate-500 mt-1">
              Manage your service catalog and pricing. Click the edit icon to modify prices.
            </p>
          </div>
          <Button variant="outline" className="border-slate-300 gap-2 w-fit" onClick={fetchServices}>
            <RotateCcw className="w-4 h-4" />
            Refresh
          </Button>
        </motion.div>

        {/* Summary Cards */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 sm:grid-cols-3 gap-5"
        >
          <Card className="border-slate-200/80 shadow-sm">
            <CardContent className="p-5 flex items-center gap-4">
              <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-teal-500 to-teal-600 flex items-center justify-center shadow-md shadow-teal-200">
                <ShoppingBag className="w-5 h-5 text-white" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900">{services.length}</p>
                <p className="text-xs text-slate-500">Active Services</p>
              </div>
            </CardContent>
          </Card>
          <Card className="border-slate-200/80 shadow-sm">
            <CardContent className="p-5 flex items-center gap-4">
              <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600 flex items-center justify-center shadow-md shadow-purple-200">
                <Layers className="w-5 h-5 text-white" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900">
                  {services.reduce((sum, s) => sum + (s.variants?.length || 0), 0)}
                </p>
                <p className="text-xs text-slate-500">Variants</p>
              </div>
            </CardContent>
          </Card>
          <Card className="border-slate-200/80 shadow-sm">
            <CardContent className="p-5 flex items-center gap-4">
              <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-600 flex items-center justify-center shadow-md shadow-emerald-200">
                <DollarSign className="w-5 h-5 text-white" />
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-900">
                  ₱{Math.min(...services.map((s) => s.basePricePerUnit)).toFixed(0)}–
                  ₱{Math.max(...services.map((s) => s.basePricePerUnit)).toFixed(0)}
                </p>
                <p className="text-xs text-slate-500">Price Range</p>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Services List */}
        {error ? (
          <Card className="border-red-200 shadow-sm">
            <CardContent className="py-16 text-center">
              <AlertCircle className="w-10 h-10 text-red-400 mx-auto mb-3" />
              <p className="text-red-600 mb-4">{error}</p>
              <Button onClick={fetchServices} variant="outline" className="border-red-300 text-red-600">
                Try Again
              </Button>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {services.map((service, i) => {
              const baseKey = getEditKey(service.serviceId);
              const isEditingBase = editingPrices.has(baseKey);
              const baseEdit = editingPrices.get(baseKey);
              const isSavingBase = savingIds.has(baseKey);

              return (
                <motion.div
                  key={service.serviceId}
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.15 + i * 0.05 }}
                >
                  <Card className="border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
                    <CardContent className="p-0">
                      {/* Service Header */}
                      <div className="flex items-center justify-between px-6 py-5">
                        <div className="flex items-center gap-4">
                          <div className="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center text-2xl">
                            {getServiceIcon(service.serviceName)}
                          </div>
                          <div>
                            <h3 className="text-base font-semibold text-slate-900">
                              {service.serviceName}
                            </h3>
                            <p className="text-xs text-slate-500 mt-0.5">
                              {service.description || `Per ${service.unitType?.toLowerCase() || "unit"}`}
                              {service.isAutoSelected && (
                                <span className="ml-2 text-teal-600 font-medium">• Auto-selected</span>
                              )}
                            </p>
                          </div>
                        </div>

                        {/* Base Price */}
                        <div className="flex items-center gap-3">
                          {isEditingBase ? (
                            <div className="flex items-center gap-2">
                              <div className="relative">
                                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-slate-400">₱</span>
                                <input
                                  type="number"
                                  step="0.01"
                                  min="0"
                                  value={baseEdit?.newPrice || ""}
                                  onChange={(e) => updateEditingPrice(baseKey, e.target.value)}
                                  className="w-28 pl-7 pr-3 py-2 rounded-lg border border-teal-300 text-sm font-medium text-slate-900 focus:outline-none focus:ring-2 focus:ring-teal-500/30 bg-teal-50/50"
                                  autoFocus
                                  onKeyDown={(e) => {
                                    if (e.key === "Enter") savePrice(baseKey);
                                    if (e.key === "Escape") cancelEditing(service.serviceId);
                                  }}
                                />
                              </div>
                              <button
                                onClick={() => savePrice(baseKey)}
                                disabled={isSavingBase}
                                className="w-8 h-8 rounded-lg bg-teal-600 hover:bg-teal-700 text-white flex items-center justify-center transition-colors disabled:opacity-50"
                              >
                                {isSavingBase ? (
                                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                                ) : (
                                  <Check className="w-3.5 h-3.5" />
                                )}
                              </button>
                              <button
                                onClick={() => cancelEditing(service.serviceId)}
                                className="w-8 h-8 rounded-lg bg-slate-200 hover:bg-slate-300 text-slate-600 flex items-center justify-center transition-colors"
                              >
                                <X className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          ) : (
                            <div className="flex items-center gap-2">
                              {!service.hasVariants && (
                                <div className="text-right">
                                  <p className="text-lg font-bold text-slate-900">
                                    ₱{service.basePricePerUnit.toFixed(2)}
                                  </p>
                                  <p className="text-xs text-slate-400">
                                    per {service.unitType?.toLowerCase() || "unit"}
                                  </p>
                                </div>
                              )}
                              {!service.hasVariants && (
                                <button
                                  onClick={() =>
                                    startEditing(service.serviceId, service.basePricePerUnit)
                                  }
                                  className="w-8 h-8 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-teal-600 flex items-center justify-center transition-colors"
                                  title="Edit price"
                                >
                                  <Edit3 className="w-4 h-4" />
                                </button>
                              )}
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Variants */}
                      {service.hasVariants && service.variants && service.variants.length > 0 && (
                        <div className="border-t border-slate-100 bg-slate-50/50">
                          <div className="px-6 py-3">
                            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
                              Variants
                            </p>
                            <div className="space-y-2">
                              {service.variants.map((variant) => {
                                const vKey = getEditKey(service.serviceId, variant.variantId);
                                const isEditingV = editingPrices.has(vKey);
                                const vEdit = editingPrices.get(vKey);
                                const isSavingV = savingIds.has(vKey);

                                return (
                                  <div
                                    key={variant.variantId}
                                    className="flex items-center justify-between bg-white rounded-lg px-4 py-3 border border-slate-200/60"
                                  >
                                    <div className="flex items-center gap-3">
                                      <Tag className="w-4 h-4 text-slate-400" />
                                      <span className="text-sm font-medium text-slate-700">
                                        {variant.variantName}
                                      </span>
                                    </div>

                                    {isEditingV ? (
                                      <div className="flex items-center gap-2">
                                        <div className="relative">
                                          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-slate-400">
                                            ₱
                                          </span>
                                          <input
                                            type="number"
                                            step="0.01"
                                            min="0"
                                            value={vEdit?.newPrice || ""}
                                            onChange={(e) => updateEditingPrice(vKey, e.target.value)}
                                            className="w-28 pl-7 pr-3 py-1.5 rounded-lg border border-teal-300 text-sm font-medium text-slate-900 focus:outline-none focus:ring-2 focus:ring-teal-500/30 bg-teal-50/50"
                                            autoFocus
                                            onKeyDown={(e) => {
                                              if (e.key === "Enter") savePrice(vKey);
                                              if (e.key === "Escape")
                                                cancelEditing(service.serviceId, variant.variantId);
                                            }}
                                          />
                                        </div>
                                        <button
                                          onClick={() => savePrice(vKey)}
                                          disabled={isSavingV}
                                          className="w-7 h-7 rounded-md bg-teal-600 hover:bg-teal-700 text-white flex items-center justify-center transition-colors disabled:opacity-50"
                                        >
                                          {isSavingV ? (
                                            <Loader2 className="w-3 h-3 animate-spin" />
                                          ) : (
                                            <Check className="w-3 h-3" />
                                          )}
                                        </button>
                                        <button
                                          onClick={() =>
                                            cancelEditing(service.serviceId, variant.variantId)
                                          }
                                          className="w-7 h-7 rounded-md bg-slate-200 hover:bg-slate-300 text-slate-600 flex items-center justify-center transition-colors"
                                        >
                                          <X className="w-3 h-3" />
                                        </button>
                                      </div>
                                    ) : (
                                      <div className="flex items-center gap-2">
                                        <span className="text-sm font-bold text-slate-900">
                                          ₱{variant.variantPrice.toFixed(2)}
                                        </span>
                                        <button
                                          onClick={() =>
                                            startEditing(
                                              service.serviceId,
                                              variant.variantPrice,
                                              variant.variantId
                                            )
                                          }
                                          className="w-7 h-7 rounded-md hover:bg-slate-100 text-slate-400 hover:text-teal-600 flex items-center justify-center transition-colors"
                                          title="Edit variant price"
                                        >
                                          <Edit3 className="w-3.5 h-3.5" />
                                        </button>
                                      </div>
                                    )}
                                  </div>
                                );
                              })}
                            </div>
                          </div>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                </motion.div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}



