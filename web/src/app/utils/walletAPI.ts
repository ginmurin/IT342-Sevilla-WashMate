import api from './api';

export interface WalletBalance {
  walletId: number;
  userId: number;
  availableBalance: number;
  currency: string;
  updatedAt: string;
}

export interface WalletTransaction {
  transactionId: number;
  amount: number;
  transactionType: 'CREDIT' | 'DEBIT';
  referenceType: 'ORDER' | 'SUBSCRIPTION' | 'WALLET_TOPUP' | 'PROMOTION';
  referenceId: number;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  description: string;
  balanceBefore: number;
  balanceAfter: number;
  createdAt: string;
  updatedAt: string;
}

export interface WalletPaymentDTO {
  paymentId: number;
  referenceType: string;
  referenceId: number;
  amount: number;
  paymentMethod: string;
  paymentStatus: string;
  createdAt: string;
}

export const walletAPI = {
  // Get wallet balance
  getBalance: () => api.get<WalletBalance>('/api/wallet/balance'),

  // Get all transactions
  getTransactions: () => api.get<WalletTransaction[]>('/api/wallet/transactions'),

  // Get pending transactions
  getPendingTransactions: () => api.get<WalletTransaction[]>('/api/wallet/transactions/pending'),

  // Get transactions by reference type
  getTransactionsByType: (type: string) =>
    api.get<WalletTransaction[]>(`/api/wallet/transactions/type/${type}`),

  // Check if wallet has sufficient balance
  checkBalance: (amount: number) =>
    api.get(`/api/wallet/balance/check?amount=${amount}`),

  // Initiate wallet top-up
  initiateTopup: (amount: number, paymentMethod: string) =>
    api.post<WalletPaymentDTO>('/api/wallet/topup/initiate', { amount, paymentMethod }),

  // Process wallet top-up payment (new endpoint - backend handles PayMongo)
  processTopup: (amount: number, paymentMethod: string) =>
    api.post('/api/wallet/topup/process', { amount, paymentMethod }),

  // Confirm wallet top-up
  confirmTopup: (paymentId: number, amount?: number, paymongoPaymentIntentId?: string) =>
    api.post<WalletBalance>(`/api/wallet/topup/confirm/${paymentId}`, { amount, paymongoPaymentIntentId }),

  // Admin: Add credit to wallet
  creditWallet: (amount: number) =>
    api.post<WalletBalance>('/api/wallet/credit', { amount }),

  // Admin: Debit wallet
  debitWallet: (amount: number, referenceType?: string, referenceId?: number) =>
    api.post<WalletBalance>('/api/wallet/debit', { amount, referenceType, referenceId }),
};
