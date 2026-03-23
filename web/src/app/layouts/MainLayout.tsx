import { useState, useEffect } from "react";
import { Outlet, Link, useNavigate, useLocation } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Navbar } from "../components/Navbar";
import { LogOut, Menu, X, Droplets } from "lucide-react";

export function MainLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const isHome = location.pathname === "/";
  const isAuthPage = ["/login", "/register", "/forgot-password", "/verify-email"].includes(location.pathname);
  const showNewNav = user && !isHome && !isAuthPage;

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 50);
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    const titles: Record<string, string> = {
      "/": "WashMate — Fresh Laundry, Delivered",
      "/login": "WashMate | Sign In",
      "/register": "WashMate | Create Account",
      "/forgot-password": "WashMate | Forgot Password",
      "/verify-email": "WashMate | Verify Email",
      "/customer": "WashMate | Dashboard",
      "/services": "WashMate | Services & Plans",
      "/my-orders": "WashMate | My Orders",
      "/wallet": "WashMate | Wallet",
      "/order/laundry-details": "WashMate | Laundry Details",
      "/order/schedule-address": "WashMate | Schedule & Address",
      "/order/payment-review": "WashMate | Review & Pay",
      "/payment/checkout": "WashMate | Checkout",
      "/payment/success": "WashMate | Payment Successful",
      "/payment/error": "WashMate | Payment Failed",
      "/payment/history": "WashMate | Payment History",
      "/shop": "WashMate | Shop Dashboard",
      "/admin": "WashMate | Admin Dashboard",
    };
    document.title = titles[location.pathname] ?? "WashMate";
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const getDashboardLink = () => {
    if (!user) return "/";
    switch (user.role) {
      case "CUSTOMER": return "/customer";
      case "SHOPOWNER": return "/shop";
      case "ADMIN": return "/admin";
      default: return "/";
    }
  };

  const headerBg = isHome
    ? scrolled
      ? "bg-gray-900/80 backdrop-blur-xl border-b border-white/5 shadow-lg"
      : "bg-transparent border-transparent"
    : "border-b border-gray-200 bg-white/95 backdrop-blur-md shadow-sm";

  const navLinks = isHome
    ? [
        { label: "Features", href: "#features", to: null },
        { label: "How It Works", href: "#how-it-works", to: null },
        { label: "Services", href: null, to: "/services" },
        { label: "Subscriptions", href: null, to: "/subscriptions" },
      ]
    : [];

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col font-sans">
      {showNewNav && <Navbar />}
      {!showNewNav && (
        <header className={`fixed top-0 z-50 w-full transition-all duration-300 ${headerBg}`}>
          <div className="container mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Link to={getDashboardLink()} className="flex items-center gap-2">
                <Droplets className={`h-6 w-6 ${isHome ? "text-teal-400" : "text-teal-600"}`} />
                <span className={`text-xl font-bold tracking-tight ${isHome ? "text-white" : "text-slate-900"}`}>
                  WashMate
                </span>
              </Link>
            </div>

            {/* Desktop Nav Links */}
            {navLinks.length > 0 && (
              <nav className="hidden md:flex items-center gap-8">
                {navLinks.map((link) =>
                  link.to ? (
                    <Link
                      key={link.label}
                      to={link.to}
                      className="text-sm font-medium text-gray-300 hover:text-white transition-colors"
                    >
                      {link.label}
                    </Link>
                  ) : (
                    <a
                      key={link.label}
                      href={link.href!}
                      className="text-sm font-medium text-gray-300 hover:text-white transition-colors"
                    >
                      {link.label}
                    </a>
                  )
                )}
              </nav>
            )}

            {/* Desktop Auth Buttons */}
            <div className="hidden sm:flex items-center gap-4">
              {user ? (
                <div className="flex items-center gap-4">
                  <span className={`text-sm font-medium ${isHome ? "text-gray-300" : "text-gray-700"}`}>
                    Hi, {user.firstName}
                  </span>
                  <button
                    onClick={handleLogout}
                    className={`inline-flex items-center gap-2 text-sm transition-colors ${isHome ? "text-gray-400 hover:text-white" : "text-gray-500 hover:text-gray-900"}`}
                  >
                    <LogOut className="h-4 w-4" />
                    Log out
                  </button>
                </div>
              ) : (
                <div className="flex items-center gap-3">
                  <Link
                    to="/login"
                    className={`text-sm font-medium px-4 py-2 rounded-lg transition-colors ${isHome ? "text-gray-300 hover:text-white" : "text-gray-600 hover:text-blue-600"}`}
                  >
                    Log in
                  </Link>
                  <Link
                    to="/register"
                    className="text-sm font-medium bg-blue-600 text-white px-5 py-2 rounded-lg hover:bg-blue-500 transition-colors shadow-sm"
                  >
                    Get Started
                  </Link>
                </div>
              )}
            </div>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className={`sm:hidden p-2 rounded-lg transition-colors ${isHome ? "text-white hover:bg-white/10" : "text-gray-600 hover:bg-gray-100"}`}
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>

          {/* Mobile Menu Drawer */}
          {mobileMenuOpen && (
            <div className={`sm:hidden border-t ${isHome ? "bg-gray-900/95 backdrop-blur-xl border-white/5" : "bg-white border-gray-200"}`}>
              <div className="px-4 py-4 space-y-3">
                {navLinks.map((link) =>
                  link.to ? (
                    <Link
                      key={link.label}
                      to={link.to}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`block py-2 text-sm font-medium ${isHome ? "text-gray-300" : "text-gray-600"}`}
                    >
                      {link.label}
                    </Link>
                  ) : (
                    <a
                      key={link.label}
                      href={link.href!}
                      onClick={() => setMobileMenuOpen(false)}
                      className={`block py-2 text-sm font-medium ${isHome ? "text-gray-300" : "text-gray-600"}`}
                    >
                      {link.label}
                    </a>
                  )
                )}
                {user ? (
                  <>
                    <p className={`py-2 text-sm ${isHome ? "text-gray-400" : "text-gray-500"}`}>
                      Hi, {user.firstName}
                    </p>
                    <button
                      onClick={handleLogout}
                      className={`flex items-center gap-2 py-2 text-sm ${isHome ? "text-gray-400" : "text-gray-500"}`}
                    >
                      <LogOut className="w-4 h-4" /> Log out
                    </button>
                  </>
                ) : (
                  <div className="flex flex-col gap-2 pt-2">
                    <Link to="/login" className={`py-2 text-sm font-medium ${isHome ? "text-gray-300" : "text-gray-600"}`}>
                      Log in
                    </Link>
                    <Link to="/register" className="text-sm font-medium bg-blue-600 text-white px-4 py-2.5 rounded-lg text-center">
                      Get Started
                    </Link>
                  </div>
                )}
              </div>
            </div>
          )}
        </header>
      )}

      <main className={`flex-1 flex flex-col ${isHome || isAuthPage ? "" : showNewNav ? "" : "container mx-auto px-4 py-8 pt-24"}`}>
        <Outlet />
      </main>
    </div>
  );
}
