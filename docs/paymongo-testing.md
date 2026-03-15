# PayMongo Test Guide

> Requires test keys in `web/.env`:
> `VITE_PAYMONGO_SECRET_KEY=sk_test_...`
> `VITE_PAYMONGO_PUBLIC_KEY=pk_test_...`

---

## Credit / Debit Card

| Scenario | Card Number | Expiry | CVV |
|----------|-------------|--------|-----|
| **Success** | `4343 4343 4343 4345` (Visa) | Any future, e.g. `12/25` | Any 3 digits |
| **Success** | `5555 4444 3333 1111` (Mastercard) | Any future | Any 3 digits |
| **Failed** (declined) | `4111 1111 1111 1111` | Any future | Any 3 digits |
| **Failed** (insufficient funds) | `4000 0000 0000 0002` | Any future | Any 3 digits |

Use any cardholder name.

---

## GCash / Maya / GrabPay (E-Wallets)

1. Select the payment method and click **Confirm & Pay**
2. PayMongo redirects you to a **test simulation page**
3. Click **"Success"** to simulate a successful payment → redirected to `/payment/success`
4. Click **"Failed"** to simulate a failed payment → redirected to `/payment/error`

No real account or phone number needed in test mode.

---

## Subscriptions

PayMongo subscriptions use the **Billing API**. For test mode:

1. Create a test customer and attach a test card (`4343 4343 4343 4345`)
2. Use `POST /v1/subscriptions` with `interval: month` or `interval: year`
3. Test renewal events are triggered via **webhooks** — use [ngrok](https://ngrok.com) to expose your local server, then register the URL in the PayMongo dashboard under **Webhooks**
4. Trigger test events from the dashboard: `payment.paid`, `payment.failed`, `subscription.created`

> For WashMate's current scope, subscriptions are not yet wired to the backend. This section is for future reference.

---

## Useful Links

- [PayMongo Test Cards](https://developers.paymongo.com/docs/testing)
- [PayMongo Dashboard (Test Mode)](https://dashboard.paymongo.com)
