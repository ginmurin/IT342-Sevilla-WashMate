package edu.cit.sevilla.washmate.controller;

import edu.cit.sevilla.washmate.dto.SubscriptionDTO;
import edu.cit.sevilla.washmate.dto.UserSubscriptionDTO;
import edu.cit.sevilla.washmate.entity.Subscription;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.UserSubscription;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    /**
     * Get current user's active subscription.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMySubscription(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        User user = userRepository.findByOauthId(oauthId)
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
     * Initiate subscription upgrade. Returns upgrade details including amount.
     */
    @PostMapping("/upgrade/{planType}")
    public ResponseEntity<Map<String, Object>> initiateUpgrade(
            @PathVariable String planType,
            @AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSubscription newSub = subscriptionService.initiateSubscriptionUpgrade(user.getUserId(), user, planType);

        Map<String, Object> response = Map.of(
                "userSubscriptionId", newSub.getUserSubscriptionId(),
                "planType", newSub.getSubscription().getPlanType(),
                "amount", newSub.getSubscription().getPlanPrice(),
                "expiryDate", newSub.getExpiryDate()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Confirm subscription upgrade by linking payment.
     */
    @PostMapping("/confirm-upgrade/{userSubscriptionId}/{paymentId}")
    public ResponseEntity<UserSubscriptionDTO> confirmUpgrade(
            @PathVariable Long userSubscriptionId,
            @PathVariable Long paymentId) {
        UserSubscription confirmed = subscriptionService.confirmSubscriptionUpgrade(userSubscriptionId, paymentId);
        return ResponseEntity.ok(subscriptionService.toUserSubscriptionDTO(confirmed));
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
