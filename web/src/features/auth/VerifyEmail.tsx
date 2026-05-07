import { useState, useRef, useEffect, KeyboardEvent, ClipboardEvent } from "react";
import { useNavigate, useLocation } from "react-router";
import { useAuth } from "@/features/auth/AuthContext";
import { authAPI } from "@/features/shared/utils/api";
import { Button } from "@/features/shared/components/Button";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/features/shared/components/Card";
import {
  Mail,
  CheckCircle2,
  AlertCircle,
  Loader2,
  ArrowRight,
  Home,
  LogOut,
  Droplets,
  PartyPopper,
  ShieldCheck,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

// OTP Input component
function OTPInput({
  length = 6,
  value,
  onChange,
  error,
  disabled,
}: {
  length?: number;
  value: string[];
  onChange: (val: string[]) => void;
  error?: boolean;
  disabled?: boolean;
}) {
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    // Auto-focus first input on mount
    inputRefs.current[0]?.focus();
  }, []);

  const handleChange = (index: number, char: string) => {
    if (!/^[0-9]?$/.test(char)) return;
    const newVal = [...value];
    newVal[index] = char;
    onChange(newVal);
    if (char && index < length - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace" && !value[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
      const newVal = [...value];
      newVal[index - 1] = "";
      onChange(newVal);
    }
  };

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, length);
    const newVal = [...value];
    for (let i = 0; i < pasted.length; i++) {
      newVal[i] = pasted[i];
    }
    onChange(newVal);
    const focusIndex = Math.min(pasted.length, length - 1);
    inputRefs.current[focusIndex]?.focus();
  };

  return (
    <div className="flex gap-2 sm:gap-3 justify-center">
      {Array.from({ length }).map((_, i) => (
        <input
          key={i}
          ref={(el) => { inputRefs.current[i] = el; }}
          type="text"
          inputMode="numeric"
          maxLength={1}
          value={value[i] || ""}
          disabled={disabled}
          onChange={(e) => handleChange(i, e.target.value)}
          onKeyDown={(e) => handleKeyDown(i, e)}
          onPaste={i === 0 ? handlePaste : undefined}
          className={`w-10 h-12 sm:w-12 sm:h-14 text-center text-lg rounded-lg border-2 bg-white transition-all focus:outline-none focus:ring-2 focus:ring-teal-500/30 disabled:bg-slate-50 disabled:cursor-not-allowed ${
            error
              ? "border-red-400 focus:border-red-500"
              : value[i]
              ? "border-teal-400 focus:border-teal-500"
              : "border-slate-200 focus:border-teal-500"
          }`}
        />
      ))}
    </div>
  );
}

type Step = "verify" | "success";

