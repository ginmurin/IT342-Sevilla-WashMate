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
import { Droplets, AlertCircle } from "lucide-react";

const loginSchema = z.object({
  email: z.string().email({ message: "Invalid email address" }),
  password: z.string().min(6, { message: "Password must be at least 6 characters" }),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);

  const from = location.state?.from?.pathname || "/";

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (data: LoginFormValues) => {
    setError(null);
    try {
      const response = await authAPI.login({ email: data.email, password: data.password });
      const backendRole = response.user.role.toLowerCase() as "customer" | "shop_owner" | "admin";
      const user = {
        id: String(response.user.id),
        name: response.user.name,
        email: response.user.email,
        role: backendRole,
      };
      login(user);
      if (backendRole === "customer") navigate("/customer", { replace: true });
      else if (backendRole === "shop_owner") navigate("/shop", { replace: true });
      else if (backendRole === "admin") navigate("/admin", { replace: true });
      else navigate(from, { replace: true });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Login failed. Please try again.";
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
            Welcome back
          </CardTitle>
          <CardDescription>
            Enter your email and password to login to your account
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

            <Button
              type="submit"
              className="w-full bg-teal-600 hover:bg-teal-700 text-white shadow-sm"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Signing in..." : "Sign in"}
            </Button>
          </form>
          

        </CardContent>
        <CardFooter className="flex justify-center border-t border-slate-100 p-4">
          <p className="text-sm text-slate-500">
            Don't have an account?{" "}
            <Link to="/register" className="font-semibold text-teal-600 hover:underline">
              Sign up
            </Link>
          </p>
        </CardFooter>
      </Card>
    </div>
  );
}
