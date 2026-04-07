# IT342 Phase 3 – Web Main Feature Submission

## 1. GitHub Link
**Repository:** https://github.com/ginmurin/IT342-Sevilla-WashMate

**Commit:** `19e5038 - IT342 Phase 3 – Web Main Feature Completed`

---

## 2. Main Feature: Order Management System with Subscriptions and Payments

The main feature implemented is a **Complete Order Management System** that enables users to:
1. **Select and customize laundry services** with variants
2. **Schedule pickup and delivery** with flexible time slots
3. **Manage addresses** and order specifications
4. **Subscribe to plans** for discounted ongoing service
5. **Process payments** through multiple methods
6. **Track orders** and manage wallet balance

This is a full-stack feature spanning service selection, order creation, subscription management, payment processing, and wallet integration.

---

## 3. Screenshots

### Step 1: Service Selection Page
![Service Selection](./docs/screenshots/01-service-selection.png)
- Shows available laundry services (Wash, Fold, Iron, Dry Clean)
- Each service displays description, price, and variants if available
- Users can select multiple services and adjust quantities
- Weight calculation based on services selected

### Step 2: Schedule & Address Page
![Schedule and Address](./docs/screenshots/02-schedule-address.png)
- Pickup date and time selection with time slot options
- Delivery date and time selection
- Address field for pickup/delivery location
- Phone number and special instructions
- Rush order option toggle

### Step 3: Payment Review Page
![Payment Review](./docs/screenshots/03-payment-review.png)
- Order summary with selected services and weight
- Pricing breakdown: Laundry Service + Delivery Fee = Total
- Discount percentage application (if subscribed)
- Payment method selection: Card, GCash, Maya, GrabPay, Wallet
- Terms and conditions checkbox

### Payment Processing - Card Checkout
![Card Payment](./docs/screenshots/04-card-payment.png)
- PayMongo card payment form
- Card details input (number, expiry, CVC)
- Amount display
- Secure 3DS authentication

### Payment Processing - GCash Checkout
![GCash Payment](./docs/screenshots/05-gcash-payment.png)
- PayMongo GCash payment interface
- Test payment page with Authorize button
- Amount display (₱234.00)
- Mobile wallet redirection

### Payment Success Page
![Payment Success](./docs/screenshots/06-payment-success.png)
- Order confirmation with order number
- Payment status: SUCCESS
- Amount paid
- Next steps and order tracking info

### Orders History Page
![Orders History](./docs/screenshots/07-orders-history.png)
- List of user's orders with order numbers
- Order status (PENDING, CONFIRMED, IN_DELIVERY, COMPLETED)
- Order amounts and dates
- Links to view order details

### Subscriptions Page
![Subscriptions](./docs/screenshots/08-subscriptions.png)
- Current subscription plan display (FREE, BASIC, PREMIUM, VIP)
- Plan features list
- Plan pricing and validity period
- Upgrade option with plan comparison

### Subscription Upgrade Review
![Subscription Upgrade](./docs/screenshots/09-subscription-upgrade.png)
- Current plan vs. new plan comparison
- Features included in each plan
- Price difference
- Payment method selection for upgrade

### Wallet Page
![Wallet](./docs/screenshots/10-wallet.png)
- Current wallet balance display
- Top-up button
- Transaction history (credits and debits)
- Transaction details (amount, type, date)

### Wallet Top-Up Checkout
![Wallet Topup](./docs/screenshots/11-wallet-topup.png)
- Amount input field
- Payment method selection
- Confirmation button
- PayMongo processing

### Database - Orders Table
![Orders Table](./docs/screenshots/12-db-orders.png)
- Order records with order_id, customer_id, order_number
- Order status tracking (PENDING → CONFIRMED → COMPLETED)
- Amount tracking (subtotal, delivery_fee, total_amount)
- Created and updated timestamps

### Database - Order Service Details
![Order Services](./docs/screenshots/13-db-order-services.png)
- Links orders to selected services
- Service quantity per order
- Selected variants
- Service pricing at time of order

