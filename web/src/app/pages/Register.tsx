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
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from "../components/Card";
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
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, { message: "Username must be at least 3 characters" })
      .max(30, { message: "Username must be at most 30 characters" })
      .regex(/^[a-zA-Z0-9_]+$/, {
        message: "Username can only contain letters, numbers, and underscores",
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
      .regex(/[A-Z]/, {
        message: "Password must contain at least one uppercase letter",
      })
      .regex(/[a-z]/, {
        message: "Password must contain at least one lowercase letter",
      })
      .regex(/[0-9]/, {
        message: "Password must contain at least one number",
      }),
    confirm_password: z.string(),
    role: z.enum(["customer", "shop_owner"] as const, {
      errorMap: () => ({ message: "Please select an account type" }),
    }),
    accept_terms: z.boolean().refine((val) => val === true, {
      message: "You must accept the terms and conditions",
    }),
  })
  .refine((data) => data.password === data.confirm_password, {
    message: "Passwords do not match",
    path: ["confirm_password"],
  });

type RegisterFormValues = z.infer<typeof registerSchema>;

// Mock existing usernames/emails for validation
// (removed — validation is now done server-side)

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
      role: "customer",
      accept_terms: false,
    },
  });

  const watchPassword = watch("password");
  const watchRole = watch("role");

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
        role: data.role.toUpperCase(),
      });
      login(result.user);
      navigate("/verify-email", { state: { email: data.email, fromRegister: true }, replace: true });
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Registration failed. Please try again.");
    }
  };

  return (
    <div className="flex-1 flex items-center justify-center py-12 px-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: "easeOut" }}
        className="w-full max-w-lg"
      >
        <Card className="shadow-xl border-slate-100">
          <CardHeader className="space-y-3 items-center text-center">
            <div className="w-12 h-12 bg-teal-100 rounded-full flex items-center justify-center mb-2">
              <Droplets className="w-6 h-6 text-teal-600" />
            </div>
            <CardTitle className="text-2xl tracking-tight text-slate-900">
              Create your account
            </CardTitle>
            <CardDescription>
              Join WashMate and get your laundry handled
            </CardDescription>

            {/* Step indicator */}
            <div className="flex items-center gap-2 pt-2">
              <div
                className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs transition-colors ${
                  step === 1
                    ? "bg-teal-100 text-teal-700"
                    : "bg-slate-100 text-slate-500"
                }`}
              >
                <span className="w-4 h-4 rounded-full bg-current/20 flex items-center justify-center text-[10px]">
                  1
                </span>
                Personal Info
              </div>
              <div className="w-6 h-px bg-slate-300" />
              <div
                className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs transition-colors ${
                  step === 2
                    ? "bg-teal-100 text-teal-700"
                    : "bg-slate-100 text-slate-500"
                }`}
              >
                <span className="w-4 h-4 rounded-full bg-current/20 flex items-center justify-center text-[10px]">
                  2
                </span>
                Security
              </div>
            </div>
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
              <AnimatePresence mode="wait">
                {step === 1 && (
                  <motion.div
                    key="step1"
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ duration: 0.2 }}
                    className="space-y-4"
                  >
                    {/* Account Type */}
                    <div className="space-y-2">
                      <label className="text-sm leading-none text-slate-700">
                        I want to join as
                      </label>
                      <div className="grid grid-cols-2 gap-3">
                        <label
                          className={`relative flex flex-col items-center gap-2 p-4 rounded-lg border-2 cursor-pointer transition-all ${
                            watchRole === "customer"
                              ? "border-teal-500 bg-teal-50"
                              : "border-slate-200 hover:border-slate-300"
                          }`}
                        >
                          <input
                            type="radio"
                            value="customer"
                            {...formRegister("role")}
                            className="sr-only"
                          />
                          <div
                            className={`w-10 h-10 rounded-full flex items-center justify-center ${
                              watchRole === "customer"
                                ? "bg-teal-100 text-teal-600"
                                : "bg-slate-100 text-slate-400"
                            }`}
                          >
                            <User className="w-5 h-5" />
                          </div>
                          <span className="text-sm text-slate-700">
                            Customer
                          </span>
                          <span className="text-xs text-slate-500 text-center">
                            Get laundry done
                          </span>
                        </label>
                        <label
                          className={`relative flex flex-col items-center gap-2 p-4 rounded-lg border-2 cursor-pointer transition-all ${
                            watchRole === "shop_owner"
                              ? "border-teal-500 bg-teal-50"
                              : "border-slate-200 hover:border-slate-300"
                          }`}
                        >
                          <input
                            type="radio"
                            value="shop_owner"
                            {...formRegister("role")}
                            className="sr-only"
                          />
                          <div
                            className={`w-10 h-10 rounded-full flex items-center justify-center ${
                              watchRole === "shop_owner"
                                ? "bg-teal-100 text-teal-600"
                                : "bg-slate-100 text-slate-400"
                            }`}
                          >
                            <Droplets className="w-5 h-5" />
                          </div>
                          <span className="text-sm text-slate-700">
                            Shop Owner
                          </span>
                          <span className="text-xs text-slate-500 text-center">
                            Run a laundry shop
                          </span>
                        </label>
                      </div>
                      {errors.role && (
                        <p className="text-sm text-red-500">
                          {errors.role.message}
                        </p>
                      )}
                    </div>

                    {/* Username */}
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
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
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }`}
                        />
                      </div>
                      {errors.username && (
                        <p className="text-sm text-red-500">
                          {errors.username.message}
                        </p>
                      )}
                    </div>

                    {/* First & Last Name */}
                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-2">
                        <label
                          className="text-sm leading-none text-slate-700"
                          htmlFor="first_name"
                        >
                          First Name
                        </label>
                        <Input
                          id="first_name"
                          placeholder="Jane"
                          autoComplete="given-name"
                          {...formRegister("first_name")}
                          className={
                            errors.first_name
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }
                        />
                        {errors.first_name && (
                          <p className="text-sm text-red-500">
                            {errors.first_name.message}
                          </p>
                        )}
                      </div>
                      <div className="space-y-2">
                        <label
                          className="text-sm leading-none text-slate-700"
                          htmlFor="last_name"
                        >
                          Last Name
                        </label>
                        <Input
                          id="last_name"
                          placeholder="Doe"
                          autoComplete="family-name"
                          {...formRegister("last_name")}
                          className={
                            errors.last_name
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }
                        />
                        {errors.last_name && (
                          <p className="text-sm text-red-500">
                            {errors.last_name.message}
                          </p>
                        )}
                      </div>
                    </div>

                    {/* Email */}
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
                        htmlFor="email"
                      >
                        Email
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
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }`}
                        />
                      </div>
                      {errors.email && (
                        <p className="text-sm text-red-500">
                          {errors.email.message}
                        </p>
                      )}
                    </div>

                    {/* Phone Number */}
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
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
                              ? "border-red-500 focus-visible:ring-red-500"
                              : ""
                          }`}
                        />
                      </div>
                      {errors.phone_number && (
                        <p className="text-sm text-red-500">
                          {errors.phone_number.message}
                        </p>
                      )}
                    </div>

                    <Button
                      type="button"
                      className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                      onClick={goToStep2}
                    >
                      Continue
                    </Button>
                  </motion.div>
                )}

                {step === 2 && (
                  <motion.div
                    key="step2"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 20 }}
                    transition={{ duration: 0.2 }}
                    className="space-y-4"
                  >
                    {/* Password */}
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
                        htmlFor="password"
                      >
                        Password
                      </label>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <Input
                          id="password"
                          type={showPassword ? "text" : "password"}
                          placeholder="Create a strong password"
                          autoComplete="new-password"
                          {...formRegister("password")}
                          className={`pl-9 pr-10 ${
                            errors.password
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
                      {errors.password && (
                        <p className="text-sm text-red-500">
                          {errors.password.message}
                        </p>
                      )}
                      <PasswordStrength password={watchPassword || ""} />
                    </div>

                    {/* Confirm Password */}
                    <div className="space-y-2">
                      <label
                        className="text-sm leading-none text-slate-700"
                        htmlFor="confirm_password"
                      >
                        Confirm Password
                      </label>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <Input
                          id="confirm_password"
                          type={showConfirm ? "text" : "password"}
                          placeholder="Re-enter your password"
                          autoComplete="new-password"
                          {...formRegister("confirm_password")}
                          className={`pl-9 pr-10 ${
                            errors.confirm_password
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
                      {errors.confirm_password && (
                        <p className="text-sm text-red-500">
                          {errors.confirm_password.message}
                        </p>
                      )}
                    </div>

                    {/* Terms & Conditions */}
                    <div className="space-y-2">
                      <div className="flex items-start gap-2">
                        <input
                          id="accept_terms"
                          type="checkbox"
                          {...formRegister("accept_terms")}
                          className="h-4 w-4 mt-0.5 rounded border-slate-300 text-teal-600 focus:ring-teal-500"
                        />
                        <label
                          htmlFor="accept_terms"
                          className="text-sm text-slate-600 select-none cursor-pointer"
                        >
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
                        </label>
                      </div>
                      {errors.accept_terms && (
                        <p className="text-sm text-red-500">
                          {errors.accept_terms.message}
                        </p>
                      )}
                    </div>

                    <div className="flex gap-3">
                      <Button
                        type="button"
                        variant="outline"
                        className="flex-1"
                        onClick={() => setStep(1)}
                      >
                        Back
                      </Button>
                      <Button
                        type="submit"
                        className="flex-1 bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
                        disabled={isSubmitting}
                      >
                        {isSubmitting ? "Creating account..." : "Create account"}
                      </Button>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </form>

            {/* OAuth divider */}
            {step === 1 && (
              <>
                <div className="relative my-6">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-slate-200" />
                  </div>
                  <div className="relative flex justify-center text-xs">
                    <span className="bg-white px-2 text-slate-500">
                      Or sign up with
                    </span>
                  </div>
                </div>
                <div className="grid grid-cols-1 gap-3">
                  <Button
                    type="button"
                    variant="outline"
                    className="gap-2 text-slate-700"
                    onClick={() => {
                      alert(
                        "Google OAuth would redirect to Google sign-up"
                      );
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
              </>
            )}
          </CardContent>
          <CardFooter className="flex justify-center border-t border-slate-100 p-4">
            <p className="text-sm text-slate-500">
              Already have an account?{" "}
              <Link
                to="/login"
                className="text-teal-600 hover:underline"
              >
                Sign in
              </Link>
            </p>
          </CardFooter>
        </Card>
      </motion.div>
    </div>
  );
}