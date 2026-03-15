import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "./Button";
import {
  Droplets,
  Home,
  ShoppingBag,
  Clock,
  Wallet as WalletIcon,
  User,
  Bell,
  Menu,
  X,
  LogOut,
  Settings,
  Heart,
  ChevronDown,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

export function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  // Derive nav context from current path so ADMIN/SHOPOWNER
  // viewing /customer still get the customer nav links.
  const currentView = location.pathname.startsWith("/admin")
    ? "admin"
    : location.pathname.startsWith("/shop")
    ? "shopowner"
    : "customer";

  const navItems =
    currentView === "customer"
      ? [
          { label: "Home", icon: Home, href: "/customer" },
          { label: "Services", icon: ShoppingBag, href: "/services" },
          { label: "My Orders", icon: Clock, href: "/my-orders" },
          { label: "Wallet", icon: WalletIcon, href: "/wallet" },
        ]
      : currentView === "shopowner"
      ? [
          { label: "Dashboard", icon: Home, href: "/shop" },
          { label: "Orders", icon: ShoppingBag, href: "/shop/orders" },
          { label: "Analytics", icon: Heart, href: "/shop/analytics" },
          { label: "Settings", icon: Settings, href: "/shop/settings" },
        ]
      : [
          { label: "Dashboard", icon: Home, href: "/admin" },
          { label: "Users", icon: User, href: "/admin/users" },
          { label: "Shops", icon: ShoppingBag, href: "/admin/shops" },
          { label: "Settings", icon: Settings, href: "/admin/settings" },
        ];

  const notifications = [
    { id: 1, message: "Your order #ORD-001 is ready for pickup", time: "5 mins ago" },
    { id: 2, message: "New service available: Premium Dry Clean", time: "2 hours ago" },
    { id: 3, message: "Wallet loaded successfully: ₱2,000", time: "1 day ago" },
  ];

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const isActive = (href: string) => location.pathname === href;

  return (
    <nav className="fixed top-0 left-0 right-0 bg-white border-b border-slate-200 shadow-sm z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to={currentView === "customer" ? "/customer" : currentView === "shopowner" ? "/shop" : "/admin"} className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-teal-600 flex items-center justify-center">
              <Droplets className="w-5 h-5 text-white" />
            </div>
            <span className="font-bold text-lg text-slate-900">WashMate</span>
          </Link>

          {/* Desktop Nav */}
          <div className="hidden md:flex items-center gap-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.href}
                  to={item.href}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                    isActive(item.href)
                      ? "bg-teal-50 text-teal-600 font-medium"
                      : "text-slate-600 hover:bg-slate-100"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span className="text-sm font-medium">{item.label}</span>
                </Link>
              );
            })}
          </div>

          {/* Right Section */}
          <div className="flex items-center gap-4">
            {/* Notifications */}
            {currentView === "customer" && (
              <div className="relative hidden sm:block">
                <button
                  onClick={() => setNotificationsOpen(!notificationsOpen)}
                  className="relative p-2 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
                >
                  <Bell className="w-5 h-5" />
                  <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
                </button>

                <AnimatePresence>
                  {notificationsOpen && (
                    <motion.div
                      initial={{ opacity: 0, y: -8 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -8 }}
                      className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl border border-slate-200 overflow-hidden"
                    >
                      <div className="max-h-96 overflow-y-auto">
                        {notifications.length === 0 ? (
                          <div className="p-8 text-center">
                            <Bell className="w-8 h-8 text-slate-300 mx-auto mb-2" />
                            <p className="text-slate-500 text-sm">No notifications</p>
                          </div>
                        ) : (
                          <div className="divide-y divide-slate-100">
                            {notifications.map((notif) => (
                              <div
                                key={notif.id}
                                className="p-4 hover:bg-slate-50 cursor-pointer transition-colors"
                              >
                                <p className="text-sm font-medium text-slate-900">{notif.message}</p>
                                <p className="text-xs text-slate-500 mt-1">{notif.time}</p>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                      <div className="bg-slate-50 p-3 text-center border-t border-slate-100">
                        <a href="#" className="text-xs font-medium text-teal-600 hover:text-teal-700">
                          View all notifications
                        </a>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            )}

            {/* Profile Dropdown */}
            <div className="relative">
              <button
                onClick={() => setProfileMenuOpen(!profileMenuOpen)}
                className="flex items-center gap-2 pl-3 pr-2 py-1.5 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
              >
                <div className="w-8 h-8 rounded-full bg-teal-100 flex items-center justify-center">
                  <User className="w-4 h-4 text-teal-600" />
                </div>
                <span className="text-sm font-medium hidden sm:inline">
                  {user?.firstName}
                </span>
                <ChevronDown className="w-4 h-4 hidden sm:block" />
              </button>

              <AnimatePresence>
                {profileMenuOpen && (
                  <motion.div
                    initial={{ opacity: 0, y: -8 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -8 }}
                    className="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-xl border border-slate-200 overflow-hidden"
                  >
                    <div className="p-4 border-b border-slate-100">
                      <p className="text-sm font-semibold text-slate-900">{user?.firstName} {user?.lastName}</p>
                      <p className="text-xs text-slate-500">{user?.email}</p>
                    </div>
                    <div className="space-y-1 p-2">
                      {currentView === "customer" && (
                        <>
                          <a
                            href="#"
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Profile Settings
                          </a>
                          <a
                            href="#"
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Saved Addresses
                          </a>
                          <a
                            href="#"
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Payment Methods
                          </a>
                        </>
                      )}
                      {currentView === "shopowner" && (
                        <>
                          <a
                            href="#"
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Shop Settings
                          </a>
                          <a
                            href="#"
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Financial Reports
                          </a>
                        </>
                      )}
                    </div>
                    <div className="border-t border-slate-100 p-2">
                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      >
                        <LogOut className="w-4 h-4" />
                        Logout
                      </button>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-slate-600 hover:bg-slate-100 rounded-lg"
            >
              {mobileMenuOpen ? (
                <X className="w-5 h-5" />
              ) : (
                <Menu className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        <AnimatePresence>
          {mobileMenuOpen && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              className="md:hidden border-t border-slate-200 bg-slate-50"
            >
              <div className="px-4 py-3 space-y-2">
                {navItems.map((item) => {
                  const Icon = item.icon;
                  return (
                    <Link
                      key={item.href}
                      to={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`flex items-center gap-3 px-4 py-2 rounded-lg transition-colors ${
                        isActive(item.href)
                          ? "bg-teal-100 text-teal-700 font-medium"
                          : "text-slate-600 hover:bg-slate-200"
                      }`}
                    >
                      <Icon className="w-5 h-5" />
                      <span>{item.label}</span>
                    </Link>
                  );
                })}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </nav>
  );
}
