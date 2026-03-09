import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { ShieldAlert, Users, Store, Activity, MoreVertical, DollarSign, ArrowUpRight } from "lucide-react";
import { Button } from "../components/Button";

export function AdminDashboard() {
  const { user } = useAuth();

  const data = [
    { name: "Jan", users: 4000, shops: 240, amt: 2400 },
    { name: "Feb", users: 3000, shops: 139, amt: 2210 },
    { name: "Mar", users: 2000, shops: 980, amt: 2290 },
    { name: "Apr", users: 2780, shops: 390, amt: 2000 },
    { name: "May", users: 1890, shops: 480, amt: 2181 },
    { name: "Jun", users: 2390, shops: 380, amt: 2500 },
    { name: "Jul", users: 3490, shops: 430, amt: 2100 },
  ];

  return (
    <div className="space-y-8 w-full">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">Platform Overview</h1>
          <p className="text-slate-500 mt-1">Administrator dashboard. Signed in as {user?.email}.</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" className="text-slate-700 bg-white">
            <Activity className="w-4 h-4 mr-2" />
            System Status
          </Button>
          <Button className="bg-slate-900 hover:bg-slate-800 text-white">
            <ShieldAlert className="w-4 h-4 mr-2" />
            Admin Actions
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="shadow-sm border-slate-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Total Users</CardTitle>
            <div className="w-8 h-8 rounded-full bg-blue-50 flex items-center justify-center">
              <Users className="w-4 h-4 text-blue-600" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-slate-900">24,592</div>
            <p className="text-sm text-emerald-600 flex items-center mt-2">
              <ArrowUpRight className="w-4 h-4 mr-1" />
              +12% vs last month
            </p>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-slate-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Partner Shops</CardTitle>
            <div className="w-8 h-8 rounded-full bg-teal-50 flex items-center justify-center">
              <Store className="w-4 h-4 text-teal-600" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-slate-900">1,245</div>
            <p className="text-sm text-emerald-600 flex items-center mt-2">
              <ArrowUpRight className="w-4 h-4 mr-1" />
              +3% vs last month
            </p>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-slate-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Platform Volume</CardTitle>
            <div className="w-8 h-8 rounded-full bg-indigo-50 flex items-center justify-center">
              <DollarSign className="w-4 h-4 text-indigo-600" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-slate-900">$2.4M</div>
            <p className="text-sm text-emerald-600 flex items-center mt-2">
              <ArrowUpRight className="w-4 h-4 mr-1" />
              +8% vs last month
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2 shadow-sm border-slate-200">
          <CardHeader>
            <CardTitle className="text-xl text-slate-800">Growth Overview</CardTitle>
          </CardHeader>
          <CardContent className="px-2">
            <div className="h-[350px] w-full mt-4">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={data}
                  margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid key="grid" strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis key="xaxis" dataKey="name" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis key="yaxis" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
                  <Tooltip 
                    cursor={{fill: '#f1f5f9'}}
                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)' }}
                  />
                  <Bar key="bar-users" dataKey="users" fill="#0d9488" radius={[4, 4, 0, 0]} name="New Users" />
                  <Bar key="bar-shops" dataKey="shops" fill="#3b82f6" radius={[4, 4, 0, 0]} name="New Shops" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-slate-200">
          <CardHeader>
            <CardTitle className="text-xl text-slate-800">Recent Applications</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="divide-y divide-slate-100">
              {[
                { name: "Sparkle Cleaners", city: "New York", status: "Pending" },
                { name: "Fresh Fold", city: "Chicago", status: "Reviewing" },
                { name: "City Laundry", city: "Seattle", status: "Pending" },
                { name: "Wash & Go", city: "Austin", status: "Reviewing" },
              ].map((shop, i) => (
                <div key={i} className="py-4 flex items-center justify-between first:pt-0 last:pb-0">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-md bg-slate-100 flex items-center justify-center">
                      <Store className="w-5 h-5 text-slate-600" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-900">{shop.name}</p>
                      <p className="text-xs text-slate-500">{shop.city}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">
                      {shop.status}
                    </span>
                    <Button variant="ghost" size="icon" className="h-8 w-8 text-slate-400">
                      <MoreVertical className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
            <Button className="w-full mt-4" variant="outline">
              View All Applications
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}