### Database - Payments Table
![Payments Table](./docs/screenshots/14-db-payments.png)
- Payment records with polymorphic pattern
- Reference_type: ORDER, SUBSCRIPTION, WALLET_TOPUP
- Reference_id linking to respective entity
- Payment method and status tracking
- PayMongo intent ID for 3DS transactions

### Database - Subscriptions & User Subscriptions
![Subscriptions Table](./docs/screenshots/15-db-subscriptions.png)
- Subscription plans (FREE, BASIC, PREMIUM, VIP)
- Plan pricing and features
- User subscriptions with active status
- Expiry date tracking

### Database - Wallets & Transactions
![Wallet Tables](./docs/screenshots/16-db-wallets.png)
- Wallet records per user with balance
- Wallet transactions with credit/debit tracking
- Transaction status and type
- Reference IDs linking to payments or orders

---

## 4. Feature Description

### Overview
The Order Management System is a complete 3-step workflow that allows users to:

**Step 1: Service Selection**
- Browse available laundry services (Wash, Fold, Iron, Dry Clean)
- Select service variants (e.g., premium wash, express fold)
- Choose quantities for each service
- View pricing and weight calculations
- See estimated total based on selections

**Step 2: Schedule & Address**
- Set pickup date and time from available slots
- Set delivery date and time
- Enter or select delivery address
- Provide contact phone number
- Add special instructions
- Option for rush order (extra delivery fee)

**Step 3: Payment & Review**
- Review complete order summary
- View pricing breakdown
- Select payment method (Card, GCash, Maya, GrabPay, Wallet)
- Accept terms and conditions
- Process payment through selected method
- Receive order confirmation

### Key Features

#### 4.1 Service Management
- Multiple service types with customizable variants
- Quantity selection for each service
- Real-time price calculation
- Service availability checking

#### 4.2 Order Scheduling
- Flexible pickup and delivery times
- Pre-defined time slots for user convenience
- Date validation (no past dates)
- Rush order option with additional fees

#### 4.3 Address Management
- Support for multiple delivery addresses
- Address validation for valid locations
- Latitude/longitude storage for mapping
- Pickup and delivery address options (can be different)

#### 4.4 Subscription System
- Four subscription plans: FREE, BASIC, PREMIUM, VIP
- Plan-based discounts on services
- Subscription upgrades with payment
- Automatic discount calculation on orders
- Subscription expiry tracking

#### 4.5 Flexible Payment System
- Multiple payment methods: Card, GCash, Maya, GrabPay, Wallet
- PayMongo integration for card and mobile wallet payments
- Wallet balance integration for direct payment
- 3DS secure transactions with payment intent tracking
- Polymorphic design supporting multiple transaction types

#### 4.6 Wallet Integration
- User wallet balance tracking
- Balance deduction for wallet payments
- Transaction history (credits and debits)
- Top-up functionality with flexible amounts

---

## 5. Inputs and Validations

### Step 1: Service Selection
**Input Fields:**
- Service selection (checkboxes for Wash, Fold, Iron, Dry Clean)
- Service variants (if available for selected service)
- Quantity per service (number input)

**Validations:**
- At least one service must be selected
- Quantity must be positive number (minimum 1)
- Services must exist in system
- Variants must be valid for selected service

### Step 2: Schedule & Address
**Input Fields:**
- Pickup date (date picker)
- Pickup time (time slot selector)
- Delivery date (date picker)
- Delivery time (time slot selector)
- Delivery address (text area)
- Phone number (text input)
- Special instructions (optional text area)
- Rush order (checkbox toggle)

**Validations:**
- Pickup date cannot be in the past
- Delivery date cannot be before pickup date
- Time slots must be within business hours
- Address must not be empty
- Phone number must be valid format (11 digits for PH)
- Rush order validates delivery address
- Address cannot be too far from service area

### Step 3: Payment Review
**Input Fields:**
- Payment method selection (radio buttons)
- Terms acceptance (checkbox)

**Validations:**
- Payment method must be selected
- Terms must be accepted
- Order data must persist from previous steps
- Order total must match calculated amount

