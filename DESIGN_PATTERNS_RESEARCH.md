# Design Patterns Research Document
## WashMate Project - IT342 Phase 3

---

## Executive Summary

This document provides comprehensive research on 6 software design patterns applied to the WashMate laundry service platform. The patterns address key architectural challenges: code duplication, tight coupling, scattered business logic, and lack of extensibility. Implementation of these patterns improves maintainability, testability, and scalability.

---

## Part 1: Creational Patterns

### Pattern 1: Factory Pattern

**Category:** Creational

**Problem it Solves:**
- Decouples object creation from usage
- Centralizes creation logic when type is determined at runtime
- Eliminates duplication of similar creation code
- Makes it easy to add new types without modifying existing code

**How it Works:**
1. Define interface that declares object creation method
2. Implement factory for each concrete type
3. Client calls factory instead of using `new` keyword
4. Client works with abstraction, not concrete classes
5. New types can be added by creating new factory implementation

**Real-World Example in WashMate:**
```
PaymentFactory Interface:
  └── OrderPaymentFactory (create ORDER type payments)
  └── SubscriptionPaymentFactory (create SUBSCRIPTION payments)
  └── WalletTopupPaymentFactory (create WALLET_TOPUP payments)
```

**Benefits:**
- Payment creation logic centralized in factories
- Easy to add new payment types without modifying PaymentService
- Clear separation between creation and usage
- Each factory has single responsibility

**Before:** Three separate methods in PaymentService (createOrderPayment, createSubscriptionPayment, createWalletTopupPayment)
**After:** Single PaymentFactory interface with three implementations

---

### Pattern 2: Builder Pattern

**Category:** Creational

**Problem it Solves:**
- Simplifies complex object construction
- Makes code more readable when creating objects with many parameters
- Allows optional parameters without constructor overloading
- Separates object construction from its representation

**Current Implementation in WashMate:**
Already implemented via Lombok @Builder annotation on all entities:
- @Builder on Payment, Order, User, Subscription, etc.
- Provides fluent API for object creation
- Generates immutable builders

**Enhancements Applied:**
- PaymentBuilder with validation logic
- Context objects (PaymentStrategyContext) using @Builder decorator
- Fluent API for complex multi-step configurations

**Benefits:**
- Clean, readable code for entity creation
- Type safety and compile-time checking
- Flexible parameter specification
- Immutable objects for thread safety

---

## Part 2: Structural Patterns

### Pattern 3: Facade Pattern

**Category:** Structural

**Problem it Solves:**
- Provides unified, simplified interface to complex subsystem
- Reduces client dependencies on internal details
- Hides complexity of underlying components
- Single point of entry for related operations

**How it Works:**
1. Create facade class that wraps multiple related subsystems
2. Facade delegates to appropriate subsystem components
3. Clients interact with facade instead of multiple services/repositories
4. Changes to subsystem don't affect client code

**Real-World Examples in WashMate:**

**1. AuthenticationHelper Facade**
```
Problem: User lookup repeated 3+ times across multiple controllers
Solution: Centralize JWT extraction and user lookup

Before:
  String oauthId = jwt.getSubject();
  User user = userRepository.findByOauthId(oauthId)
    .orElseThrow(() -> new RuntimeException("User not found"));
  // ... repeat in OrderController, SubscriptionController, WalletController

After:
  User user = authHelper.getAuthenticatedUser(jwt);
```

**2. DTOConverter Facade**
```
Problem: DTO conversion logic scattered across services
Solution: Centralize all conversion logic in single component

Services involved:
  - OrderService.toOrderDTO()
  - OrderService.toPaymentDTO()
  - SubscriptionService.toUserSubscriptionDTO()
  - WalletController conversions
  - Custom wallet/transaction conversions

Single facade point:
  DTOConverter.toOrderDTO()
  DTOConverter.toPaymentDTO()
  DTOConverter.toUserSubscriptionDTO()
  DTOConverter.toWalletDTO()
  DTOConverter.toWalletTransactionDTO()
```

**3. OrderFacade**
```
Problem: Order creation requires coordinating multiple services
Complex workflow:
  - Create order (OrderService)
  - Create payment (PaymentFactory + PaymentService)
  - Handle wallet deduction (WalletService)
  - Update subscriptions if applicable (SubscriptionService)

Solution: Single facade coordinating all steps
  OrderFacade.createOrder()
  OrderFacade.initiatePayment()
  OrderFacade.confirmPayment()
```

**Benefits:**
- ~50 lines of duplicate code eliminated in AuthenticationHelper
- Controllers now inject single facade instead of multiple services
- Easier to test complex workflows in isolation
- Changes to services don't require controller updates

---

### Pattern 4: Strategy Pattern

**Category:** Structural

**Problem it Solves:**
- Defines family of algorithms and encapsulates each one
- Makes algorithms interchangeable at runtime
- Client can choose algorithm based on context
- Eliminates long if-else chains for different cases

