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
  Menu,
  X,
  LogOut,
  Settings,
  Heart,
  ChevronDown,
  Crown,
  Bell,
  Store,
  Palette,
  Package,
  Users,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import { NotificationDropdown } from "./NotificationDropdown";

export function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);

  // Derive nav context from current path so ADMIN
  // viewing /customer still get the customer nav links.
  const currentView = user?.role === "ADMIN" 
    ? (location.pathname.startsWith("/admin") || location.pathname === "/settings" ? "admin" : "customer")
    : user?.role === "SHOP_OWNER" 
    ? "shop" 
    : "customer";

  const navItems =
    currentView === "admin"
      ? [
          { label: "Dashboard", icon: Home, href: "/admin" },
          { label: "Users", icon: Users, href: "/admin/users" },
          { label: "Orders", icon: Package, href: "/admin/orders" },
          { label: "Subscriptions", icon: Crown, href: "/admin/subscriptions" },
        ]
      : currentView === "shop"
      ? [
          { label: "Dashboard", icon: Store, href: "/shop" },
          { label: "Orders", icon: ShoppingBag, href: "/shop/orders" },
          { label: "Services", icon: Palette, href: "/shop/services" },
          { label: "Subscriptions", icon: Crown, href: "/shop/subscriptions" },
        ]
      : [
          { label: "Home", icon: Home, href: "/customer" },
          { label: "Services", icon: ShoppingBag, href: "/services" },
          { label: "Subscriptions", icon: Crown, href: "/subscriptions" },
          { label: "My Orders", icon: Clock, href: "/my-orders" },
          { label: "Wallet", icon: WalletIcon, href: "/wallet" },
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
          <Link to={currentView === "admin" ? "/admin" : currentView === "shop" ? "/shop" : "/customer"} className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-teal-500 to-teal-600 flex items-center justify-center shadow-md shadow-teal-200">
              <Droplets className="w-5 h-5 text-white" />
            </div>
            <span className="font-bold text-xl text-slate-900 tracking-tight">WashMate</span>
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
              <NotificationDropdown />
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
                          <Link
                            to="/payment/history"
                            onClick={() => setProfileMenuOpen(false)}
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Payment History
                          </Link>
                          <Link
                            to="/settings"
                            onClick={() => setProfileMenuOpen(false)}
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Settings
                          </Link>
                        </>
                      )}
                      {currentView === "shop" && (
                        <>
                          <Link
                            to="/settings"
                            onClick={() => setProfileMenuOpen(false)}
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Account Settings
                          </Link>
                        </>
                      )}
                      {currentView === "admin" && (
                        <>
                          <Link
                            to="/admin/settings"
                            onClick={() => setProfileMenuOpen(false)}
                            className="block px-3 py-2 text-sm text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                          >
                            Account Settings
                          </Link>
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
