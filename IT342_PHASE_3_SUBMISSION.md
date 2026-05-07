# IT342 Phase 3 – Third-Party Authentication (Google OAuth2) Submission

## 1. GitHub Link
**Repository:** https://github.com/ginmurin/IT342-Sevilla-WashMate

**Main Branch:** All code merged and pushed

**Key Commits:**
- `19e5038 - IT342 Phase 3 – Web Main Feature Completed`
- Multiple commits showing OAuth implementation progression

---

## 2. Main Feature: Google OAuth2 Authentication

The main feature implemented is **Google OAuth2 Authentication** that allows users to register and log in using their Google accounts alongside traditional email/password authentication. The system seamlessly links Google accounts with existing user profiles and automatically initializes subscriptions for new OAuth users.

---

## 3. Screenshots

### Main Feature Page - Order Payment Review
![Order Payment Review](./docs/screenshots/order-payment-review.png)
- Step 3 of order workflow showing payment method selection
- Payment methods: Card, GCash, Maya, GrabPay, Wallet
- Order summary with services, weight, schedule, and delivery address
- Cost summary sidebar with pricing breakdown

### Payment Method Selection (Cards Display)
![Payment Methods](./docs/screenshots/payment-methods.png)
- Grid layout showing all 5 payment options
- Each method displays icon, name, and description
- Visual feedback when method is selected

### PayMongo Payment Checkout
![PayMongo Checkout](./docs/screenshots/paymongo-checkout.png)
- GCash payment test page
- Shows amount, authorize button, and expire/fail test options
- Demonstrates successful transaction flow

### Payment Success Page
![Payment Success](./docs/screenshots/payment-success.png)
- Confirmation page after successful payment
- Shows order ID, amount, payment status
- Displays transaction details and next steps

### Subscription Upgrade Payment
![Subscription Upgrade Review](./docs/screenshots/subscription-upgrade-review.png)
- Plan comparison showing current vs. new plan
- Payment method selection
- Premium plan with crown icon indicator

### Database Records - Payments Table
![Payments Table](./docs/screenshots/db-payments-table.png)
- Shows Payment records with polymorphic pattern
- Columns: payment_id, reference_type, reference_id, amount, payment_method, payment_status, paymongo_payment_intent_id
- Records for ORDER, SUBSCRIPTION, and WALLET_TOPUP types
- payment_date and payment_intent_id populated on confirmation

### Database Records - Orders Table
![Orders Table](./docs/screenshots/db-orders-table.png)
- Shows Order records including order_id, customer details, services, amounts
- order_status tracks lifecycle (PENDING → CONFIRMED → COMPLETED)
- Links to Payment table via polymorphic relationship

### Database Records - User Subscriptions Table
![User Subscriptions Table](./docs/screenshots/db-subscriptions-table.png)
- Shows UserSubscription records with plan type, pricing, expiry dates
- Links to Payment records through polymorphic reference_id

---

## 4. Feature Description

### Overview
The Payment System provides a unified payment flow for all transaction types in WashMate:
- **Orders:** Users can pay for laundry services
- **Subscriptions:** Users can upgrade subscription plans
- **Wallet Top-ups:** Users can add balance to wallet for future purchases

### Key Features

#### 4.1 Polymorphic Payment Pattern
Instead of direct foreign keys, the system uses:
- **reference_type:** Enum (ORDER, SUBSCRIPTION, WALLET_TOPUP)
- **reference_id:** ID of the referenced entity
- **Benefits:** Flexibility to support new transaction types without schema changes

#### 4.2 Multiple Payment Methods
- **Card:** Visa, Mastercard via PayMongo
- **GCash:** Mobile wallet via PayMongo source
- **Maya:** Mobile wallet via PayMongo source
- **GrabPay:** Mobile wallet via PayMongo source
- **Wallet:** Direct deduction from user's WashMate wallet balance

#### 4.3 Payment Flow
1. **Initiation:** Frontend calls backend to create Payment (PENDING status)
2. **Gateway Processing:** PayMongo processes payment for card/mobile wallets
3. **Confirmation:** Frontend sends PayMongo intent ID back to backend
4. **Completion:** Payment status updates to SUCCESS, amounts recorded

#### 4.4 Security
- PayMongo Payment Intent ID for 3DS secure transactions
- JWT authentication on all endpoints
- Server-side payment confirmation
- Immutable payment records

---

## 5. Inputs and Validations

