import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth, Role } from "../contexts/AuthContext";

interface ProtectedRouteProps {
  allowedRoles?: Role[];
}

export function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { user, isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    // Redirect based on role
    switch (user.role) {
      case "customer":
        return <Navigate to="/customer" replace />;
      case "shop_owner":
        return <Navigate to="/shop" replace />;
      case "admin":
        return <Navigate to="/admin" replace />;
      default:
        return <Navigate to="/" replace />;
    }
  }

  return <Outlet />;
}
