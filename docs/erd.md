erDiagram
  users ||--o{ orders : places
  users ||--o{ addresses : has
  users ||--o{ notifications : receives
  users ||--o{ user_subscriptions : "subscribes to"
  users ||--|| wallets : owns
  users ||--o{ feedbacks : writes
 
  wallets ||--o{ wallet_transactions : records
 
  orders ||--o{ order_services : contains
  orders ||--o{ feedbacks : has
  orders }o--|| addresses : "pickup address"
  orders }o--|| addresses : "delivery address"
 
  services ||--o{ order_services : "included in"
 
  subscriptions ||--o{ user_subscriptions : "has subscribers"
 
  %% POLYMORPHIC RELATIONSHIPS (no direct FK, connected via reference_type + reference_id)
  payments ||..o{ orders : "references via polymorphic"
  payments ||..o{ user_subscriptions : "references via polymorphic"
  payments ||..o{ wallet_transactions : "references via polymorphic"
 
  users {
    bigint user_id PK
    varchar email UK
    varchar username UK
    varchar first_name
    varchar last_name
    varchar phone_number
    varchar password_hash
    varchar oauth_provider
    varchar oauth_id UK
    varchar role "CUSTOMER, SHOP_OWNER, ADMIN"
    varchar status "ACTIVE, INACTIVE, DEACTIVATED"
    boolean email_verified
    boolean two_factor_enabled
    timestamp created_at
    timestamp updated_at
  }

  orders {
    bigint order_id PK
    bigint customer_id FK
    bigint pickup_address_id FK
    bigint delivery_address_id FK
    varchar order_number UK
    varchar status "PENDING, CONFIRMED, PICKED_UP, IN_PROCESS, READY, DELIVERED, CANCELLED"
    numeric total_amount
    numeric total_weight
    numeric rush_fee
    boolean is_rush_order
    text special_instructions
    timestamp pickup_schedule
    timestamp delivery_schedule
    timestamp created_at
    timestamp updated_at
  }
 
  user_subscriptions {
    bigint user_subscription_id PK
    bigint user_id FK
    bigint subscription_id FK
    varchar status "ACTIVE, EXPIRED, CANCELLED"
    date start_date
    date end_date
    boolean auto_renew
    varchar paymongo_payment_id "PayMongo reference only"
    timestamp created_at
    timestamp updated_at
  }
 
  payments {
    bigint payment_id PK
    varchar reference_type "ORDER | SUBSCRIPTION | WALLET_TOPUP"
    bigint reference_id "ID of order or user_subscription or wallet_transaction"
    numeric amount
    varchar payment_method "GCASH | MAYA | CARD | GRABPAY | WALLET"
    varchar payment_status "PENDING | PROCESSING | COMPLETED | FAILED | REFUNDED"
    varchar paymongo_payment_intent_id UK
    varchar transaction_id
    timestamp payment_date
    timestamp created_at
    timestamp updated_at
  }

  %% Other entities remain the same...
  addresses {
    bigint address_id PK
    bigint user_id FK
    varchar label "HOME, OFFICE, OTHER"
    text full_address
    varchar city
    numeric latitude
    numeric longitude
    boolean is_default
    timestamp created_at
    timestamp updated_at
  }
 
  notifications {
    bigint notification_id PK
    bigint user_id FK
    varchar notification_type "ORDER_UPDATE, PAYMENT, PROMO, FEEDBACK, SYSTEM"
    varchar title
    text message
    varchar reference_type "ORDER, PAYMENT, FEEDBACK"
    bigint reference_id
    boolean is_read
    boolean is_sent
    timestamp sent_at
    timestamp created_at
  }
 
  subscriptions {
    bigint subscription_id PK
    varchar plan_type "FREE, PREMIUM"
    numeric plan_price "0 for FREE, 299 for PREMIUM"
    int orders_included "0 means unlimited"
    int discount_percentage "0 for FREE, 15 for PREMIUM"
    text description
    timestamp created_at
    timestamp updated_at
  }
 
  wallets {
    bigint wallet_id PK
    bigint user_id FK
    numeric available_balance
    varchar currency "PHP"
    timestamp created_at
    timestamp updated_at
  }
 
  wallet_transactions {
    bigint transaction_id PK
    bigint wallet_id FK
    numeric amount
    varchar transaction_type "TOP_UP, DEDUCTION, REFUND, REWARD"
    varchar reference_type "ORDER, PAYMENT, PROMOTION"
    bigint reference_id
    varchar status "PENDING, COMPLETED, FAILED"
    text description
    numeric balance_before
    numeric balance_after
    timestamp created_at
    timestamp updated_at
  }
 
  services {
    bigint service_id PK
    varchar service_name "Wash and Fold, Wash and Iron, Dry Clean"
    varchar unit_type "KG, PIECE"
    numeric price_per_unit "Shop owners can edit this"
    text description
    boolean is_active
    timestamp created_at
    timestamp updated_at
  }
 
  order_services {
    bigint order_service_id PK
    bigint order_id FK
    bigint service_id FK
    numeric quantity
    numeric unit_price "Price at time of order"
    numeric subtotal
  }
 
  feedbacks {
    bigint feedback_id PK
    bigint order_id FK
    bigint customer_id FK
    int star_rating "1 to 5"
    varchar feedback_type "PRAISE, SUGGESTION, COMPLAINT"
    text comment_text
    text admin_response
    varchar status "PENDING, REVIEWED, RESOLVED"
    timestamp created_at
    timestamp updated_at
  }