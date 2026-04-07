package edu.cit.sevilla.washmate.singleton;

import edu.cit.sevilla.washmate.entity.Subscription;
import edu.cit.sevilla.washmate.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Singleton cache for subscription plans.
 *
 * Caches subscription plans (FREE, BASIC, PREMIUM, VIP) to avoid repeated database lookups.
 * Thread-safe implementation using ReadWriteLock for concurrent access.
 *
 * Benefits:
 * - Reduces database queries for frequently accessed plan data
 * - Thread-safe access for concurrent requests
 * - Single point of access for plan information
 * - Easy to invalidate cache if plans change
 *
 * Lifecycle:
 * - Initialized on application startup via @PostConstruct
 * - All plans cached in memory
 * - Thread-safe read operations
 * - Optional cache refresh via invalidate() method
 */
@Component
@RequiredArgsConstructor
public class SubscriptionPlanCache {

    private final SubscriptionRepository subscriptionRepository;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String, Subscription> planCache = new HashMap<>();

    /**
     * Initialize cache on application startup.
     * Loads all subscription plans from database into memory.
     */
    @PostConstruct
    public void initializeCache() {
        lock.writeLock().lock();
        try {
            List<Subscription> plans = subscriptionRepository.findAll();
            planCache.clear();
            for (Subscription plan : plans) {
                planCache.put(plan.getPlanType().toUpperCase(), plan);
            }
            System.out.println("DEBUG: SubscriptionPlanCache initialized with " + planCache.size() + " plans");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get a subscription plan by type from cache.
     * @param planType The plan type (FREE, BASIC, PREMIUM, VIP)
     * @return The Subscription plan, or null if not found
     */
    public Subscription getPlanByType(String planType) {
        lock.readLock().lock();
        try {
            return planCache.get(planType.toUpperCase());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get all cached subscription plans.
     * @return Unmodifiable map of cached plans
     */
    public Map<String, Subscription> getAllPlans() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new HashMap<>(planCache));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Invalidate cache and reload from database.
     * Call this if subscription plans are modified at runtime.
     */
    public void invalidateCache() {
        lock.writeLock().lock();
        try {
            initializeCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Check if plan exists in cache.
     * @param planType The plan type to check
     * @return true if plan exists in cache
     */
    public boolean hasPlan(String planType) {
        lock.readLock().lock();
        try {
            return planCache.containsKey(planType.toUpperCase());
        } finally {
            lock.readLock().unlock();
        }
    }
}