### Subscription Upgrade
**Input Fields:**
- Plan type selection (BASIC, PREMIUM, VIP)
- Payment method selection
- Terms acceptance

**Validations:**
- User must have existing subscription
- New plan must be higher or equal tier
- Plan must exist in system
- Amount must match plan price

### Wallet Top-Up
**Input Fields:**
- Top-up amount (number input)
- Payment method selection (Card, GCash, Maya, GrabPay)

**Validations:**
- Amount must be positive number
- Minimum amount: ₱100
- Maximum amount: ₱50,000
- Payment method must be valid
- User wallet must exist

---

## 6. How the Feature Works

### Complete Order Workflow

#### Step 1: Service Selection Flow
1. User navigates to `/services` page
2. **Frontend:** Fetches available services from `GET /api/services`
3. **Backend:** Returns list of services with pricing and variants
4. User selects services, variants, and quantities
5. **Frontend:** Calculates subtotal based on selected services
6. **Order Context:** Stores selected services in state
7. User clicks "Next" button
8. **Validation:** Ensures at least one service is selected
9. **Navigation:** Routes to Step 2 (Schedule & Address)

#### Step 2: Schedule & Address Flow
1. User selects pickup date, time, delivery date, time
2. User enters delivery address and phone number
3. User can optionally add special instructions
4. User can toggle rush order option
5. **Frontend:** Calculates delivery fee (0 for regular, ₱50 for rush)
6. **Frontend:** Loads user's current subscription
7. **Frontend:** Applies discount if user has active subscription
   - FREE: 0% discount
   - BASIC: 5% discount
   - PREMIUM: 10% discount
   - VIP: 15% discount
8. **Order Context:** Stores schedule, address, and fee data
9. User clicks "Review Order"
10. **Navigation:** Routes to Step 3 (Payment Review)

#### Step 3: Payment Review and Order Creation
1. User reviews order summary with all details
2. User selects payment method
3. User accepts terms and conditions
4. User clicks "Proceed to Payment"
5. **Frontend:** Calls `submitOrder()` from OrderContext
6. **Backend:** `POST /api/orders/create` with:
   ```
   {
     services: [{serviceId, quantity, variant}],
     totalWeight: number,
     deliveryFee: number,
     pickupSchedule: ISO datetime,
     deliverySchedule: ISO datetime,
     pickupAddressString: string,
     deliveryAddressString: string,
     specialInstructions: string,
     isRushOrder: boolean
   }
   ```
7. **Backend - OrderService:**
   - Validates order data
   - Calculates subtotal from services
   - Creates Order record with status PENDING
   - Creates OrderServiceDetail records for each service
   - Returns orderId
8. **Frontend:** Receives orderId
9. **Frontend:** Calls `orderAPI.initiatePayment(orderId, paymentMethod)`
10. **Backend - PaymentService:**
    - Creates Payment record with:
      ```
      reference_type: "ORDER"
      reference_id: orderId
      payment_method: selected method
      amount: order total
      payment_status: "PENDING"
      ```
    - Returns paymentId

#### Payment Processing (Based on Method)

**For Card Payments:**
1. **Frontend:** Routes to `/payment/checkout`
2. Passes orderId, paymentId, amount to checkout page
3. **Frontend:** Creates PayMongo PaymentIntent
4. **Frontend:** Displays card form
5. User enters card details
6. **Frontend:** Submits to PayMongo
7. **PayMongo:** Processes payment (may require 3DS)
8. **Frontend:** Captures payment_intent_id from response
9. **Frontend:** Routes to `/payment/success` with paymentId and intentId
10. **Frontend:** Calls `orderAPI.confirmPayment(orderId, paymentId, intentId)`
11. **Backend:** Updates Payment record:
    - status: "SUCCESS"
    - paymongo_payment_intent_id: intentId
    - payment_date: now
12. **Backend:** Updates Order record:
    - status: "CONFIRMED"
13. **Frontend:** Shows success page with order details

