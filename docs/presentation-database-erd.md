# WashMate Database ERD for Presentation

Use this ERD for the presentation. It is aligned with the Supabase tables shown in your screenshots.

```mermaid
erDiagram
    users ||--o{ addresses : owns
    users ||--o{ orders : places
    users ||--|| wallets : owns
    users ||--o{ notifications : receives
    users ||--o{ feedbacks : writes
    users ||--o{ verification_codes : receives
    users ||--o{ user_subscriptions : subscribes

    services ||--o{ service_variants : has
    services ||--o{ order_services : selected_in

    orders ||--o{ order_services : contains
    orders ||--o{ feedbacks : receives
    orders }o--|| addresses : pickup_address
    orders }o--|| addresses : delivery_address

    wallets ||--o{ wallet_transactions : records
    subscriptions ||--o{ user_subscriptions : plan_for

    payments ||..o{ orders : reference_order
    payments ||..o{ wallet_transactions : reference_wallet_topup
    payments ||..o{ user_subscriptions : reference_subscription

    users {
        int8 user_id PK
        timestamp created_at
        varchar email
        bool email_verified
        varchar oauth_id
        varchar oauth_provider
        varchar password_hash
        varchar phone_number
        varchar role
        varchar status
        bool two_factor_enabled
        timestamp updated_at
        varchar username
        varchar first_name
        varchar last_name
    }

    addresses {
        int8 address_id PK
        varchar city
        timestamp created_at
        text full_address
        bool is_default
        varchar label
        numeric latitude
        numeric longitude
        timestamp updated_at
        int8 user_id FK
    }

    services {
        int8 service_id PK
        numeric base_price_per_unit
        timestamp created_at
        text description
        bool is_active
        varchar service_name
        varchar unit_type
        timestamp updated_at
        bool has_variants
        bool is_auto_selected
    }

    service_variants {
        int8 variant_id PK
        timestamp created_at
        int4 display_order
        bool is_active
        varchar variant_name
        numeric variant_price
        int8 service_id FK
    }

    orders {
        int8 order_id PK
        timestamp created_at
        timestamp delivery_schedule
        bool is_rush_order
        varchar order_number
        timestamp pickup_schedule
        text special_instructions
        varchar status
        numeric total_amount
        numeric total_weight
        timestamp updated_at
        int8 customer_id FK
        int8 delivery_address_id FK
        int8 pickup_address_id FK
        numeric delivery_fee
    }

    order_services {
        int8 order_service_id PK
        numeric quantity
        numeric subtotal
        numeric unit_price
        int8 order_id FK
        int8 service_id FK
    }

    payments {
        int8 payment_id PK
        numeric amount
        timestamp created_at
        timestamp payment_date
        varchar payment_method
        varchar payment_status
        timestamp updated_at
        varchar paymongo_payment_intent_id
        int8 reference_id
        varchar reference_type
        varchar transaction_id
    }

    wallets {
        int8 wallet_id PK
        numeric available_balance
        timestamp created_at
        varchar currency
        timestamp updated_at
        int8 user_id FK
    }

    wallet_transactions {
        int8 transaction_id PK
        numeric amount
        timestamp created_at
        text description
        int8 reference_id
        varchar reference_type
        varchar status
        varchar transaction_type
        timestamp updated_at
        int8 wallet_id FK
        numeric balance_after
        numeric balance_before
    }

    subscriptions {
        int8 subscription_id PK
        timestamp created_at
        int4 orders_included
        numeric plan_price
        varchar plan_type
        timestamp updated_at
        int4 discount_percentage
        int4 orders_limit
    }

    user_subscriptions {
        int8 user_subscription_id PK
        timestamp created_at
        date expiry_date
        date start_date
        varchar status
        int8 payment_id
        int8 subscription_id FK
        int8 user_id FK
        timestamp updated_at
        varchar paymongo_payment_id
    }

    notifications {
        int8 notification_id PK
        timestamp created_at
        bool is_read
        bool is_sent
        text message
        varchar notification_type
        int8 reference_id
        varchar reference_type
        timestamp sent_at
        varchar title
        int8 user_id FK
    }

    feedbacks {
        int8 feedback_id PK
        text admin_response
        text comment_text
        timestamp created_at
        varchar feedback_type
        int4 star_rating
        timestamp updated_at
        int8 customer_id FK
        int8 order_id FK
    }

    verification_codes {
        int8 id PK
        varchar code
        varchar code_type
        timestamp created_at
        timestamp expires_at
        int4 failed_attempts
        bool is_used
        int8 user_id FK
    }
```

## Recording Cue

Use this diagram after the architecture diagram. Emphasize:

- `users` connects to customer data, orders, wallets, notifications, verification codes, subscriptions, and feedback.
- `orders` connects to selected services through `order_services`.
- `payments` uses `reference_type` and `reference_id` so it can support order payments, subscription payments, and wallet top-ups.
- `feedbacks` stores only the delivered order review data: rating, comment, optional admin response, customer, and order.
