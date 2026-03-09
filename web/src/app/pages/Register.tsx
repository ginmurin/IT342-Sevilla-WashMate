import { useState } from "react";
import { useNavigate, Link } from "react-router";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { authAPI } from "../utils/api";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "../components/Card";
import { Droplets, AlertCircle } from "lucide-react";

const registerSchema = z.object({
  name: z.string().min(2, { message: "Name must be at least 2 characters" }),
  email: z.string().email({ message: "Invalid email address" }),
  password: z.string().min(6, { message: "Password must be at least 6 characters" }),
  role: z.enum(["customer", "shop_owner", "admin"] as const, {
    error: () => ({ message: "Please select a valid role" })
  }),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export function Register() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: "",
      email: "",
      password: "",
      role: "customer",
    },
  });

  const onSubmit = async (data: RegisterFormValues) => {
    setError(null);
    try {
      await authAPI.register({
        name: data.name,
        email: data.email,
        password: data.password,
        phone: "",
        confirmPassword: data.password,
        acceptTerms: true,
        role: data.role,
      });
      navigate("/login", { replace: true, state: { message: "Registration successful! Please log in." } });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Registration failed. Please try again.";
      setError(message);
    }
  };

  return (
    <div className="flex-1 flex items-center justify-center">
      <Card className="w-full max-w-md shadow-xl border-slate-100">
        <CardHeader className="space-y-3 items-center text-center">
          <div className="w-12 h-12 bg-teal-100 rounded-full flex items-center justify-center mb-2">
            <Droplets className="w-6 h-6 text-teal-600" />
          </div>
          <CardTitle className="text-2xl font-bold tracking-tight text-slate-900">
            Create an Account
          </CardTitle>
          <CardDescription>
            Join WashMate to manage your laundry services
          </CardDescription>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-4 flex items-center gap-2 p-3 text-sm text-red-600 bg-red-50 rounded-md">
              <AlertCircle className="w-4 h-4" />
              {error}
            </div>
          )}
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium leading-none text-slate-700" htmlFor="name">
                Full Name
              </label>
              <Input
                id="name"
                placeholder="John Doe"
                {...register("name")}
                className={errors.name ? "border-red-500 focus-visible:ring-red-500" : ""}
              />
              {errors.name && (
                <p className="text-sm text-red-500">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium leading-none text-slate-700" htmlFor="email">
                Email
              </label>
              <Input
                id="email"
                type="email"
                placeholder="name@example.com"
                {...register("email")}
                className={errors.email ? "border-red-500 focus-visible:ring-red-500" : ""}
              />
              {errors.email && (
                <p className="text-sm text-red-500">{errors.email.message}</p>
              )}
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium leading-none text-slate-700" htmlFor="password">
                Password
              </label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                {...register("password")}
                className={errors.password ? "border-red-500 focus-visible:ring-red-500" : ""}
              />
              {errors.password && (
                <p className="text-sm text-red-500">{errors.password.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium leading-none text-slate-700" htmlFor="role">
                Account Type
              </label>
              <select
                id="role"
                {...register("role")}
                className="flex h-10 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm ring-offset-background placeholder:text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal-500 focus-visible:border-transparent disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="customer">Customer</option>
                <option value="shop_owner">Shop Owner</option>
                <option value="admin">Administrator</option>
              </select>
              {errors.role && (
                <p className="text-sm text-red-500">{errors.role.message}</p>
              )}
            </div>

            <Button
              type="submit"
              className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Creating account..." : "Sign up"}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex justify-center border-t border-slate-100 p-4">
          <p className="text-sm text-slate-500">
            Already have an account?{" "}
            <Link to="/login" className="font-semibold text-teal-600 hover:underline">
              Log in
            </Link>
          </p>
        </CardFooter>
      </Card>
    </div>
  );
}
