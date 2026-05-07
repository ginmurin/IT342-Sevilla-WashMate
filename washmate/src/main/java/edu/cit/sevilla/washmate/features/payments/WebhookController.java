package edu.cit.sevilla.washmate.features.payments;

import edu.cit.sevilla.washmate.features.orders.Order;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscription;
import edu.cit.sevilla.washmate.features.wallet.WalletTransaction;
import edu.cit.sevilla.washmate.features.orders.OrderRepository;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import edu.cit.sevilla.washmate.features.wallet.WalletTransactionRepository;
import edu.cit.sevilla.washmate.features.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;
import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.wallet.Wallet;

/**
 * Webhook Controller for PayMongo
 * Handles payment confirmation webhooks from PayMongo.
 * Updates payment status and related Order/Subscription/Wallet records.
 */
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PayMongoService payMongoService;
    private final PaymentService paymentService;
    private final WalletService walletService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Receive PayMongo webhook for payment events.
     * Validates webhook signature and processes payment status changes.
     */
    @PostMapping("/paymongo")
    public ResponseEntity<Map<String, String>> handlePayMongoWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Paymongo-Signature", required = false) String paymongoSignature) {

        try {
            log.info("📨 Received PayMongo webhook");

            // Verify webhook signature
            if (paymongoSignature != null && !paymongoSignature.isEmpty()) {
                boolean isValid = payMongoService.verifyWebhookSignature(rawPayload, paymongoSignature);
                if (!isValid) {
                    log.warn("❌ Webhook signature verification failed");
                    return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
                }
            }

            // Parse webhook payload
            Map<String, Object> payload = objectMapper.readValue(rawPayload, Map.class);
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            if (data == null) {
                log.warn("⚠️  Invalid webhook payload - no data field");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload"));
            }

            String eventType = (String) data.get("type");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");

            if ("payment_intent.succeeded".equals(eventType) || "payment.success".equals(eventType)) {
                handlePaymentSuccess(attributes);
            } else if ("payment_intent.failed".equals(eventType) || "payment.failed".equals(eventType)) {
                handlePaymentFailure(attributes);
            } else if ("source.chargeable".equals(eventType)) {
                handleSourceChargeable(attributes);
            }

            return ResponseEntity.ok(Map.of("success", "true"));

        } catch (Exception e) {
            log.error("❌ Error processing PayMongo webhook", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Handle successful payment event.
     * Updates payment status and related order/subscription/wallet.
     */
    private void handlePaymentSuccess(Map<String, Object> attributes) {
        try {
            String paymentIntentId = (String) attributes.get("id");
            String status = (String) attributes.get("status");

            log.info("✅ Payment succeeded: {}", paymentIntentId);

            // Find payment by PayMongo intent ID
            Optional<Payment> paymentOpt = paymentRepository.findByPaymongoPaymentIntentId(paymentIntentId);

            if (paymentOpt.isEmpty()) {
                log.warn("⚠️  No payment found for intent ID: {}", paymentIntentId);
                return;
            }

            Payment payment = paymentOpt.get();

            // Update payment status
            payment.setPaymentStatus("COMPLETED");
            payment.setPaymongoPaymentIntentId(paymentIntentId);
            payment.setPaymentDate(java.time.LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("💾 Updated payment {} to COMPLETED", payment.getPaymentId());

            // Update related entity based on reference type
            handlePaymentCompletion(payment);

        } catch (Exception e) {
            log.error("❌ Error handling payment success", e);
        }
    }

    /**
     * Handle failed payment event.
     * Updates payment status to FAILED.
     */
    private void handlePaymentFailure(Map<String, Object> attributes) {
        try {
            String paymentIntentId = (String) attributes.get("id");

            log.warn("❌ Payment failed: {}", paymentIntentId);

            // Find payment by PayMongo intent ID
            Optional<Payment> paymentOpt = paymentRepository.findByPaymongoPaymentIntentId(paymentIntentId);

            if (paymentOpt.isEmpty()) {
                log.warn("⚠️  No payment found for intent ID: {}", paymentIntentId);
                return;
            }

            Payment payment = paymentOpt.get();

            // Update payment status
            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);

            log.info("💾 Updated payment {} to FAILED", payment.getPaymentId());

        } catch (Exception e) {
            log.error("❌ Error handling payment failure", e);
        }
    }

    /**
     * Handle source chargeable event (for e-wallet sources).
     */
    private void handleSourceChargeable(Map<String, Object> attributes) {
        try {
            String sourceId = (String) attributes.get("id");

            log.info("💳 Source chargeable: {}", sourceId);

            // Find payment by source ID (if we're storing it)
            // For now, we'll just log it as webhooks for sources are typically handled differently

            log.info("ℹ️  Source {} is chargeable", sourceId);

        } catch (Exception e) {
            log.error("❌ Error handling source chargeable", e);
        }
    }

    /**
     * Handle payment completion - update related order/subscription/wallet.
     */
    private void handlePaymentCompletion(Payment payment) {
        try {
            String referenceType = payment.getReferenceType();
            Long referenceId = payment.getReferenceId();

            switch (referenceType) {
                case "ORDER":
                    // Update order status to CONFIRMED
                    Optional<Order> orderOpt = orderRepository.findById(referenceId);
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        order.setStatus("CONFIRMED");
                        orderRepository.save(order);
                        log.info("📦 Updated order {} to CONFIRMED", referenceId);
                    }
                    break;

                case "SUBSCRIPTION":
                    // Update subscription status to ACTIVE
                    Optional<UserSubscription> subOpt = userSubscriptionRepository.findById(referenceId);
                    if (subOpt.isPresent()) {
                        UserSubscription sub = subOpt.get();
                        sub.setStatus("ACTIVE");
                        userSubscriptionRepository.save(sub);
                        log.info("🎯 Updated subscription {} to ACTIVE", referenceId);
                    }
                    break;

                case "WALLET_TOPUP":
                    // Update wallet transaction and wallet balance
                    Optional<WalletTransaction> txnOpt = walletTransactionRepository.findById(referenceId);
                    if (txnOpt.isPresent()) {
                        WalletTransaction transaction = txnOpt.get();
                        transaction.setStatus("COMPLETED");
                        walletTransactionRepository.save(transaction);

                        // Add balance to wallet
                        walletService.addToWallet(
                                transaction.getWallet().getUser().getUserId(),
                                transaction.getAmount()
                        );
                        log.info("💰 Completed wallet top-up transaction {} with amount {}", referenceId, transaction.getAmount());
                    }
                    break;

                default:
                    log.warn("⚠️  Unknown reference type: {}", referenceType);
            }

        } catch (Exception e) {
            log.error("❌ Error handling payment completion", e);
        }
    }
}