**How it Works:**
1. Define strategy interface with common method signature
2. Implement each algorithm as concrete strategy class
3. Client receives appropriate strategy (dependency injection, factory, etc)
4. Client calls strategy.execute() regardless of implementation
5. Adding new strategies requires only new class, no changes to client

**Real-World Example in WashMate: PaymentMethodStrategy**

**Problem:**
- Different payment methods (CARD, GCASH, MAYA, GRABPAY, WALLET) have different validation/processing
- Future payment methods need to be added
- Code maintainability with if-else chains for each method

**Solution:**
```
PaymentMethodStrategy Interface
  ├── CardPaymentStrategy
  │   └── Validates card amount, processes via PayMongo
  ├── GCashPaymentStrategy
  │   └── Validates GCash amounts, minimum ₱1.00
  ├── MayaPaymentStrategy
  │   └── Validates Maya amounts, minimum ₱1.00
  ├── GrabPayPaymentStrategy
  │   └── Validates GrabPay amounts, minimum ₱1.00
  └── WalletPaymentStrategy
      └── Validates wallet amounts, deducts from balance
```

**Before (Without Strategy):**
```java
if ("CARD".equals(method)) {
  // Validate and process card
} else if ("GCASH".equals(method)) {
  // Validate and process GCash
} else if ("MAYA".equals(method)) {
  // Validate and process Maya
} else if ("GRABPAY".equals(method)) {
  // Validate and process GrabPay
} else if ("WALLET".equals(method)) {
  // Deduct from wallet
}
// Adding new method requires modifying this code
```

**After (With Strategy):**
```java
PaymentMethodStrategy strategy = strategyFactory.getStrategy(method);
strategy.validate(context);
strategy.process(context);
// Adding new method: just create new Strategy class, no other code changes
```

**Also Applied: Status Enums**
- Replace string constants with type-safe enums
- OrderStatus: PENDING, CONFIRMED, IN_PROGRESS, READY_FOR_PICKUP, PICKED_UP, IN_DELIVERY, DELIVERED, CANCELLED, COMPLETED
- PaymentStatus: PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
- WalletTransactionStatus: PENDING, COMPLETED, FAILED, REFUNDED
- SubscriptionStatus: ACTIVE, EXPIRED, CANCELLED, PENDING

**Benefits:**
- Easy to add new payment methods without modifying existing code
- Each payment method logic isolated in own class
- Type safety: compiler catches invalid status values
- IDE autocomplete for enum values
- Single source of truth for valid statuses

---

## Part 3: Behavioral Patterns

### Pattern 5: Observer Pattern

**Category:** Behavioral

**Problem it Solves:**
- Define one-to-many dependency between objects
- When one object changes state, all dependents are notified
- Decouples subject from observers
- Enables loose coupling between components

**How it Works:**
1. Define observer interface with update methods
2. Subject maintains list of observers
3. When subject state changes, it notifies all registered observers
4. Each observer implements response to state change
5. Observers can be added/removed at runtime

**Real-World Example in WashMate: Payment Confirmation**

**Problem:**
When a payment is confirmed, multiple things must happen:
1. Order status must be updated (OrderService)
2. Subscription must be activated (SubscriptionService)
3. Wallet balance must be updated (WalletService)

Tight coupling solution:
```java
// In PaymentService
public void confirmPayment(...) {
  // ... confirm payment
  orderService.updateOrderStatus(...);  // Coupled to OrderService
  subscriptionService.activateSubscription(...);  // Coupled to SubscriptionService
  walletService.processTopup(...);  // Coupled to WalletService
}
// Problem: Adding wallet refunds requires modifying PaymentService
// Problem: PaymentService knows too much about other services
```

**Solution with Observer Pattern:**
```
PaymentEventPublisher (Subject)
  └── Maintains list of PaymentObserver instances
  └── On payment confirmed, notifies:
      └── OrderPaymentObserver (updates order status)
      └── SubscriptionPaymentObserver (activates subscription)
      └── WalletPaymentObserver (updates wallet balance)
```

**Benefits:**
- PaymentService no longer depends on other services
- New side-effects can be added by creating new Observer class
- Easy to test each observer independently
- Enables asynchronous processing of side-effects
- Follows Open/Closed Principle: open for extension, closed for modification

---

### Pattern 6: Template Method Pattern (Bonus Enhancement)

**Category:** Behavioral

**Problem it Solves:**
- Define skeleton of algorithm in base class
- Let subclasses override specific steps
- Avoid code duplication in similar algorithms
- Enforce algorithm structure across implementations

**How it Works:**
1. Create abstract base class with template method
2. Template method defines algorithm steps (calls abstract methods)
3. Concrete subclasses implement abstract methods
4. Base class ensures all subclasses follow same structure

**Application in WashMate: AbstractPaymentProcessor**

