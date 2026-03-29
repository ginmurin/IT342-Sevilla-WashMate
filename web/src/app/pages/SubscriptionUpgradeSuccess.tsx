import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import * as subscriptionService from '../services/subscription';
import { Card, CardContent } from '../components/Card';
import { Button } from '../components/Button';
import {
  CheckCircle2,
  Home,
  ArrowRight,
  Copy,
  Sparkles,
  AlertCircle,
  Settings,
  Loader2,
  Crown,
} from 'lucide-react';
import { motion } from 'motion/react';

export default function SubscriptionUpgradeSuccess() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const hasConfirmed = useRef(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [subscriptionData, setSubscriptionData] = useState<any>(null);

  const userSubId = searchParams.get('userSubscriptionId') || '';

  useEffect(() => {
    const confirmUpgrade = async () => {
      if (hasConfirmed.current) return;
      hasConfirmed.current = true;

      try {
        const paymentId = searchParams.get('paymentId') || '1';

        if (!userSubId) {
          throw new Error('Missing subscription ID');
        }

        // Log the values for debugging
        console.log('Debug - userSubId:', userSubId);
        console.log('Debug - paymentId:', paymentId);
        console.log('Debug - All search params:', Object.fromEntries(searchParams));

        // Check if userSubId is actually a number
        if (!/^\d+$/.test(userSubId)) {
          throw new Error(`Invalid subscription ID format: "${userSubId}". Expected a number.`);
        }

        const confirmed = await subscriptionService.confirmUpgrade(userSubId, paymentId, 'CARD');
        setSubscriptionData(confirmed);
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Failed to confirm upgrade';
        setError(message);
        console.error('Error confirming upgrade:', err);
      } finally {
        setLoading(false);
      }
    };

    confirmUpgrade();
  }, [searchParams, userSubId]);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(userSubId);
  };

  const isPremium = subscriptionData?.planType === 'PREMIUM';

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
          <p className="text-slate-600 font-medium">Confirming your upgrade...</p>
          <p className="text-slate-500 text-sm mt-1">This may take a moment</p>
        </motion.div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 px-4">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-md w-full"
        >
          <Card className="border-red-200 shadow-lg">
            <CardContent className="pt-8 pb-8 text-center">
              <div className="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
                <AlertCircle className="w-8 h-8 text-red-600" />
              </div>
              <h1 className="text-2xl font-bold text-slate-900 mb-2">Upgrade Failed</h1>
              <p className="text-slate-600 mb-6">{error}</p>
              <Button
                onClick={() => navigate('/subscriptions')}
                className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg font-medium"
              >
                Back to Plans
              </Button>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    );
  }

  return (
    <div
      className={`min-h-screen pt-20 pb-12 flex items-center ${
        isPremium
          ? 'bg-gradient-to-br from-amber-50 via-yellow-50 to-orange-50'
          : 'bg-gradient-to-br from-emerald-50 to-teal-50'
      }`}
    >
      <div className="max-w-2xl mx-auto px-4 w-full">
        {/* Success Animation */}
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2, duration: 0.4 }}
          className="text-center mb-8"
        >
          <motion.div animate={{ rotate: 360 }} transition={{ duration: 0.6, delay: 0.2 }}>
            <div
              className={`w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6 shadow-lg ${
                isPremium
                  ? 'bg-gradient-to-br from-amber-400 via-yellow-500 to-amber-500'
                  : 'bg-gradient-to-br from-emerald-400 to-teal-500'
              }`}
            >
              {isPremium ? (
                <Crown className="w-10 h-10 text-white" />
              ) : (
                <CheckCircle2 className="w-10 h-10 text-white" />
              )}
            </div>
          </motion.div>
        </motion.div>

        {/* Success Message */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="text-center mb-8"
        >
          <h1 className="text-4xl font-bold text-slate-900 mb-2">
            {isPremium ? 'Welcome to Premium!' : 'Upgrade Successful!'}
          </h1>
          <p className="text-lg text-slate-600">
            {isPremium ? (
              <>
                You now have access to all{' '}
                <span className="font-semibold text-amber-600">Premium</span> benefits
              </>
            ) : (
              <>
                Welcome to the{' '}
                <span className="font-semibold text-teal-600">{subscriptionData?.planType}</span>{' '}
                plan
              </>
            )}
          </p>
        </motion.div>

        {/* Details Card */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="mb-8 relative"
        >
          {isPremium && (
            <div className="absolute -inset-1 bg-gradient-to-r from-amber-400 via-yellow-500 to-amber-400 rounded-3xl blur-lg opacity-20" />
          )}
          <Card
            className={`relative bg-white shadow-lg ${
              isPremium ? 'border-amber-200' : 'border-emerald-200'
            }`}
          >
            <CardContent className="pt-8 pb-8 space-y-6">
              {/* Premium Badge */}
              {isPremium && (
                <div className="flex justify-center mb-4">
                  <span className="inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-500 text-white rounded-full text-sm font-bold shadow-lg">
                    <Crown className="w-4 h-4" />
                    Premium Member
                  </span>
                </div>
              )}

              {/* Subscription Details */}
              <div className="space-y-4">
                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Subscription ID</span>
                  <div className="flex items-center gap-2">
                    <code className="font-mono font-semibold text-slate-900 text-sm">
                      {userSubId.slice(0, 16)}...
                    </code>
                    <button
                      onClick={copyToClipboard}
                      className="p-1 hover:bg-slate-100 rounded transition-colors"
                      title="Copy reference"
                    >
                      <Copy className="w-4 h-4 text-slate-500" />
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Plan</span>
                  <span
                    className={`flex items-center gap-2 text-lg font-bold ${
                      isPremium ? 'text-amber-600' : 'text-teal-600'
                    }`}
                  >
                    {isPremium ? (
                      <Crown className="w-5 h-5" />
                    ) : (
                      <Sparkles className="w-5 h-5" />
                    )}
                    {subscriptionData?.planType}
                  </span>
                </div>

                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Monthly Price</span>
                  <span className="text-xl font-bold text-slate-900">
                    ₱{subscriptionData?.planPrice}
                  </span>
                </div>

                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Discount</span>
                  <span className="text-lg font-bold text-emerald-600">
                    {subscriptionData?.discountPercentage}% off
                  </span>
                </div>

                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <span className="text-slate-600">Status</span>
                  <span
                    className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-sm font-medium ${
                      isPremium
                        ? 'bg-amber-50 text-amber-700'
                        : 'bg-emerald-50 text-emerald-700'
                    }`}
                  >
                    <CheckCircle2 className="w-4 h-4" />
                    Active
                  </span>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-slate-600">Valid Until</span>
                  <span className="text-slate-900 font-medium">
                    {new Date(subscriptionData?.expiryDate).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                    })}
                  </span>
                </div>
              </div>

              {/* Info Box */}
              <div
                className={`rounded-lg p-4 flex items-start gap-3 border ${
                  isPremium
                    ? 'bg-gradient-to-br from-amber-50 to-yellow-50 border-amber-200'
                    : 'bg-gradient-to-br from-teal-50 to-emerald-50 border-teal-200'
                }`}
              >
                {isPremium ? (
                  <Crown className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
                ) : (
                  <Sparkles className="w-5 h-5 text-teal-600 shrink-0 mt-0.5" />
                )}
                <div>
                  <p className="text-sm font-medium text-slate-900">
                    {isPremium ? 'Welcome to the Premium family!' : 'Your subscription is now active'}
                  </p>
                  <p className="text-xs text-slate-600 mt-1">
                    {isPremium
                      ? 'Enjoy priority support, exclusive perks, and maximum discounts on all services!'
                      : 'Enjoy your new benefits! Your plan will auto-renew monthly unless cancelled.'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Action Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="grid grid-cols-1 md:grid-cols-2 gap-3"
        >
          <Button
            onClick={() => navigate('/subscription/management')}
            className={`h-12 rounded-lg font-medium flex items-center justify-center gap-2 ${
              isPremium
                ? 'bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-500 hover:from-amber-600 hover:via-yellow-600 hover:to-amber-600 text-white shadow-lg shadow-amber-200'
                : 'bg-emerald-600 hover:bg-emerald-700 text-white'
            }`}
          >
            <Settings className="w-4 h-4" />
            Manage Subscription
          </Button>
          <Button
            onClick={() => navigate('/')}
            variant="outline"
            className="border-slate-300 h-12 rounded-lg font-medium flex items-center justify-center gap-2"
          >
            <Home className="w-4 h-4" />
            Go Home
          </Button>
        </motion.div>

        {/* Order Laundry Button */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
          className="mt-3"
        >
          <Button
            onClick={() => navigate('/services')}
            className={`w-full h-12 rounded-lg font-medium flex items-center justify-center gap-2 ${
              isPremium
                ? 'bg-amber-600 hover:bg-amber-700 text-white'
                : 'bg-teal-600 hover:bg-teal-700 text-white'
            }`}
          >
            Browse Services
            <ArrowRight className="w-4 h-4" />
          </Button>
        </motion.div>

        {/* Info Box */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.7 }}
          className="mt-8 text-center"
        >
          <p className="text-sm text-slate-600">
            A confirmation email has been sent to your registered email address
          </p>
        </motion.div>
      </div>
    </div>
  );
}
