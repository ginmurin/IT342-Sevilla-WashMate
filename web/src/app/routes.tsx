import { createBrowserRouter } from "react-router";
import { MainLayout } from "./layouts/MainLayout";
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
import { CustomerDashboard } from "./pages/CustomerDashboard";
import { ShopDashboard } from "./pages/ShopDashboard";
import { AdminDashboard } from "./pages/AdminDashboard";
import SubscriptionPlans from "./pages/SubscriptionPlans";
import SubscriptionUpgradeReview from "./pages/SubscriptionUpgradeReview";
import SubscriptionUpgradeCheckout from "./pages/SubscriptionUpgradeCheckout";
import SubscriptionUpgradeSuccess from "./pages/SubscriptionUpgradeSuccess";
import SubscriptionManagement from "./pages/SubscriptionManagement";
import SubscriptionHistory from "./pages/SubscriptionHistory";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: MainLayout,
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
        path: "services",
        Component: Services,
      },
      // Customer Routes — also accessible by SHOPOWNER and ADMIN
      {
        element: <ProtectedRoute allowedRoles={["CUSTOMER", "SHOPOWNER", "ADMIN"]} />,
        children: [
          {
            path: "customer",
            Component: CustomerDashboard,
          },
        ],
      },
      // Shop Owner Routes — also accessible by ADMIN
      {
        element: <ProtectedRoute allowedRoles={["SHOPOWNER", "ADMIN"]} />,
        children: [
          {
            path: "shop",
            Component: ShopDashboard,
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
        ],
      },
      // Subscription Routes — accessible by all authenticated users
      {
        element: <ProtectedRoute allowedRoles={["CUSTOMER", "SHOPOWNER", "ADMIN"]} />,
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