**Example:** Payment processing workflow
```
AbstractPaymentProcessor (template)
  ├── validatePaymentDetails() [abstract - implemented by subclasses]
  ├── createPaymentRecord() [abstract]
  ├── processWithGateway() [abstract]
  ├── confirmPayment() [concrete template method]
  │   └── validate() → process() → confirm()
  ├── CardPaymentProcessor
  │   ├── validateCardDetails()
  │   ├── createCardPaymentRecord()
  │   └── processCardViaPayMongo()
  ├── SubscriptionPaymentProcessor
  │   ├── validateSubscriptionPayment()
  │   ├── createSubscriptionRecord()
  │   └── processSubscriptionCharge()
  └── WalletPaymentProcessor
      ├── validateWalletBalance()
      ├── createWalletTransaction()
      └── deductFromWallet()
```

**Benefits:**
- Ensures consistent payment processing across different types
- Reduces code duplication
- Algorithm structure enforced at compile time
- Easy to add new payment processor types

---

## Part 4: Pattern Summary & Architecture Impact

### Patterns Applied & Their Locations

| Pattern | Type | Location | Purpose |
|---------|------|----------|---------|
| Factory | Creational | `factory/PaymentFactory` + 3 implementations | Centralize payment object creation |
| Builder | Creational | Entity annotations + `PaymentStrategyContext` | Fluent, readable object construction |
| Facade | Structural | `facade/AuthenticationHelper`, `DTOConverter`, `OrderFacade` | Simplify and unify complex operations |
| Strategy | Structural | `strategy/PaymentMethodStrategy` + 5 implementations | Encapsulate payment method variations |
| Enums | Structural | `enums/OrderStatus`, `PaymentStatus`, etc. | Type-safe status management |
| Observer | Behavioral | `observer/PaymentObserver` + 3 implementations, `PaymentEventPublisher` | Decouple payment side-effects |
| Singleton | Creational | `singleton/SubscriptionPlanCache` | Cache shared resource efficiently |

### Architecture Improvements

**Before (Monolithic):**
```
Controllers
  └── Multiple Service Injections
      └── Direct Repository Access
      └── Scattered DTO Conversions
      └── Inline User Lookups
      └── String Constants for Status
      └── Tight Coupling
```

**After (Patterns-Based):**
```
Controllers
  └── Facade Injection (OrderFacade, OrderFacade)
      ├── AuthenticationHelper (centralized auth)
      ├── DTOConverter (unified conversions)
      ├── PaymentFactory (strategy creation)
      ├── PaymentEventPublisher (observers)
      └── Enums (type-safe status)

Services
  ├── PaymentObserver implementations (address payments)
  ├── PaymentMethodStrategy implementations (payment processing)
  └── SubscriptionPlanCache (shared resource)
```

### Code Quality Metrics

| Metric | Before | After | Change |
|--------|--------|-------|--------|
| User Lookup Code Duplication | 4 locations | 1 (AuthenticationHelper) | -75% |
| Payment Creation Methods | 3 separate | 1 factory interface | -66% |
| DTO Conversion Locations | 4+ services | 1 DTOConverter | Centralized |
| Status String Constants | Throughout | Enums | Type-safe |
| Payment Confirmation Logic | Coupled | Observer-based | Decoupled |
| Service Dependencies in Controllers | Multiple | 1-2 facades | Simplified |

---

## Part 5: Benefits & Future Extensions

### Immediate Benefits

1. **Reduced Code Duplication:** ~200-300 lines eliminated
2. **Improved Maintainability:** Changes localized to single pattern class
3. **Better Testability:** Each pattern component testable in isolation
4. **Type Safety:** Enums prevent invalid status values at compile time
5. **Clear Separation of Concerns:** Each pattern handles one responsibility

### Future Extensions Made Simple

**Add New Payment Method (Card → Point of Sale):**
```java
// Only need to add this class:
@Component
public class PointOfSalePaymentStrategy implements PaymentMethodStrategy {
  // Implementation for POS
}
// No changes to PaymentService, PaymentController, or payment logic
```

**Add New Payment Side-Effect (Email notification):**
```java
// Only need to add this observer:
@Component
public class EmailNotificationObserver implements PaymentObserver {
  // Send email on payment confirmation
}
// No changes to PaymentService or other observers
```

**Add New Status Type:**
```java
public enum RefundStatus {
  PENDING, PROCESSING, COMPLETED, FAILED
  // New status available everywhere via enum
}
```

---

## Conclusion

The 6 design patterns applied to WashMate address fundamental software engineering challenges:
- **Factory** → Object creation flexibility
- **Builder** → Complex object construction clarity
- **Facade** → Interface simplification and code consolidation
- **Strategy** → Algorithm variation encapsulation
- **Observer** → Loose coupling for side-effects
- **Singleton** → Efficient resource caching

These patterns create a more maintainable, testable, and extensible codebase that can grow with the application's needs while maintaining code quality and reducing technical debt.

---

*Document Generated: IT342 Phase 3 - Design Patterns Implementation*
*Implementation Date: 2026-04-07*
*Status: Complete*
