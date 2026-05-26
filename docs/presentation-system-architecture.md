# WashMate System Architecture Diagram

Use this diagram during the architecture section of the presentation.

```mermaid
flowchart TB
    UserCustomer["Customer"]
    UserShop["Shop Owner"]
    UserAdmin["Administrator"]

    Web["React Web App\nweb/src"]
    Mobile["Android Kotlin App\nmobile/app/src/main"]

    Axios["Axios API Client\nweb/src/features/shared/utils/api.ts"]
    Retrofit["Retrofit + OkHttp\nmobile/api/RetrofitClient.kt\nmobile/api/AuthApi.kt\nmobile/api/HttpInterceptor.kt"]

    Backend["Spring Boot REST API\nwashmate/src/main/java"]

    Auth["Auth Module\nAuthController\nAuthService\nJWT + OTP + 2FA + Google OAuth"]
    Orders["Orders Module\nOrderController\nOrderService\nOrderRepository"]
    Payments["Payments Module\nPaymentService\nPayMongoService\nWebhookController"]
    Wallet["Wallet Module\nWalletController\nWalletService"]
    Subs["Subscription Module\nSubscriptionController\nSubscriptionService"]
    Services["Laundry Services Module\nServiceController\nWashServiceRepository"]
    Notif["Notifications Module\nNotificationController\nNotificationService"]
    Feedback["Feedback Module\nFeedbackController\nFeedbackService"]
    Admin["Admin Module\nAdminController"]

    DB["Supabase PostgreSQL\nusers, orders, services,\npayments, wallets,\nsubscriptions, notifications,\nfeedbacks"]
    PayMongo["PayMongo API\nCard and e-wallet checkout"]
    Gmail["Gmail SMTP\nEmail OTP and 2FA codes"]
    Google["Google OAuth\nWeb callback and mobile ID token"]
    Redis["Redis\nRate limiting and temporary auth data"]

    UserCustomer --> Web
    UserShop --> Web
    UserAdmin --> Web
    UserCustomer --> Mobile

    Web --> Axios
    Mobile --> Retrofit

    Axios -->|Bearer JWT + JSON REST| Backend
    Retrofit -->|Bearer JWT + JSON REST| Backend

    Backend --> Auth
    Backend --> Orders
    Backend --> Payments
    Backend --> Wallet
    Backend --> Subs
    Backend --> Services
    Backend --> Notif
    Backend --> Feedback
    Backend --> Admin

    Auth --> DB
    Orders --> DB
    Payments --> DB
    Wallet --> DB
    Subs --> DB
    Services --> DB
    Notif --> DB
    Feedback --> DB
    Admin --> DB

    Payments --> PayMongo
    Wallet --> PayMongo
    Subs --> PayMongo
    Auth --> Gmail
    Auth --> Google
    Auth --> Redis
```

## Recording Cue

Show this diagram while explaining:

- React web and Android mobile are the clients.
- Spring Boot is the central REST API.
- PostgreSQL stores persistent data.
- PayMongo, Gmail SMTP, Google OAuth, and Redis are external/infrastructure integrations.
- Backend modules are separated by feature and follow controller-service-repository structure.
