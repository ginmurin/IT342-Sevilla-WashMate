import { createContext, useContext, useState, ReactNode } from "react";

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
  initializePayment: (order: PaymentOrder) => void;
  confirmPayment: (transactionId: string, receipt: any) => void;
  addTransaction: (transaction: Transaction) => void;
  addToWallet: (amount: number) => void;
  clearPayment: () => void;
}

const PaymentContext = createContext<PaymentContextType | undefined>(undefined);

export function PaymentProvider({ children }: { children: ReactNode }) {
  const [currentOrder, setCurrentOrder] = useState<PaymentOrder | null>(null);
  const [walletBalance, setWalletBalance] = useState(0);
  const [transactions, setTransactions] = useState<Transaction[]>([]);

  const initializePayment = (order: PaymentOrder) => {
    setCurrentOrder(order);
  };

  const confirmPayment = (transactionId: string, receipt: any) => {
    // In a real app, this would verify with backend
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

  const clearPayment = () => {
    setCurrentOrder(null);
  };

  return (
    <PaymentContext.Provider
      value={{
        currentOrder,
        walletBalance,
        transactions,
        initializePayment,
        confirmPayment,
        addTransaction,
        addToWallet,
        clearPayment,
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
