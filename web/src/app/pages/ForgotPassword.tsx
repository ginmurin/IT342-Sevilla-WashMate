import { useState } from 'react';
import { Link } from 'react-router';
import { Mail, ArrowLeft } from 'lucide-react';
import { SuccessMessage } from '../components/shared/SuccessMessage';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    setIsLoading(false);
    setIsSubmitted(true);
  };

  if (isSubmitted) {
    return (
      <div className="min-h-screen flex items-center justify-center p-6 bg-[#F3F4F6]">
        <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8 lg:p-10 text-center">
          <div className="mb-6 flex justify-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
              <Mail className="h-8 w-8 text-green-600" />
            </div>
          </div>
          
          <h1 className="mb-3">Check Your Email</h1>
          <p className="text-gray-600 mb-6">
            We've sent password reset instructions to <strong>{email}</strong>
          </p>
          
          <Link
            to="/login"
            className="block w-full rounded-lg bg-[#2563EB] py-3 text-white hover:bg-[#1d4ed8] transition-all duration-200"
          >
            Back to Login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-[#F3F4F6]">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8 lg:p-10">
        <Link
          to="/login"
          className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Login
        </Link>

        <h1 className="mb-2">Forgot Password?</h1>
        <p className="text-gray-600 mb-8">
          Enter your email address and we'll send you instructions to reset your password.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label htmlFor="email" className="mb-2 block text-sm text-gray-700">
              Email Address
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
              <input
                id="email"
                type="email"
                required
                placeholder="juan@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full rounded-lg border border-gray-300 bg-white py-3 pl-11 pr-4 focus:border-[#2563EB] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20 transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full rounded-lg bg-[#2563EB] py-3 text-white hover:bg-[#1d4ed8] disabled:bg-gray-400 transition-all duration-200"
          >
            {isLoading ? 'Sending...' : 'Send Reset Instructions'}
          </button>
        </form>
      </div>
    </div>
  );
}
