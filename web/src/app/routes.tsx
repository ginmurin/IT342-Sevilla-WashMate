import { createBrowserRouter } from "react-router";
import { MainLayout } from "./layouts/MainLayout";
import { ProtectedRoute } from "./components/ProtectedRoute";

import { Home } from "./pages/Home";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import VerifyEmail from "./pages/VerifyEmail";
import AuthCallback from "./pages/AuthCallback";
import { CustomerDashboard } from "./pages/CustomerDashboard";
import { ShopDashboard } from "./pages/ShopDashboard";
import { AdminDashboard } from "./pages/AdminDashboard";

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
      // Customer Routes
      {
        element: <ProtectedRoute allowedRoles={["customer"]} />,
        children: [
          {
            path: "customer",
            Component: CustomerDashboard,
          },
        ],
      },
      // Shop Owner Routes
      {
        element: <ProtectedRoute allowedRoles={["shop_owner"]} />,
        children: [
          {
            path: "shop",
            Component: ShopDashboard,
          },
        ],
      },
      // Admin Routes
      {
        element: <ProtectedRoute allowedRoles={["admin"]} />,
        children: [
          {
            path: "admin",
            Component: AdminDashboard,
          },
        ],
      },
    ],
  },
]);