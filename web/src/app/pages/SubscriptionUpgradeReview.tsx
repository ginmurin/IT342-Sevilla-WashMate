import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { useSubscription } from '../contexts/SubscriptionContext';
import { subscriptionAPI } from '../utils/subscriptionAPI';
import * as subscriptionService from '../services/subscription';
import { Card, CardHeader, CardTitle, CardContent } from '../components/Card';
import { Button } from '../components/Button';
import {
  ArrowRight,
  ArrowLeft,
  AlertCircle,
  CreditCard,
  Smartphone,
  CheckCircle2,
  Loader2,
  Sparkles,
  Crown,
  Wallet,
} from 'lucide-react';
import { motion } from 'motion/react';

const PAYMENT_METHODS = [
  {
    id: 'gcash',
    name: 'GCash',
    icon: Smartphone,
    description: 'Pay using GCash mobile wallet',
    color: 'from-blue-500 to-cyan-500',
  },
  {
    id: 'maya',
    name: 'Maya',
    icon: Smartphone,
    description: 'Pay using Maya mobile wallet',
    color: 'from-purple-500 to-pink-500',
  },
  {
    id: 'card',
    name: 'Credit/Debit Card',
    icon: CreditCard,
    description: 'Visa, Mastercard, or other cards',
    color: 'from-orange-500 to-red-500',
  },
  {
    id: 'grab_pay',
    name: 'GrabPay',
    icon: Smartphone,
    description: 'Pay through GrabPay',
    color: 'from-green-500 to-emerald-500',
  },
  {
    id: 'wallet',
    name: 'Wallet',
    icon: Wallet,
    description: 'Pay using your WashMate wallet balance',
    color: 'from-teal-500 to-cyan-500',
  },
];