**For Mobile Wallet Payments (GCash, Maya, GrabPay):**
1. **Frontend:** Creates PayMongo Source
2. **Frontend:** Redirects to wallet provider
3. User authorizes payment on mobile wallet
4. **PayMongo:** Redirects back to success URL with payment intent ID
5. **Frontend:** Extracts intentId from redirect URL
6. **Frontend:** Routes to `/payment/success`
7. **Backend:** Confirms payment (same as card)

**For Wallet Payments:**
1. **Frontend:** Checks wallet balance
2. **Frontend:** Routes directly to `/payment/success`
3. Calls `orderAPI.confirmPayment` with wallet method
4. **Backend:**
   - Updates Payment: status "SUCCESS"
   - Updates Order: status "CONFIRMED"
   - Deducts amount from user wallet
   - Creates WalletTransaction (DEBIT) for deduction
5. **Frontend:** Shows success page

#### Order Confirmation
1. **Frontend:** Displays order number, status, and confirmation
2. User can view order history on `/my-orders`
3. **Backend:** Order status transitions: PENDING → CONFIRMED → IN_DELIVERY → COMPLETED

### Subscription Upgrade Flow
1. User navigates to `/subscriptions`
2. **Frontend:** Fetches current subscription and available plans
3. User selects new plan (BASIC, PREMIUM, VIP)
4. User selects payment method
5. **Frontend:** Calls `subscriptionAPI.initiateUpgrade(planType)`
6. **Backend:**
   - Creates UserSubscription with new plan
   - Creates Payment record with reference_type: "SUBSCRIPTION"
   - Returns userSubscriptionId, paymentId, amount
7. **Frontend:** Routes to upgrade checkout
8. Processes payment (card, wallet, or mobile wallet)
9. **Frontend:** Calls `subscriptionAPI.confirmUpgrade(userSubscriptionId, paymentId, intentId)`
10. **Backend:**
    - Updates Payment: status "SUCCESS"
    - Updates UserSubscription: status "ACTIVE"
    - Sets expiry_date based on plan
11. **Frontend:** Shows success page

### Wallet Management Flow
1. User navigates to `/wallet`
2. **Frontend:** Fetches `GET /api/wallet` to display balance
3. **Frontend:** Fetches wallet transactions history
4. User clicks "Top-Up" button
5. User enters amount and selects payment method
6. **Frontend:** Calls `walletAPI.initiateTopup(amount, paymentMethod)`
7. **Backend:**
   - Creates WalletTransaction (CREDIT) with status PENDING
   - Creates Payment record with reference_type: "WALLET_TOPUP"
   - Links them bidirectionally
   - Returns paymentId, transactionId
8. **Frontend:** Processes payment through PayMongo
9. **Frontend:** Calls `walletAPI.confirmTopup(paymentId, amount, intentId)`
10. **Backend:**
    - Updates Payment: status "SUCCESS"
    - Updates WalletTransaction: status "SUCCESS"
    - Adds amount to user wallet balance
    - Updates wallet last_transaction_date
11. **Frontend:** Displays updated balance and success message

---

## 7. API Endpoints

### Service Endpoints
```
GET /api/services
  Output: List<ServiceResponse> { serviceId, serviceName, description, basePrice, hasVariants }

GET /api/services/{serviceId}/variants
  Output: List<ServiceVariantDTO> { variantId, serviceName, variantName, priceModifier }
```

### Order Endpoints
```
POST /api/orders/create
  Input: CreateOrderRequest {
    services: [{serviceId, quantity, selectedVariantId?}],
    totalWeight: number,
    deliveryFee: number,
    pickupSchedule: ISO datetime,
    deliverySchedule: ISO datetime,
    pickupAddressString: string,
    deliveryAddressString: string,
    specialInstructions: string,
    isRushOrder: boolean
  }
  Output: OrderResponse { orderId, orderNumber, customerId, totalAmount, orderStatus }

GET /api/orders/{orderId}
  Output: OrderResponse

GET /api/orders/my-orders
  Output: List<OrderResponse>

POST /api/orders/{orderId}/payment/initiate
  Input: { paymentMethod: string }
  Output: OrderPaymentDTO { paymentId, orderId, amount, paymentMethod, paymentStatus }

POST /api/orders/{orderId}/payment/confirm/{paymentId}
  Input: { amount?: number, paymongoPaymentIntentId?: string }
  Output: PaymentDTO { paymentId, status, paymentDate, paymongoPaymentIntentId }

GET /api/orders/{orderId}/payments
  Output: List<PaymentDTO>
```

