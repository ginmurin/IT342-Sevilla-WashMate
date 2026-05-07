import { ReactNode } from "react";
import { OrderProvider } from "@/features/orders/OrderContext";
import { PaymentProvider } from "@/features/shared/contexts/PaymentContext";
import { WalletProvider } from "@/features/wallet/WalletContext";
import { SubscriptionProvider } from "@/features/subscriptions/SubscriptionContext";
import { NotificationProvider } from "@/features/shared/contexts/NotificationContext";

interface RouterProvidersProps {
  children: ReactNode;
}

/**
 * Context providers that depend on router context (use useNavigate, etc.)
 * These must be rendered inside RouterProvider
 */
export function RouterProviders({ children }: RouterProvidersProps) {
  return (
    <NotificationProvider>
      <OrderProvider>
        <PaymentProvider>
          <WalletProvider>
            <SubscriptionProvider>
              {children}
            </SubscriptionProvider>
          </WalletProvider>
        </PaymentProvider>
      </OrderProvider>
    </NotificationProvider>
  );
}


