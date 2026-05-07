import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { User, Mail, Phone, Lock, Eye, EyeOff, Loader2, CheckCircle2 } from 'lucide-react';
import { useNavigate, Link } from 'react-router';
import type { RegisterData } from "@/features/shared/types";
import { authAPI } from "@/features/shared/utils/api";
import { useAuth } from "@/features/auth/AuthContext";
import { ErrorMessage } from '../shared/ErrorMessage';
import { getPasswordStrength } from "@/features/shared/utils/validation";

export function RegisterForm() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [password, setPassword] = useState('');

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<RegisterData>();

  const watchedPassword = watch('password', '');
  const watchedConfirmPassword = watch('confirmPassword', '');
  const passwordStrength = getPasswordStrength(watchedPassword);

  const onSubmit = async (data: RegisterData) => {
    setIsLoading(true);
    setError('');

    try {
      const response = await authAPI.register(data);
      if (response.requiresEmailVerification) {
        // Redirect to email verification page with state
        navigate('/verify-email', { 
          state: { 
            userId: response.userId,
            email: response.email 
          } 
        });
      } else {
        // Fallback for direct login (if ever enabled)
        navigate('/login', { state: { message: 'Registration successful. Please login.' } });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full">
      <div className="mb-8 text-center">
        <h1 className="mb-2">Get Started</h1>
        <p className="text-gray-600">Create your WashMate account</p>
      </div>

      {error && (
        <div className="mb-6">
          <ErrorMessage message={error} dismissible onDismiss={() => setError('')} />
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label htmlFor="firstName" className="mb-2 block text-sm text-gray-700">
              First Name
            </label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
              <input
                id="firstName"
                type="text"
                placeholder="Juan"
                className={`w-full rounded-lg border ${
                  errors.firstName ? 'border-red-500' : 'border-gray-300'
                } bg-white py-3 pl-11 pr-4 focus:border-[#2563EB] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 transition-all`}
                {...register('firstName', {
                  required: 'First name is required',
                  minLength: { value: 1, message: 'First name is required' },
                })}
              />
            </div>
            {errors.firstName && <p className="mt-1 text-sm text-red-500">{errors.firstName.message}</p>}
          </div>
          <div>
            <label htmlFor="lastName" className="mb-2 block text-sm text-gray-700">
              Last Name
            </label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
              <input
                id="lastName"
                type="text"
                placeholder="Dela Cruz"
                className={`w-full rounded-lg border ${
                  errors.lastName ? 'border-red-500' : 'border-gray-300'
                } bg-white py-3 pl-11 pr-4 focus:border-[#2563EB] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 transition-all`}
                {...register('lastName', {
                  required: 'Last name is required',
                  minLength: { value: 1, message: 'Last name is required' },
                })}
              />
            </div>
            {errors.lastName && <p className="mt-1 text-sm text-red-500">{errors.lastName.message}</p>}
          </div>
        </div>

        <div>
          <label htmlFor="email" className="mb-2 block text-sm text-gray-700">
            Email Address
          </label>
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
            <input
              id="email"
              type="email"
              placeholder="juan@example.com"
              className={`w-full rounded-lg border ${
                errors.email ? 'border-red-500' : 'border-gray-300'
              } bg-white py-3 pl-11 pr-4 focus:border-[#2563EB] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 transition-all`}
              {...register('email', {
                required: 'Email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Invalid email address',
                },
              })}
            />
          </div>
          {errors.email && <p className="mt-1 text-sm text-red-500">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="phone" className="mb-2 block text-sm text-gray-700">
            Phone Number
          </label>
          <div className="relative">
            <Phone className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
            <input
              id="phone"
              type="tel"
              placeholder="+63 912 345 6789"
              className={`w-full rounded-lg border ${
                errors.phone ? 'border-red-500' : 'border-gray-300'
              } bg-white py-3 pl-11 pr-4 focus:border-[#2563EB] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 transition-all`}
              {...register('phone', {
                required: 'Phone number is required',
                pattern: {
                  value: /^\+63\s?\d{3}\s?\d{3}\s?\d{4}$/,
                  message: 'Invalid Philippine phone number format',
                },
              })}
            />
          </div>
          {errors.phone && <p className="mt-1 text-sm text-red-500">{errors.phone.message}</p>}
        </div>

        <div>
          <label htmlFor="password" className="mb-2 block text-sm text-gray-700">
            Password
          </label>
          <div className="relative">
            <Lock className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              placeholder="Create a strong password"
              className={`w-full rounded-lg border ${
                errors.password ? 'border-red-500' : 'border-gray-300'
              } bg-white py-3 pl-11 pr-11 focus:border-[#2563EB] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 transition-all`}
              {...register('password', {
                required: 'Password is required',
                minLength: {
                  value: 8,
                  message: 'Password must be at least 8 characters',
                },
                pattern: {
                  value: /^(?=.*[A-Z])(?=.*[0-9])/,
                  message: 'Password must contain at least one uppercase letter and one number',
                },
              })}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
            </button>
          </div>
          {watchedPassword && (
            <div className="mt-2">
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs text-gray-600">Password strength:</span>
                <span className="text-xs capitalize" style={{ color: passwordStrength.color }}>
                  {passwordStrength.strength}
                </span>
              </div>
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className="h-full transition-all duration-300"
                  style={{
                    width: `${passwordStrength.percentage}%`,
                    backgroundColor: passwordStrength.color,
                  }}
                />
              </div>
              <p className="mt-1 text-xs text-gray-500">
                Min 8 chars, uppercase, number
              </p>
            </div>
          )}
          {errors.password && <p className="mt-1 text-sm text-red-500">{errors.password.message}</p>}
        </div>

        <div>
          <label htmlFor="confirmPassword" className="mb-2 block text-sm text-gray-700">
            Confirm Password
          </label>
          <div className="relative">
            <Lock className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
            <input
              id="confirmPassword"
              type={showConfirmPassword ? 'text' : 'password'}
              placeholder="Confirm your password"
              className={`w-full rounded-lg border ${
                errors.confirmPassword ? 'border-red-500' : 'border-gray-300'
              } bg-white py-3 pl-11 pr-11 focus:border-[#2563EB] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 transition-all`}
              {...register('confirmPassword', {
                required: 'Please confirm your password',
                validate: (value) => value === watchedPassword || "Passwords don't match",
              })}
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
            </button>
            {watchedConfirmPassword && watchedConfirmPassword === watchedPassword && (
              <CheckCircle2 className="absolute right-10 top-1/2 -translate-y-1/2 h-5 w-5 text-green-500" />
            )}
          </div>
          {errors.confirmPassword && (
            <p className="mt-1 text-sm text-red-500">{errors.confirmPassword.message}</p>
          )}
        </div>

        <div>
          <label className="flex items-start">
            <input
              type="checkbox"
              className="mt-1 h-4 w-4 rounded border-gray-300 text-[#2563EB] focus:ring-[#2563EB]"
              {...register('acceptTerms', {
                required: 'You must accept the terms and conditions',
              })}
            />
            <span className="ml-2 text-sm text-gray-600">
              I agree to the{' '}
              <Link to="/terms" className="text-[#2563EB] hover:text-[#1d4ed8]">
                Terms of Service
              </Link>{' '}
              and{' '}
              <Link to="/privacy" className="text-[#2563EB] hover:text-[#1d4ed8]">
                Privacy Policy
              </Link>
            </span>
          </label>
          {errors.acceptTerms && (
            <p className="mt-1 text-sm text-red-500">{errors.acceptTerms.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full rounded-lg bg-[#2563EB] py-3 text-white hover:bg-[#1d4ed8] disabled:bg-gray-400 transition-all duration-200 flex items-center justify-center gap-2"
        >
          {isLoading ? (
            <>
              <Loader2 className="h-5 w-5 animate-spin" />
              <span>Creating account...</span>
            </>
          ) : (
            'Create Account'
          )}
        </button>
      </form>

      <div className="my-6 flex items-center">
        <div className="flex-1 border-t border-gray-300"></div>
        <span className="px-4 text-sm text-gray-500">OR</span>
        <div className="flex-1 border-t border-gray-300"></div>
      </div>

      <button
        type="button"
        className="w-full rounded-lg border border-gray-300 bg-white py-3 text-gray-700 hover:bg-gray-50 transition-all duration-200 flex items-center justify-center gap-3"
      >
        <svg className="h-5 w-5" viewBox="0 0 24 24">
          <path
            fill="#4285F4"
            d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
          />
          <path
            fill="#34A853"
            d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
          />
          <path
            fill="#FBBC05"
            d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
          />
          <path
            fill="#EA4335"
            d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
          />
        </svg>
        Sign up with Google
      </button>

      <p className="mt-6 text-center text-sm text-gray-600">
        Already have an account?{' '}
        <Link to="/login" className="text-[#2563EB] hover:text-[#1d4ed8]">
          Sign in
        </Link>
      </p>
    </div>
  );
}



