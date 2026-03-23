import { RouterProvider } from "react-router";
import { AuthProvider } from "./contexts/AuthContext";
import { OrderProvider } from "./contexts/OrderContext";
import { PaymentProvider } from "./contexts/PaymentContext";
import { WalletProvider } from "./contexts/WalletContext";
import { SubscriptionProvider } from "./contexts/SubscriptionContext";
import { router } from "./routes";

export default function App() {
  return (
    <AuthProvider>
      <OrderProvider>
        <PaymentProvider>
          <WalletProvider>
            <SubscriptionProvider>
              <RouterProvider router={router} />
            </SubscriptionProvider>
          </WalletProvider>
        </PaymentProvider>
      </OrderProvider>
    </AuthProvider>
  );
}
