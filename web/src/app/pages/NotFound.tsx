import { useNavigate } from "react-router";
import { motion } from "motion/react";
import { Home, ArrowRight, Search, AlertTriangle } from "lucide-react";
import { Button } from "../components/Button";

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-teal-50 flex items-center justify-center p-4 overflow-hidden">
      {/* Animated background blobs */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <motion.div
          className="absolute top-20 left-10 w-72 h-72 bg-blue-200 rounded-full mix-blend-multiply filter blur-3xl opacity-20"
          animate={{
            x: [0, 50, -50, 0],
            y: [0, 30, -30, 0],
          }}
          transition={{ duration: 8, repeat: Infinity, ease: "easeInOut" }}
        />
        <motion.div
          className="absolute bottom-20 right-10 w-72 h-72 bg-teal-200 rounded-full mix-blend-multiply filter blur-3xl opacity-20"
          animate={{
            x: [0, -50, 50, 0],
            y: [0, -30, 30, 0],
          }}
          transition={{ duration: 8, repeat: Infinity, ease: "easeInOut", delay: 1 }}
        />
      </div>

      {/* Main content */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="relative z-10 max-w-2xl w-full text-center"
      >
        {/* Animated 404 number */}
        <motion.div
          initial={{ opacity: 0, scale: 0.5 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, delay: 0.1 }}
          className="mb-8"
        >
          <div className="relative inline-block">
            {/* Large background 404 */}
            <motion.div
              className="text-9xl font-black bg-gradient-to-r from-blue-400 via-teal-400 to-emerald-400 bg-clip-text text-transparent opacity-20 absolute inset-0 flex items-center justify-center"
              animate={{ rotate: [0, 5, -5, 0] }}
              transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
            >
              404
            </motion.div>

            {/* Foreground 404 with animation */}
            <motion.div
              className="text-8xl font-black bg-gradient-to-r from-blue-600 via-teal-500 to-emerald-500 bg-clip-text text-transparent relative z-10"
              animate={{ y: [0, -10, 0] }}
              transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
            >
              404
            </motion.div>
          </div>
        </motion.div>

        {/* Icon */}
        <motion.div
          initial={{ opacity: 0, scale: 0 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="flex justify-center mb-8"
        >
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 8, repeat: Infinity, ease: "linear" }}
            className="bg-gradient-to-br from-blue-100 to-teal-100 p-6 rounded-full"
          >
            <Search className="w-12 h-12 text-teal-600" />
          </motion.div>
        </motion.div>

        {/* Heading */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="mb-4"
        >
          <h1 className="text-4xl md:text-5xl font-bold text-slate-900 mb-3">
            Page Not Found
          </h1>
          <p className="text-lg text-slate-600">
            Oops! We couldn't find the page you're looking for. It might have been moved or deleted.
          </p>
        </motion.div>

        {/* Decorative line */}
        <motion.div
          initial={{ scaleX: 0 }}
          animate={{ scaleX: 1 }}
          transition={{ duration: 0.8, delay: 0.4 }}
          className="h-1 bg-gradient-to-r from-transparent via-teal-500 to-transparent my-8 origin-center"
        />

        {/* Suggestions */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.5 }}
          className="mb-8 text-slate-600"
        >
          <p className="text-sm mb-4 flex items-center justify-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-500" />
            <span>Here are some helpful links:</span>
          </p>
          <div className="flex flex-wrap gap-2 justify-center text-sm">
            <button
              onClick={() => navigate("/orders")}
              className="px-3 py-1 bg-slate-100 hover:bg-slate-200 rounded-full transition-colors text-slate-700"
            >
              Orders
            </button>
            <button
              onClick={() => navigate("/subscriptions")}
              className="px-3 py-1 bg-slate-100 hover:bg-slate-200 rounded-full transition-colors text-slate-700"
            >
              Subscriptions
            </button>
            <button
              onClick={() => navigate("/wallet")}
              className="px-3 py-1 bg-slate-100 hover:bg-slate-200 rounded-full transition-colors text-slate-700"
            >
              Wallet
            </button>
            <button
              onClick={() => navigate("/profile")}
              className="px-3 py-1 bg-slate-100 hover:bg-slate-200 rounded-full transition-colors text-slate-700"
            >
              Profile
            </button>
          </div>
        </motion.div>

        {/* Action buttons */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.6 }}
          className="flex flex-col sm:flex-row gap-4 justify-center items-center"
        >
          <Button
            onClick={() => navigate("/")}
            className="bg-gradient-to-r from-teal-500 to-emerald-500 hover:from-teal-600 hover:to-emerald-600 text-white px-8 py-3 rounded-lg font-medium flex items-center gap-2 transition-all hover:shadow-lg"
          >
            <Home className="w-5 h-5" />
            Back to Home
          </Button>

          <Button
            onClick={() => navigate(-1)}
            variant="outline"
            className="border-slate-300 text-slate-700 px-8 py-3 rounded-lg font-medium hover:bg-slate-50 flex items-center gap-2 transition-colors"
          >
            <ArrowRight className="w-5 h-5 rotate-180" />
            Go Back
          </Button>
        </motion.div>

        {/* Footer message */}
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.6, delay: 0.7 }}
          className="mt-12 text-sm text-slate-500"
        >
          Error Code: 404 | Need help?{" "}
          <a href="mailto:support@washmate.com" className="text-teal-600 hover:underline">
            Contact support
          </a>
        </motion.p>
      </motion.div>

      {/* Floating particles effect */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        {[...Array(5)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute w-2 h-2 bg-teal-400 rounded-full opacity-20"
            initial={{
              x: Math.random() * window.innerWidth,
              y: Math.random() * window.innerHeight,
            }}
            animate={{
              y: [0, -100, 0],
              x: [0, Math.random() * 50 - 25, 0],
            }}
            transition={{
              duration: 4 + i,
              repeat: Infinity,
              ease: "easeInOut",
            }}
          />
        ))}
      </div>
    </div>
  );
}
