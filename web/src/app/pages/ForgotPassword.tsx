import { useState, useRef, useEffect, KeyboardEvent, ClipboardEvent } from "react";
import { Link, useNavigate } from "react-router";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { supabase } from "../../lib/supabase";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "../components/Card";
import {
  Droplets,
  Mail,
  ArrowLeft,
  AlertCircle,
  Lock,
  Eye,
  EyeOff,
  CheckCircle2,
  ShieldCheck,
  KeyRound,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

// Schemas for each step
const emailSchema = z.object({
  email: z.string().email({ message: "Please enter a valid email address" }),
});

const newPasswordSchema = z
  .object({
    password: z
      .string()
      .min(8, { message: "Password must be at least 8 characters" })
      .regex(/[A-Z]/, { message: "Must contain an uppercase letter" })
      .regex(/[a-z]/, { message: "Must contain a lowercase letter" })
      .regex(/[0-9]/, { message: "Must contain a number" }),
    confirm_password: z.string(),
  })
  .refine((data) => data.password === data.confirm_password, {
    message: "Passwords do not match",
    path: ["confirm_password"],
  });

type EmailFormValues = z.infer<typeof emailSchema>;
type NewPasswordFormValues = z.infer<typeof newPasswordSchema>;

// Password strength component
function PasswordStrength({ password }: { password: string }) {
  const checks = [
    { label: "8+ characters", met: password.length >= 8 },
    { label: "Uppercase letter", met: /[A-Z]/.test(password) },
    { label: "Lowercase letter", met: /[a-z]/.test(password) },
    { label: "Number", met: /[0-9]/.test(password) },
  ];
  const strength = checks.filter((c) => c.met).length;
  const strengthLabel =
    strength === 0
      ? ""
      : strength <= 2
      ? "Weak"
      : strength === 3
      ? "Fair"
      : "Strong";
  const strengthColor =
    strength <= 2
      ? "bg-red-400"
      : strength === 3
      ? "bg-yellow-400"
      : "bg-green-500";

  if (!password) return null;

  return (
    <div className="space-y-2 mt-2">
      <div className="flex gap-1">
        {[1, 2, 3, 4].map((i) => (
          <div
            key={i}
            className={`h-1 flex-1 rounded-full transition-colors ${
              i <= strength ? strengthColor : "bg-slate-200"
            }`}
          />
        ))}
      </div>
      <div className="flex justify-between">
        <p className="text-xs text-slate-500">{strengthLabel}</p>
      </div>
      <div className="grid grid-cols-2 gap-1">
        {checks.map((check) => (
          <div
            key={check.label}
            className={`flex items-center gap-1 text-xs ${
              check.met ? "text-green-600" : "text-slate-400"
            }`}
          >
            <CheckCircle2 className="w-3 h-3" />
            {check.label}
          </div>
        ))}
      </div>
    </div>
  );
}

// OTP Input component
function OTPInput({
  length = 6,
  value,
  onChange,
  error,
}: {
  length?: number;
  value: string[];
  onChange: (val: string[]) => void;
  error?: boolean;
}) {
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

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
    <div className="flex gap-2 justify-center">
      {Array.from({ length }).map((_, i) => (
        <input
          key={i}
          ref={(el) => { inputRefs.current[i] = el; }}
          type="text"
          inputMode="numeric"
          maxLength={1}
          value={value[i] || ""}
          onChange={(e) => handleChange(i, e.target.value)}
          onKeyDown={(e) => handleKeyDown(i, e)}
          onPaste={i === 0 ? handlePaste : undefined}
          className={`w-11 h-12 text-center text-lg font-semibold rounded-lg border-2 bg-white transition-all focus:outline-none focus:ring-2 focus:ring-teal-500/30 ${
            error
              ? "border-red-400 focus:border-red-500"
              : value[i]
              ? "border-teal-400 focus:border-teal-500"
              : "border-slate-400 focus:border-teal-500"
          }`}
        />
      ))}
    </div>
  );
}

type Step = "email" | "otp" | "new-password" | "success";

