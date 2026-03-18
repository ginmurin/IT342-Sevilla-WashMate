import { useAuth } from "../contexts/AuthContext";
import { Store, DollarSign, ShoppingBag, Users, Construction } from "lucide-react";
import { motion } from "motion/react";

const PLACEHOLDERS = [
  { label: "Total Revenue", icon: DollarSign, color: "bg-teal-50 text-teal-600" },
  { label: "Active Orders", icon: ShoppingBag, color: "bg-blue-50 text-blue-600" },
  { label: "Customers", icon: Users, color: "bg-purple-50 text-purple-600" },
];

export function ShopDashboard() {
  const { user } = useAuth();

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-slate-50 to-slate-100 p-6 md:p-10">
      <div className="max-w-5xl mx-auto space-y-8">

        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row md:items-center justify-between gap-4"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-teal-600 flex items-center justify-center shadow-md">
              <Store className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-slate-900">Partner Dashboard</h1>
              <p className="text-sm text-slate-500">
                Welcome, <span className="font-medium text-slate-700">{user?.firstName}</span>
              </p>
            </div>
          </div>
          <span className="inline-flex items-center gap-2 bg-amber-100 text-amber-700 text-xs font-semibold px-3 py-1.5 rounded-full border border-amber-200 w-fit">
            <Construction className="w-3.5 h-3.5" />
            Under Development
          </span>
        </motion.div>

        {/* Stat placeholders */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-5"
        >
          {PLACEHOLDERS.map((stat) => {
            const Icon = stat.icon;
            return (
              <div
                key={stat.label}
                className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 flex items-center gap-4"
              >
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 ${stat.color}`}>
                  <Icon className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm text-slate-500">{stat.label}</p>
                  <p className="text-3xl font-bold text-slate-300 mt-0.5">—</p>
                </div>
              </div>
            );
          })}
        </motion.div>

        {/* Coming soon panel */}
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="bg-white rounded-2xl border border-slate-200 shadow-sm p-12 text-center"
        >
          <div className="w-16 h-16 bg-teal-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <Store className="w-8 h-8 text-teal-400" />
          </div>
          <h2 className="text-xl font-bold text-slate-700 mb-2">Shop management coming soon</h2>
          <p className="text-slate-400 text-sm max-w-md mx-auto">
            Order management, revenue tracking, customer history, and shop settings
            will be available here once the backend is connected.
          </p>
        </motion.div>

      </div>
    </div>
  );
}
