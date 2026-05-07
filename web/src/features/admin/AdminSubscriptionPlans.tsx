import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import * as subscriptionService from "@/features/shared/services/subscription";
import { SubscriptionData } from "@/features/shared/services/subscription";
import { Card, CardContent } from "@/features/shared/components/Card";
import { Button } from "@/features/shared/components/Button";
import { Crown, Check, Sparkles, Star, Zap, Loader2, Edit3, X, Save, RotateCcw } from 'lucide-react';
import { motion } from 'motion/react';

export default function AdminSubscriptionPlans() {
  const navigate = useNavigate();
  const [plans, setPlans] = useState<SubscriptionData[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingPlanId, setEditingPlanId] = useState<number | null>(null);
  const [newPrice, setNewPrice] = useState<string>('');
  const [savingId, setSavingId] = useState<number | null>(null);

  useEffect(() => {
    fetchPlans();
  }, []);

  const fetchPlans = async () => {
    try {
      setLoading(true);
      const fetchedPlans = await subscriptionService.getSubscriptionPlans();
      setPlans(fetchedPlans);
    } catch (error) {
      console.error('Failed to load plans:', error);
    } finally {
      setLoading(false);
    }
  };

  const startEditing = (plan: SubscriptionData) => {
    setEditingPlanId(plan.subscriptionId);
    setNewPrice(plan.planPrice.toString());
  };

  const cancelEditing = () => {
    setEditingPlanId(null);
    setNewPrice('');
  };

  const savePrice = async (planId: number) => {
    const priceValue = parseFloat(newPrice);
    if (isNaN(priceValue) || priceValue < 0) {
      alert('Please enter a valid price');
      return;
    }

    try {
      setSavingId(planId);
      await subscriptionService.updateSubscriptionPrice(planId, priceValue);
      
      // Update local state
      setPlans(prev => prev.map(p => 
        p.subscriptionId === planId ? { ...p, planPrice: priceValue } : p
      ));
      
      cancelEditing();
    } catch (error) {
      console.error('Failed to update price:', error);
      alert('Failed to update price');
    } finally {
      setSavingId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 pt-20">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center"
        >
          <div className="w-16 h-16 rounded-full bg-teal-100 flex items-center justify-center mx-auto mb-4">
            <Loader2 className="w-8 h-8 text-teal-600 animate-spin" />
          </div>
          <p className="text-slate-600 font-medium">Loading platform plans...</p>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-12"
        >
          <div>
            <h1 className="text-3xl font-bold text-slate-900 mb-2">Subscription Management</h1>
            <p className="text-slate-600">Manage pricing and tiers for WashMate subscriptions</p>
          </div>
          <Button 
            variant="outline" 
            className="border-slate-300 gap-2 w-fit bg-white" 
            onClick={fetchPlans}
          >
            <RotateCcw className="w-4 h-4" />
            Refresh Plans
          </Button>
        </motion.div>

        {/* Plans Grid */}
        <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          {plans
            .sort((a, b) => {
              if (a.planType === 'FREE') return -1;
              if (b.planType === 'FREE') return 1;
              return 0;
            })
            .map((plan, index) => {
            const isPremium = plan.planType === 'PREMIUM';
            const isEditing = editingPlanId === plan.subscriptionId;
            const isSaving = savingId === plan.subscriptionId;

            return (
              <motion.div
                key={plan.subscriptionId}
                initial={{ opacity: 0, y: 24 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                className="relative pt-6"
              >
                {/* Premium Recommended Badge */}
                {isPremium && (
                  <div className="absolute top-2 right-6 flex items-center gap-1.5 bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-500 text-white px-4 py-1.5 rounded-full text-sm font-bold shadow-xl z-20">
                    <Crown className="w-4 h-4" />
                    Popular Tier
                  </div>
                )}

                <Card
                  className={`relative overflow-hidden transition-all duration-300 ${
                    isPremium
                      ? 'border-2 border-amber-400 bg-gradient-to-br from-amber-50 via-yellow-50 to-orange-50 shadow-xl'
                      : 'border-2 border-slate-200 bg-white/80'
                  }`}
                >
                  <CardContent className="pt-10 pb-8 px-8 relative">
                    {/* Plan Icon & Name */}
                    <div className="flex items-center gap-3 mb-6">
                      <div
                        className={`w-12 h-12 rounded-xl flex items-center justify-center ${
                          isPremium
                            ? 'bg-gradient-to-br from-amber-400 to-yellow-500 shadow-lg shadow-amber-200'
                            : 'bg-gradient-to-br from-teal-400 to-teal-500 shadow-lg shadow-teal-200'
                        }`}
                      >
                        {isPremium ? (
                          <Crown className="w-6 h-6 text-white" />
                        ) : (
                          <Sparkles className="w-6 h-6 text-white" />
                        )}
                      </div>
                      <div>
                        <h3
                          className={`text-2xl font-bold ${
                            isPremium ? 'text-amber-900' : 'text-slate-900'
                          }`}
                        >
                          {plan.planType}
                        </h3>
                        <p className={isPremium ? 'text-amber-600' : 'text-slate-500'}>
                          Platform Tier
                        </p>
                      </div>
                    </div>

                    {/* Price with Editing */}
                    <div className="mb-8 p-4 bg-white/40 rounded-2xl border border-white/60">
                      {isEditing ? (
                        <div className="space-y-3">
                          <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                            New Monthly Price
                          </label>
                          <div className="flex gap-2">
                            <div className="relative flex-1">
                              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 font-bold">₱</span>
                              <input
                                type="number"
                                value={newPrice}
                                onChange={(e) => setNewPrice(e.target.value)}
                                className="w-full pl-8 pr-4 py-2 rounded-xl border-2 border-teal-500 focus:outline-none bg-white font-bold text-xl"
                                autoFocus
                              />
                            </div>
                            <Button 
                              onClick={() => savePrice(plan.subscriptionId)}
                              disabled={isSaving}
                              className="bg-teal-600 hover:bg-teal-700 text-white p-3 rounded-xl shrink-0"
                            >
                              {isSaving ? <Loader2 className="w-5 h-5 animate-spin" /> : <Save className="w-5 h-5" />}
                            </Button>
                            <Button 
                              onClick={cancelEditing}
                              variant="outline"
                              className="border-slate-300 p-3 rounded-xl shrink-0"
                            >
                              <X className="w-5 h-5" />
                            </Button>
                          </div>
                        </div>
                      ) : (
                        <div className="flex items-center justify-between">
                          <div>
                            <div className="flex items-baseline gap-1">
                              <span className={`text-4xl font-bold ${isPremium ? 'text-amber-900' : 'text-slate-900'}`}>
                                ₱{plan.planPrice}
                              </span>
                              <span className="text-slate-500">/month</span>
                            </div>
                            <p className="text-xs text-slate-400 mt-1">Last updated: {new Date(plan.createdAt).toLocaleDateString()}</p>
                          </div>
                          {plan.planType !== 'FREE' && (
                            <Button
                              onClick={() => startEditing(plan)}
                              variant="ghost"
                              className={`w-10 h-10 rounded-full flex items-center justify-center p-0 ${
                                isPremium ? 'text-amber-600 hover:bg-amber-100' : 'text-teal-600 hover:bg-teal-50'
                              }`}
                            >
                              <Edit3 className="w-5 h-5" />
                            </Button>
                          )}
                        </div>
                      )}
                    </div>

                    {/* Features List */}
                    <ul className="space-y-4 mb-2">
                      <li className="flex items-center gap-3">
                        <div className={`w-5 h-5 rounded-full flex items-center justify-center ${isPremium ? 'bg-amber-100' : 'bg-teal-100'}`}>
                          <Check className={`w-3.5 h-3.5 ${isPremium ? 'text-amber-600' : 'text-teal-600'}`} />
                        </div>
                        <span className="text-sm text-slate-700">
                          {plan.ordersIncluded ?? 'Unlimited'} orders included
                        </span>
                      </li>
                      <li className="flex items-center gap-3">
                        <div className={`w-5 h-5 rounded-full flex items-center justify-center ${isPremium ? 'bg-amber-100' : 'bg-teal-100'}`}>
                          <Check className={`w-3.5 h-3.5 ${isPremium ? 'text-amber-600' : 'text-teal-600'}`} />
                        </div>
                        <span className="text-sm text-slate-700">
                          {plan.discountPercentage}% discount on services
                        </span>
                      </li>
                    </ul>
                  </CardContent>
                </Card>
              </motion.div>
            );
          })}
        </div>

        {/* Admin Note */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
          className="mt-12 p-6 bg-amber-50 rounded-2xl border border-amber-200 text-center"
        >
          <div className="flex items-center justify-center gap-2 text-amber-800 font-semibold mb-2">
            <Zap className="w-5 h-5" />
            Pricing Management
          </div>
          <p className="text-amber-700 text-sm max-w-2xl mx-auto">
            Changes made here will take effect immediately for new subscriptions. 
            Existing subscribers will continue on their current price until their next renewal cycle.
          </p>
        </motion.div>
      </div>
    </div>
  );
}