### Subscription Endpoints
```
GET /api/subscriptions/plans
  Output: List<SubscriptionDTO> { subscriptionId, planType, planPrice, features }

GET /api/subscriptions/me
  Output: UserSubscriptionDTO { userSubscriptionId, planType, status, expiryDate }

GET /api/subscriptions/history
  Output: List<UserSubscriptionDTO>

POST /api/subscriptions/upgrade/{planType}
  Input: (none, JWT authenticated)
  Output: Map { userSubscriptionId, paymentId, planType, amount, expiryDate }

POST /api/subscriptions/confirm-upgrade/{userSubscriptionId}/{paymentId}
  Input: { paymentMethod?: string, amount?: number, paymongoPaymentIntentId?: string }
  Output: UserSubscriptionDTO
```

### Wallet Endpoints
```
GET /api/wallet
  Output: WalletDTO { walletId, balance, lastTransactionDate, transactionCount }

GET /api/wallet/transactions
  Output: List<WalletTransactionDTO> { transactionId, amount, type, status, createdAt }

POST /api/wallet/topup/initiate
  Input: { amount: number, paymentMethod: string }
  Output: Map { paymentId, transactionId, amount, paymentStatus }

POST /api/wallet/topup/confirm/{paymentId}
  Input: { amount?: number, paymongoPaymentIntentId?: string }
  Output: WalletDTO
```

---

## 8. Database Tables Involved

### wash_services
```sql
service_id (PK)
service_name (WASH | FOLD | IRON | DRY_CLEAN)
description
base_price (decimal)
has_variants (boolean)
active (boolean)
created_at
```

### service_variants
```sql
variant_id (PK)
service_id (FK)
variant_name
price_modifier (decimal)
active (boolean)
created_at
```

### orders
```sql
order_id (PK)
customer_id (FK)
order_number (unique, e.g., WM-68BC4EE1)
order_status (PENDING | CONFIRMED | IN_DELIVERY | COMPLETED)
total_amount (decimal)
subtotal (decimal)
delivery_fee (decimal)
special_instructions (text)
is_rush_order (boolean)
pickup_address (text)
delivery_address (text)
pickup_latitude (decimal)
pickup_longitude (decimal)
delivery_latitude (decimal)
delivery_longitude (decimal)
pickup_schedule (datetime)
delivery_schedule (datetime)
created_at
updated_at
```

### order_service_details
```sql
order_service_detail_id (PK)
order_id (FK)
service_id (FK)
variant_id (FK, nullable)
quantity (integer)
service_price (decimal at time of order)
created_at
```

### subscriptions
```sql
subscription_id (PK)
plan_type (FREE | BASIC | PREMIUM | VIP)
plan_price (decimal)
discount_percentage (decimal)
features_json (text)
active (boolean)
created_at
```

### user_subscriptions
```sql
user_subscription_id (PK)
user_id (FK)
subscription_id (FK)
status (ACTIVE | EXPIRED)
start_date (datetime)
expiry_date (datetime)
created_at
updated_at
```

### payments (Polymorphic)
```sql
payment_id (PK)
user_id (FK)
reference_type (ORDER | SUBSCRIPTION | WALLET_TOPUP)
reference_id (ID of order/subscription/transaction)
amount (decimal)
payment_method (CARD | GCASH | MAYA | GRAB_PAY | WALLET)
payment_status (PENDING | SUCCESS | FAILED)
paymongo_payment_intent_id (text, nullable)
payment_date (datetime, nullable)
created_at
updated_at
```

### wallets
```sql
wallet_id (PK)
user_id (FK, unique)
balance (decimal, default 0)
last_transaction_date (datetime, nullable)
created_at
updated_at
```

