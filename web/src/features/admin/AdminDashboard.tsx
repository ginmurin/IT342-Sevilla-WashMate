import { useEffect, useState } from "react";
import { useAuth } from "@/features/auth/AuthContext";
import { 
  ShieldCheck, 
  Users, 
  Store, 
  ShoppingBag, 
  DollarSign, 
  TrendingUp, 
  ChevronRight,
  Loader2,
  AlertCircle,
  ArrowUpRight,
  Activity
} from "lucide-react";
import { motion } from "motion/react";
import { adminAPI } from "@/features/shared/utils/api";
import { useNavigate } from "react-router";
import { Card, CardContent } from "@/features/shared/components/Card";
import { Button } from "@/features/shared/components/Button";

interface AdminStats {
  totalUsers: number;
  totalShops: number;
  totalOrders: number;
  totalRevenue: number;
}

export function AdminDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setIsLoading(true);
        const data = await adminAPI.getGlobalStats();
        setStats(data);
      } catch (err) {
        console.error("Failed to fetch admin stats:", err);
        setError("Failed to load platform statistics");
      } finally {
        setIsLoading(false);
      }
    };
    fetchStats();
  }, []);

  const formatCurrency = (n: number) =>
    `₱${n.toLocaleString("en-PH", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

  if (isLoading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] bg-slate-50 flex items-center justify-center">
        <Loader2 className="w-10 h-10 text-teal-600 animate-spin" />
      </div>
    );
  }

  const statCards = [
    {
      label: "Total Revenue",
      value: formatCurrency(stats?.totalRevenue || 0),
      icon: DollarSign,
      color: "from-emerald-500 to-emerald-600",
      shadow: "shadow-emerald-200",
      description: "Platform lifetime earnings"
    },
    {
      label: "System Users",
      value: stats?.totalUsers.toString() || "0",
      icon: Users,
      color: "from-blue-500 to-blue-600",
      shadow: "shadow-blue-200",
      description: "Registered customers & owners"
    },
    {
      label: "Total Orders",
      value: stats?.totalOrders.toString() || "0",
      icon: ShoppingBag,
      color: "from-indigo-500 to-indigo-600",
      shadow: "shadow-indigo-200",
      description: "Successful laundry requests"
    }
  ];

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 px-6 pb-12 pt-20 md:px-10">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row md:items-center justify-between gap-4"
        >
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-slate-900 flex items-center justify-center shadow-lg shadow-slate-200">
              <ShieldCheck className="w-7 h-7 text-white" />
            </div>
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-slate-900">Platform Overview</h1>
              <p className="text-sm text-slate-500">
                Welcome, Admin <span className="font-semibold text-slate-700">{user?.firstName}</span>. System is healthy.
              </p>
            </div>
          </div>
          <div className="flex gap-3">
            <Button 
              variant="outline" 
              className="border-slate-200 bg-white"
              onClick={() => navigate("/admin/users")}
            >
              Manage Users
            </Button>
            <Button 
              className="bg-teal-600 hover:bg-teal-700 text-white shadow-lg shadow-teal-200"
              onClick={() => navigate("/admin/orders")}
            >
              All Orders
            </Button>
          </div>
        </motion.div>

        {error && (
          <div className="bg-red-50 border border-red-100 text-red-600 p-4 rounded-2xl flex items-center gap-3">
            <AlertCircle className="w-5 h-5" />
            <p className="text-sm font-medium">{error}</p>
          </div>
        )}

        {/* Quick Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
          {statCards.map((card, i) => (
            <motion.div
              key={card.label}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
            >
              <Card className="border-slate-200/80 shadow-sm hover:shadow-md transition-all group cursor-default">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${card.color} flex items-center justify-center text-white shadow-lg ${card.shadow} group-hover:scale-110 transition-transform`}>
                      <card.icon className="w-6 h-6" />
                    </div>
                    <ArrowUpRight className="w-5 h-5 text-slate-300" />
                  </div>
                  <h3 className="text-3xl font-extrabold text-slate-900 tracking-tight">{card.value}</h3>
                  <p className="text-sm font-semibold text-slate-400 mt-1">{card.label}</p>
                  <p className="text-xs text-slate-500 mt-2 border-t border-slate-50 pt-2">{card.description}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>

        {/* Management Shortcuts */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* User Management CTA */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.4 }}
          >
            <Card className="border-slate-200/80 shadow-sm overflow-hidden h-full">
              <CardContent className="p-0">
                <div className="p-8 space-y-6">
                  <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
                    <Users className="w-6 h-6 text-blue-600" />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold text-slate-900">User Management</h3>
                    <p className="text-slate-500 mt-2 text-sm leading-relaxed">
                      Monitor all registered users, adjust permissions, and manage account statuses across the platform.
                    </p>
                  </div>
                  <Button 
                    variant="ghost" 
                    className="w-full justify-between text-blue-600 hover:bg-blue-50 group font-bold"
                    onClick={() => navigate("/admin/users")}
                  >
                    Go to User List
                    <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* System Control CTA */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.5 }}
          >
            <Card className="border-slate-200/80 shadow-sm overflow-hidden h-full bg-slate-900 text-white">
              <CardContent className="p-0">
                <div className="p-8 space-y-6">
                  <div className="w-12 h-12 bg-white/10 rounded-xl flex items-center justify-center">
                    <TrendingUp className="w-6 h-6 text-teal-400" />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold">Platform Revenue</h3>
                    <p className="text-slate-400 mt-2 text-sm leading-relaxed">
                      View system-wide transaction history, track subscription earnings, and manage global service pricing.
                    </p>
                  </div>
                  <Button 
                    variant="ghost" 
                    className="w-full justify-between text-teal-400 hover:bg-white/5 group font-bold"
                    onClick={() => navigate("/admin/orders")}
                  >
                    View All Transactions
                    <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </div>

        {/* Activity Feed Placeholder */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
        >
          <Card className="border-slate-200/80 shadow-sm">
            <CardContent className="p-8 flex flex-col items-center text-center space-y-4">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center">
                <Activity className="w-8 h-8 text-slate-400" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-slate-900">System Logs</h3>
                <p className="text-slate-500 text-sm max-w-sm mx-auto">
                  Audit logs and real-time system activity monitoring will be integrated in the next update.
                </p>
              </div>
            </CardContent>
          </Card>
        </motion.div>

      </div>
    </div>
  );
}



