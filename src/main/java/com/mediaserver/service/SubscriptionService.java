package com.mediaserver.service;

import com.mediaserver.model.Subscription;
import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.model.User;
import com.mediaserver.repository.SubscriptionPlanRepository;
import com.mediaserver.repository.SubscriptionRepository;
import com.mediaserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service to manage subscription transitions and ensure ONE active subscription per user.
 * Handles plan changes, trial creation, and subscription history.
 */
@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a FREE trial subscription for new users.
     * This creates the initial ACTIVE subscription with 30-day trial.
     * The FREE plan can only be used once per user.
     */
    public Subscription createFreeTrialSubscription(String userId) {
        // Check if user already has any subscription
        Optional<Subscription> existing = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (existing.isPresent()) {
            throw new IllegalStateException("User already has an active subscription");
        }

        // Check if user has already used the free plan
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        if (user.getHasUsedFreePlan() != null && user.getHasUsedFreePlan()) {
            throw new IllegalStateException("User has already used the FREE plan and cannot use it again");
        }

        // Get FREE plan
        Optional<SubscriptionPlan> freePlanOpt = subscriptionPlanRepository.findByPlanCode("free");
        if (freePlanOpt.isEmpty()) {
            throw new IllegalStateException("FREE plan not found");
        }

        SubscriptionPlan freePlan = freePlanOpt.get();

        // Create FREE trial subscription
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPlanId(freePlan.getId());
        subscription.setPlanType("FREE");
        subscription.setStatus("ACTIVE");
        subscription.setIsTrial(true);
        subscription.setTrialEndsAt(LocalDateTime.now().plusDays(30)); // 30-day trial
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusDays(30));
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());

        // Mark that user has used the free plan
        user.setHasUsedFreePlan(Boolean.TRUE);
        userRepository.save(user);

        return subscriptionRepository.save(subscription);
    }

    /**
     * Change user's plan - expires old subscription and creates new ACTIVE one.
     * This ensures only ONE active subscription per user.
     */
    public Subscription changePlan(String userId, String newPlanId, String PriceId, String SubscriptionId) {
        // Get new plan details
        Optional<SubscriptionPlan> newPlanOpt = subscriptionPlanRepository.findByHexId(newPlanId);
        if (newPlanOpt.isEmpty()) {
            throw new IllegalArgumentException("New plan not found: " + newPlanId);
        }
        SubscriptionPlan newPlan = newPlanOpt.get();

        // Expire current active subscription
        Optional<Subscription> currentActive = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (currentActive.isPresent()) {
            Subscription oldSub = currentActive.get();
            oldSub.setStatus("EXPIRED");
            oldSub.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(oldSub);
        }

        // Create new ACTIVE subscription
        Subscription newSubscription = new Subscription();
        newSubscription.setUserId(userId);
        newSubscription.setPlanId(newPlan.getId());
        newSubscription.setPlanType(newPlan.getName().toUpperCase().contains("FREE") ? "FREE" :
                                   newPlan.getName().toUpperCase().contains("BASIC") ? "BASIC" :
                                   newPlan.getName().toUpperCase().contains("ADVANCED") ? "ADVANCED" : "UNKNOWN");
        newSubscription.setStatus("ACTIVE");
        newSubscription.setPreviousPlanType(currentActive.isPresent() ? currentActive.get().getPlanType() : null);
        newSubscription.setIsTrial(false); // Paid plans are not trials
        newSubscription.setSubscriptionId(SubscriptionId);
        newSubscription.setPriceId(PriceId);
        newSubscription.setCurrentPeriodStart(LocalDateTime.now());

        // Set period end based on interval
        LocalDateTime periodEnd;
        if ("MONTHLY".equals(newPlan.getInterval())) {
            periodEnd = LocalDateTime.now().plusMonths(1);
        } else if ("YEARLY".equals(newPlan.getInterval())) {
            periodEnd = LocalDateTime.now().plusYears(1);
        } else {
            periodEnd = LocalDateTime.now().plusMonths(1); // Default to monthly
        }
        newSubscription.setCurrentPeriodEnd(periodEnd);
        newSubscription.setCreatedAt(LocalDateTime.now());
        newSubscription.setUpdatedAt(LocalDateTime.now());

        return subscriptionRepository.save(newSubscription);
    }

    /**
     * Get the ONE active subscription for a user.
     * Returns null if no active subscription exists.
     */
    public Subscription getActiveSubscription(String userId) {
        Optional<Subscription> active = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");
        return active.orElse(null);
    }

    /**
     * Get subscription history for a user (all subscriptions ordered by creation date desc).
     */
    public List<Subscription> getSubscriptionHistory(String userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Check if user has an active subscription.
     */
    public boolean hasActiveSubscription(String userId) {
        return getActiveSubscription(userId) != null;
    }

    /**
     * Cancel user's active subscription.
     */
    public boolean cancelSubscription(String userId) {
        Optional<Subscription> active = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (active.isPresent()) {
            Subscription sub = active.get();
            sub.setStatus("CANCELLED");
            sub.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            return true;
        }
        return false;
    }
}