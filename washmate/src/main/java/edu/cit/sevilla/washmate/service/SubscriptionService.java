package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.dto.SubscriptionDTO;
import edu.cit.sevilla.washmate.entity.Subscription;
import edu.cit.sevilla.washmate.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    /** Returns the FREE plan, creating it if it doesn't exist yet. */
    public Subscription getOrCreateFreePlan() {
        return subscriptionRepository.findByPlanType("FREE")
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.builder()
                                .planType("FREE")
                                .planPrice(BigDecimal.ZERO)
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
}
