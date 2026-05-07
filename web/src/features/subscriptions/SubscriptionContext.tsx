import React, { createContext, useCallback, useContext, useState } from 'react';
import * as subscriptionService from "@/features/shared/services/subscription";

export interface UpgradeData {
  currentPlan?: string;
  newPlan: string;
  amount: number;
  userSubscriptionId?: string;
  paymentMethod?: 'gcash' | 'maya' | 'card' | 'grab_pay';
}

interface SubscriptionContextType {
  upgradeData: UpgradeData;
  setUpgradeData: (partial: Partial<UpgradeData>) => void;
  submitUpgrade: () => Promise<{ userSubscriptionId: string }>;
  loading: boolean;
  error: string | null;
}

const SubscriptionContext = createContext<SubscriptionContextType | undefined>(undefined);

export const SubscriptionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [upgradeData, setUpgradeDataState] = useState<UpgradeData>({
    newPlan: '',
    amount: 0,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const setUpgradeData = useCallback((partial: Partial<UpgradeData>) => {
    setUpgradeDataState((prev) => ({ ...prev, ...partial }));
    setError(null);
  }, []);

  const submitUpgrade = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await subscriptionService.initiateUpgrade(upgradeData.newPlan);
      setUpgradeData({
        userSubscriptionId: response.userSubscriptionId,
        amount: response.amount,
      });
      return { userSubscriptionId: response.userSubscriptionId };
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to initiate upgrade';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [upgradeData.newPlan, setUpgradeData]);

  const value: SubscriptionContextType = {
    upgradeData,
    setUpgradeData,
    submitUpgrade,
    loading,
    error,
  };

  return (
    <SubscriptionContext.Provider value={value}>
      {children}
    </SubscriptionContext.Provider>
  );
};

export const useSubscription = () => {
  const context = useContext(SubscriptionContext);
  if (!context) {
    throw new Error('useSubscription must be used within SubscriptionProvider');
  }
  return context;
};




