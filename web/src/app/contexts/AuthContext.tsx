import { createContext, useContext, useState, ReactNode } from "react";
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
      const stored = localStorage.getItem("washmate_user");
      if (!stored) return null;
      const parsed: User = JSON.parse(stored);
      // Normalize role in case it was stored as uppercase from a previous session
      parsed.role = String(parsed.role).toUpperCase() as User["role"];
      return parsed;
    } catch {
      return null;
    }
  });

  const normalizeUser = (userData: User): User => ({
    ...userData,
    role: String(userData.role).toUpperCase() as User["role"],
  });

  const login = (userData: User) => {
    const normalized = normalizeUser(userData);
    setUser(normalized);
    localStorage.setItem("washmate_user", JSON.stringify(normalized));
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
