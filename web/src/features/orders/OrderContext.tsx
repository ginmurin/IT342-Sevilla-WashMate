import { createContext, useContext, useState, ReactNode } from "react";
import { useNavigate } from "react-router";
import { orderAPI, type CreateOrderRequest, type OrderService } from "@/features/shared/services/order";
import { type ServiceResponse } from "@/features/shared/services/service";

export type ServiceType = "wash" | "fold" | "iron" | "dry_clean";

export interface OrderData {
  // Step 1: Laundry Details
  selectedServices: Map<string, number>;  // serviceName -> quantity
  weight?: number;
  serviceQuantities?: Record<string, number>;
  selectedVariants?: Record<number, number>;  // serviceId -> variantId
  specialInstructions?: string;
  estimatedPrice?: number;
  subtotal?: number;
  deliveryFee?: number;  // Delivery fee (may be 0, 50, etc)
  discountPercentage?: number;
  discountAmount?: number;
  isRushOrder?: boolean;
  // Cache of available services for submitOrder to use
  availableServices?: ServiceResponse[];

  // Step 2: Schedule & Address
  pickupDate?: string;
  pickupTime?: string;
  deliveryDate?: string;
  deliveryTime?: string;
  address?: string;
  lat?: number;
  lng?: number;
  phoneNumber?: string;
  notes?: string;
  pickupAddressId?: number;
  deliveryAddressId?: number;

  // Step 3: Payment & Review
  paymentMethod?: "gcash" | "maya" | "card" | "grab_pay" | "wallet";
  totalAmount?: number;

  // Order ID from backend after submission
  orderId?: number;
}

interface OrderContextType {
  orderData: OrderData;
  currentStep: 1 | 2 | 3;
  setOrderData: (data: Partial<OrderData>) => void;
  setCurrentStep: (step: 1 | 2 | 3) => void;
  nextStep: () => void;
  prevStep: () => void;
  resetOrder: () => void;
  submitOrder: (navigateToPaymentReview?: boolean) => Promise<any>;
  isSubmitting: boolean;
  submitError: string | null;
}

const OrderContext = createContext<OrderContextType | undefined>(undefined);

const initialOrder: OrderData = {
  selectedServices: new Map(),
  weight: 5,
  serviceQuantities: {},
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
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const navigate = useNavigate();

  const setOrderData = (data: Partial<OrderData>) => {
    setOrderDataState((prev) => {
      // Special handling for selectedServices Map to maintain reference
      if (data.selectedServices) {
        return { ...prev, ...data };
      }
      return { ...prev, ...data };
    });
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

  const submitOrder = async (navigateToPaymentReview = true): Promise<any> => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      // Validate at least one service is selected
      if (orderData.selectedServices.size === 0) {
        throw new Error("Please select at least one service");
      }

      // Helper function to convert time slot string to 24-hour format
      // Input: "10:00 AM – 12:00 PM" or "02:00 PM – 04:00 PM"
      // Output: "10:00" or "14:00" (ISO 8601 time)
      const parseTimeSlot = (timeSlot: string): string => {
        if (!timeSlot) return "09:00"; // Default time

        // Extract the start time (first time in the range)
        const startTime = timeSlot.split(" – ")[0];
        const match = startTime.match(/(\d{1,2}):(\d{2})\s*(AM|PM)/i);

        if (!match) return "09:00";

        let hours = parseInt(match[1], 10);
        const minutes = match[2];
        const period = match[3].toUpperCase();

        // Convert to 24-hour format
        if (period === "PM" && hours !== 12) {
          hours += 12;
        } else if (period === "AM" && hours === 12) {
          hours = 0;
        }

        return `${String(hours).padStart(2, "0")}:${minutes}`;
      };

      // Create service name to ID mapping from the cached services
      const serviceNameToId: Record<string, number> = {};
      const serviceIdToService: Record<number, any> = {};
      if (orderData.availableServices) {
        orderData.availableServices.forEach(svc => {
          serviceNameToId[svc.serviceName] = svc.serviceId;
          serviceIdToService[svc.serviceId] = svc;
        });
      }

      // Convert selectedServices Map<serviceName, quantity> to backend format
      const services: OrderService[] = Array.from(orderData.selectedServices.entries()).map(
        ([serviceName, quantity]) => {
          const serviceId = serviceNameToId[serviceName] || 1;
          const service = serviceIdToService[serviceId];
          const serviceObj: OrderService = {
            serviceId,
            quantity
          };

          // Include selected variant if service has variants
          if (service?.hasVariants && orderData.selectedVariants?.[serviceId]) {
            serviceObj.selectedVariantId = orderData.selectedVariants[serviceId];
          }

          return serviceObj;
        }
      );

      // DEBUG: Log services being sent
      console.log(`[OrderContext] Submitting order with ${services.length} services:`, services);

      // Build the request with properly formatted dates/times
      const request: CreateOrderRequest = {
        services,
        deliveryFee: orderData.deliveryFee || 0,  // Include delivery fee (may be 0 or 50)
        totalWeight: orderData.weight,
        specialInstructions: orderData.specialInstructions,
        pickupSchedule: orderData.pickupDate && orderData.pickupTime
          ? `${orderData.pickupDate}T${parseTimeSlot(orderData.pickupTime)}:00`
          : undefined,
        deliverySchedule: orderData.deliveryDate && orderData.deliveryTime
          ? `${orderData.deliveryDate}T${parseTimeSlot(orderData.deliveryTime)}:00`
          : undefined,
        isRushOrder: orderData.isRushOrder || false,
        pickupAddressId: orderData.pickupAddressId,
        deliveryAddressId: orderData.deliveryAddressId,
        // Include address strings from frontend if no IDs provided
        pickupAddressString: orderData.address,
        pickupLatitude: orderData.lat,
        pickupLongitude: orderData.lng,
        deliveryAddressString: orderData.address,
        deliveryLatitude: orderData.lat,
        deliveryLongitude: orderData.lng
      };

      const response = await orderAPI.createOrder(request);
      console.log('✅ Order creation response:', response);

      const { orderId } = response;

      if (!orderId) {
        throw new Error('Failed to get orderId from order creation');
      }

      console.log('✅ Order submitted with ID:', orderId);

      // Store entire orderData in localStorage for persistence
      // Convert Map to Object for JSON serialization
      const updatedOrderData = {
        ...orderData,
        orderId,
        selectedServices: Object.fromEntries(orderData.selectedServices) // Convert Map to Object
      };
      localStorage.setItem('currentOrderData', JSON.stringify(updatedOrderData));
      localStorage.setItem('currentOrderId', String(orderId));

      // Store orderId in orderData for payment processing
      setOrderData({ orderId });

      // Navigate to payment review page with order details
      if (navigateToPaymentReview) {
        navigate(`/order/payment-review`);
      }

      // Return the response so PaymentReview can access orderId if needed
      return response;
    } catch (error) {
      console.error('❌ Order creation failed:', error);
      const errorMessage = error instanceof Error ? error.message : 'Order creation failed';
      setSubmitError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
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
        isSubmitting,
        submitError,
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