export default function SubscriptionUpgradeReview() {
  const navigate = useNavigate();
  const { upgradeData, setUpgradeData } = useSubscription();
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [selectedMethod, setSelectedMethod] = useState<string>('');
  const [plans, setPlans] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  const isPremium = upgradeData.newPlan === 'PREMIUM';

  useEffect(() => {
    const fetchPlans = async () => {
      const fetchedPlans = await subscriptionService.getSubscriptionPlans();
      setPlans(fetchedPlans);
    };
    fetchPlans();
  }, []);

  if (!upgradeData.newPlan) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-500">Invalid upgrade data. Please start over.</p>
      </div>
    );
  }

  const newPlan = plans.find((p) => p.planType === upgradeData.newPlan) || {
    planPrice: upgradeData.amount,
    discountPercentage: 0,
  };

  const handleProceed = async () => {
    if (!selectedMethod) {
      setPaymentError('Please select a payment method');
      return;
    }
    if (!agreeTerms) {
      setPaymentError('Please agree to the terms and conditions');
      return;
    }

    setPaymentError(null);
    setLoading(true);
    setUpgradeData({ paymentMethod: selectedMethod as any });

    try {
      // 1. Initiate upgrade with backend to get userSubscriptionId and paymentId
      const upgradeResponse = await subscriptionAPI.initiateUpgrade(upgradeData.newPlan);
      console.log('📝 Subscription upgrade API response:', upgradeResponse);

      const { userSubscriptionId, amount, paymentId } = upgradeResponse.data || upgradeResponse;

      if (!userSubscriptionId) {
        throw new Error('Failed to get userSubscriptionId from upgrade initiation');
      }

      console.log('📝 Subscription upgrade initiated:', { userSubscriptionId, amount, paymentId, paymentMethod: selectedMethod });

      // 2. Handle wallet payment (direct success, no checkout)
      if (selectedMethod === 'wallet') {
        navigate('/subscription/upgrade-success', {
          state: { userSubscriptionId, amount, paymentId, paymentMethod: 'wallet' },
        });
        return;
      }

      // 3. Handle other payment methods (need checkout/gateway)
      navigate('/subscription/upgrade-checkout', {
        state: { userSubscriptionId, amount, paymentId, paymentMethod: selectedMethod },
      });
    } catch (err) {
      console.error('❌ Upgrade initiation error:', err);
      setPaymentError(err instanceof Error ? err.message : 'Failed to initiate upgrade. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12">
      <div className="max-w-4xl mx-auto px-4 space-y-8">
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
            <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
              Review Your Upgrade
              {isPremium ? (
                <Crown className="w-7 h-7 text-amber-500" />
              ) : (
                <Sparkles className="w-7 h-7 text-teal-500" />
              )}
            </h1>
            <p className="text-slate-600 mt-1">Confirm the details before proceeding to payment</p>
          </div>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Plan Comparison */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card
                className={`shadow-sm ${
                  isPremium
                    ? 'border-amber-200 bg-gradient-to-br from-amber-50/50 to-yellow-50/50'
                    : 'border-slate-200'
                }`}
              >
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    {isPremium ? (
                      <Crown className="w-5 h-5 text-amber-500" />
                    ) : (
                      <Sparkles className="w-5 h-5 text-teal-500" />
                    )}
                    Plan Comparison
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-center gap-6 py-4">
                    {/* Current Plan */}
                    <div className="text-center flex-1">
                      <p className="text-sm text-slate-500 mb-2">Current Plan</p>
                      <div className="bg-slate-100 rounded-xl p-4">
                        <p className="text-2xl font-bold text-slate-700">
                          {upgradeData.currentPlan || 'FREE'}
                        </p>
                      </div>
                    </div>

                    {/* Arrow */}
                    <motion.div
                      initial={{ scale: 0 }}
                      animate={{ scale: 1 }}
                      transition={{ delay: 0.3, type: 'spring', stiffness: 200 }}
                      className="flex-shrink-0"
                    >
                      <div
                        className={`w-12 h-12 rounded-full flex items-center justify-center ${
                          isPremium ? 'bg-amber-100' : 'bg-teal-100'
                        }`}
                      >
                        <ArrowRight
                          className={`w-6 h-6 ${isPremium ? 'text-amber-600' : 'text-teal-600'}`}
                        />
                      </div>
                    </motion.div>

                    {/* New Plan */}
                    <div className="text-center flex-1">
                      <p className="text-sm text-slate-500 mb-2">New Plan</p>
                      <div
                        className={`rounded-xl p-4 ${
                          isPremium
                            ? 'bg-gradient-to-br from-amber-400 via-yellow-500 to-amber-500 shadow-lg shadow-amber-200'
                            : 'bg-gradient-to-br from-teal-500 to-teal-600'
                        }`}
                      >
                        <p className="text-2xl font-bold text-white flex items-center justify-center gap-2">
                          {isPremium ? (
                            <Crown className="w-6 h-6" />
                          ) : (
                            <Sparkles className="w-6 h-6" />
                          )}
                          {upgradeData.newPlan}
                        </p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Cost Breakdown */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.15 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Cost Breakdown</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between py-2">
                      <span className="text-slate-600">New Plan Price (Monthly)</span>
                      <span className="font-semibold text-slate-900">₱{newPlan.planPrice}</span>
                    </div>
                    <div className="flex justify-between py-2">
                      <span className="text-slate-600">Discount</span>
                      <span className="font-semibold text-green-600">
                        -{newPlan.discountPercentage}%
                      </span>
                    </div>
                    <div className="border-t border-slate-200 pt-3 flex justify-between">
                      <span className="font-semibold text-slate-900">Total First Payment</span>
                      <span className="text-xl font-bold text-teal-600">
                        ₱{upgradeData.amount}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Payment Method Selection */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">Select Payment Method</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-3">
                    {PAYMENT_METHODS.map((method) => {
                      const Icon = method.icon;
                      return (
                        <motion.button
                          key={method.id}
                          onClick={() => setSelectedMethod(method.id)}
                          whileHover={{ scale: 1.02 }}
                          whileTap={{ scale: 0.98 }}
                          className={`p-4 rounded-lg border-2 transition-all text-left ${
                            selectedMethod === method.id
                              ? isPremium
                                ? 'border-amber-500 bg-amber-50'
                                : 'border-teal-500 bg-teal-50'
                              : 'border-slate-200 hover:border-slate-300 bg-white'
                          }`}
                        >
                          <div
                            className={`w-10 h-10 rounded-lg flex items-center justify-center mb-2 bg-gradient-to-br ${method.color} text-white`}
                          >
                            <Icon className="w-5 h-5" />
                          </div>
                          <p className="font-semibold text-slate-900 text-sm">{method.name}</p>
                          <p className="text-xs text-slate-600 mt-1">{method.description}</p>
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
              transition={{ delay: 0.25 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardContent className="pt-6">
                  <label className="flex items-start gap-3 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={agreeTerms}
                      onChange={(e) => setAgreeTerms(e.target.checked)}
                      className={`w-5 h-5 mt-0.5 rounded border-slate-300 ${
                        isPremium
                          ? 'text-amber-600 focus:ring-amber-500'
                          : 'text-teal-600 focus:ring-teal-500'
                      }`}
                    />
                    <div>
                      <p className="text-sm text-slate-700">
                        I agree to the{' '}
                        <a
                          href="#"
                          className={`hover:underline font-medium ${
                            isPremium ? 'text-amber-600' : 'text-teal-600'
                          }`}
                        >
                          Terms of Service
                        </a>{' '}
                        and{' '}
                        <a
                          href="#"
                          className={`hover:underline font-medium ${
                            isPremium ? 'text-amber-600' : 'text-teal-600'
                          }`}
                        >
                          Privacy Policy
                        </a>
                      </p>
                      <p className="text-xs text-slate-500 mt-1">
                        By upgrading, you acknowledge the {upgradeData.newPlan} subscription plan
                        terms, including monthly auto-renewal.
                      </p>
                    </div>
                  </label>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* Sidebar Summary */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="lg:col-span-1"
          >
            <div className="sticky top-24 space-y-4">
              <Card className="relative border-none shadow-lg bg-gradient-to-br from-teal-500 to-teal-700 text-white">
                <CardContent className="pt-6 space-y-4">
                  <div className="flex items-center gap-2 mb-2">
                    <Sparkles className="w-5 h-5" />
                    <span className="font-semibold text-sm">
                      {isPremium ? 'Premium Upgrade' : 'Plan Upgrade'}
                    </span>
                  </div>

                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-white/80">Plan</span>
                      <span className="font-semibold">{upgradeData.newPlan}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-white/80">Monthly Price</span>
                      <span className="font-semibold">₱{newPlan.planPrice}</span>
                    </div>
                    {newPlan.discountPercentage > 0 && (
                      <div className="flex justify-between text-sm">
                        <span className="text-white/80">Discount</span>
                        <span className="font-semibold text-green-300">
                          -{newPlan.discountPercentage}%
                        </span>
                      </div>
                    )}
                  </div>

                  <div className="border-t pt-4 flex justify-between items-center border-teal-400">
                    <span className="font-semibold">Total</span>
                    <span className="text-3xl font-bold">₱{upgradeData.amount}</span>
                  </div>

                  <div className="bg-white/10 rounded-lg p-3 space-y-2 text-xs text-white/80">
                    <div className="flex items-start gap-2">
                      <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                      <span>100% secure payment</span>
                    </div>
                    <div className="flex items-start gap-2">
                      <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                      <span>Cancel anytime</span>
                    </div>
                    <div className="flex items-start gap-2">
                      <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                      <span>Instant activation</span>
                    </div>
                    <div className="flex items-start gap-2">
                      <Sparkles className="w-4 h-4 shrink-0 mt-0.5" />
                      <span>
                        {isPremium ? 'Exclusive member perks' : 'Enhanced member benefits'}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {paymentError && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-lg"
                >
                  <AlertCircle className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
                  <p className="text-sm text-red-700">{paymentError}</p>
                </motion.div>
              )}

              <Button
                onClick={handleProceed}
                disabled={loading || !agreeTerms || !selectedMethod}
                className="w-full h-11 rounded-lg font-medium disabled:opacity-50 flex items-center justify-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Processing…
                  </>
                ) : (
                  <>
                    <Sparkles className="w-4 h-4" />
                    Proceed to Payment
                  </>
                )}
              </Button>

              <Button
                onClick={() => navigate('/subscriptions')}
                variant="outline"
                className="w-full border-slate-300"
              >
                Cancel
              </Button>

              {/* Auto-Renewal Info */}
              <div className="rounded-lg p-3 flex items-start gap-2 bg-teal-50 border border-teal-200">
                <AlertCircle className="w-4 h-4 mt-0.5 shrink-0 text-teal-600" />
                <p className="text-xs text-teal-800">
                  This plan will automatically renew monthly for ₱{newPlan.planPrice} unless
                  cancelled.
                </p>
              </div>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
