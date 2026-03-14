import { createContext, useContext, useState, ReactNode } from "react";

export type ServiceType = "wash" | "fold" | "iron" | "dry_clean";

export interface OrderData {
  // Step 1: Laundry Details
  selectedServices?: ServiceType[];
  weight?: number;
  specialInstructions?: string;
  estimatedPrice?: number;

  // Step 2: Schedule & Address
  pickupDate?: string;
  pickupTime?: string;
  deliveryDate?: string;
  deliveryTime?: string;
  address?: string;
  phoneNumber?: string;
  notes?: string;

  // Step 3: Payment & Review
  paymentMethod?: "gcash" | "maya" | "card" | "grab_pay";
  totalAmount?: number;
}

interface OrderContextType {
  orderData: OrderData;
  currentStep: 1 | 2 | 3;
  setOrderData: (data: Partial<OrderData>) => void;
  setCurrentStep: (step: 1 | 2 | 3) => void;
  nextStep: () => void;
  prevStep: () => void;
  resetOrder: () => void;
  submitOrder: () => Promise<void>;
}

const OrderContext = createContext<OrderContextType | undefined>(undefined);

const initialOrder: OrderData = {
  selectedServices: [],
  weight: 5,
  specialInstructions: "",
  estimatedPrice: 0,
  pickupDate: "",
  pickupTime: "",
  deliveryDate: "",
  deliveryTime: "",
  address: "",
  phoneNumber: "",
  notes: "",
  paymentMethod: "card",
  totalAmount: 0,
};

export function OrderProvider({ children }: { children: ReactNode }) {
  const [orderData, setOrderDataState] = useState<OrderData>(initialOrder);
  const [currentStep, setCurrentStep] = useState<1 | 2 | 3>(1);

  const setOrderData = (data: Partial<OrderData>) => {
    setOrderDataState((prev) => ({ ...prev, ...data }));
  };

  const nextStep = () => {
    if (currentStep < 3) setCurrentStep((currentStep + 1) as 1 | 2 | 3);
  };

  const prevStep = () => {
    if (currentStep > 1) setCurrentStep((currentStep - 1) as 1 | 2 | 3);
  };

  const resetOrder = () => {
    setOrderDataState(initialOrder);
    setCurrentStep(1);
  };

  const submitOrder = async () => {
    console.log("Submitting order:", orderData);
    await new Promise((resolve) => setTimeout(resolve, 1000));
  };

  return (
    <OrderContext.Provider
      value={{
        orderData,
        currentStep,
        setOrderData,
        setCurrentStep,
        nextStep,
        prevStep,
        resetOrder,
        submitOrder,
      }}
    >
      {children}
    </OrderContext.Provider>
  );
}

export function useOrder() {
  const context = useContext(OrderContext);
  if (context === undefined) {
    throw new Error("useOrder must be used within an OrderProvider");
  }
  return context;
}
