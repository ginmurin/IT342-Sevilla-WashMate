import { ReactNode } from "react";
import { OrderProvider } from "../contexts/OrderContext";
import { PaymentProvider } from "../contexts/PaymentContext";
import { WalletProvider } from "../contexts/WalletContext";
import { SubscriptionProvider } from "../contexts/SubscriptionContext";

interface RouterProvidersProps {
  children: ReactNode;
}

/**
 * Context providers that depend on router context (use useNavigate, etc.)
 * These must be rendered inside RouterProvider
 */
export function RouterProviders({ children }: RouterProvidersProps) {
  return (
    <OrderProvider>
      <PaymentProvider>
        <WalletProvider>
          <SubscriptionProvider>
            {children}
          </SubscriptionProvider>
        </WalletProvider>
      </PaymentProvider>
    </OrderProvider>
  );
}