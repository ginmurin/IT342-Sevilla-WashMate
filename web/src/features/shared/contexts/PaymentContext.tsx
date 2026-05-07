import { createContext, useContext, useState, ReactNode, useEffect } from "react";
import { walletAPI, WalletTransaction } from "@/features/wallet/walletAPI";
import { useAuth } from "@/features/auth/AuthContext";

export interface PaymentOrder {
  id?: string;
  serviceType: "wash_fold" | "wash_iron" | "dry_clean";
  weight?: number;
  amount: number;
  description?: string;
}

export interface Transaction {
  id: string;
  orderId?: string;
  amount: number;
  status: "pending" | "completed" | "failed";
  serviceType: string;
  date: string;
  paymentMethod?: string;
  referenceNumber?: string;
}

interface PaymentContextType {
  currentOrder: PaymentOrder | null;
  walletBalance: number;
  transactions: Transaction[];
  isLoading: boolean;
  error: string | null;
  initializePayment: (order: PaymentOrder) => void;
  confirmPayment: (transactionId: string, receipt: any) => void;
  addTransaction: (transaction: Transaction) => void;
  addToWallet: (amount: number) => void;
  deductFromWallet: (amount: number) => void;
  clearPayment: () => void;
  refetchWalletData: () => Promise<void>;
}

const PaymentContext = createContext<PaymentContextType | undefined>(undefined);

export function PaymentProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth(); // Only fetch data if user is authenticated
  const [currentOrder, setCurrentOrder] = useState<PaymentOrder | null>(null);
  const [walletBalance, setWalletBalance] = useState(0);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch wallet data from backend
  const fetchWalletData = async () => {
    try {
      setIsLoading(true);
      setError(null);

      // Fetch balance
      const balanceResponse = await walletAPI.getBalance();
      setWalletBalance(balanceResponse.data.availableBalance || 0);

      // Fetch transactions
      const transactionsResponse = await walletAPI.getTransactions();
      const mappedTransactions = transactionsResponse.data.map(
        (txn: WalletTransaction) => ({
          id: `TXN-${txn.transactionId}`,
          orderId: txn.referenceType === "ORDER" ? String(txn.referenceId) : undefined,
          amount: txn.amount,
          status: txn.status.toLowerCase() as "pending" | "completed" | "failed",
          serviceType: txn.referenceType,
          date: new Date(txn.createdAt).toISOString().split("T")[0],
          paymentMethod: txn.referenceType,
          referenceNumber: String(txn.transactionId),
        })
      );
      setTransactions(mappedTransactions);
    } catch (err: any) {
      // Only log error if not a 401 (unauthenticated is expected on public pages)
      if (err.response?.status !== 401) {
        console.error("Failed to fetch wallet data:", err);
        setError(err.message || "Failed to load wallet data");
      }
      // For 401, silently fail - user is not authenticated
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch wallet data on component mount (only if authenticated)
  useEffect(() => {
    if (isAuthenticated) {
      fetchWalletData();
    } else {
      setIsLoading(false); // Not authenticated, so no data to load
    }
  }, [isAuthenticated]);

  const initializePayment = (order: PaymentOrder) => {
    setCurrentOrder(order);
  };

  const confirmPayment = (transactionId: string, receipt: any) => {
    const updatedTransactions = transactions.map((t) =>
      t.id === transactionId ? { ...t, status: "completed" as const } : t
    );
    setTransactions(updatedTransactions);
  };

  const addTransaction = (transaction: Transaction) => {
    setTransactions((prev) => [transaction, ...prev]);
  };

  const addToWallet = (amount: number) => {
    setWalletBalance((prev) => prev + amount);
  };

  const deductFromWallet = (amount: number) => {
    setWalletBalance((prev) => Math.max(0, prev - amount));
  };

  const clearPayment = () => {
    setCurrentOrder(null);
  };

  return (
    <PaymentContext.Provider
      value={{
        currentOrder,
        walletBalance,
        transactions,
        isLoading,
        error,
        initializePayment,
        confirmPayment,
        addTransaction,
        addToWallet,
        deductFromWallet,
        clearPayment,
        refetchWalletData: fetchWalletData,
      }}
    >
      {children}
    </PaymentContext.Provider>
  );
}

export function usePayment() {
  const context = useContext(PaymentContext);
  if (context === undefined) {
    throw new Error("usePayment must be used within a PaymentProvider");
  }
  return context;
}




