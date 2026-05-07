package edu.cit.sevilla.washmate.controller;

import edu.cit.sevilla.washmate.dto.SubscriptionDTO;
import edu.cit.sevilla.washmate.dto.UserSubscriptionDTO;
import edu.cit.sevilla.washmate.entity.Payment;
import edu.cit.sevilla.washmate.entity.Subscription;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.UserSubscription;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.service.SubscriptionService;
import edu.cit.sevilla.washmate.service.PayMongoService;
import edu.cit.sevilla.washmate.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final PayMongoService payMongoService;
    private final PaymentService paymentService;

    /**
     * Get current user's active subscription.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMySubscription(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var current = subscriptionService.getCurrentSubscription(user.getUserId());
        if (current.isPresent()) {
            return ResponseEntity.ok(subscriptionService.toUserSubscriptionDTO(current.get()));
        }

        // No active subscription found
        return ResponseEntity.notFound().build();
    }

    /**
     * Get all available subscription plans.
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionDTO>> getAvailablePlans() {
        List<Subscription> plans = subscriptionService.getAllPlans();
        List<SubscriptionDTO> dtos = plans.stream()
                .map(subscriptionService::getSubscriptionDTO)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Initiate subscription upgrade. Returns upgrade details including paymentId.
     */
    @PostMapping("/upgrade/{planType}")
    public ResponseEntity<Map<String, Object>> initiateUpgrade(
            @PathVariable String planType,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            String oauthId = jwt.getSubject();
            User user = userRepository.findByOauthId(oauthId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("DEBUG - Initiating upgrade for user: " + user.getUserId() + ", plan: " + planType);

            UserSubscription newSub = subscriptionService.initiateSubscriptionUpgrade(user.getUserId(), user, planType);

            System.out.println("DEBUG - Created UserSubscription with ID: " + newSub.getUserSubscriptionId());
            System.out.println("DEBUG - UserSubscription details: " + newSub);

            // Get the Payment that was created during initiate
            java.util.List<Payment> payments = subscriptionService.getSubscriptionPayments(newSub.getUserSubscriptionId());
            Long paymentId = null;
            if (!payments.isEmpty()) {
                paymentId = payments.get(0).getPaymentId();
                System.out.println("DEBUG - Payment created with ID: " + paymentId);
            }

            Map<String, Object> response = Map.of(
                    "userSubscriptionId", newSub.getUserSubscriptionId(),
                    "paymentId", paymentId != null ? paymentId : "",
                    "planType", newSub.getSubscription().getPlanType(),
                    "amount", newSub.getSubscription().getPlanPrice(),
                    "expiryDate", newSub.getExpiryDate()
            );

            System.out.println("DEBUG - Response being sent: " + response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in initiateUpgrade: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initiate subscription upgrade: " + e.getMessage(), e);
        }
    }

    /**
     * Process subscription upgrade payment (handle PayMongo integration).
     * Backend handles all PayMongo API calls - frontend never sees payment details.
     */
    @PostMapping("/upgrade/{planType}/process")
    public ResponseEntity<Map<String, Object>> processUpgradePayment(
            @PathVariable String planType,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            Long userId = Long.parseLong(jwt.getSubject());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String paymentMethod = (String) request.get("paymentMethod");

            // Initiate subscription upgrade (creates PENDING payment)
            UserSubscription newSub = subscriptionService.initiateSubscriptionUpgrade(user.getUserId(), user, planType);
            java.util.List<Payment> payments = subscriptionService.getSubscriptionPayments(newSub.getUserSubscriptionId());
            Payment payment = payments.isEmpty() ? null : payments.get(0);

            if (payment == null) {
                throw new RuntimeException("Payment creation failed");
            }

            BigDecimal planPrice = newSub.getSubscription().getPlanPrice();

            // Handle payment based on method
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", payment.getPaymentId());
            response.put("userSubscriptionId", newSub.getUserSubscriptionId());
            response.put("planType", planType);
            response.put("amount", planPrice);
            response.put("paymentMethod", paymentMethod);

            switch (paymentMethod.toUpperCase()) {
                case "CARD":
                    // Create PayMongo payment intent for card
                    Map<String, String> intentResult = payMongoService.createPaymentIntent(planPrice);
                    response.put("paymentIntentId", intentResult.get("paymentIntentId"));
                    response.put("clientKey", intentResult.get("clientKey"));
                    break;

                case "GCASH":
                case "PAYMAYA":
                case "GRAB_PAY":
                    // Create PayMongo source for e-wallet
                    String sourceType = paymentMethod.toLowerCase();
                    String successUrl = "http://localhost:5173/subscription/upgrade-success?userSubscriptionId=" + newSub.getUserSubscriptionId() + "&paymentId=" + payment.getPaymentId();
                    String failureUrl = "http://localhost:5173/subscription/upgrade-error";

                    Map<String, String> sourceResult = payMongoService.createSource(sourceType, planPrice, successUrl, failureUrl);
                    response.put("checkoutUrl", sourceResult.get("checkoutUrl"));
                    response.put("sourceId", sourceResult.get("sourceId"));
                    break;

                case "WALLET":
                    // Wallet payment - no PayMongo needed
                    response.put("walletPayment", true);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process subscription upgrade payment: " + e.getMessage(), e);
        }
    }

    /**
     * Confirm subscription upgrade by linking payment.
     */
    @PostMapping("/confirm-upgrade/{userSubscriptionId}/{paymentId}")
    public ResponseEntity<UserSubscriptionDTO> confirmUpgrade(
            @PathVariable Long userSubscriptionId,
            @PathVariable String paymentId,
            @RequestParam(required = false, defaultValue = "CARD") String paymentMethod) {

        try {
            System.out.println("DEBUG - userSubscriptionId: " + userSubscriptionId);
            System.out.println("DEBUG - paymentId: " + paymentId);
            System.out.println("DEBUG - paymentMethod: " + paymentMethod);

            UserSubscription confirmed = subscriptionService.confirmSubscriptionUpgrade(userSubscriptionId, paymentId, paymentMethod);
            return ResponseEntity.ok(subscriptionService.toUserSubscriptionDTO(confirmed));
        } catch (Exception e) {
            System.err.println("ERROR in confirmUpgrade: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to confirm subscription upgrade: " + e.getMessage(), e);
        }
    }

    /**
     * Get subscription history for current user.
     */
    @GetMapping("/history")
    public ResponseEntity<List<UserSubscriptionDTO>> getSubscriptionHistory(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserSubscription> history = subscriptionService.getSubscriptionHistory(user.getUserId());
        List<UserSubscriptionDTO> dtos = history.stream()
                .map(subscriptionService::toUserSubscriptionDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Initialize all existing users with FREE subscription if they don't have one.
     * This is a one-time setup endpoint.
     */
    @PostMapping("/initialize-free-users")
    public ResponseEntity<Map<String, Object>> initializeFreeUsers() {
        int initializedCount = subscriptionService.initializeUsersWithFreeSubscription();

        Map<String, Object> response = Map.of(
            "message", "User initialization completed",
            "initializedUsersCount", initializedCount,
            "timestamp", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }
}
