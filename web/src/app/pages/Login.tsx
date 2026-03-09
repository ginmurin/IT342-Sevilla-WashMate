import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuth } from "../contexts/AuthContext";
import { authAPI } from "../utils/api";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "../components/Card";
import { Droplets, AlertCircle, Eye, EyeOff } from "lucide-react";
import { motion } from "motion/react";

const loginSchema = z.object({
  emailOrUsername: z
    .string()
    .min(1, { message: "Email or username is required" }),
  password: z
    .string()
    .min(6, { message: "Password must be at least 6 characters" }),
  rememberMe: z.boolean().optional(),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  const from = location.state?.from?.pathname || "/";

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      emailOrUsername: "",
      password: "",
      rememberMe: false,
    },
  });

  const onSubmit = async (data: LoginFormValues) => {
    setError(null);
    try {
      const result = await authAPI.login({ emailOrUsername: data.emailOrUsername, password: data.password });
      login(result.user);
      const role = result.user.role;
      if (role === 'customer') navigate("/customer", { replace: true });
      else if (role === 'shop_owner') navigate("/shop", { replace: true });
      else if (role === 'admin') navigate("/admin", { replace: true });
      else navigate(from, { replace: true });
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Invalid credentials.");
    }
  };

  return (
    <div className="flex-1 flex items-center justify-center py-12 px-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: "easeOut" }}
        className="w-full max-w-md"
      >
        <Card className="shadow-xl border-slate-100">
          <CardHeader className="space-y-3 items-center text-center">
            <div className="w-12 h-12 bg-teal-100 rounded-full flex items-center justify-center mb-2">
              <Droplets className="w-6 h-6 text-teal-600" />
            </div>
            <CardTitle className="text-2xl tracking-tight text-slate-900">
              Welcome back
            </CardTitle>
            <CardDescription>
              Sign in with your email or username
            </CardDescription>
          </CardHeader>
          <CardContent>
            {error && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                className="mb-4 flex items-center gap-2 p-3 text-sm text-red-600 bg-red-50 rounded-md"
              >
                <AlertCircle className="w-4 h-4 shrink-0" />
                {error}
              </motion.div>
            )}
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              {/* Email or Username */}
              <div className="space-y-2">
                <label
                  className="text-sm leading-none text-slate-700"
                  htmlFor="emailOrUsername"
                >
                  Email or Username
                </label>
                <Input
                  id="emailOrUsername"
                  type="text"
                  placeholder="name@example.com or username"
                  autoComplete="username"
                  {...register("emailOrUsername")}
                  className={
                    errors.emailOrUsername
                      ? "border-red-500 focus-visible:ring-red-500"
                      : ""
                  }
                />
                {errors.emailOrUsername && (
                  <p className="text-sm text-red-500">
                    {errors.emailOrUsername.message}
                  </p>
                )}
              </div>

              {/* Password */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <label
                    className="text-sm leading-none text-slate-700"
                    htmlFor="password"
                  >
                    Password
                  </label>
                  <Link
                    to="/forgot-password"
                    className="text-xs text-teal-600 hover:underline"
                  >
                    Forgot password?
                  </Link>
                </div>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="••••••••"
                    autoComplete="current-password"
                    {...register("password")}
                    className={
                      errors.password
                        ? "border-red-500 focus-visible:ring-red-500 pr-10"
                        : "pr-10"
                    }
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                    tabIndex={-1}
                  >
                    {showPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
                {errors.password && (
                  <p className="text-sm text-red-500">
                    {errors.password.message}
                  </p>
                )}
              </div>

              {/* Remember Me */}
              <div className="flex items-center gap-2">
                <input
                  id="rememberMe"
                  type="checkbox"
                  {...register("rememberMe")}
                  className="h-4 w-4 rounded border-slate-300 text-teal-600 focus:ring-teal-500"
                />
                <label
                  htmlFor="rememberMe"
                  className="text-sm text-slate-600 select-none cursor-pointer"
                >
                  Remember me
                </label>
              </div>

              <Button
                type="submit"
                className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                disabled={isSubmitting}
              >
                {isSubmitting ? "Signing in..." : "Sign in"}
              </Button>
            </form>

            {/* OAuth divider */}
            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-200" />
              </div>
              <div className="relative flex justify-center text-xs">
                <span className="bg-white px-2 text-slate-500">
                  Or continue with
                </span>
              </div>
            </div>

            {/* OAuth buttons */}
            <div className="grid grid-cols-1 gap-3">
              <Button
                type="button"
                variant="outline"
                className="gap-2 text-slate-700"
                onClick={() => {
                  // Mock Google OAuth - would set oauth_provider: "google"
                  alert("Google OAuth would redirect to Google sign-in");
                }}
              >
                <svg className="w-4 h-4" viewBox="0 0 24 24">
                  <path
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"
                    fill="#4285F4"
                  />
                  <path
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                    fill="#34A853"
                  />
                  <path
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                    fill="#FBBC05"
                  />
                  <path
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                    fill="#EA4335"
                  />
                </svg>
                Google
              </Button>
              {/* Facebook button removed */}
            </div>
          </CardContent>
          <CardFooter className="flex justify-center border-t border-slate-100 p-4">
            <p className="text-sm text-slate-500">
              Don't have an account?{" "}
              <Link
                to="/register"
                className="text-teal-600 hover:underline"
              >
                Sign up
              </Link>
            </p>
          </CardFooter>
        </Card>
      </motion.div>
    </div>
  );
}