### wallet_transactions
```sql
transaction_id (PK)
wallet_id (FK)
reference_type (TOPUP | DEDUCTION)
reference_id (Payment ID for TOPUP, Order ID for DEDUCTION)
amount (decimal)
transaction_type (CREDIT | DEBIT)
status (PENDING | SUCCESS)
created_at
```

---

## 9. Frontend Components Added/Modified

### Step 1: Service Selection Components
- `web/src/app/pages/Services.tsx` - Service catalog and selection

### Step 2: Schedule & Address Components
- `web/src/app/pages/OrderSchedule.tsx` - Schedule selection and address input
- Date and time slot selection components

### Step 3: Payment Review Components
- `web/src/app/pages/PaymentReview.tsx` - Order summary and payment method selection
- Order context management for multi-step workflow

### Payment Processing Components
- `web/src/app/pages/PaymentCheckout.tsx` - Card payment via PayMongo
- `web/src/app/pages/PaymentSuccess.tsx` - Payment confirmation page
- PayMongo integration utilities

### Order Management Components
- `web/src/app/pages/MyOrders.tsx` - Order history and status tracking
- Order detail view component

### Subscription Components
- `web/src/app/pages/Subscriptions.tsx` - Plan display and management
- `web/src/app/pages/SubscriptionUpgradeReview.tsx` - Upgrade selection
- `web/src/app/pages/SubscriptionUpgradeCheckout.tsx` - Upgrade payment
- `web/src/app/pages/SubscriptionUpgradeSuccess.tsx` - Upgrade confirmation

### Wallet Components
- `web/src/app/pages/Wallet.tsx` - Balance display and transaction history
- `web/src/app/pages/WalletPaymentCheckout.tsx` - Top-up payment
- `web/src/app/pages/WalletPaymentSuccess.tsx` - Top-up confirmation
- `web/src/app/pages/WalletPaymentReview.tsx` - Top-up review

### API Utility Modules (NEW)
- `web/src/app/utils/orderAPI.ts` - Order API client methods
- `web/src/app/utils/subscriptionAPI.ts` - Subscription API client
- `web/src/app/utils/walletAPI.ts` - Wallet API client

### Context Providers
- `web/src/app/contexts/OrderContext.tsx` - Multi-step order state management
- `web/src/app/contexts/SubscriptionContext.tsx` - Subscription state
- `web/src/app/contexts/PaymentContext.tsx` - Payment and wallet state
- `web/src/app/contexts/WalletContext.tsx` - Wallet state management

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

### Service Selection Testing
- ✅ Services display correctly with pricing
- ✅ Service variants load and display
- ✅ Multiple services can be selected
- ✅ Quantity adjustment works
- ✅ Price calculation updates in real-time
- ✅ At least one service validation

### Schedule & Address Testing
- ✅ Date picker prevents past dates
- ✅ Time slot selector displays available times
- ✅ Pickup date validation
- ✅ Delivery date cannot be before pickup
- ✅ Address input and validation
- ✅ Phone number format validation
- ✅ Rush order toggle and fee calculation
- ✅ Special instructions optional field

### Subscription Discount Testing
- ✅ FREE plan: 0% discount
- ✅ BASIC plan: 5% discount applied
- ✅ PREMIUM plan: 10% discount applied
- ✅ VIP plan: 15% discount applied
- ✅ Discount amount calculation
- ✅ Final total reflects discount

### Order Creation Testing
- ✅ Order successfully created with all services
- ✅ Order ID returned and stored
- ✅ Order status starts as PENDING
- ✅ Order number generated correctly
- ✅ Order amount matches submission total
- ✅ Order stored in database with all details

### Payment Initiation Testing
- ✅ Payment record created for each order
- ✅ Reference type set to ORDER
- ✅ Reference ID points to correct order
- ✅ Payment amount matches order total
- ✅ Payment status starts as PENDING
- ✅ Payment ID returned to frontend

### Card Payment Testing
- ✅ PayMongo PaymentIntent created
- ✅ Card form displays correctly
- ✅ Payment intent ID captured
- ✅ 3DS authentication flow works
- ✅ Successful payment confirmation

