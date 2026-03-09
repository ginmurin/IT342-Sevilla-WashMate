import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router';
import { Mail, CheckCircle2, Loader2 } from 'lucide-react';

export default function VerifyEmail() {
  const location = useLocation();
  const email = location.state?.email || 'your email';
  const [cooldown, setCooldown] = useState(0);
  const [isResending, setIsResending] = useState(false);

  useEffect(() => {
    if (cooldown > 0) {
      const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [cooldown]);

  const handleResendEmail = async () => {
    setIsResending(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    setIsResending(false);
    setCooldown(60);
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-[#F3F4F6]">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8 lg:p-10 text-center">
        <div className="mb-6 flex justify-center">
          <div className="relative">
            <div className="w-20 h-20 bg-gradient-to-br from-[#2563EB] to-[#14B8A6] rounded-full flex items-center justify-center">
              <Mail className="h-10 w-10 text-white" />
            </div>
            <div className="absolute -bottom-1 -right-1 bg-green-500 rounded-full p-1">
              <CheckCircle2 className="h-6 w-6 text-white" />
            </div>
          </div>
        </div>

        <h1 className="mb-3">Verify Your Email</h1>
        <p className="text-gray-600 mb-6">
          We've sent a verification link to
        </p>
        <p className="text-[#2563EB] mb-6">{email}</p>
        
        <p className="text-sm text-gray-600 mb-8">
          Please check your inbox and click the verification link to activate your account.
        </p>

        <div className="space-y-3">
          <button
            onClick={handleResendEmail}
            disabled={cooldown > 0 || isResending}
            className="w-full rounded-lg border-2 border-[#2563EB] bg-white py-3 text-[#2563EB] hover:bg-[#2563EB] hover:text-white disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 flex items-center justify-center gap-2"
          >
            {isResending ? (
              <>
                <Loader2 className="h-5 w-5 animate-spin" />
                <span>Sending...</span>
              </>
            ) : cooldown > 0 ? (
              <span>Resend Email ({cooldown}s)</span>
            ) : (
              <span>Resend Verification Email</span>
            )}
          </button>

          <Link
            to="/login"
            className="block w-full rounded-lg bg-[#2563EB] py-3 text-white hover:bg-[#1d4ed8] transition-all duration-200"
          >
            Back to Login
          </Link>
        </div>

        <p className="mt-6 text-sm text-gray-500">
          Wrong email?{' '}
          <Link to="/register" className="text-[#2563EB] hover:text-[#1d4ed8]">
            Change email address
          </Link>
        </p>
      </div>
    </div>
  );
}
