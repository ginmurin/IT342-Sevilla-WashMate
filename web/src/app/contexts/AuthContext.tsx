import { createContext, useContext, useState, ReactNode, useEffect, useRef, useCallback } from "react";
import { authAPI } from "../utils/api";
import { supabase } from "../../lib/supabase";
import type { User as ApiUser } from "../types";

export type Role = "CUSTOMER" | "SHOPOWNER" | "ADMIN";

export type User = ApiUser;

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (userData: User) => void;
  logout: () => Promise<void>;
  verifyEmail: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    try {
      // Use sessionStorage instead of localStorage so session clears when browser closes
      const stored = sessionStorage.getItem("washmate_user");
      if (!stored) return null;
      const parsed: User = JSON.parse(stored);
      // Normalize role in case it was stored as uppercase from a previous session
      parsed.role = String(parsed.role).toUpperCase() as User["role"];
      return parsed;
    } catch {
      return null;
    }
  });

  const inactivityTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const warningTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const INACTIVITY_LIMIT = 30 * 60 * 1000; // 30 minutes in milliseconds
  const WARNING_TIME = 28 * 60 * 1000; // Show warning at 28 minutes (2 min before logout)

  const normalizeUser = (userData: User): User => ({
    ...userData,
    role: String(userData.role).toUpperCase() as User["role"],
  });

  const login = (userData: User) => {
    const normalized = normalizeUser(userData);
    setUser(normalized);
    // Use sessionStorage instead of localStorage
    sessionStorage.setItem("washmate_user", JSON.stringify(normalized));
  };

  const logout = useCallback(async () => {
    setUser(null);
    // Clear both sessionStorage and localStorage for safety
    sessionStorage.removeItem("washmate_user");
    localStorage.removeItem("washmate_user");
    await supabase.auth.signOut();
    await authAPI.logout();
  }, []);

  const verifyEmail = () => {
    if (!user) return;
    const updated = { ...user, emailVerified: true };
    setUser(updated);
    sessionStorage.setItem("washmate_user", JSON.stringify(updated));
  };

  // Clear old localStorage data on mount (cleanup from previous implementation)
  useEffect(() => {
    localStorage.removeItem("washmate_user");
  }, []);

  // Auto logout functionality
  useEffect(() => {
    if (!user) return; // Only set up auto-logout if user is logged in

    const resetInactivityTimer = () => {
      // Clear existing timeouts
      if (inactivityTimeoutRef.current) {
        clearTimeout(inactivityTimeoutRef.current);
      }
      if (warningTimeoutRef.current) {
        clearTimeout(warningTimeoutRef.current);
      }

      // Set warning timeout (2 minutes before logout)
      warningTimeoutRef.current = setTimeout(() => {
        console.log("Auto-logout warning: You will be logged out in 2 minutes due to inactivity");
        // You could show a toast notification here instead of console.log
        alert("You will be logged out in 2 minutes due to inactivity. Move your mouse to stay logged in.");
      }, WARNING_TIME);

      // Set logout timeout
      inactivityTimeoutRef.current = setTimeout(() => {
        console.log("Auto-logout: User inactive for 30 minutes");
        logout();
      }, INACTIVITY_LIMIT);
    };

    // Events that indicate user activity
    const events = ["mousedown", "mousemove", "keypress", "scroll", "touchstart", "click"];

    // Reset timer on any user activity
    events.forEach((event) => {
      window.addEventListener(event, resetInactivityTimer);
    });

    // Initialize the timer
    resetInactivityTimer();

    // Cleanup
    return () => {
      if (inactivityTimeoutRef.current) {
        clearTimeout(inactivityTimeoutRef.current);
      }
      if (warningTimeoutRef.current) {
        clearTimeout(warningTimeoutRef.current);
      }
      events.forEach((event) => {
        window.removeEventListener(event, resetInactivityTimer);
      });
    };
  }, [user, logout]); // Re-run when user or logout changes

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        login,
        logout,
        verifyEmail,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
