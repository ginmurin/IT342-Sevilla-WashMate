package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.features.subscriptions.Subscription;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscription;
import edu.cit.sevilla.washmate.features.subscriptions.SubscriptionRepository;
import edu.cit.sevilla.washmate.features.subscriptions.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.wallet.Wallet;
import edu.cit.sevilla.washmate.features.payments.Payment;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionRenewalService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Runs every hour to process subscription renewals.
     * FREE plans auto-renew without payment.
     * PREMIUM plans are marked as EXPIRED and require manual renewal.
     */
    @Scheduled(cron = "0 0 * * * *")  // Runs every hour at the top of the hour
    public void processRenewals() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // Find subscriptions that have expired
            List<UserSubscription> expiredSubs = userSubscriptionRepository
                    .findByStatusAndExpiryDateBefore("ACTIVE", now);

            if (expiredSubs.isEmpty()) {
                log.debug("No subscriptions to renew at {}", now);
                return;
            }

            log.info("Processing {} expired subscriptions at {}", expiredSubs.size(), now);

            for (UserSubscription sub : expiredSubs) {
                Subscription plan = sub.getSubscription();

                if (plan.getPlanPrice().compareTo(BigDecimal.ZERO) == 0) {
                    // FREE plan - auto-renew without payment
                    sub.setExpiryDate(now.plusMonths(1));
                    userSubscriptionRepository.save(sub);
                    log.info("Auto-renewed FREE subscription for user {}, new expiry: {}",
                            sub.getUser().getUserId(), sub.getExpiryDate());
                } else {
                    // PREMIUM plan - mark as expired (user must manually renew with payment)
                    // Future enhancement: auto-charge from wallet
                    sub.setStatus("EXPIRED");
                    userSubscriptionRepository.save(sub);
                    log.info("Marked PREMIUM subscription EXPIRED for user {}, requires manual renewal",
                            sub.getUser().getUserId());
                }
            }
        } catch (Exception e) {
            log.error("Error processing subscription renewals", e);
        }
    }
}

