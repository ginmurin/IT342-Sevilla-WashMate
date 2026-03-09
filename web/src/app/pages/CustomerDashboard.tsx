import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { ShoppingBag, Clock, CheckCircle2, ChevronRight, PackageSearch, MapPin } from "lucide-react";

export function CustomerDashboard() {
  const { user } = useAuth();

  const activeOrders = [
    { id: "ORD-2023-01", status: "In Progress", type: "Wash & Fold", date: "Today", price: "$24.50" },
    { id: "ORD-2023-02", status: "Ready for Pickup", type: "Dry Cleaning", date: "Yesterday", price: "$45.00" },
  ];

  return (
    <div className="space-y-6 max-w-5xl mx-auto w-full">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">My Laundry</h1>
          <p className="text-slate-500 mt-1">Welcome back, {user?.name}. Here's the status of your orders.</p>
        </div>
        <Button className="bg-teal-600 hover:bg-teal-700 w-full md:w-auto">
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
            <div className="text-4xl font-bold">2</div>
            <p className="text-teal-100 text-sm mt-1">1 ready for pickup</p>
          </CardContent>
        </Card>
        
        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-medium text-slate-700">Loyalty Points</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-slate-900">450</div>
            <p className="text-slate-500 text-sm mt-1">50 points until next reward</p>
          </CardContent>
        </Card>
        
        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-medium text-slate-700">Total Spent</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-slate-900">$340</div>
            <p className="text-slate-500 text-sm mt-1">Across 12 orders</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-8">
        <Card className="lg:col-span-2 shadow-sm border-slate-200">
          <CardHeader className="border-b border-slate-100 pb-4">
            <CardTitle className="text-xl text-slate-800">Recent Orders</CardTitle>
            <CardDescription>View and track your current laundry orders.</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <ul className="divide-y divide-slate-100">
              {activeOrders.map((order, i) => (
                <li key={i} className="flex items-center justify-between p-6 hover:bg-slate-50 transition-colors">
                  <div className="flex items-start gap-4">
                    <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center shrink-0">
                      {order.status === "Ready for Pickup" ? (
                        <CheckCircle2 className="w-5 h-5 text-teal-600" />
                      ) : (
                        <Clock className="w-5 h-5 text-amber-500" />
                      )}
                    </div>
                    <div>
                      <p className="font-semibold text-slate-900">{order.type}</p>
                      <div className="flex items-center gap-2 text-sm text-slate-500 mt-1">
                        <span>{order.id}</span>
                        <span>•</span>
                        <span>{order.date}</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-right hidden sm:block">
                      <p className="font-medium text-slate-900">{order.price}</p>
                      <p className={order.status === "Ready for Pickup" ? "text-teal-600 text-sm font-medium" : "text-amber-500 text-sm font-medium"}>
                        {order.status}
                      </p>
                    </div>
                    <Button variant="ghost" size="icon" className="text-slate-400">
                      <ChevronRight className="w-5 h-5" />
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-slate-200">
          <CardHeader>
            <CardTitle className="text-xl text-slate-800">Find a Shop</CardTitle>
            <CardDescription>Nearby WashMate partners</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="bg-slate-100 rounded-lg p-6 text-center border border-slate-200 border-dashed mb-4">
              <MapPin className="w-8 h-8 text-slate-400 mx-auto mb-2" />
              <p className="text-sm font-medium text-slate-600">Location access needed</p>
            </div>
            <Button variant="outline" className="w-full">
              <PackageSearch className="w-4 h-4 mr-2" />
              Search manually
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
