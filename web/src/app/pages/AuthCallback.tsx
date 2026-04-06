import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { supabase } from "../../lib/supabase";
import { authAPI } from "../utils/api";
import { useAuth } from "../contexts/AuthContext";
import { Droplets, AlertCircle } from "lucide-react";
import { motion } from "motion/react";
import { Button } from "../components/Button";

export default function AuthCallback() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let unsubscribed = false;

    const syncAndRedirect = async (session: NonNullable<Awaited<ReturnType<typeof supabase.auth.getSession>>["data"]["session"]>) => {
      if (unsubscribed) return;
      try {
        const meta = session.user.user_metadata ?? {};

        // Google sends "full_name"; split it into first/last for the backend
        const fullName: string = meta.full_name || meta.name || "";
        const [firstName = "", ...rest] = fullName.trim().split(" ");
        const lastName = rest.join(" ");

        const syncResult = await authAPI.sync({
          email: session.user.email!,
          uuid: session.user.id,
          jwt: session.access_token,
          user_metadata: {
            ...meta,
            first_name: meta.first_name || firstName,
            last_name: meta.last_name || lastName,
          },
        });

        login(syncResult.user);

        const role = String(syncResult.user.role).toUpperCase();
        if (role === "CUSTOMER") navigate("/customer", { replace: true });
        else if (role === "ADMIN") navigate("/admin", { replace: true });
        else navigate("/", { replace: true });
      } catch (err) {
        if (!unsubscribed) {
          setError(err instanceof Error ? err.message : "Failed to complete sign-in.");
        }
      }
    };

    // Try to get an existing session first (handles implicit/PKCE exchange)
    supabase.auth.getSession().then(({ data: { session }, error: sessionError }) => {
      if (sessionError) {
        setError(sessionError.message);
        return;
      }
      if (session) {
        syncAndRedirect(session);
        return;
      }

      // Session not ready yet — listen for the SIGNED_IN event (PKCE flow)
      const { data: { subscription } } = supabase.auth.onAuthStateChange((event, session) => {
        if (event === "SIGNED_IN" && session) {
          subscription.unsubscribe();
          syncAndRedirect(session);
        } else if (event === "SIGNED_OUT") {
          setError("Authentication was cancelled. Please try again.");
        }
      });

      // Clean up listener if component unmounts before auth fires
      return () => {
        unsubscribed = true;
        subscription.unsubscribe();
      };
    });

    return () => { unsubscribed = true; };
  }, []);

  if (error) {
    return (
      <div className="flex-1 flex items-center justify-center min-h-screen px-4">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-sm text-center space-y-4"
        >
          <div className="w-14 h-14 bg-red-100 rounded-full flex items-center justify-center mx-auto">
            <AlertCircle className="w-7 h-7 text-red-500" />
          </div>
          <h2 className="text-xl font-semibold text-slate-800">Sign-in failed</h2>
          <p className="text-sm text-slate-500">{error}</p>
          <Button
            onClick={() => navigate("/login")}
            className="w-full bg-teal-600 hover:bg-teal-700 text-white"
          >
            Back to Login
          </Button>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex items-center justify-center min-h-screen">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="flex flex-col items-center gap-5"
      >
        {/* Logo */}
        <div className="w-12 h-12 rounded-xl bg-teal-100 flex items-center justify-center">
          <Droplets className="w-6 h-6 text-teal-600" />
        </div>

        {/* Spinner */}
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ repeat: Infinity, duration: 1, ease: "linear" }}
          className="w-8 h-8 border-3 border-teal-200 border-t-teal-600 rounded-full"
        />

        <p className="text-sm text-slate-500">Completing sign-in…</p>
      </motion.div>
    </div>
  );
}
