import { createContext, useContext, useState, ReactNode, useEffect, useRef, useCallback } from "react";
import { authAPI } from "@/features/shared/utils/api";
import type { User as ApiUser } from "@/features/shared/types";

export type Role = "CUSTOMER" | "SHOP_OWNER" | "ADMIN";

export interface User extends ApiUser {
  id: number;
  email: string;
  username: string | null;
  firstName: string;
  lastName: string;
  role: Role;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (user: User) => void;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
  refreshAccessToken: () => Promise<string | null>;
  verifyEmail: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<User | null>(() => {
    try {
      // Use sessionStorage so session clears when browser closes
      const stored = sessionStorage.getItem("washmate_user");
      if (!stored) return null;
      const parsed: User = JSON.parse(stored);
      parsed.role = String(parsed.role).toUpperCase() as Role;
      return parsed;
    } catch {
      return null;
    }
  });

  const [isLoading, setIsLoading] = useState(false);
  const inactivityTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const warningTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const refreshTokenTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const INACTIVITY_LIMIT = 30 * 60 * 1000; // 30 minutes
  const WARNING_TIME = 28 * 60 * 1000; // 28 minutes
  const TOKEN_REFRESH_INTERVAL = 14 * 60 * 1000; // Refresh every 14 minutes (access token is 15)

  const setUser = useCallback((userData: User | null) => {
    setUserState(userData);
    if (userData) {
      sessionStorage.setItem("washmate_user", JSON.stringify(userData));
    } else {
      sessionStorage.removeItem("washmate_user");
    }
  }, []);

  const normalizeUser = (userData: Partial<User>): User => ({
    id: userData.id || 0,
    email: userData.email || "",
    username: userData.username || null,
    firstName: userData.firstName || "",
    lastName: userData.lastName || "",
    role: (String(userData.role || "CUSTOMER").toUpperCase() as Role),
    phone: userData.phone || userData.phoneNumber,
    phoneNumber: userData.phoneNumber || userData.phone,
    emailVerified: userData.emailVerified,
    twoFactorEnabled: userData.twoFactorEnabled,
  });

  const login = (userData: User) => {
    const normalized = normalizeUser(userData);
    setUser(normalized);
  };

  const refreshAccessToken = useCallback(async (): Promise<string | null> => {
    try {
      const refreshToken = localStorage.getItem("washmate_refresh_token");
      if (!refreshToken) {
        console.warn("No refresh token available");
        return null;
      }

      const newAccessToken = await authAPI.refreshToken(refreshToken);
      localStorage.setItem("washmate_access_token", newAccessToken);
      return newAccessToken;
    } catch (error) {
      console.error("Failed to refresh token:", error);
      // If refresh fails, logout user
      logout();
      return null;
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      const refreshToken = localStorage.getItem("washmate_refresh_token");
      if (refreshToken) {
        await authAPI.logout(refreshToken);
      }
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      // Clear all auth data regardless of logout API result
      setUserState(null);
      sessionStorage.removeItem("washmate_user");
      localStorage.removeItem("washmate_access_token");
      localStorage.removeItem("washmate_refresh_token");

      // Clear timeouts
      if (inactivityTimeoutRef.current) {
        clearTimeout(inactivityTimeoutRef.current);
      }
      if (warningTimeoutRef.current) {
        clearTimeout(warningTimeoutRef.current);
      }
      if (refreshTokenTimeoutRef.current) {
        clearTimeout(refreshTokenTimeoutRef.current);
      }
    }
  }, []);

  const verifyEmail = useCallback(() => {
    if (!user) return;
    const updated = { ...user };
    setUser(updated);
  }, [user]);

  // Clear old localStorage data on mount (cleanup)
  useEffect(() => {
    localStorage.removeItem("washmate_user");
  }, []);

  // Auto logout and token refresh functionality
  useEffect(() => {
    if (!user) {
      // Clear timeouts when user logs out
      if (inactivityTimeoutRef.current) {
        clearTimeout(inactivityTimeoutRef.current);
      }
      if (warningTimeoutRef.current) {
        clearTimeout(warningTimeoutRef.current);
      }
      if (refreshTokenTimeoutRef.current) {
        clearTimeout(refreshTokenTimeoutRef.current);
      }
      return;
    }

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

    // Set up token refresh interval (refresh token every 14 minutes)
    refreshTokenTimeoutRef.current = setInterval(() => {
      refreshAccessToken();
    }, TOKEN_REFRESH_INTERVAL);

    // Cleanup
    return () => {
      if (inactivityTimeoutRef.current) {
        clearTimeout(inactivityTimeoutRef.current);
      }
      if (warningTimeoutRef.current) {
        clearTimeout(warningTimeoutRef.current);
      }
      if (refreshTokenTimeoutRef.current) {
        clearInterval(refreshTokenTimeoutRef.current);
      }
      events.forEach((event) => {
        window.removeEventListener(event, resetInactivityTimer);
      });
    };
  }, [user, logout, refreshAccessToken]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        setUser,
        refreshAccessToken,
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