### Mobile Wallet Payment Testing
- ✅ GCash payment via PayMongo source
- ✅ Maya payment via PayMongo source
- ✅ GrabPay payment via PayMongo source
- ✅ Redirect to payment provider
- ✅ Return from provider with intent ID

### Wallet Payment Testing
- ✅ Wallet balance checked before payment
- ✅ Balance deducted on payment confirmation
- ✅ WalletTransaction created for deduction
- ✅ Wallet balance updates correctly

### Payment Confirmation Testing
- ✅ Payment status updated to SUCCESS
- ✅ PayMongo intent ID stored in database
- ✅ Payment date recorded
- ✅ Order status updated to CONFIRMED
- ✅ Success page displays correctly

### Subscription Upgrade Testing
- ✅ Subscription plans load correctly
- ✅ Current subscription displays
- ✅ Upgrade to higher plan works
- ✅ Payment created for upgrade
- ✅ UserSubscription updated on confirmation
- ✅ Expiry date set correctly
- ✅ Discount applies after upgrade

### Wallet Top-Up Testing
- ✅ Wallet balance displays correctly
- ✅ Top-up amount input works
- ✅ Payment method selection for top-up
- ✅ WalletTransaction created
- ✅ Payment record created with WALLET_TOPUP type
- ✅ Balance updated after confirmation
- ✅ Transaction history shows credit

### Wallet Transaction History Testing
- ✅ All credits (top-ups) visible
- ✅ All debits (order payments) visible
- ✅ Transaction dates correct
- ✅ Transaction amounts accurate
- ✅ Transaction types correctly labeled

### Database Integrity Testing
- ✅ Polymorphic reference pattern works
- ✅ No orphaned payment records
- ✅ Order service details link correctly
- ✅ Subscription records persist
- ✅ Wallet balance consistency
- ✅ Transaction history completeness

### Error Handling Testing
- ✅ Invalid payment method rejected
- ✅ Insufficient wallet balance message
- ✅ Network error handling
- ✅ Timeout handling
- ✅ User-friendly error messages
- ✅ Proper validation error feedback

### UI/UX Testing
- ✅ Step indicators show progress
- ✅ Navigation between steps
- ✅ Data persistence across steps
- ✅ Loading states display
- ✅ Form validation feedback
- ✅ Success/error animations

---

## 12. Summary

This implementation delivers a **complete, production-ready Order Management System** that:

### Core Features
- **3-Step Order Workflow:** Service selection → Schedule & Address → Payment Review
- **Service Management:** Browse, select, and customize laundry services with variants
- **Smart Scheduling:** Flexible pickup and delivery with time slots and rush options
- **Subscription Integration:** Automatic discounts based on subscription plan
- **Flexible Payments:** Card, GCash, Maya, GrabPay, and Wallet payment methods
- **Wallet System:** Balance management, transaction history, and top-up functionality
- **Polymorphic Payment Design:** Single Payment table supporting Orders, Subscriptions, Wallet Top-ups

### Technical Excellence
- Clean 3-step UI flow with progress indicators
- Real-time price calculations with discount application
- Secure PayMongo integration with 3DS authentication
- Server-side payment confirmation and validation
- JWT authentication on all endpoints
- Proper database design with foreign keys and constraints
- Comprehensive error handling and validation

### User Experience
- Intuitive multi-step order creation
- Clear order summaries and confirmations
- Multiple payment method options
- Transaction history and order tracking
- Wallet balance visibility and top-up convenience
- Seamless subscription upgrades

### Data Integrity
- All payments tied to specific orders, subscriptions, or wallet transactions
- Transaction history tracking for audit purposes
- Immutable payment records
- Proper status transitions and lifecycle management
- Bidirectional linking between related entities

### Business Value
- Support for recurring revenue (subscriptions)
- Multiple payment methods for customer convenience
- Wallet system for customer retention
- Comprehensive order and payment tracking
- Flexible architecture for future transaction types

The system is fully integrated between frontend, backend, and database, with all workflows working end-to-end and properly tested.
