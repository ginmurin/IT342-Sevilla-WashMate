import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { useSubscription } from '../contexts/SubscriptionContext';
import * as subscriptionService from '../services/subscription';
import { SubscriptionData } from '../services/subscription';
import { Card, CardContent } from '../components/Card';
import { Button } from '../components/Button';
import { Crown, Check, Sparkles, Star, Zap, Loader2 } from 'lucide-react';
import { motion } from 'motion/react';

export default function SubscriptionPlans() {
  const navigate = useNavigate();
  const { setUpgradeData } = useSubscription();
  const [plans, setPlans] = useState<SubscriptionData[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPlanType, setCurrentPlanType] = useState<string | null>(null);

  useEffect(() => {
    const fetchPlans = async () => {
      try {
        const fetchedPlans = await subscriptionService.getSubscriptionPlans();
        setPlans(fetchedPlans);

        const current = await subscriptionService.getCurrentSubscription();
        if (current) {
          setCurrentPlanType(current.planType);
        }
      } catch (error) {
        console.error('Failed to load plans:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchPlans();
  }, []);

  const handleUpgrade = (plan: SubscriptionData) => {
    if (plan.planType === currentPlanType) {
      return;
    }

    setUpgradeData({
      currentPlan: currentPlanType || 'FREE',
      newPlan: plan.planType,
      amount: Number(plan.planPrice),
    });

    navigate('/subscription/upgrade-review');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center"
        >
          <div className="w-16 h-16 rounded-full bg-teal-100 flex items-center justify-center mx-auto mb-4">
            <Loader2 className="w-8 h-8 text-teal-600 animate-spin" />
          </div>
          <p className="text-slate-600 font-medium">Loading subscription plans...</p>
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
          className="text-center mb-12"
        >
          <h1 className="text-4xl font-bold text-slate-900 mb-3">Subscription Plans</h1>
          <p className="text-slate-600 text-lg">Choose the perfect plan for your laundry needs</p>
        </motion.div>

        {/* Plans Grid */}
        <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto mt-8">
          {plans
            .sort((a, b) => {
              // Sort FREE first, then PREMIUM
              if (a.planType === 'FREE') return -1;
              if (b.planType === 'FREE') return 1;
              return 0;
            })
            .map((plan, index) => {
            const isCurrentPlan = plan.planType === currentPlanType;
            const isPremium = plan.planType === 'PREMIUM';
            const isHigherTierUpgrade = currentPlanType === 'FREE' && plan.planType !== 'FREE';

            return (
              <motion.div
                key={plan.subscriptionId}
                initial={{ opacity: 0, y: 24 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 + 0.2 }}
                whileHover={{ y: -4 }}
                className="relative pt-6"
              >
                {/* Premium Glow Effect */}
                {isPremium && !isCurrentPlan && (
                  <div className="absolute -inset-1 bg-gradient-to-r from-amber-400 via-yellow-500 to-amber-400 rounded-3xl blur-lg opacity-30 animate-pulse" />
                )}

                {/* Current Plan Badge - Outside Card to avoid clipping */}
                {isCurrentPlan && (
                  <motion.div
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ delay: 0.3, type: 'spring', stiffness: 200 }}
                    className={`absolute top-2 left-6 px-4 py-1.5 rounded-full text-sm font-semibold shadow-lg z-20 ${
                      isPremium
                        ? 'bg-gradient-to-r from-amber-500 to-yellow-500 text-white'
                        : 'bg-teal-500 text-white'
                    }`}
                  >
                    Current Plan
                  </motion.div>
                )}

                {/* Premium Recommended Badge - Outside Card to avoid clipping */}
                {isPremium && !isCurrentPlan && (
                  <motion.div
                    initial={{ scale: 0, rotate: -12 }}
                    animate={{ scale: 1, rotate: 0 }}
                    transition={{ delay: 0.4, type: 'spring', stiffness: 200 }}
                    className="absolute top-2 right-6 flex items-center gap-1.5 bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-500 text-white px-4 py-1.5 rounded-full text-sm font-bold shadow-xl z-20"
                  >
                    <Crown className="w-4 h-4" />
                    Best Value
                  </motion.div>
                )}

                <Card
                  className={`relative overflow-hidden transition-all duration-300 ${
                    isPremium
                      ? isCurrentPlan
                        ? 'border-2 border-amber-500 bg-gradient-to-br from-amber-50 via-yellow-50 to-orange-50 shadow-xl'
                        : 'border-2 border-amber-400 bg-gradient-to-br from-amber-50 via-yellow-50 to-orange-50 shadow-xl hover:shadow-2xl'
                      : isCurrentPlan
                        ? 'border-2 border-teal-500 bg-white shadow-lg'
                        : 'border-2 border-slate-200 bg-white/80 hover:border-slate-300 hover:shadow-md'
                  }`}
                >
                  {/* Premium Decorative Elements */}
                  {isPremium && (
                    <>
                      <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-amber-300/20 to-transparent rounded-bl-full" />
                      <div className="absolute bottom-0 left-0 w-24 h-24 bg-gradient-to-tr from-yellow-300/20 to-transparent rounded-tr-full" />
                    </>
                  )}

                  <CardContent className="pt-10 pb-8 px-8 relative">
                    {/* Plan Icon & Name */}
                    <div className="flex items-center gap-3 mb-4">
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
                        {isPremium && (
                          <p className="text-amber-600 text-sm font-medium">Most Popular</p>
                        )}
                      </div>
                    </div>

                    {/* Price */}
                    <div className="mb-6">
                      <div className="flex items-baseline gap-1">
                        <span
                          className={`text-5xl font-bold ${
                            isPremium ? 'text-amber-900' : 'text-slate-900'
                          }`}
                        >
                          ₱{plan.planPrice}
                        </span>
                        <span className={isPremium ? 'text-amber-700' : 'text-slate-600'}>
                          /month
                        </span>
                      </div>
                      {isPremium && (
                        <p className="text-amber-600 text-sm mt-1 flex items-center gap-1">
                          <Zap className="w-4 h-4" />
                          Save up to {plan.discountPercentage}% on every order
                        </p>
                      )}
                    </div>

                    {/* Features */}
                    <ul className="space-y-4 mb-8">
                      <li className="flex items-center gap-3">
                        <div
                          className={`w-5 h-5 rounded-full flex items-center justify-center ${
                            isPremium ? 'bg-amber-100' : 'bg-teal-100'
                          }`}
                        >
                          <Check
                            className={`w-3.5 h-3.5 ${
                              isPremium ? 'text-amber-600' : 'text-teal-600'
                            }`}
                          />
                        </div>
                        <span className={isPremium ? 'text-amber-900' : 'text-slate-700'}>
                          {plan.ordersIncluded ?? 'Unlimited'} orders included
                        </span>
                      </li>
                      <li className="flex items-center gap-3">
                        <div
                          className={`w-5 h-5 rounded-full flex items-center justify-center ${
                            isPremium ? 'bg-amber-100' : 'bg-teal-100'
                          }`}
                        >
                          <Check
                            className={`w-3.5 h-3.5 ${
                              isPremium ? 'text-amber-600' : 'text-teal-600'
                            }`}
                          />
                        </div>
                        <span className={isPremium ? 'text-amber-900' : 'text-slate-700'}>
                          {plan.discountPercentage}% discount on services
                        </span>
                      </li>
                      {isPremium && (
                        <>
                          <li className="flex items-center gap-3">
                            <div className="w-5 h-5 rounded-full flex items-center justify-center bg-amber-100">
                              <Star className="w-3.5 h-3.5 text-amber-600" />
                            </div>
                            <span className="text-amber-900 font-medium">Priority support</span>
                          </li>
                          <li className="flex items-center gap-3">
                            <div className="w-5 h-5 rounded-full flex items-center justify-center bg-amber-100">
                              <Zap className="w-3.5 h-3.5 text-amber-600" />
                            </div>
                            <span className="text-amber-900 font-medium">
                              Early access to new services
                            </span>
                          </li>
                          <li className="flex items-center gap-3">
                            <div className="w-5 h-5 rounded-full flex items-center justify-center bg-amber-100">
                              <Crown className="w-3.5 h-3.5 text-amber-600" />
                            </div>
                            <span className="text-amber-900 font-medium">Exclusive member perks</span>
                          </li>
                        </>
                      )}
                    </ul>

                    {/* Button */}
                    <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                      <Button
                        onClick={() => handleUpgrade(plan)}
                        disabled={isCurrentPlan}
                        className={`w-full h-12 rounded-xl font-semibold transition-all duration-200 ${
                          isCurrentPlan
                            ? 'bg-slate-200 text-slate-500 cursor-not-allowed'
                            : isPremium
                              ? 'bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-500 hover:from-amber-600 hover:via-yellow-600 hover:to-amber-600 text-white shadow-lg shadow-amber-200 hover:shadow-xl hover:shadow-amber-300'
                              : isHigherTierUpgrade
                                ? 'bg-teal-500 text-white hover:bg-teal-600'
                                : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                        }`}
                      >
                        {isCurrentPlan
                          ? 'Current Plan'
                          : isPremium
                            ? 'Upgrade to Premium'
                            : `${isHigherTierUpgrade ? 'Upgrade' : 'Select'} Plan`}
                      </Button>
                    </motion.div>

                    {/* Renewal Info */}
                    {isCurrentPlan && (
                      <p
                        className={`text-sm mt-4 text-center ${
                          isPremium ? 'text-amber-600' : 'text-slate-500'
                        }`}
                      >
                        Renews automatically each month
                      </p>
                    )}
                  </CardContent>
                </Card>
              </motion.div>
            );
          })}
        </div>

        {/* Bottom Info */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="text-center mt-12"
        >
          <p className="text-slate-500 text-sm">
            All plans include access to our full service network. Cancel anytime.
          </p>
        </motion.div>
      </div>
    </div>
  );
}
