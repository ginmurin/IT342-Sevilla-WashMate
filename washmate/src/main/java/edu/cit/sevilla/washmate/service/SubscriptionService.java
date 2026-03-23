package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.SubscriptionDTO;
import edu.cit.sevilla.washmate.dto.UserSubscriptionDTO;
import edu.cit.sevilla.washmate.entity.Subscription;
import edu.cit.sevilla.washmate.entity.User;
import edu.cit.sevilla.washmate.entity.UserSubscription;
import edu.cit.sevilla.washmate.repository.SubscriptionRepository;
import edu.cit.sevilla.washmate.repository.UserRepository;
import edu.cit.sevilla.washmate.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;

    /** Returns the FREE plan, creating it if it doesn't exist yet. */
    public Subscription getOrCreateFreePlan() {
        return subscriptionRepository.findByPlanType("FREE")
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.builder()
                                .planType("FREE")
                                .planPrice(BigDecimal.ZERO)
                                .ordersIncluded(null) // Unlimited orders
                                .discountPercentage(0) // No discount
                                .build()
                ));
    }

    public Optional<SubscriptionDTO> getSubscriptionDTO(Subscription subscription) {
        if (subscription == null) return Optional.empty();
        return Optional.of(toDTO(subscription));
    }

    private SubscriptionDTO toDTO(Subscription s) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(s.getSubscriptionId());
        dto.setPlanType(s.getPlanType());
        dto.setPlanPrice(s.getPlanPrice());
        dto.setOrdersIncluded(s.getOrdersIncluded());
        dto.setDiscountPercentage(s.getDiscountPercentage());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }

    // ===== New methods for subscription upgrade/renewal =====

    /** Returns all available subscription plans. */
    public List<Subscription> getAllPlans() {
        return subscriptionRepository.findAll();
    }

    /** Initiates a subscription upgrade for a user. */
    public UserSubscription initiateSubscriptionUpgrade(Long userId, User user, String newPlanType) {
        Subscription newPlan = subscriptionRepository.findByPlanType(newPlanType)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + newPlanType));

        // Mark any existing ACTIVE subscription as EXPIRED
        userSubscriptionRepository.findByUserUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("EXPIRED");
                    userSubscriptionRepository.save(existing);
                });

        // Create new subscription
        LocalDateTime now = LocalDateTime.now();
        UserSubscription newSub = UserSubscription.builder()
                .user(user)
                .subscription(newPlan)
                .startDate(now)
                .expiryDate(now.plusMonths(1))
                .status("ACTIVE")
                .build();

        return userSubscriptionRepository.save(newSub);
    }

    /** Confirms subscription upgrade by linking the payment. */
    public UserSubscription confirmSubscriptionUpgrade(Long userSubscriptionId, Long paymentId) {
        UserSubscription sub = userSubscriptionRepository.findById(userSubscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        // Link the payment (paymentId gets converted to Payment entity by caller if needed)
        // For now, we just mark it as confirmed by the presence of paymentId
        sub.setStatus("ACTIVE");
        return userSubscriptionRepository.save(sub);
    }

    /** Get current active subscription for a user. */
    public Optional<UserSubscription> getCurrentSubscription(Long userId) {
        return userSubscriptionRepository.findByUserUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE");
    }

    /** Get subscription history for a user. */
    public List<UserSubscription> getSubscriptionHistory(Long userId) {
        return userSubscriptionRepository.findByUserUserId(userId);
    }

    /** Convert UserSubscription to DTO. */
    public UserSubscriptionDTO toUserSubscriptionDTO(UserSubscription us) {
        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        dto.setUserSubscriptionId(us.getUserSubscriptionId());
        dto.setUserId(us.getUser().getUserId());
        dto.setSubscriptionId(us.getSubscription().getSubscriptionId());
        dto.setPlanType(us.getSubscription().getPlanType());
        dto.setPlanPrice(us.getSubscription().getPlanPrice());
        dto.setDiscountPercentage(us.getSubscription().getDiscountPercentage());
        dto.setStartDate(us.getStartDate());
        dto.setExpiryDate(us.getExpiryDate());
        dto.setStatus(us.getStatus());
        dto.setCreatedAt(us.getCreatedAt());
        return dto;
    }

    /** Initialize all existing users with FREE subscription if they don't have one. */
    public int initializeUsersWithFreeSubscription() {
        // Get or create the FREE plan
        Subscription freePlan = getOrCreateFreePlan();

        // Find all users
        List<User> allUsers = userRepository.findAll();
        int initializedCount = 0;

        LocalDateTime now = LocalDateTime.now();

        for (User user : allUsers) {
            // Check if user already has an active subscription
            Optional<UserSubscription> existingSubscription = getCurrentSubscription(user.getUserId());

            if (existingSubscription.isEmpty()) {
                // Create FREE subscription for this user
                UserSubscription newSubscription = UserSubscription.builder()
                        .user(user)
                        .subscription(freePlan)
                        .startDate(now)
                        .expiryDate(now.plusYears(10)) // FREE plan never expires
                        .status("ACTIVE")
                        .build();

                userSubscriptionRepository.save(newSubscription);
                initializedCount++;
            }
        }

        return initializedCount;
    }
}
