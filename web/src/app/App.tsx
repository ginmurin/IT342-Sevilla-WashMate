import { RouterProvider } from "react-router";
import { AuthProvider } from "./contexts/AuthContext";
import { OrderProvider } from "./contexts/OrderContext";
import { PaymentProvider } from "./contexts/PaymentContext";
import { WalletProvider } from "./contexts/WalletContext";
import { router } from "./routes";

export default function App() {
  return (
    <AuthProvider>
      <OrderProvider>
        <PaymentProvider>
          <WalletProvider>
            <RouterProvider router={router} />
          </WalletProvider>
        </PaymentProvider>
      </OrderProvider>
    </AuthProvider>
  );
}
