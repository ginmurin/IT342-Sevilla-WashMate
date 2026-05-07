import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import * as subscriptionService from "@/features/shared/services/subscription";
import { UserSubscriptionData } from "@/features/shared/services/subscription";
import { Calendar, DollarSign, TrendingUp } from 'lucide-react';

export default function SubscriptionManagement() {
  const navigate = useNavigate();
  const [current, setCurrent] = useState<UserSubscriptionData | null>(null);
  const [history, setHistory] = useState<UserSubscriptionData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [current, history] = await Promise.all([
          subscriptionService.getCurrentSubscription(),
          subscriptionService.getSubscriptionHistory(),
        ]);
        setCurrent(current);
        setHistory(history);
      } catch (error) {
        console.error('Failed to load subscription data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-500">Loading subscription details...</p>
      </div>
    );
  }

  if (!current) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 py-12 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <p className="text-gray-600 mb-6">No active subscription found</p>
          <button
            onClick={() => navigate('/subscriptions')}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700"
          >
            View Plans
          </button>
        </div>
      </div>
    );
  }

  const daysUntilRenewal = Math.ceil(
    (new Date(current.expiryDate).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Subscription Management</h1>
          <p className="text-gray-600">View and manage your current subscription</p>
        </div>

        {/* Current Subscription Card */}
        <div className="bg-gradient-to-r from-blue-500 to-indigo-600 rounded-2xl shadow-lg p-8 text-white mb-8">
          <div className="grid md:grid-cols-2 gap-8">
            <div>
              <p className="text-blue-100 mb-2">Current Plan</p>
              <h2 className="text-4xl font-bold mb-4">{current.planType}</h2>
              <div className="space-y-2">
                <p className="text-blue-100">
                  <span className="font-semibold">₱{current.planPrice}</span> per month
                </p>
                <p className="text-blue-100">
                  <span className="font-semibold">{current.discountPercentage}%</span> discount on services
                </p>
              </div>
            </div>

            <div className="space-y-4">
              {/* Renewal Date */}
              <div className="bg-white/20 rounded-lg p-4 backdrop-blur-sm">
                <div className="flex items-center gap-2 mb-2">
                  <Calendar className="w-5 h-5" />
                  <p className="text-sm text-blue-100">Renewal Date</p>
                </div>
                <p className="text-2xl font-bold">
                  {new Date(current.expiryDate).toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                  })}
                </p>
                <p className="text-xs text-blue-100 mt-1">{daysUntilRenewal} days remaining</p>
              </div>

              {/* Status */}
              <div className="bg-white/20 rounded-lg p-4 backdrop-blur-sm">
                <p className="text-sm text-blue-100 mb-1">Status</p>
                <p className="text-xl font-bold flex items-center gap-2">
                  <span className="w-3 h-3 bg-green-400 rounded-full"></span>
                  {current.status}
                </p>
              </div>
            </div>
          </div>

          {/* Action Button */}
          <div className="mt-8 pt-8 border-t border-white/20 flex gap-4">
            <button
              onClick={() => navigate('/subscriptions')}
              className="flex-1 px-6 py-2 bg-white text-blue-600 rounded-lg font-semibold hover:bg-blue-50 transition-colors"
            >
              Upgrade Plan
            </button>
          </div>
        </div>

        {/* Billing Summary Cards */}
        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-blue-100 rounded-lg">
                <DollarSign className="w-6 h-6 text-blue-600" />
              </div>
              <h3 className="font-semibold text-gray-900">Monthly Cost</h3>
            </div>
            <p className="text-3xl font-bold text-gray-900">₱{current.planPrice}</p>
          </div>

          <div className="bg-white rounded-xl shadow p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-green-100 rounded-lg">
                <TrendingUp className="w-6 h-6 text-green-600" />
              </div>
              <h3 className="font-semibold text-gray-900">Savings</h3>
            </div>
            <p className="text-3xl font-bold text-green-600">{current.discountPercentage}%</p>
          </div>

          <div className="bg-white rounded-xl shadow p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-amber-100 rounded-lg">
                <Calendar className="w-6 h-6 text-amber-600" />
              </div>
              <h3 className="font-semibold text-gray-900">Member Since</h3>
            </div>
            <p className="text-2xl font-bold text-gray-900">
              {new Date(current.startDate).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
              })}
            </p>
          </div>
        </div>

        {/* Recent Billing History */}
        {history.length > 0 && (
          <div className="bg-white rounded-2xl shadow-lg p-8">
            <h3 className="text-lg font-semibold text-gray-900 mb-6">Billing History</h3>

            <div className="space-y-4">
              {history.slice(0, 5).map((sub) => (
                <div key={sub.userSubscriptionId} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                  <div>
                    <p className="font-semibold text-gray-900">{sub.planType} Plan</p>
                    <p className="text-sm text-gray-600">
                      {new Date(sub.startDate).toLocaleDateString()} - {new Date(sub.expiryDate).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-gray-900">₱{sub.planPrice}</p>
                    <p className={`text-sm font-semibold ${sub.status === 'ACTIVE' ? 'text-green-600' : 'text-gray-600'}`}>
                      {sub.status}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {history.length > 5 && (
              <button
                onClick={() => navigate('/subscription/history')}
                className="w-full mt-6 px-6 py-2 text-blue-600 font-semibold hover:bg-blue-50 rounded-lg"
              >
                View All Transactions
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}




