import { createContext, useContext, useState, ReactNode } from "react";
import { walletAPI, type WalletPaymentDTO } from "./walletAPI";

export interface WalletTopUpData {
  amount: number;
  paymentMethod?: "gcash" | "maya" | "card" | "grab_pay";
}

interface WalletContextType {
  topUpData: WalletTopUpData;
  setTopUpData: (data: Partial<WalletTopUpData>) => void;
  submitTopUp: () => Promise<WalletPaymentDTO>;
}

const WalletContext = createContext<WalletContextType | undefined>(undefined);

const initialTopUp: WalletTopUpData = {
  amount: 0,
  paymentMethod: "card",
};

export function WalletProvider({ children }: { children: ReactNode }) {
  const [topUpData, setTopUpDataState] = useState<WalletTopUpData>(initialTopUp);

  const setTopUpData = (data: Partial<WalletTopUpData>) => {
    setTopUpDataState((prev) => ({ ...prev, ...data }));
  };

  const submitTopUp = async () => {
    if (!topUpData.amount || topUpData.amount <= 0) {
      throw new Error("Invalid top-up amount");
    }

    if (!topUpData.paymentMethod) {
      throw new Error("Payment method not selected");
    }

    try {
      // Call API to initiate wallet top-up and create payment record
      const response = await walletAPI.initiateTopup(
        topUpData.amount,
        topUpData.paymentMethod
      );

      console.log("Wallet top-up initiated:", response.data);
      return response.data;
    } catch (error) {
      console.error("Failed to initiate wallet top-up:", error);
      throw error;
    }
  };

  return (
    <WalletContext.Provider
      value={{
        topUpData,
        setTopUpData,
        submitTopUp,
      }}
    >
      {children}
    </WalletContext.Provider>
  );
}

export function useWallet() {
  const context = useContext(WalletContext);
  if (context === undefined) {
    throw new Error("useWallet must be used within a WalletProvider");
  }
  return context;
}