export default function VerifyEmail() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, login, logout } = useAuth();

  const email = location.state?.email || user?.email || "your email";
  const fromRegister = location.state?.fromRegister || false;

  const [step, setStep] = useState<Step>("verify");
  const [otp, setOtp] = useState<string[]>(Array(6).fill(""));
  const [otpError, setOtpError] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [cooldown, setCooldown] = useState(fromRegister ? 60 : 0);

  // Cooldown timer
  useEffect(() => {
    if (cooldown > 0) {
      const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [cooldown]);

  // If user is already verified, redirect them
  useEffect(() => {
    if (user?.emailVerified) {
      const dashPath =
        user.role === "CUSTOMER"
          ? "/customer"
          : user.role === "SHOP_OWNER"
          ? "/shop"
          : user.role === "ADMIN"
          ? "/admin"
          : "/";
      navigate(dashPath, { replace: true });
    }
  }, [user, navigate]);

  // Mask email for display
  const maskedEmail = email
    ? email.replace(/(.{2})(.*)(@.*)/, (_: string, a: string, b: string, c: string) => a + "*".repeat(b.length) + c)
    : "";

  const handleVerifyOtp = async () => {
    setError(null);
    const code = otp.join("");
    if (code.length !== 6) {
      setOtpError(true);
      setError("Please enter all 6 digits.");
      return;
    }

    setIsLoading(true);
    try {
      // Get user ID from state or AuthContext
      const userId = location.state?.userId || user?.id;
      if (!userId) {
        setError("User ID not found. Please try again.");
        setIsLoading(false);
        return;
      }

      const response = await authAPI.verifyEmail(userId, code);
      login(response.user);

      setIsLoading(false);
      setOtpError(false);
      setStep("success");
    } catch (err: any) {
      setIsLoading(false);
      setOtpError(true);
      setError(err.message || "Invalid verification code. Please try again.");
    }
  };

  const handleResendEmail = async () => {
    setIsResending(true);
    setError(null);
    setOtpError(false);
    try {
      await authAPI.resendOtp(email);
      setOtp(Array(6).fill(""));
      setCooldown(60);
    } catch (err: any) {
      setError(err.message || "Failed to resend code. Please try again.");
    }
    setIsResending(false);
  };

  const handleGoToDashboard = () => {
    if (!user) {
      navigate("/login", { replace: true });
      return;
    }
    const dashPath =
      user.role === "CUSTOMER"
        ? "/customer"
        : user.role === "SHOP_OWNER"
        ? "/shop"
        : user.role === "ADMIN"
        ? "/admin"
        : "/";
    navigate(dashPath, { replace: true });
  };

  const handleGoHome = () => {
    navigate("/", { replace: true });
  };

  const handleLogoutAndExit = () => {
    logout();
    navigate("/", { replace: true });
  };

  return (
    <div className="flex-1 flex items-center justify-center min-h-screen pt-16 pb-12 px-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: "easeOut" }}
        className="w-full max-w-md"
      >
        {/* Logo */}
        <div className="flex items-center justify-center gap-2 mb-6">
          <div className="w-9 h-9 rounded-lg bg-teal-600 flex items-center justify-center">
            <Droplets className="w-5 h-5 text-white" />
          </div>
          <span className="text-slate-800 text-xl font-bold tracking-tight">WashMate</span>
        </div>
        <AnimatePresence mode="wait">
          {/* Step 1: OTP Verification */}
          {step === "verify" && (
            <motion.div
              key="verify"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ duration: 0.25 }}
            >
              <Card className="shadow-xl border-slate-100">
                <CardHeader className="space-y-3 items-center text-center">
                  {/* Animated mail icon */}
                  <motion.div
                    initial={{ scale: 0.8 }}
                    animate={{ scale: 1 }}
                    transition={{ duration: 0.3 }}
                    className="relative"
                  >
                    <div className="w-16 h-16 bg-gradient-to-br from-teal-400 to-teal-600 rounded-full flex items-center justify-center shadow-lg shadow-teal-200">
                      <Mail className="w-8 h-8 text-white" />
                    </div>
                    <motion.div
                      initial={{ scale: 0 }}
                      animate={{ scale: 1 }}
                      transition={{ delay: 0.3, type: "spring", stiffness: 200 }}
                      className="absolute -bottom-1 -right-1 w-6 h-6 bg-amber-400 rounded-full flex items-center justify-center shadow-sm"
                    >
                      <span className="text-xs">!</span>
                    </motion.div>
                  </motion.div>

                  <CardTitle className="text-2xl tracking-tight text-slate-900">
                    Verify your email
                  </CardTitle>
                  <CardDescription className="max-w-xs">
                    {fromRegister
                      ? "We've sent a 6-digit verification code to"
                      : "Enter the verification code sent to"}
                  </CardDescription>
                  <p className="text-sm text-teal-600 bg-teal-50 px-3 py-1 rounded-full">
                    {maskedEmail}
                  </p>
                </CardHeader>

                <CardContent className="space-y-6">
                  {error && (
                    <motion.div
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: "auto" }}
                      className="flex items-center gap-2 p-3 text-sm text-red-600 bg-red-50 rounded-md"
                    >
                      <AlertCircle className="w-4 h-4 shrink-0" />
                      {error}
                    </motion.div>
                  )}

                  {/* OTP Input */}
                  <div className="space-y-2">
                    <label className="text-sm text-slate-600 text-center block">
                      Enter verification code
                    </label>
                    <OTPInput
                      value={otp}
                      onChange={(val) => {
                        setOtp(val);
                        setOtpError(false);
                        setError(null);
                      }}
                      error={otpError}
                      disabled={isLoading}
                    />
                  </div>

                  {/* Verify button */}
                  <Button
                    type="button"
                    className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                    disabled={isLoading || otp.join("").length !== 6}
                    onClick={handleVerifyOtp}
                  >
                    {isLoading ? (
                      <span className="flex items-center gap-2">
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Verifying...
                      </span>
                    ) : (
                      "Verify email"
                    )}
                  </Button>

                  {/* Resend section */}
                  <div className="text-center space-y-2">
                    <p className="text-sm text-slate-500">
                      Didn't receive the code?
                    </p>
                    <button
                      onClick={handleResendEmail}
                      disabled={cooldown > 0 || isResending}
                      className="text-sm text-teal-600 hover:underline disabled:text-slate-400 disabled:no-underline disabled:cursor-not-allowed transition-colors inline-flex items-center gap-1.5"
                    >
                      {isResending ? (
                        <>
                          <Loader2 className="w-3 h-3 animate-spin" />
                          Sending...
                        </>
                      ) : cooldown > 0 ? (
                        `Resend code in ${cooldown}s`
                      ) : (
                        "Resend verification code"
                      )}
                    </button>
                  </div>

                  {/* Divider */}
                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <div className="w-full border-t border-slate-200" />
                    </div>
                    <div className="relative flex justify-center text-xs">
                      <span className="bg-white px-2 text-slate-400">or</span>
                    </div>
                  </div>

                  {/* Secondary actions */}
                  <div className="flex flex-col gap-2">
                    <Button
                      variant="outline"
                      className="w-full gap-2 text-slate-600"
                      onClick={handleGoHome}
                    >
                      <Home className="w-4 h-4" />
                      Back to Home
                    </Button>
                    <Button
                      variant="ghost"
                      className="w-full gap-2 text-slate-500 hover:text-red-600"
                      onClick={handleLogoutAndExit}
                    >
                      <LogOut className="w-4 h-4" />
                      Sign out & Exit
                    </Button>
                  </div>

                  {/* Hint */}
                  <p className="text-xs text-slate-400 text-center">
                    Check your spam folder if you can't find the email.
                    <br />
                    The code expires in 10 minutes.
                  </p>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {/* Step 2: Success */}
          {step === "success" && (
            <motion.div
              key="success"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.35, ease: "easeOut" }}
            >
              <Card className="shadow-xl border-slate-100 overflow-hidden">
                {/* Success banner */}
                <div className="bg-gradient-to-r from-teal-500 to-emerald-500 px-6 py-8 text-center relative overflow-hidden">
                  {/* Floating confetti dots */}
                  {[...Array(12)].map((_, i) => (
                    <motion.div
                      key={i}
                      initial={{ y: -20, opacity: 0 }}
                      animate={{
                        y: [0, -15, 0],
                        opacity: [0, 1, 0.6],
                      }}
                      transition={{
                        delay: 0.2 + i * 0.1,
                        duration: 2,
                        repeat: Infinity,
                        repeatType: "reverse",
                      }}
                      className="absolute w-2 h-2 rounded-full"
                      style={{
                        left: `${8 + i * 8}%`,
                        top: `${20 + (i % 3) * 25}%`,
                        backgroundColor: [
                          "#fbbf24",
                          "#f472b6",
                          "#a78bfa",
                          "#34d399",
                          "#60a5fa",
                          "#fb923c",
                        ][i % 6],
                      }}
                    />
                  ))}

                  <motion.div
                    initial={{ scale: 0, rotate: -180 }}
                    animate={{ scale: 1, rotate: 0 }}
                    transition={{
                      delay: 0.15,
                      type: "spring",
                      stiffness: 200,
                      damping: 12,
                    }}
                    className="w-20 h-20 bg-white/20 backdrop-blur-sm rounded-full flex items-center justify-center mx-auto mb-4"
                  >
                    <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center">
                      <ShieldCheck className="w-8 h-8 text-teal-600" />
                    </div>
                  </motion.div>

                  <motion.h2
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.3 }}
                    className="text-xl text-white mb-1"
                  >
                    Email Verified!
                  </motion.h2>
                  <motion.p
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.4 }}
                    className="text-teal-100 text-sm"
                  >
                    Your account is now fully activated
                  </motion.p>
                </div>

                <CardContent className="pt-6 space-y-4">
                  {/* Welcome message */}
                  <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.5 }}
                    className="text-center space-y-2"
                  >
                    <div className="flex items-center justify-center gap-2 text-slate-700">
                      <PartyPopper className="w-5 h-5 text-amber-500" />
                      <span className="text-sm">
                        Welcome to WashMate, <strong>{user?.firstName || "there"}</strong>!
                      </span>
                      <PartyPopper className="w-5 h-5 text-amber-500 scale-x-[-1]" />
                    </div>
                    <p className="text-xs text-slate-500">
                      What would you like to do next?
                    </p>
                  </motion.div>

                  {/* Action buttons */}
                  <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.6 }}
                    className="space-y-3"
                  >
                    {/* Primary CTA: Go to Dashboard */}
                    <Button
                      className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm h-12 gap-2"
                      onClick={handleGoToDashboard}
                    >
                      <Droplets className="w-4 h-4" />
                      Go to Dashboard
                      <ArrowRight className="w-4 h-4 ml-auto" />
                    </Button>

                    {/* Secondary: Back to Home */}
                    <Button
                      variant="outline"
                      className="w-full gap-2 text-slate-600 h-11"
                      onClick={handleGoHome}
                    >
                      <Home className="w-4 h-4" />
                      Back to Home
                    </Button>

                    {/* Tertiary: Logout */}
                    <Button
                      variant="ghost"
                      className="w-full gap-2 text-slate-400 hover:text-slate-600 text-xs"
                      onClick={handleLogoutAndExit}
                    >
                      <LogOut className="w-3.5 h-3.5" />
                      Sign out & Exit
                    </Button>
                  </motion.div>
                </CardContent>
              </Card>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </div>
  );
}



