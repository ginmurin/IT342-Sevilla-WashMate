import { useState, useEffect } from "react";
import { useNavigate, useLocation, Link } from "react-router";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuth } from "@/features/auth/AuthContext";
import { authAPI } from "@/features/shared/utils/api";
import { Button } from "@/features/shared/components/Button";
import { Input } from "@/features/shared/components/Input";
import {
  Droplets,
  AlertCircle,
  Eye,
  EyeOff,
  Mail,
  Lock,
  Store,
  Users,
  Shield,
  ArrowLeft,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import laundryHero from "../../assets/laundry-hero.png";
import type { User } from "@/features/shared/types";

type Step = "credentials" | "otp" | "two-factor" | "role-select";

const loginSchema = z.object({
  emailOrUsername: z.string().min(1, "Email or username is required"),
  password: z.string().min(6, "Password must be at least 6 characters"),
  rememberMe: z.boolean().optional(),
});

const otpSchema = z.object({
  code: z.string().length(6, "OTP must be 6 digits"),
});

type LoginFormValues = z.infer<typeof loginSchema>;
type OtpFormValues = z.infer<typeof otpSchema>;

function GoogleIcon() {
  return (
    <svg className="w-4 h-4" viewBox="0 0 24 24">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4" />
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
  );
}

type RoleOption = {
  label: string;
  description: string;
  destination: "/customer" | "/shop" | "/admin";
  icon: React.ElementType;
  iconClass: string;
  hoverClass: string;
  activeClass: string;
};

function buildRoleOptions(dbRole: string): RoleOption[] {
  const options: RoleOption[] = [];

  if (dbRole === "ADMIN") {
    options.push({
      label: "Admin Panel",
      description: "Manage the entire platform",
      destination: "/admin",
      icon: Shield,
      iconClass: "bg-purple-100 text-purple-600",
      hoverClass: "hover:border-purple-400 hover:bg-purple-50",
      activeClass: "border-purple-500 bg-purple-50",
    });
  }

  if (dbRole === "ADMIN" || dbRole === "SHOP_OWNER") {
    options.push({
      label: "Shop Owner",
      description: "Manage your laundry shop",
      destination: "/shop",
      icon: Store,
      iconClass: "bg-blue-100 text-blue-600",
      hoverClass: "hover:border-blue-400 hover:bg-blue-50",
      activeClass: "border-blue-500 bg-blue-50",
    });
  }

  options.push({
    label: "Customer",
    description: "Book laundry services",
    destination: "/customer",
    icon: Users,
    iconClass: "bg-teal-100 text-teal-600",
    hoverClass: "hover:border-teal-400 hover:bg-teal-50",
    activeClass: "border-teal-500 bg-teal-50",
  });

  return options;
}

export function Login() {
  const { login, user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [step, setStep] = useState<Step>("credentials");
  const [dbRole, setDbRole] = useState<string>("");
  const [pendingUserId, setPendingUserId] = useState<number | null>(null);
  const [pendingEmail, setPendingEmail] = useState<string>("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset: resetLoginForm,
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { emailOrUsername: "", password: "", rememberMe: false },
  });

  const {
    register: registerOtp,
    handleSubmit: handleOtpSubmit,
    formState: { errors: otpErrors, isSubmitting: isOtpSubmitting },
    reset: resetOtpForm,
  } = useForm<OtpFormValues>({
    resolver: zodResolver(otpSchema),
    defaultValues: { code: "" },
  });

  // Redirect already-authenticated users to prevent infinite loops
  useEffect(() => {
    if (isAuthenticated && user) {
      if (user.role === "CUSTOMER") {
        navigate("/customer", { replace: true });
      } else if (user.role === "ADMIN") {
        navigate("/admin", { replace: true });
      } else if (user.role === "SHOP_OWNER") {
        navigate("/shop", { replace: true });
      } else {
        navigate("/", { replace: true });
      }
    }
  }, [isAuthenticated, user, navigate]);

  // ── Step 1: Authenticate with email/password ──────────────────────────────
  const onSubmit = async (data: LoginFormValues) => {
    setError(null);
    try {
      const response = await authAPI.login({
        emailOrUsername: data.emailOrUsername,
        password: data.password,
      });

      // If email verification required, show OTP screen
      if (response.requiresEmailVerification) {
        setPendingUserId(response.userId || null);
        setPendingEmail(response.user.email);
        setStep("otp");
        resetOtpForm();
        return;
      }

      if (response.requiresTwoFactor) {
        setPendingUserId(response.userId || null);
        setPendingEmail(response.user.email);
        setStep("two-factor");
        resetOtpForm();
        return;
      }

      // Email is verified, check role
      const userRole = String(response.user.role).toUpperCase();
      login(response.user);

      if (userRole === "CUSTOMER") {
        navigate("/customer", { replace: true });
      } else {
        setDbRole(userRole);
        setStep("role-select");
      }
    } catch (err: any) {
      setError(err.message || "Login failed. Please check your credentials.");
    }
  };

  // ── Step 2: Verify OTP ─────────────────────────────────────────────────────
  const onOtpSubmit = async (data: OtpFormValues) => {
    setError(null);
    if (!pendingUserId) {
      setError("User ID not found. Please try again.");
      return;
    }

    try {
      const response = step === "two-factor"
        ? await authAPI.verifyTwoFactorLogin(pendingUserId, data.code)
        : await authAPI.verifyEmail(pendingUserId, data.code);

      const userRole = String(response.user.role).toUpperCase();
      login(response.user);

      if (userRole === "CUSTOMER") {
        navigate("/customer", { replace: true });
      } else {
        setDbRole(userRole);
        setStep("role-select");
      }
    } catch (err: any) {
      setError(err.message || "Invalid code. Please try again.");
    }
  };

  const handleResendOtp = async () => {
    setError(null);
    if (step === "two-factor") {
      if (!pendingUserId) return;
      try {
        await authAPI.resendTwoFactorLogin(pendingUserId);
        setError(null);
        alert("Code resent to your email");
      } catch (err: any) {
        setError(err.message || "Failed to resend code.");
      }
      return;
    }

    if (!pendingEmail) return;

    try {
      await authAPI.resendOtp(pendingEmail);
      setError(null); // Clear error if any
      // Show success message
      alert("OTP resent to your email");
    } catch (err: any) {
      setError(err.message || "Failed to resend OTP.");
    }
  };

  const handleGoogleSignIn = () => {
    // Redirect to backend OAuth endpoint
    const backendUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    window.location.href = `${backendUrl}/api/auth/google/login`;
  };

  const roleOptions = buildRoleOptions(dbRole);

  return (
    <div className="flex-1 flex items-stretch min-h-screen pt-16">

      {/* ── Left image panel ── */}
      <div className="hidden lg:flex lg:w-[48%] relative overflow-hidden">
        <img
          src={laundryHero}
          alt="Fresh laundry hanging on a clothesline"
          className="absolute inset-0 w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
        <div className="relative z-10 flex flex-col justify-end p-10 xl:p-14 w-full">
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, delay: 0.2 }}>
            <div className="flex items-center gap-2.5 mb-5">
              <div className="w-9 h-9 rounded-lg bg-white/20 backdrop-blur-sm flex items-center justify-center">
                <Droplets className="w-5 h-5 text-white" />
              </div>
              <span className="text-white text-lg font-medium tracking-tight">WashMate</span>
            </div>
            <h2 className="text-white text-3xl xl:text-4xl font-semibold leading-tight">
              Your laundry,<br />simplified.
            </h2>
            <p className="text-white/80 mt-3 max-w-sm text-sm leading-relaxed">
              Experience the freshest clean with WashMate. Professional care for your clothes, delivered to your door.
            </p>
          </motion.div>
        </div>
      </div>

      {/* ── Right form panel ── */}
      <div className="flex-1 flex items-center justify-center px-4 py-8 sm:px-8 lg:px-12 xl:px-16 bg-white">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, ease: "easeOut" }}
          className="w-full max-w-md"
        >
          {/* Mobile-only header */}
          <div className="lg:hidden flex items-center gap-2.5 mb-6">
            <div className="w-9 h-9 rounded-lg bg-blue-100 flex items-center justify-center">
              <Droplets className="w-5 h-5 text-blue-600" />
            </div>
            <span className="text-slate-800 text-lg font-medium tracking-tight">WashMate</span>
          </div>

          {/* Title */}
          <div className="mb-8">
            <h1 className="text-2xl font-semibold text-slate-900 tracking-tight">
              {step === "credentials"
                ? "Welcome Back"
                : step === "otp"
                  ? "Verify Email"
                  : step === "two-factor"
                    ? "Two-Factor Verification"
                    : "Choose Dashboard"}
            </h1>
            <p className="text-slate-500 mt-1.5 text-sm">
              {step === "credentials"
                ? "Sign in with your email or username."
                : step === "otp"
                  ? "Enter the code sent to your email."
                  : step === "two-factor"
                    ? "Enter the two-factor code sent to your email."
                  : `You're signed in as ${dbRole === "ADMIN" ? "Admin" : "Shop Owner"}. Where would you like to go?`}
            </p>
          </div>

          {/* Error banner */}
          {error && (
            <motion.div
              initial={{ opacity: 0, y: -8 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-5 flex items-start gap-2.5 p-3.5 text-sm text-red-700 bg-red-50 border border-red-100 rounded-xl"
            >
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </motion.div>
          )}

          <AnimatePresence mode="wait">

            {/* ── Step 1: Credentials ── */}
            {step === "credentials" && (
              <motion.form
                key="credentials"
                initial={{ opacity: 0, x: 16 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -16 }}
                onSubmit={handleSubmit(onSubmit)}
                className="space-y-5"
              >
                {/* Email / Username */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700" htmlFor="emailOrUsername">
                    Email or Username
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                    <Input
                      id="emailOrUsername"
                      type="text"
                      placeholder="name@example.com or username"
                      autoComplete="username"
                      {...register("emailOrUsername")}
                      className={`pl-9 ${errors.emailOrUsername ? "border-red-400 focus-visible:ring-red-400" : ""}`}
                    />
                  </div>
                  {errors.emailOrUsername && (
                    <p className="text-xs text-red-500">{errors.emailOrUsername.message}</p>
                  )}
                </div>

                {/* Password */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700" htmlFor="password">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                    <Input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      placeholder="Enter your password"
                      autoComplete="current-password"
                      {...register("password")}
                      className={`pl-9 pr-10 ${errors.password ? "border-red-400 focus-visible:ring-red-400" : ""}`}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                      tabIndex={-1}
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                  {errors.password && (
                    <p className="text-xs text-red-500">{errors.password.message}</p>
                  )}
                </div>

                {/* Remember me + Forgot password */}
                <div className="flex items-center justify-between">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input type="checkbox" {...register("rememberMe")} className="h-4 w-4 rounded border-slate-300" />
                    <span className="text-sm text-slate-600">Remember me</span>
                  </label>
                  <Link to="/forgot-password" className="text-xs font-medium text-teal-600 hover:text-teal-700 hover:underline">
                    Forgot Password?
                  </Link>
                </div>

                <Button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg gap-2"
                >
                  {isSubmitting ? (
                    <motion.div
                      animate={{ rotate: 360 }}
                      transition={{ repeat: Infinity, duration: 1, ease: "linear" }}
                      className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full"
                    />
                  ) : "Sign In"}
                </Button>

                {/* Divider */}
                <div className="relative my-1">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-slate-200" />
                  </div>
                  <div className="relative flex justify-center text-xs">
                    <span className="bg-white px-3 text-slate-400 uppercase tracking-wider">or continue with</span>
                  </div>
                </div>

                <Button
                  type="button"
                  variant="outline"
                  onClick={handleGoogleSignIn}
                  className="w-full gap-2.5 text-slate-700 h-11 bg-white hover:bg-slate-50 border-slate-200"
                >
                  <GoogleIcon />
                  Sign in with Google
                </Button>
              </motion.form>
            )}

            {/* ── Step 1.5: OTP Verification ── */}
            {(step === "otp" || step === "two-factor") && (
              <motion.form
                key="otp"
                initial={{ opacity: 0, x: 16 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -16 }}
                onSubmit={handleOtpSubmit(onOtpSubmit)}
                className="space-y-5"
              >
                <p className="text-sm text-slate-600">
                  We found an account for <strong>{pendingEmail}</strong>. Enter the 6-digit {step === "two-factor" ? "code" : "OTP"} sent to your email.
                </p>

                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700" htmlFor="otp-code">
                    Verification Code
                  </label>
                  <Input
                    id="otp-code"
                    type="text"
                    placeholder="000000"
                    maxLength={6}
                    inputMode="numeric"
                    {...registerOtp("code")}
                    className={`text-center tracking-widest text-lg font-mono ${
                      otpErrors.code ? "border-red-400 focus-visible:ring-red-400" : ""
                    }`}
                  />
                  {otpErrors.code && (
                    <p className="text-xs text-red-500">{otpErrors.code.message}</p>
                  )}
                </div>

                <Button
                  type="submit"
                  disabled={isOtpSubmitting}
                  className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg"
                >
                  {isOtpSubmitting ? (
                    <motion.div
                      animate={{ rotate: 360 }}
                      transition={{ repeat: Infinity, duration: 1, ease: "linear" }}
                      className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full"
                    />
                  ) : "Verify"}
                </Button>

                <div className="flex flex-col gap-2">
                  <button
                    type="button"
                    onClick={handleResendOtp}
                    className="text-sm text-teal-600 hover:text-teal-700 font-medium"
                  >
                    {step === "two-factor" ? "Resend Code" : "Resend OTP"}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setStep("credentials");
                      setPendingUserId(null);
                      setPendingEmail("");
                      resetLoginForm();
                      resetOtpForm();
                    }}
                    className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-700 transition-colors"
                  >
                    <ArrowLeft className="w-3 h-3" /> Back to login
                  </button>
                </div>
              </motion.form>
            )}

            {/* ── Step 2: Role-select (shop_owner / admin only) ── */}
            {step === "role-select" && (
              <motion.div
                key="role-select"
                initial={{ opacity: 0, x: 16 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -16 }}
                className="space-y-3"
              >
                {roleOptions.map((opt) => {
                  const Icon = opt.icon;
                  return (
                    <button
                      key={opt.destination}
                      type="button"
                      onClick={() => navigate(opt.destination, { replace: true })}
                      className={`w-full h-20 rounded-xl border-2 border-slate-200 bg-white transition-all flex items-center px-6 gap-4 text-left ${opt.hoverClass}`}
                    >
                      <div className={`w-11 h-11 rounded-lg flex items-center justify-center shrink-0 ${opt.iconClass}`}>
                        <Icon className="w-5 h-5" />
                      </div>
                      <div>
                        <p className="font-semibold text-slate-900 text-sm">{opt.label}</p>
                        <p className="text-xs text-slate-500 mt-0.5">{opt.description}</p>
                      </div>
                    </button>
                  );
                })}

                <button
                  type="button"
                  onClick={() => { setStep("credentials"); setDbRole(""); setError(null); }}
                  className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-700 mt-2 transition-colors"
                >
                  <ArrowLeft className="w-4 h-4" /> Sign in with a different account
                </button>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Footer */}
          {step === "credentials" && (
            <p className="text-sm text-slate-500 text-center mt-8">
              Don't have an account?{" "}
              <Link to="/register" className="text-teal-600 hover:underline font-medium">
                Register now
              </Link>
            </p>
          )}
        </motion.div>
      </div>
    </div>
  );
}