### Order Payment
**Input Fields:**
- Order ID (from order creation)
- Payment Method (selected from UI)
- Amount (calculated from services + delivery fee)

**Validations:**
- Order must exist and belong to authenticated user
- Payment method must be valid (card, gcash, maya, grab_pay, wallet)
- Amount must match order total
- Payment not already confirmed

### Subscription Upgrade
**Input Fields:**
- Plan Type (BASIC, PREMIUM, VIP)
- Payment Method (selected from UI)
- Amount (plan price)

**Validations:**
- User must have current subscription
- New plan must be higher tier than current
- Plan must exist in system
- Payment method must be valid

### Wallet Top-Up
**Input Fields:**
- Amount (user enters)
- Payment Method (card, gcash, maya, grab_pay)

**Validations:**
- Amount must be positive (minimum ₱100)
- Payment method must be valid
- User wallet must exist

---

## 6. How the Feature Works

### Step-by-Step Workflow

#### Order Payment Flow:
1. User selects order services, schedule, address (Steps 1-2)
2. User selects payment method on Payment Review page (Step 3)
3. **Frontend:** `PaymentReview.tsx` calls `submitOrder()` context function
4. **Backend:** OrderService creates Order record (PENDING status)
5. **Frontend:** Calls `orderAPI.initiatePayment(orderId, paymentMethod)`
6. **Backend:** PaymentService creates Payment record with:
   - reference_type: "ORDER"
   - reference_id: orderId
   - paymentMethod: selected method
   - payment_status: "PENDING"
7. **Frontend:** Receives paymentId
8. **For Card Payment:**
   - Routes to `/payment/checkout`
   - Creates PayMongo PaymentIntent
   - Captures payment_intent_id
9. **For Mobile Wallets (GCash, Maya, GrabPay):**
   - Creates PayMongo Source
   - Redirects to wallet provider
   - Returns to success page with intent ID
10. **For Wallet Payment:**
    - Directly routes to success page
11. **Success Page:** Calls `confirmPayment(orderId, paymentId, intentId)`
12. **Backend:**
    - Updates Payment: status="SUCCESS", paymongo_payment_intent_id=intentId
    - Updates Order: status="CONFIRMED"
    - For wallet: deducts from user wallet balance

#### Subscription Upgrade Flow:
- Similar to order, but uses subscription endpoints
- Creates Payment with reference_type="SUBSCRIPTION"
- Updates UserSubscription status to ACTIVE

#### Wallet Top-Up Flow:
- Creates WalletTransaction record
- Creates Payment with reference_type="WALLET_TOPUP"
- Links them bidirectionally (transaction.referenceId = paymentId)
- On confirmation: adds balance to user wallet

---

## 7. API Endpoints

### Order Endpoints
```
POST /api/orders/create
  Input: CreateOrderRequest (services, schedule, address, etc.)
  Output: OrderResponse { orderId, orderNumber, totalAmount, status }

POST /api/orders/{orderId}/payment/initiate
  Input: { paymentMethod: string }
  Output: { paymentId: number, orderId: number, amount: number, ... }

POST /api/orders/{orderId}/payment/confirm/{paymentId}
  Input: { amount?: number, paymongoPaymentIntentId?: string }
  Output: PaymentDTO { paymentId, status, paymentDate, ... }
```

### Subscription Endpoints
```
POST /api/subscriptions/upgrade/{planType}
  Input: (none, JWT authenticated)
  Output: { userSubscriptionId, paymentId, planType, amount, expiryDate }

POST /api/subscriptions/confirm-upgrade/{userSubscriptionId}/{paymentId}
  Input: { paymentMethod?: string, amount?: number, paymongoPaymentIntentId?: string }
  Output: UserSubscriptionDTO { ... }

GET /api/subscriptions/plans
  Output: List<SubscriptionDTO>

GET /api/subscriptions/me
  Output: UserSubscriptionDTO (current user's active subscription)
```

### Wallet Endpoints
```
POST /api/wallet/topup/initiate
  Input: { amount: number, paymentMethod: string }
  Output: { paymentId: number, transactionId: number, amount: number }

POST /api/wallet/topup/confirm/{paymentId}
  Input: { amount?: number, paymongoPaymentIntentId?: string }
  Output: WalletDTO { balance, transactionCount, ... }

GET /api/wallet
  Output: WalletDTO { balance, lastTransaction, ... }
```

---

