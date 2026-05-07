import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router";
import { useAuth } from "@/features/auth/AuthContext";
import { Droplets, AlertCircle } from "lucide-react";
import { motion } from "motion/react";
import { Button } from "@/features/shared/components/Button";
import type { User } from "@/features/shared/types";

export default function AuthCallback() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Get tokens and user info from query parameters
        const accessToken = searchParams.get("accessToken");
        const refreshToken = searchParams.get("refreshToken");
        const userId = searchParams.get("userId");
        const email = searchParams.get("email");
        const firstName = searchParams.get("firstName");
        const lastName = searchParams.get("lastName");
        const username = searchParams.get("username");
        const role = searchParams.get("role");

        if (!accessToken || !refreshToken || !userId) {
          setError("Missing authentication data");
          setIsLoading(false);
          return;
        }

        // Store tokens in localStorage
        localStorage.setItem("washmate_access_token", accessToken);
        localStorage.setItem("washmate_refresh_token", refreshToken);

        // Create user object
        const user: User = {
          id: parseInt(userId),
          email: email || "",
          username: username || null,
          firstName: firstName || "",
          lastName: lastName || "",
          role: (role || "CUSTOMER") as "CUSTOMER" | "SHOP_OWNER" | "ADMIN",
        };

        // Login and redirect to dashboard
        login(user);
        const userRole = String(role).toUpperCase();
        if (userRole === "CUSTOMER") navigate("/customer", { replace: true });
        else if (userRole === "ADMIN") navigate("/admin", { replace: true });
        else if (userRole === "SHOP_OWNER") navigate("/shop", { replace: true });
        else navigate("/", { replace: true });
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "Authentication failed";
        setError(errorMessage);
        console.error("OAuth callback error:", err);
      } finally {
        setIsLoading(false);
      }
    };

    handleCallback();
  }, []);

  if (error) {
    return (
      <div className="flex-1 flex items-center justify-center min-h-screen px-4">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-sm text-center space-y-4"
        >
          <div className="w-14 h-14 bg-red-100 rounded-full flex items-center justify-center mx-auto">
            <AlertCircle className="w-7 h-7 text-red-500" />
          </div>
          <h2 className="text-xl font-semibold text-slate-800">Sign-in failed</h2>
          <p className="text-sm text-slate-500">{error}</p>
          <Button
            onClick={() => navigate("/login")}
            className="w-full bg-teal-600 hover:bg-teal-700 text-white"
          >
            Back to Login
          </Button>
        </motion.div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex-1 flex items-center justify-center min-h-screen">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="flex flex-col items-center gap-5"
        >
          {/* Logo */}
          <div className="w-12 h-12 rounded-xl bg-teal-100 flex items-center justify-center">
            <Droplets className="w-6 h-6 text-teal-600" />
          </div>

          {/* Spinner */}
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ repeat: Infinity, duration: 1, ease: "linear" }}
            className="w-8 h-8 border-3 border-teal-200 border-t-teal-600 rounded-full"
          />

          <p className="text-sm text-slate-500">Completing sign-in…</p>
        </motion.div>
      </div>
    );
  }

  return null;
}



