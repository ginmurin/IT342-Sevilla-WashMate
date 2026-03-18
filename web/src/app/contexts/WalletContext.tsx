import { createContext, useContext, useState, ReactNode } from "react";

export interface WalletTopUpData {
  amount: number;
  paymentMethod?: "gcash" | "maya" | "card" | "grab_pay";
}

interface WalletContextType {
  topUpData: WalletTopUpData;
  setTopUpData: (data: Partial<WalletTopUpData>) => void;
  submitTopUp: () => Promise<void>;
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
    console.log("Submitting wallet top-up:", topUpData);
    await new Promise((resolve) => setTimeout(resolve, 1000));
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
