import { useNavigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { ShoppingBag, PackageSearch, MapPin } from "lucide-react";

export function CustomerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="space-y-6 max-w-5xl mx-auto w-full">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">My Laundry</h1>
          <p className="text-slate-500 mt-1">Welcome back, {user?.first_name}. Here's the status of your orders.</p>
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

        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-medium text-slate-700">Loyalty Points</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-slate-900">0</div>
            <p className="text-slate-500 text-sm mt-1">Start earning with your first order</p>
          </CardContent>
        </Card>

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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-8">
        <Card className="lg:col-span-2 shadow-sm border-slate-200">
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