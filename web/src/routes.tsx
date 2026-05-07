import { createBrowserRouter } from "react-router";
import {  MainLayout  } from "@/features/shared/layouts/MainLayout";
import {  RouterProviders  } from "@/features/shared/components/RouterProviders";
import {  ProtectedRoute  } from "@/features/shared/components/ProtectedRoute";

import { Home } from "@/features/shared/Home";
import { Login } from "@/features/auth/Login";
import { Register } from "@/features/auth/Register";
import ForgotPassword from "@/features/auth/ForgotPassword";
import VerifyEmail from "@/features/auth/VerifyEmail";
import AuthCallback from "@/features/auth/AuthCallback";
import PaymentCheckout from "@/features/orders/PaymentCheckout";
import PaymentSuccess from "@/features/orders/PaymentSuccess";
import PaymentError from "@/features/shared/NotFound";
import PaymentHistory from "@/features/orders/PaymentHistory";
import Settings from "@/features/shared/Settings";
import Wallet from "@/features/wallet/Wallet";
import WalletPaymentReview from "@/features/wallet/WalletPaymentReview";
import WalletPaymentCheckout from "@/features/wallet/WalletPaymentCheckout";
import WalletPaymentSuccess from "@/features/wallet/WalletPaymentSuccess";
import WalletPaymentError from "@/features/shared/NotFound";
import LaundryDetails from "@/features/orders/LaundryDetails";
import ScheduleAddress from "@/features/orders/ScheduleAddress";
import PaymentReview from "@/features/orders/PaymentReview";
import MyOrders from "@/features/orders/MyOrders";
import Services from "@/features/customer/Services";
import Notifications from "@/features/shared/Notifications";
import { CustomerDashboard } from "@/features/customer/CustomerDashboard";
import { AdminDashboard } from "@/features/admin/AdminDashboard";
import AdminUsers from "@/features/admin/AdminUsers";
import AdminSubscriptionPlans from "@/features/admin/AdminSubscriptionPlans";
import SubscriptionPlans from "@/features/subscriptions/SubscriptionPlans";
import SubscriptionUpgradeReview from "@/features/subscriptions/SubscriptionUpgradeReview";
import SubscriptionUpgradeCheckout from "@/features/subscriptions/SubscriptionUpgradeCheckout";
import SubscriptionUpgradeSuccess from "@/features/subscriptions/SubscriptionUpgradeSuccess";
import SubscriptionManagement from "@/features/subscriptions/SubscriptionManagement";
import SubscriptionHistory from "@/features/subscriptions/SubscriptionHistory";
import OrderDetail from "@/features/orders/OrderDetail";
import { ShopDashboard } from "@/features/shop/ShopDashboard";
import ShopOrders from "@/features/shop/ShopOrders";
import ShopServices from "@/features/shop/ShopServices";
import ShopSettings from "@/features/shop/ShopSettings";

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