## 8. Database Tables Involved

### payments (Polymorphic Payment Records)
```sql
payment_id (PK)
reference_type (ORDER | SUBSCRIPTION | WALLET_TOPUP)
reference_id (ID of order/subscription/transaction)
user_id (FK, who made payment)
amount
payment_method (CARD | GCASH | MAYA | GRAB_PAY | WALLET)
payment_status (PENDING | SUCCESS | FAILED)
paymongo_payment_intent_id
payment_date
created_at
updated_at
```

### orders
```sql
order_id (PK)
customer_id (FK)
order_number (unique)
order_status (PENDING | CONFIRMED | IN_DELIVERY | COMPLETED)
total_amount
subtotal
delivery_fee
created_at
```

### user_subscriptions
```sql
user_subscription_id (PK)
user_id (FK)
subscription_id (FK)
status (ACTIVE | EXPIRED)
start_date
expiry_date
created_at
```

### subscriptions
```sql
subscription_id (PK)
plan_type (FREE | BASIC | PREMIUM | VIP)
plan_price
features_json
active
```

### wallets
```sql
wallet_id (PK)
user_id (FK, unique)
balance
last_transaction_date
created_at
updated_at
```

### wallet_transactions
```sql
transaction_id (PK)
wallet_id (FK)
reference_type (TOPUP | DEDUCTION)
reference_id (Payment ID for TOPUP, Order ID for DEDUCTION)
amount
transaction_type (CREDIT | DEBIT)
status (PENDING | SUCCESS)
created_at
```

---

## 9. Frontend Components Added/Modified

### New Files
- `web/src/app/utils/orderAPI.ts` - Order API client
- `web/src/app/utils/subscriptionAPI.ts` - Subscription API client
- `web/src/app/utils/walletAPI.ts` - Wallet API client

### Modified Components
- `web/src/app/pages/PaymentReview.tsx` - Order payment selection
- `web/src/app/pages/PaymentCheckout.tsx` - Card payment via PayMongo
- `web/src/app/pages/PaymentSuccess.tsx` - Payment confirmation
- `web/src/app/pages/SubscriptionUpgradeReview.tsx` - Subscription payment selection
- `web/src/app/pages/SubscriptionUpgradeCheckout.tsx` - Subscription payment processing
- `web/src/app/pages/SubscriptionUpgradeSuccess.tsx` - Subscription confirmation
- `web/src/app/pages/Wallet.tsx` - Wallet balance and transactions
- `web/src/app/pages/WalletPaymentCheckout.tsx` - Wallet top-up checkout
- `web/src/app/pages/WalletPaymentSuccess.tsx` - Wallet top-up confirmation
- `web/src/app/contexts/OrderContext.tsx` - Order state management
- `web/src/app/contexts/PaymentContext.tsx` - Payment state with wallet integration

---

## 10. Technology Stack

### Frontend
- React 18 with TypeScript
- React Router for navigation
- Framer Motion for animations
- PayMongo JS library for payment processing
- Axios for API calls
- Tailwind CSS for styling

### Backend
- Spring Boot 3.x
- Spring Security with JWT
- JPA/Hibernate for persistence
- PostgreSQL database
- PayMongo API integration

### Database
- PostgreSQL with proper constraints
- Sequences for ID generation
- Foreign keys with cascade rules

---

## 11. Testing Performed

### Manual Testing
- ✅ Order creation with service selection
- ✅ Payment initiation for orders
- ✅ Card payment via PayMongo
- ✅ GCash payment via PayMongo source
- ✅ Payment confirmation with intent ID capture
- ✅ Order status update to CONFIRMED
- ✅ Subscription upgrade with payment
- ✅ Wallet top-up and balance update
- ✅ Wallet payment method for orders
- ✅ Database records with correct polymorphic references
- ✅ Payment history retrieval
- ✅ Error handling and messaging

### Validations Tested
- Payment method validation
- Amount verification
- JWT authentication
- User authorization
- Transaction uniqueness
- Status transitions

---

## 12. Summary

This implementation delivers a **production-ready payment system** that:
- Supports multiple payment methods through PayMongo
- Uses a flexible polymorphic design for scalability
- Provides secure server-side confirmation
- Maintains data integrity with proper database design
- Offers clear user experience with step-by-step flows
- Handles errors gracefully with user-friendly messages

The system is fully integrated between frontend, backend, and database, with all payment flows working end-to-end.
