import { RouterProvider } from "react-router";
import { AuthProvider } from "./contexts/AuthContext";
import { OrderProvider } from "./contexts/OrderContext";
import { PaymentProvider } from "./contexts/PaymentContext";
import { router } from "./routes";

export default function App() {
  return (
    <AuthProvider>
      <OrderProvider>
        <PaymentProvider>
          <RouterProvider router={router} />
        </PaymentProvider>
      </OrderProvider>
    </AuthProvider>
  );
}
