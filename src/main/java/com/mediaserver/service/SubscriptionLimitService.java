


package com.mediaserver.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediaserver.model.Subscription;
import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.repository.ContentRepository;
import com.mediaserver.repository.DeviceRepository;
import com.mediaserver.repository.GroupRepository;
import com.mediaserver.repository.PlaylistRepository;
import com.mediaserver.repository.SubscriptionPlanRepository;
import com.mediaserver.repository.SubscriptionRepository;

/**
 * Service to check and enforce subscription limits for resources.
 * CRITICAL: This service looks for ACTIVE subscriptions ONLY.
 * Expired trials should NOT be counted as active.
 */
@Service
public class SubscriptionLimitService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private GroupRepository groupRepository;


    
    /**
     * Get the active subscription plan for a user.
     * PRIORITY ORDER:
     * 1. Check for PAID subscription with PriceId
     * 2. Check for ACTIVE trial subscription (if not expired)
     * 3. Return null if no valid subscription found
     */
    public SubscriptionPlan getUserPlanWithLimits(String userId) {
        System.out.println("\n🔍 [SubscriptionLimitService] Getting plan with limits for user: " + userId);
        
        // Find active subscription
        Optional<Subscription> activeSubscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");

        if (activeSubscription.isPresent()) {
            Subscription subscription = activeSubscription.get();
            System.out.println("✅ Found ACTIVE subscription: " + subscription.getId());
            System.out.println("   Status: " + subscription.getStatus());
            System.out.println("   Plan ID: " + subscription.getPlanId());
            System.out.println("   Stripe Price ID: " + subscription.getPriceId());
            System.out.println("   Is Trial: " + subscription.getIsTrial());
            
            // ✅ CRITICAL: Validate trial hasn't expired
            if (subscription.getIsTrial() != null && subscription.getIsTrial()) {
                if (subscription.getTrialEndsAt() != null && LocalDateTime.now().isAfter(subscription.getTrialEndsAt())) {
                    System.out.println("⏰ FREE TRIAL HAS EXPIRED for user " + userId);
                    System.out.println("   Trial ended at: " + subscription.getTrialEndsAt());
                    System.out.println("   Current time: " + LocalDateTime.now());
                    // Trial has expired - should not be used
                    return null;
                } else if (subscription.getTrialEndsAt() != null) {
                    long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getTrialEndsAt());
                    System.out.println("⏳ FREE TRIAL ACTIVE - Days remaining: " + daysLeft);
                }
            }
            
            // ✅ PRIORITY 1: Check if this is a PAID subscription with Stripe price ID
            if (subscription.getPriceId() != null && !subscription.getPriceId().isEmpty()) {
                System.out.println("🔐 Found PAID subscription with Stripe Price ID: " + subscription.getPriceId());
                
                // Try to find plan by Stripe price ID (for paid subscriptions)
                Optional<SubscriptionPlan> planByPriceId = subscriptionPlanRepository.findByPriceId(subscription.getPriceId());
                if (planByPriceId.isPresent()) {
                    SubscriptionPlan plan = planByPriceId.get();
                    System.out.println("✅ Using PAID plan: " + plan.getName() + " (ID: " + plan.getId() + ")");
                    System.out.println("   ├─ Max Content: " + plan.getMaxContent());
                    System.out.println("   ├─ Max Playlists: " + plan.getMaxPlaylists());
                    System.out.println("   ├─ Max Devices: " + plan.getMaxDevices());
                    System.out.println("   └─ Max Groups: " + plan.getMaxGroups());
                    return plan;
                } else {
                    System.out.println("⚠️ Stripe Price ID provided but plan not found in database");
                }
            }

            // ✅ PRIORITY 2: Try to find plan by planId (for free trial or plan code)
            String planId = subscription.getPlanId();
            if (planId != null && !planId.isEmpty()) {
                System.out.println("🔍 Looking up plan by planId: " + planId);
                Optional<SubscriptionPlan> planById = subscriptionPlanRepository.findByHexId(planId);
                if (planById.isPresent()) {
                    SubscriptionPlan plan = planById.get();
                    System.out.println("✅ Using plan: " + plan.getName() + " (ID: " + plan.getId() + ")");
                    System.out.println("   ├─ Max Content: " + plan.getMaxContent());
                    System.out.println("   ├─ Max Playlists: " + plan.getMaxPlaylists());
                    System.out.println("   ├─ Max Devices: " + plan.getMaxDevices());
                    System.out.println("   └─ Max Groups: " + plan.getMaxGroups());
                    return plan;
                }
            }

            // If we reach here, subscription exists but plan not found
            System.err.println("❌ ERROR: Active subscription found but no matching plan!");
            System.err.println("   Subscription ID: " + subscription.getId());
            System.err.println("   Plan ID: " + subscription.getPlanId());
            System.err.println("   Stripe Price ID: " + subscription.getPriceId());
            return null;
        }

        // No active subscription found
        System.out.println("⚠️ No active subscription found for user " + userId);
        System.out.println("   ➡️ User must purchase a plan to use resources");
        return null;
    }

    /**
     * Check if user can create a new content item.
     * If user has no active subscription, throw error instead of silently allowing.
     */
    public void checkContentLimit(String userId) {
        System.out.println("\n📊 [checkContentLimit] Limit checks are DISABLED. Content upload allowed.");
    }

    /**
     * Check if user can create a new playlist.
     */
    public void checkPlaylistLimit(String userId) {
        System.out.println("\n📊 [checkPlaylistLimit] Limit checks are DISABLED. Playlist creation allowed.");
    }

    /**
     * Check if user can create a new device.
     */
    public void checkDeviceLimit(String userId) {
        System.out.println("\n📊 [checkDeviceLimit] Limit checks are DISABLED. Device registration allowed.");
    }

    /**
     * Check if user can create a new group.
     */
    public void checkGroupLimit(String userId) {
        System.out.println("\n📊 [checkGroupLimit] Limit checks are DISABLED. Group creation allowed.");
    }

    /**
     * Get comprehensive usage statistics for a user.
     */
    public Map<String, Object> getUserUsageStats(String userId) {
        System.out.println("\n📈 [getUserUsageStats] Getting usage statistics for: " + userId);
        
        SubscriptionPlan plan = getUserPlanWithLimits(userId);
        
        long contentCount = contentRepository.findByUserId(userId).size();
        long playlistCount = playlistRepository.findByUserId(userId).size();
        long deviceCount = deviceRepository.findByUserId(userId).size();
        long groupCount = groupRepository.findByUserId(userId).size();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Only add plan info if an active subscription exists
        if (plan != null) {
            stats.put("planName", plan.getName());
            stats.put("planId", plan.getId());
            stats.put("hasActiveSubscription", true);
        } else {
            stats.put("planName", "None");
            stats.put("planId", null);
            stats.put("hasActiveSubscription", false);
        }
        
        // Content stats
        Map<String, Object> contentStats = new HashMap<>();
        contentStats.put("current", contentCount);
        if (plan != null && plan.getMaxContent() != null) {
            contentStats.put("limit", plan.getMaxContent());
            contentStats.put("remaining", Math.max(0, plan.getMaxContent() - contentCount));
            contentStats.put("percentage", plan.getMaxContent() > 0 ? (contentCount * 100.0 / plan.getMaxContent()) : 0);
        } else {
            contentStats.put("limit", null);
            contentStats.put("remaining", "unlimited");
            contentStats.put("percentage", 0);
        }
        stats.put("content", contentStats);
        
        // Playlist stats
        Map<String, Object> playlistStats = new HashMap<>();
        playlistStats.put("current", playlistCount);
        if (plan != null && plan.getMaxPlaylists() != null) {
            playlistStats.put("limit", plan.getMaxPlaylists());
            playlistStats.put("remaining", Math.max(0, plan.getMaxPlaylists() - playlistCount));
            playlistStats.put("percentage", plan.getMaxPlaylists() > 0 ? (playlistCount * 100.0 / plan.getMaxPlaylists()) : 0);
        } else {
            playlistStats.put("limit", null);
            playlistStats.put("remaining", "unlimited");
            playlistStats.put("percentage", 0);
        }
        stats.put("playlists", playlistStats);
        
        // Device stats
        Map<String, Object> deviceStats = new HashMap<>();
        deviceStats.put("current", deviceCount);
        if (plan != null && plan.getMaxDevices() != null) {
            deviceStats.put("limit", plan.getMaxDevices());
            deviceStats.put("remaining", Math.max(0, plan.getMaxDevices() - deviceCount));
            deviceStats.put("percentage", plan.getMaxDevices() > 0 ? (deviceCount * 100.0 / plan.getMaxDevices()) : 0);
        } else {
            deviceStats.put("limit", null);
            deviceStats.put("remaining", "unlimited");
            deviceStats.put("percentage", 0);
        }
        stats.put("devices", deviceStats);
        
        // Group stats
        Map<String, Object> groupStats = new HashMap<>();
        groupStats.put("current", groupCount);
        if (plan != null && plan.getMaxGroups() != null) {
            groupStats.put("limit", plan.getMaxGroups());
            groupStats.put("remaining", Math.max(0, plan.getMaxGroups() - groupCount));
            groupStats.put("percentage", plan.getMaxGroups() > 0 ? (groupCount * 100.0 / plan.getMaxGroups()) : 0);
        } else {
            groupStats.put("limit", null);
            groupStats.put("remaining", "unlimited");
            groupStats.put("percentage", 0);
        }
        stats.put("groups", groupStats);
        
        // Check if user is in free trial
        Optional<Subscription> subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            if (sub.getIsTrial() != null && sub.getIsTrial() && sub.getTrialEndsAt() != null) {
                long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), sub.getTrialEndsAt());
                if (daysRemaining > 0) {
                    stats.put("freeTrialActive", true);
                    stats.put("freeTrialDaysRemaining", daysRemaining);
                } else {
                    stats.put("freeTrialActive", false);
                    stats.put("freeTrialExpired", true);
                }
            }
        }
        
        return stats;
    }

    /**
     * Get remaining days for current subscription period.
     */
    public long getSubscriptionDaysRemaining(String userId) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");
        
        if (subscription.isPresent() && subscription.get().getCurrentPeriodEnd() != null) {
            LocalDateTime endDate = subscription.get().getCurrentPeriodEnd();
            return Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), endDate));
        }
        
        return 0;
    }

    /**
     * Custom exception for subscription limit violations.
     */
    public static class SubscriptionLimitExceededException extends RuntimeException {
        public SubscriptionLimitExceededException(String message) {
            super(message);
        }
    }
}