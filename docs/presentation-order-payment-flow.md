# WashMate Order, Payment, Notification, and Feedback Flow

Use this diagram when explaining component interaction and data flow. This version uses a Mermaid flowchart because it is more reliable across Markdown previewers than a sequence diagram with REST paths.

```mermaid
flowchart TD
    A["Customer selects laundry services<br/>Web or Android app"]
    B["Client sends create order request<br/>POST /api/orders/create"]
    C["Spring Security validates Bearer JWT"]
    D["OrderController receives request"]
    E["OrderService calculates totals<br/>and builds Order entity"]
    F["OrderRepository saves order<br/>and order_services rows"]
    G["PostgreSQL stores order data"]

    H["Customer chooses payment method"]
    I["Client sends payment process request<br/>POST /api/orders/:id/payment/process"]
    J["OrderController verifies ownership"]
    K["Payment record is created"]

    L{"Payment method?"}
    M["Card payment<br/>PayMongo checkout session"]
    N["E-wallet payment<br/>PayMongo source"]
    O["Wallet payment<br/>WalletService deducts balance"]

    P["PayMongo returns checkout URL<br/>or wallet transaction is recorded"]
    Q["Payment status and order status<br/>are updated in PostgreSQL"]

    R["Shop owner or admin updates<br/>order status to DELIVERED"]
    S["NotificationService creates<br/>feedback request notification"]
    T["Customer submits feedback<br/>POST /api/feedbacks/orders/:id"]
    U["FeedbackService validates delivered order<br/>and saves rating/comment"]
    V["Web and mobile fetch feedback<br/>GET /api/feedbacks/orders/:id"]
    W["Order detail page displays<br/>feedback rating and comment"]

    A --> B --> C --> D --> E --> F --> G
    G --> H --> I --> J --> K --> L
    L --> M
    L --> N
    L --> O
    M --> P
    N --> P
    O --> P
    P --> Q --> R --> S --> T --> U --> V --> W
```

## Recording Cue

Show this diagram before or during the demo. Then demonstrate:

- Creating or viewing an order.
- Processing or showing payment.
- Updating or viewing delivered status.
- Submitting or displaying feedback.

Connect each visible UI action to the backend endpoint and database action.
