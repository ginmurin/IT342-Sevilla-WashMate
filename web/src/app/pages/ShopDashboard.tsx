import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { Settings, Users, Droplets, DollarSign, TrendingUp, Filter } from "lucide-react";

export function ShopDashboard() {
  const { user } = useAuth();

  const data = [
    { name: "Mon", revenue: 400, orders: 24 },
    { name: "Tue", revenue: 300, orders: 13 },
    { name: "Wed", revenue: 550, orders: 38 },
    { name: "Thu", revenue: 450, orders: 29 },
    { name: "Fri", revenue: 700, orders: 48 },
    { name: "Sat", revenue: 850, orders: 60 },
    { name: "Sun", revenue: 650, orders: 45 },
  ];

  return (
    <div className="space-y-8 w-full">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">Partner Dashboard</h1>
          <p className="text-slate-500 mt-1">Hello {user?.first_name}, here is your shop's performance this week.</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" className="text-slate-700 bg-white">
            <Filter className="w-4 h-4 mr-2" />
            This Week
          </Button>
          <Button className="bg-teal-600 hover:bg-teal-700">
            <Settings className="w-4 h-4 mr-2" />
            Manage Shop
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="shadow-sm border-slate-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Total Revenue</CardTitle>
            <DollarSign className="w-4 h-4 text-slate-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">$3,900.00</div>
            <p className="text-xs text-teal-600 flex items-center mt-1">
              <TrendingUp className="w-3 h-3 mr-1" />
              +15.3% from last week
            </p>
          </CardContent>
        </Card>
        
        <Card className="shadow-sm border-slate-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Active Orders</CardTitle>
            <Droplets className="w-4 h-4 text-slate-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">42</div>
            <p className="text-xs text-slate-500 flex items-center mt-1">
              12 ready for pickup
            </p>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-slate-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">New Customers</CardTitle>
            <Users className="w-4 h-4 text-slate-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">18</div>
            <p className="text-xs text-teal-600 flex items-center mt-1">
              <TrendingUp className="w-3 h-3 mr-1" />
              +4 this week
            </p>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-slate-200">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Completion Rate</CardTitle>
            <Settings className="w-4 h-4 text-slate-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">98.5%</div>
            <p className="text-xs text-teal-600 flex items-center mt-1">
              Top 10% of partners
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2 shadow-sm border-slate-200">
          <CardHeader>
            <CardTitle className="text-xl text-slate-800">Revenue Overview</CardTitle>
          </CardHeader>
          <CardContent className="px-2">
            <div className="h-[300px] w-full mt-4">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart
                  data={data}
                  margin={{ top: 10, right: 30, left: 0, bottom: 0 }}
                >
                  <defs>
                    <linearGradient id="shopColorRevenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#0d9488" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#0d9488" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid key="grid" strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis key="xaxis" dataKey="name" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis key="yaxis" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(value) => `$${value}`} />
                  <Tooltip 
                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)' }}
                    itemStyle={{ color: '#0f172a' }}
                    formatter={(value: number) => [`$${value}`, "Revenue"]}
                  />
                  <Area key="area-revenue" type="monotone" dataKey="revenue" stroke="#0d9488" strokeWidth={3} fillOpacity={1} fill="url(#shopColorRevenue)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-slate-200">
          <CardHeader>
            <CardTitle className="text-xl text-slate-800">Recent Activity</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="flex items-center gap-4">
                  <div className="w-2 h-2 rounded-full bg-teal-500 mt-1 shrink-0"></div>
                  <div>
                    <p className="text-sm font-medium text-slate-900">New order received <span className="text-slate-500 font-normal">#ORD-{902 + i}</span></p>
                    <p className="text-xs text-slate-500">{i * 15} minutes ago</p>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="ghost" className="w-full mt-6 text-teal-600 hover:text-teal-700 hover:bg-teal-50">
              View All Orders
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}