export default function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>("email");
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [otp, setOtp] = useState<string[]>(Array(6).fill(""));
  const [otpError, setOtpError] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  // Cooldown timer
  useEffect(() => {
    if (cooldown > 0) {
      const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [cooldown]);

  // Email form
  const emailForm = useForm<EmailFormValues>({
    resolver: zodResolver(emailSchema),
    defaultValues: { email: "" },
  });

  // Password form
  const passwordForm = useForm<NewPasswordFormValues>({
    resolver: zodResolver(newPasswordSchema),
    defaultValues: { password: "", confirm_password: "" },
  });

  const watchPassword = passwordForm.watch("password");

  // Step 1: Submit email
  const handleEmailSubmit = async (data: EmailFormValues) => {
    setError(null);
    setIsLoading(true);
    const { error: resetError } = await supabase.auth.resetPasswordForEmail(data.email);
    setIsLoading(false);

    if (resetError) {
      setError(resetError.message);
      return;
    }

    setEmail(data.email);
    setCooldown(60);
    setStep("otp");
  };

  // Step 2: Verify OTP
  const handleVerifyOtp = async () => {
    setError(null);
    const code = otp.join("");
    if (code.length !== 6) {
      setOtpError(true);
      setError("Please enter all 6 digits.");
      return;
    }

    setIsLoading(true);
    const { error: verifyError } = await supabase.auth.verifyOtp({
      email,
      token: code,
      type: "recovery",
    });
    setIsLoading(false);

    if (verifyError) {
      setOtpError(true);
      setError("Invalid verification code. Please try again.");
      return;
    }

    setOtpError(false);
    setStep("new-password");
  };

  // Resend OTP
  const handleResendOtp = async () => {
    setIsLoading(true);
    const { error: resendError } = await supabase.auth.resetPasswordForEmail(email);
    setIsLoading(false);
    if (!resendError) {
      setOtp(Array(6).fill(""));
      setOtpError(false);
      setError(null);
      setCooldown(60);
    } else {
      setError("Failed to resend code. Please try again.");
    }
  };

  // Step 3: Set new password
  const handleNewPassword = async (data: NewPasswordFormValues) => {
    setError(null);
    setIsLoading(true);
    const { error: updateError } = await supabase.auth.updateUser({
      password: data.password,
    });
    setIsLoading(false);
    if (updateError) {
      setError(updateError.message);
      return;
    }
    setStep("success");
  };

  // Mask email for display
  const maskedEmail = email
    ? email.replace(/(.{2})(.*)(@.*)/, (_, a, b, c) => a + "*".repeat(b.length) + c)
    : "";

  // Step config for header
  const stepConfig: Record<Step, { icon: typeof Mail; title: string; desc: string }> = {
    email: {
      icon: Mail,
      title: "Forgot your password?",
      desc: "Enter your email and we'll send you a verification code",
    },
    otp: {
      icon: KeyRound,
      title: "Enter verification code",
      desc: `We sent a 6-digit code to ${maskedEmail}`,
    },
    "new-password": {
      icon: Lock,
      title: "Create new password",
      desc: "Your new password must be different from previously used passwords",
    },
    success: {
      icon: ShieldCheck,
      title: "Password reset successful!",
      desc: "Your password has been changed successfully",
    },
  };

  const currentStep = stepConfig[step];
  const StepIcon = currentStep.icon;

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
            {/* Back button */}
            {step !== "success" && (
              <div className="w-full flex justify-start">
                <button
                  onClick={() => {
                    if (step === "email") navigate("/login");
                    else if (step === "otp") setStep("email");
                    else if (step === "new-password") setStep("otp");
                  }}
                  className="inline-flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-700 transition-colors"
                >
                  <ArrowLeft className="w-4 h-4" />
                  Back
                </button>
              </div>
            )}

            <motion.div
              key={step}
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ duration: 0.3 }}
              className={`w-14 h-14 rounded-full flex items-center justify-center ${
                step === "success" ? "bg-green-100" : "bg-teal-100"
              }`}
            >
              <StepIcon
                className={`w-7 h-7 ${
                  step === "success" ? "text-green-600" : "text-teal-600"
                }`}
              />
            </motion.div>

            <CardTitle className="text-2xl tracking-tight text-slate-900">
              {currentStep.title}
            </CardTitle>
            <CardDescription>{currentStep.desc}</CardDescription>

            {/* Step progress dots (except success) */}
            {step !== "success" && (
              <div className="flex items-center gap-2 pt-1">
                {(["email", "otp", "new-password"] as Step[]).map((s, i) => (
                  <div key={s} className="flex items-center gap-2">
                    <div
                      className={`w-2 h-2 rounded-full transition-colors ${
                        s === step
                          ? "bg-teal-500"
                          : (["email", "otp", "new-password"].indexOf(step) > i)
                          ? "bg-teal-300"
                          : "bg-slate-400"
                      }`}
                    />
                    {i < 2 && <div className="w-8 h-px bg-slate-400" />}
                  </div>
                ))}
              </div>
            )}
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

            <AnimatePresence mode="wait">
              {/* Step 1: Email */}
              {step === "email" && (
                <motion.div
                  key="email"
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  transition={{ duration: 0.2 }}
                >
                  <form
                    onSubmit={emailForm.handleSubmit(handleEmailSubmit)}
                    className="space-y-4"
                  >
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
                        htmlFor="forgot-email"
                      >
                        Email Address
                      </label>
                      <div className="relative">
                        <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <Input
                          id="forgot-email"
                          type="email"
                          placeholder="name@example.com"
                          autoComplete="email"
                          {...emailForm.register("email")}
                          className={`pl-9 ${
                            emailForm.formState.errors.email
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }`}
                        />
                      </div>
                      {emailForm.formState.errors.email && (
                        <p className="text-sm text-red-500">
                          {emailForm.formState.errors.email.message}
                        </p>
                      )}
                    </div>

                    <Button
                      type="submit"
                      className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                      disabled={isLoading}
                    >
                      {isLoading ? "Sending code..." : "Send verification code"}
                    </Button>
                  </form>

                  <div className="mt-6 text-center">
                    <p className="text-sm text-slate-500">
                      Remember your password?{" "}
                      <Link
                        to="/login"
                        className="text-teal-600 hover:underline"
                      >
                        Sign in
                      </Link>
                    </p>
                  </div>
                </motion.div>
              )}

              {/* Step 2: OTP */}
              {step === "otp" && (
                <motion.div
                  key="otp"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  transition={{ duration: 0.2 }}
                  className="space-y-6"
                >
                  <OTPInput
                    value={otp}
                    onChange={(val) => {
                      setOtp(val);
                      setOtpError(false);
                      setError(null);
                    }}
                    error={otpError}
                  />

                  <Button
                    type="button"
                    className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                    disabled={isLoading || otp.join("").length !== 6}
                    onClick={handleVerifyOtp}
                  >
                    {isLoading ? "Verifying..." : "Verify code"}
                  </Button>

                  <div className="text-center space-y-2">
                    <p className="text-sm text-slate-500">
                      Didn't receive the code?
                    </p>
                    <button
                      onClick={handleResendOtp}
                      disabled={cooldown > 0 || isLoading}
                      className="text-sm text-teal-600 hover:underline disabled:text-slate-400 disabled:no-underline disabled:cursor-not-allowed transition-colors"
                    >
                      {cooldown > 0
                        ? `Resend code in ${cooldown}s`
                        : "Resend code"}
                    </button>
                  </div>
                </motion.div>
              )}

              {/* Step 3: New Password */}
              {step === "new-password" && (
                <motion.div
                  key="new-password"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  transition={{ duration: 0.2 }}
                >
                  <form
                    onSubmit={passwordForm.handleSubmit(handleNewPassword)}
                    className="space-y-4"
                  >
                    {/* New Password */}
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
                        htmlFor="new-password"
                      >
                        New Password
                      </label>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <Input
                          id="new-password"
                          type={showPassword ? "text" : "password"}
                          placeholder="Create a strong password"
                          autoComplete="new-password"
                          {...passwordForm.register("password")}
                          className={`pl-9 pr-10 ${
                            passwordForm.formState.errors.password
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }`}
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
                      {passwordForm.formState.errors.password && (
                        <p className="text-sm text-red-500">
                          {passwordForm.formState.errors.password.message}
                        </p>
                      )}
                      <PasswordStrength password={watchPassword || ""} />
                    </div>

                    {/* Confirm Password */}
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
                        htmlFor="confirm-new-password"
                      >
                        Confirm New Password
                      </label>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <Input
                          id="confirm-new-password"
                          type={showConfirm ? "text" : "password"}
                          placeholder="Re-enter your password"
                          autoComplete="new-password"
                          {...passwordForm.register("confirm_password")}
                          className={`pl-9 pr-10 ${
                            passwordForm.formState.errors.confirm_password
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }`}
                        />
                        <button
                          type="button"
                          onClick={() => setShowConfirm(!showConfirm)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                          tabIndex={-1}
                        >
                          {showConfirm ? (
                            <EyeOff className="w-4 h-4" />
                          ) : (
                            <Eye className="w-4 h-4" />
                          )}
                        </button>
                      </div>
                      {passwordForm.formState.errors.confirm_password && (
                        <p className="text-sm text-red-500">
                          {passwordForm.formState.errors.confirm_password.message}
                        </p>
                      )}
                    </div>

                    <Button
                      type="submit"
                      className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                      disabled={isLoading}
                    >
                      {isLoading ? "Resetting password..." : "Reset password"}
                    </Button>
                  </form>
                </motion.div>
              )}

              {/* Step 4: Success */}
              {step === "success" && (
                <motion.div
                  key="success"
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.3 }}
                  className="space-y-4 text-center"
                >
                  {/* Success animation */}
                  <motion.div
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ delay: 0.1, type: "spring", stiffness: 200 }}
                    className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto"
                  >
                    <CheckCircle2 className="w-8 h-8 text-green-600" />
                  </motion.div>

                  <p className="text-sm text-slate-600">
                    You can now sign in with your new password.
                  </p>

                  <div className="space-y-3 pt-2">
                    <Button
                      className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                      onClick={() => navigate("/login")}
                    >
                      Sign in now
                    </Button>
                    <Button
                      variant="outline"
                      className="w-full"
                      onClick={() => navigate("/")}
                    >
                      Back to Home
                    </Button>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
}
