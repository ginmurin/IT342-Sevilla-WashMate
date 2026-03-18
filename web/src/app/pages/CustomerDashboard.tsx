import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { ShoppingBag, Crown, Zap, Tag, Star } from "lucide-react";
import { getMySubscription, type SubscriptionData } from "../services/subscription";

export function CustomerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [subscription, setSubscription] = useState<SubscriptionData | null>(null);

  useEffect(() => {
    getMySubscription().then(setSubscription);
  }, []);

  const isPremium = subscription?.planType === "PREMIUM";

  return (
    <div className="space-y-6 max-w-5xl mx-auto w-full pt-20 px-4 pb-12">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">My Laundry</h1>
          <p className="text-slate-500 mt-1">Welcome back, {user?.firstName}. Here's the status of your orders.</p>
        </div>
        <Button
          className="bg-teal-600 hover:bg-teal-700 w-full md:w-auto"
          onClick={() => navigate("/order/laundry-details")}
        >
          <ShoppingBag className="w-4 h-4 mr-2" />
          New Order
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-gradient-to-br from-teal-500 to-teal-700 text-white border-none shadow-md">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-medium opacity-90">Active Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold">0</div>
            <p className="text-teal-100 text-sm mt-1">No active orders</p>
          </CardContent>
        </Card>

        {isPremium ? (
          <Card className="bg-gradient-to-br from-amber-400 via-amber-500 to-orange-500 text-white border-none shadow-lg">
            <CardHeader className="pb-2">
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg font-medium text-white/90">Subscription</CardTitle>
                <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full font-semibold tracking-wide">ACTIVE</span>
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2 mb-3">
                <Crown className="w-6 h-6 text-white drop-shadow" />
                <span className="text-2xl font-bold text-white">Premium</span>
              </div>
              <div className="space-y-1.5">
                <div className="flex items-center gap-2 text-white/90 text-sm">
                  <Tag className="w-3.5 h-3.5 shrink-0" />
                  <span>{subscription?.discountPercentage ?? 15}% off all orders</span>
                </div>
                <div className="flex items-center gap-2 text-white/90 text-sm">
                  <Zap className="w-3.5 h-3.5 shrink-0" />
                  <span>Priority order processing</span>
                </div>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card className="border-slate-200 shadow-sm">
            <CardHeader className="pb-2">
              <CardTitle className="text-lg font-medium text-slate-700">Subscription</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2 mb-1">
                <Star className="w-5 h-5 text-slate-300" />
                <span className="text-xl font-bold text-slate-900">Free</span>
              </div>
              <p className="text-slate-400 text-xs mb-3">No discounts · Standard processing</p>
              <button
                onClick={() => navigate("/services")}
                className="text-xs font-semibold text-amber-600 hover:text-amber-700 underline underline-offset-2"
              >
                Upgrade to Premium →
              </button>
            </CardContent>
          </Card>
        )}

        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-medium text-slate-700">Total Spent</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-slate-900">₱0</div>
            <p className="text-slate-500 text-sm mt-1">No completed orders yet</p>
          </CardContent>
        </Card>
      </div>

      <div className="mt-8">
        <Card className="shadow-sm border-slate-200">
          <CardHeader className="border-b border-slate-100 pb-4">
            <CardTitle className="text-xl text-slate-800">Recent Orders</CardTitle>
            <CardDescription>View and track your current laundry orders.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="py-10 text-center">
              <ShoppingBag className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <p className="text-slate-500 mb-4">You have no orders yet</p>
              <Button
                className="bg-teal-600 hover:bg-teal-700 text-white"
                onClick={() => navigate("/order/laundry-details")}
              >
                Place Your First Order
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}