import { useState } from "react";
import { useNavigate, Link } from "react-router";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuth } from "../contexts/AuthContext";
import { authAPI } from "../utils/api";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import {
  Droplets,
  AlertCircle,
  Eye,
  EyeOff,
  User,
  Mail,
  Phone,
  Lock,
  CheckCircle2,
  ArrowRight,
  ArrowLeft,
  Sparkles,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import registerHero from "../../assets/register-hero.png";

const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, { message: "Username must be at least 3 characters" })
      .max(30, { message: "Username must be at most 30 characters" })
      .regex(/^[a-zA-Z0-9_]+$/, {
        message: "Only letters, numbers, and underscores allowed",
      }),
    first_name: z
      .string()
      .min(1, { message: "First name is required" })
      .max(50, { message: "First name must be at most 50 characters" }),
    last_name: z
      .string()
      .min(1, { message: "Last name is required" })
      .max(50, { message: "Last name must be at most 50 characters" }),
    email: z.string().email({ message: "Invalid email address" }),
    phone_number: z
      .string()
      .min(10, { message: "Phone number must be at least 10 digits" })
      .regex(/^[+]?[\d\s()-]+$/, {
        message: "Invalid phone number format",
      }),
    password: z
      .string()
      .min(8, { message: "Password must be at least 8 characters" })
      .regex(/[A-Z]/, { message: "Must contain an uppercase letter" })
      .regex(/[a-z]/, { message: "Must contain a lowercase letter" })
      .regex(/[0-9]/, { message: "Must contain a number" }),
    confirm_password: z.string(),
    accept_terms: z.boolean().refine((val) => val === true, {
      message: "You must accept the terms and conditions",
    }),
  })
  .refine((data) => data.password === data.confirm_password, {
    message: "Passwords do not match",
    path: ["confirm_password"],
  });

type RegisterFormValues = z.infer<typeof registerSchema>;

