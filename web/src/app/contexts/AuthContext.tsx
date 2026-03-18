import { createContext, useContext, useState, ReactNode } from "react";
import { authAPI } from "../utils/api";
import { supabase } from "../../lib/supabase";
import type { User as ApiUser } from "../types";

export type Role = "customer" | "shop_owner" | "admin";

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
    // Try to restore from localStorage if available
    try {
      const stored = localStorage.getItem("washmate_user");
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const login = (userData: User) => {
    setUser(userData);
    localStorage.setItem("washmate_user", JSON.stringify(userData));
  };

  const logout = async () => {
    setUser(null);
    localStorage.removeItem("washmate_user");
    await supabase.auth.signOut();
    await authAPI.logout();
  };

  const verifyEmail = () => {
    if (!user) return;
    const updated = { ...user, emailVerified: true };
    setUser(updated);
    localStorage.setItem("washmate_user", JSON.stringify(updated));
  };

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
