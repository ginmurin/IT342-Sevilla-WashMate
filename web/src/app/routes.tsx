import { createBrowserRouter } from "react-router";
import { MainLayout } from "./layouts/MainLayout";
import { RouterProviders } from "./components/RouterProviders";
import { ProtectedRoute } from "./components/ProtectedRoute";

import { Home } from "./pages/Home";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import VerifyEmail from "./pages/VerifyEmail";
import AuthCallback from "./pages/AuthCallback";
import PaymentCheckout from "./pages/PaymentCheckout";
import PaymentSuccess from "./pages/PaymentSuccess";
import PaymentError from "./pages/PaymentError";
import PaymentHistory from "./pages/PaymentHistory";
import Settings from "./pages/Settings";
import Wallet from "./pages/Wallet";
import WalletPaymentReview from "./pages/WalletPaymentReview";
import WalletPaymentCheckout from "./pages/WalletPaymentCheckout";
import WalletPaymentSuccess from "./pages/WalletPaymentSuccess";
import WalletPaymentError from "./pages/WalletPaymentError";
import LaundryDetails from "./pages/LaundryDetails";
import ScheduleAddress from "./pages/ScheduleAddress";
import PaymentReview from "./pages/PaymentReview";
import MyOrders from "./pages/MyOrders";
import Services from "./pages/Services";
import Notifications from "./pages/Notifications";
import { CustomerDashboard } from "./pages/CustomerDashboard";
import { AdminDashboard } from "./pages/AdminDashboard";
import AdminUsers from "./pages/AdminUsers";
import AdminSubscriptionPlans from "./pages/AdminSubscriptionPlans";
import SubscriptionPlans from "./pages/SubscriptionPlans";
import SubscriptionUpgradeReview from "./pages/SubscriptionUpgradeReview";
import SubscriptionUpgradeCheckout from "./pages/SubscriptionUpgradeCheckout";
import SubscriptionUpgradeSuccess from "./pages/SubscriptionUpgradeSuccess";
import SubscriptionManagement from "./pages/SubscriptionManagement";
import SubscriptionHistory from "./pages/SubscriptionHistory";
import OrderDetail from "./pages/OrderDetail";
import { ShopDashboard } from "./pages/ShopDashboard";
import ShopOrders from "./pages/ShopOrders";
import ShopServices from "./pages/ShopServices";
import ShopSettings from "./pages/ShopSettings";

// Wrapper component that provides router-dependent contexts to MainLayout
function AppWithProviders() {
  return (
    <RouterProviders>
      <MainLayout />
    </RouterProviders>
  );
}

export const router = createBrowserRouter([
  {
    path: "/",
    Component: AppWithProviders,
    children: [
      {
        index: true,
        Component: Home,
      },
      {
        path: "login",
        Component: Login,
      },
      {
        path: "register",
        Component: Register,
      },
      {
        path: "forgot-password",
        Component: ForgotPassword,
      },
      {
        path: "verify-email",
        Component: VerifyEmail,
      },
      {
        path: "auth/callback",
        Component: AuthCallback,
      },
      {
        path: "payment/checkout",
        Component: PaymentCheckout,
      },
      {
        path: "payment/success",
        Component: PaymentSuccess,
      },
      {
        path: "payment/error",
        Component: PaymentError,
      },
      {
        path: "payment/history",
        Component: PaymentHistory,
      },
      {
        path: "settings",
        Component: Settings,
      },
      {
        path: "wallet",
        Component: Wallet,
      },
      {
        path: "wallet/payment-review",
        Component: WalletPaymentReview,
      },
      {
        path: "wallet/payment-checkout",
        Component: WalletPaymentCheckout,
      },
      {
        path: "wallet/payment-success",
        Component: WalletPaymentSuccess,
      },
      {
        path: "wallet/payment-error",
        Component: WalletPaymentError,
      },
      {
        path: "order/laundry-details",
        Component: LaundryDetails,
      },
      {
        path: "order/schedule-address",
        Component: ScheduleAddress,
      },
      {
        path: "order/payment-review",
        Component: PaymentReview,
      },
      {
        path: "my-orders",
        Component: MyOrders,
      },
      {
        path: "orders/:orderId",
        Component: OrderDetail,
      },
      {
        path: "services",
        Component: Services,
      },
      // Customer Routes — also accessible by ADMIN
      {
        element: <ProtectedRoute allowedRoles={["CUSTOMER", "ADMIN"]} />,
        children: [
          {
            path: "customer",
            Component: CustomerDashboard,
          },
          {
            path: "notifications",
            Component: Notifications,
          },
        ],
      },
      // Admin Routes
      {
        element: <ProtectedRoute allowedRoles={["ADMIN"]} />,
        children: [
          {
            path: "admin",
            Component: AdminDashboard,
          },
          {
            path: "admin/users",
            Component: AdminUsers,
          },
          {
            path: "admin/subscriptions",
            Component: AdminSubscriptionPlans,
          },
          {
            path: "admin/orders",
            Component: ShopOrders,
          },
          {
            path: "admin/settings",
            Component: Settings,
          },
        ],
      },
      // Shop Owner Routes
      {
        element: <ProtectedRoute allowedRoles={["SHOP_OWNER"]} />,
        children: [
          {
            path: "shop",
            Component: ShopDashboard,
          },
          {
            path: "shop/orders",
            Component: ShopOrders,
          },
          {
            path: "shop/services",
            Component: ShopServices,
          },
          {
            path: "shop/subscriptions",
            Component: AdminSubscriptionPlans,
          },
          {
            path: "shop/settings",
            Component: ShopSettings,
          },
        ],
      },
      // Subscription Routes — accessible by all authenticated users
      {
        element: <ProtectedRoute allowedRoles={["CUSTOMER", "ADMIN", "SHOP_OWNER"]} />,
        children: [
          {
            path: "subscriptions",
            Component: SubscriptionPlans,
          },
          {
            path: "subscription/upgrade-review",
            Component: SubscriptionUpgradeReview,
          },
          {
            path: "subscription/upgrade-checkout",
            Component: SubscriptionUpgradeCheckout,
          },
          {
            path: "subscription/upgrade-success",
            Component: SubscriptionUpgradeSuccess,
          },
          {
            path: "subscription/management",
            Component: SubscriptionManagement,
          },
          {
            path: "subscription/history",
            Component: SubscriptionHistory,
          },
        ],
      },
    ],
  },
]);