function PasswordStrength({ password }: { password: string }) {
  const checks = [
    { label: "8+ characters", met: password.length >= 8 },
    { label: "Uppercase", met: /[A-Z]/.test(password) },
    { label: "Lowercase", met: /[a-z]/.test(password) },
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
      ? "bg-amber-400"
      : "bg-emerald-500";
  const strengthTextColor =
    strength <= 2
      ? "text-red-500"
      : strength === 3
      ? "text-amber-500"
      : "text-emerald-500";

  if (!password) return null;

  return (
    <motion.div
      initial={{ opacity: 0, height: 0 }}
      animate={{ opacity: 1, height: "auto" }}
      className="space-y-2 mt-2"
    >
      <div className="flex items-center gap-2">
        <div className="flex gap-1 flex-1">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
                i <= strength ? strengthColor : "bg-slate-100"
              }`}
            />
          ))}
        </div>
        <span className={`text-xs ${strengthTextColor}`}>{strengthLabel}</span>
      </div>
      <div className="grid grid-cols-2 gap-x-3 gap-y-1">
        {checks.map((check) => (
          <motion.div
            key={check.label}
            initial={false}
            animate={{ opacity: 1 }}
            className={`flex items-center gap-1.5 text-xs transition-colors ${
              check.met ? "text-emerald-600" : "text-slate-300"
            }`}
          >
            <CheckCircle2 className="w-3 h-3 shrink-0" />
            {check.label}
          </motion.div>
        ))}
      </div>
    </motion.div>
  );
}

// Google SVG icon
function GoogleIcon() {
  return (
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
  );
}

export function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [step, setStep] = useState<1 | 2>(1);

  const {
    register: formRegister,
    handleSubmit,
    watch,
    trigger,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: "",
      first_name: "",
      last_name: "",
      email: "",
      phone_number: "",
      password: "",
      confirm_password: "",
      accept_terms: false,
    },
  });

  const watchPassword = watch("password");

  const goToStep2 = async () => {
    const isValid = await trigger([
      "username",
      "first_name",
      "last_name",
      "email",
      "phone_number",
    ]);
    if (isValid) setStep(2);
  };

  const onSubmit = async (data: RegisterFormValues) => {
    setError(null);
    try {
      const result = await authAPI.register({
        username: data.username,
        firstName: data.first_name,
        lastName: data.last_name,
        email: data.email,
        phone: data.phone_number,
        password: data.password,
        confirmPassword: data.confirm_password,
        acceptTerms: data.accept_terms,
      });

      login(result.user);
      navigate("/verify-email", {
        state: { email: data.email, fromRegister: true },
        replace: true,
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Registration failed. Please try again.";
      if (message.toLowerCase().includes("username")) setStep(1);
      setError(message);
    }
  };

  const stepInfo = [
    { num: 1, label: "Your Info" },
    { num: 2, label: "Security" },
  ];

  return (
    <div className="flex-1 flex items-stretch min-h-screen pt-16">
      {/* Left image panel — hidden on mobile (matches Login layout) */}
      <div className="hidden lg:flex lg:w-[48%] relative overflow-hidden">
        {/* Full-bleed hero image */}
        <img
          src={registerHero}
          alt="Fresh laundry hanging on a clothesline"
          className="absolute inset-0 w-full h-full object-cover"
        />

        {/* Dark gradient overlay at the bottom for text readability */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />

        {/* Overlay content at bottom-left */}
        <div className="relative z-10 flex flex-col justify-end p-10 xl:p-14 w-full">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            {/* Logo */}
            <div className="flex items-center gap-2.5 mb-5">
              <div className="w-9 h-9 rounded-lg bg-white/20 backdrop-blur-sm flex items-center justify-center">
                <Droplets className="w-5 h-5 text-white" />
              </div>
              <span className="text-white text-lg font-medium tracking-tight">
                WashMate
              </span>
            </div>

            {/* Tagline */}
            <h2 className="text-white text-3xl xl:text-4xl font-semibold leading-tight">
              Fresh laundry,
              <br />
              delivered to your door.
            </h2>
            <p className="text-white/80 mt-3 max-w-sm text-sm leading-relaxed">
              Join thousands of happy customers who trust WashMate for their
              premium laundry needs.
            </p>
          </motion.div>
        </div>
      </div>

      {/* Right form panel */}
      <div className="flex-1 flex items-center justify-center px-4 py-8 sm:px-8 lg:px-12 xl:px-16 bg-white">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, ease: "easeOut" }}
          className="w-full max-w-md"
        >
          {/* Mobile-only header */}
          <div className="lg:hidden flex items-center gap-2.5 mb-6">
            <div className="w-9 h-9 rounded-lg bg-teal-100 flex items-center justify-center">
              <Droplets className="w-5 h-5 text-teal-600" />
            </div>
            <span className="text-slate-800 text-lg font-medium tracking-tight">
              WashMate
            </span>
          </div>

          {/* Title */}
          <div className="mb-6">
            <h1 className="text-2xl font-semibold text-slate-900 tracking-tight">
              Create Account
            </h1>
            <p className="text-slate-500 mt-1 text-sm">
              Enter your details to get started with WashMate.
            </p>
          </div>

          {/* Google OAuth — at top */}
          <Button
            type="button"
            variant="outline"
            className="w-full gap-2 text-slate-700 h-11 bg-white hover:bg-slate-50 border-slate-200"
            onClick={() => {
              alert("Google OAuth would redirect to Google sign-up");
            }}
          >
            <GoogleIcon />
            Sign up with Google
          </Button>

          {/* Divider */}
          <div className="relative my-5">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200" />
            </div>
            <div className="relative flex justify-center text-xs">
              <span className="bg-white px-3 text-slate-400 uppercase tracking-wider">
                or register with email
              </span>
            </div>
          </div>

          {/* Step indicator */}
          <div className="flex items-center gap-0 mb-6">
            {stepInfo.map((s, i) => (
              <div key={s.num} className="flex items-center flex-1">
                <div className="flex items-center gap-2">
                  <motion.div
                    animate={{
                      backgroundColor:
                        step >= s.num ? "rgb(13 148 136)" : "rgb(241 245 249)",
                      color:
                        step >= s.num ? "white" : "rgb(148 163 184)",
                    }}
                    transition={{ duration: 0.3 }}
                    className="w-8 h-8 rounded-full flex items-center justify-center text-xs shrink-0"
                  >
                    {step > s.num ? (
                      <CheckCircle2 className="w-4 h-4" />
                    ) : (
                      s.num
                    )}
                  </motion.div>
                  <span
                    className={`text-xs whitespace-nowrap transition-colors ${
                      step >= s.num ? "text-teal-700" : "text-slate-400"
                    }`}
                  >
                    {s.label}
                  </span>
                </div>
                {i < stepInfo.length - 1 && (
                  <div className="flex-1 mx-3">
                    <div className="h-px bg-slate-200 relative">
                      <motion.div
                        animate={{ width: step > s.num ? "100%" : "0%" }}
                        transition={{ duration: 0.4 }}
                        className="absolute inset-y-0 left-0 bg-teal-500"
                      />
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Error */}
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

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)}>
            <AnimatePresence mode="wait">
              {step === 1 && (
                <motion.div
                  key="step1"
                  initial={{ opacity: 0, x: -24 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -24 }}
                  transition={{ duration: 0.25 }}
                  className="space-y-4"
                >
                  {/* Full Name */}
                  <div className="space-y-1.5">
                    <label
                      className="text-sm font-medium text-slate-700"
                      htmlFor="full_name"
                    >
                      Full Name
                    </label>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <div className="relative">
                          <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                          <Input
                            id="first_name"
                            placeholder="First name"
                            autoComplete="given-name"
                            {...formRegister("first_name")}
                            className={`pl-9 ${
                              errors.first_name
                                ? "border-red-400 focus-visible:ring-red-400"
                                : ""
                            }`}
                          />
                        </div>
                        {errors.first_name && (
                          <p className="text-xs text-red-500 mt-1">
                            {errors.first_name.message}
                          </p>
                        )}
                      </div>
                      <div>
                        <Input
                          id="last_name"
                          placeholder="Last name"
                          autoComplete="family-name"
                          {...formRegister("last_name")}
                          className={
                            errors.last_name
                              ? "border-red-400 focus-visible:ring-red-400"
                              : ""
                          }
                        />
                        {errors.last_name && (
                          <p className="text-xs text-red-500 mt-1">
                            {errors.last_name.message}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Username */}
                  <div className="space-y-1.5">
                    <label
                      className="text-sm font-medium text-slate-700"
                      htmlFor="username"
                    >
                      Username
                    </label>
                    <div className="relative">
                      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">
                        @
                      </span>
                      <Input
                        id="username"
                        placeholder="your_username"
                        autoComplete="username"
                        {...formRegister("username")}
                        className={`pl-7 ${
                          errors.username
                            ? "border-red-400 focus-visible:ring-red-400"
                            : ""
                        }`}
                      />
                    </div>
                    {errors.username && (
                      <p className="text-xs text-red-500">
                        {errors.username.message}
                      </p>
                    )}
                  </div>

                  {/* Email */}
                  <div className="space-y-1.5">
                    <label
                      className="text-sm font-medium text-slate-700"
                      htmlFor="email"
                    >
                      Email Address
                    </label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                      <Input
                        id="email"
                        type="email"
                        placeholder="name@example.com"
                        autoComplete="email"
                        {...formRegister("email")}
                        className={`pl-9 ${
                          errors.email
                            ? "border-red-400 focus-visible:ring-red-400"
                            : ""
                        }`}
                      />
                    </div>
                    {errors.email && (
                      <p className="text-xs text-red-500">
                        {errors.email.message}
                      </p>
                    )}
                  </div>

                  {/* Phone */}
                  <div className="space-y-1.5">
                    <label
                      className="text-sm font-medium text-slate-700"
                      htmlFor="phone_number"
                    >
                      Phone Number
                    </label>
                    <div className="relative">
                      <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                      <Input
                        id="phone_number"
                        type="tel"
                        placeholder="+63 912 345 6789"
                        autoComplete="tel"
                        {...formRegister("phone_number")}
                        className={`pl-9 ${
                          errors.phone_number
                            ? "border-red-400 focus-visible:ring-red-400"
                            : ""
                        }`}
                      />
                    </div>
                    {errors.phone_number && (
                      <p className="text-xs text-red-500">
                        {errors.phone_number.message}
                      </p>
                    )}
                  </div>

                  {/* Continue */}
                  <Button
                    type="button"
                    className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm h-11 gap-2 rounded-lg"
                    onClick={goToStep2}
                  >
                    Continue
                    <ArrowRight className="w-4 h-4" />
                  </Button>
                </motion.div>
              )}

              {step === 2 && (
                <motion.div
                  key="step2"
                  initial={{ opacity: 0, x: 24 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: 24 }}
                  transition={{ duration: 0.25 }}
                  className="space-y-4"
                >
                  {/* Security header */}
                  <div className="flex items-center gap-2.5 p-3 bg-teal-50 rounded-xl border border-teal-100 mb-2">
                    <div className="w-8 h-8 rounded-lg bg-teal-100 flex items-center justify-center shrink-0">
                      <Sparkles className="w-4 h-4 text-teal-600" />
                    </div>
                    <p className="text-xs text-teal-700">
                      Almost there! Create a strong password to secure your account.
                    </p>
                  </div>

                  {/* Password */}
                  <div className="space-y-1.5">
                    <label
                      className="text-sm font-medium text-slate-700"
                      htmlFor="password"
                    >
                      Password
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                      <Input
                        id="password"
                        type={showPassword ? "text" : "password"}
                        placeholder="Min. 8 characters"
                        autoComplete="new-password"
                        {...formRegister("password")}
                        className={`pl-9 pr-10 ${
                          errors.password
                            ? "border-red-400 focus-visible:ring-red-400"
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
                    {errors.password && (
                      <p className="text-xs text-red-500">
                        {errors.password.message}
                      </p>
                    )}
                    <PasswordStrength password={watchPassword || ""} />
                  </div>

                  {/* Confirm Password */}
                  <div className="space-y-1.5">
                    <label
                      className="text-sm font-medium text-slate-700"
                      htmlFor="confirm_password"
                    >
                      Confirm Password
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                      <Input
                        id="confirm_password"
                        type={showConfirm ? "text" : "password"}
                        placeholder="Re-enter password"
                        autoComplete="new-password"
                        {...formRegister("confirm_password")}
                        className={`pl-9 pr-10 ${
                          errors.confirm_password
                            ? "border-red-400 focus-visible:ring-red-400"
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
                    {errors.confirm_password && (
                      <p className="text-xs text-red-500">
                        {errors.confirm_password.message}
                      </p>
                    )}
                  </div>

                  {/* Terms */}
                  <div className="space-y-1.5">
                    <label className="flex items-start gap-2.5 cursor-pointer group">
                      <input
                        type="checkbox"
                        {...formRegister("accept_terms")}
                        className="h-4 w-4 mt-0.5 rounded border-slate-300 text-teal-600 focus:ring-teal-500"
                      />
                      <span className="text-sm text-slate-600 leading-snug">
                        I agree to the{" "}
                        <a
                          href="#"
                          className="text-teal-600 hover:underline"
                          onClick={(e) => e.preventDefault()}
                        >
                          Terms of Service
                        </a>{" "}
                        and{" "}
                        <a
                          href="#"
                          className="text-teal-600 hover:underline"
                          onClick={(e) => e.preventDefault()}
                        >
                          Privacy Policy
                        </a>
                      </span>
                    </label>
                    {errors.accept_terms && (
                      <p className="text-xs text-red-500">
                        {errors.accept_terms.message}
                      </p>
                    )}
                  </div>

                  {/* Buttons */}
                  <div className="flex gap-3 pt-1">
                    <Button
                      type="button"
                      variant="outline"
                      className="gap-1.5 bg-white hover:bg-slate-50 border-slate-200"
                      onClick={() => setStep(1)}
                    >
                      <ArrowLeft className="w-4 h-4" />
                      Back
                    </Button>
                    <Button
                      type="submit"
                      className="flex-1 bg-teal-600 hover:bg-teal-700 text-white shadow-sm h-11 gap-2 rounded-lg"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? (
                        <>
                          <motion.div
                            animate={{ rotate: 360 }}
                            transition={{
                              repeat: Infinity,
                              duration: 1,
                              ease: "linear",
                            }}
                            className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full"
                          />
                          Creating account...
                        </>
                      ) : (
                        <>
                          Create Account
                          <ArrowRight className="w-4 h-4" />
                        </>
                      )}
                    </Button>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </form>

          {/* Footer */}
          <p className="text-sm text-slate-500 text-center mt-6">
            Already have an account?{" "}
            <Link to="/login" className="text-teal-600 hover:underline font-medium">
              Log in
            </Link>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
