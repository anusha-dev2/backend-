package com.mediaserver.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.model.Subscription;
import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.repository.SubscriptionPlanRepository;
import com.mediaserver.repository.SubscriptionRepository;
import com.mediaserver.service.SubscriptionLimitService;

/**
 * REST Controller for retrieving subscription usage statistics.
 * Provides endpoints to get detailed usage information for users' subscriptions.
 */
@RestController
@RequestMapping("/subscription")
public class SubscriptionUsageController {

    @Autowired
    private SubscriptionLimitService subscriptionLimitService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * Get comprehensive usage statistics for a user's subscription.
     * Returns current usage counts, limits, and percentages for content, playlists, devices, and groups.
     *
     * @param userId The ID of the user
     * @return Map containing usage statistics including plan details and resource usage
     */
    @GetMapping("/usage/{userId}")
    public ResponseEntity<Map<String, Object>> getSubscriptionUsage(@PathVariable String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Map<String, Object> usageStats = subscriptionLimitService.getUserUsageStats(userId);
            return ResponseEntity.ok(usageStats);
        } catch (Exception e) {
            // Log the exception if needed
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get remaining days for the current subscription period.
     *
     * @param userId The ID of the user
     * @return Map containing days remaining information
     */
    @GetMapping("/days-remaining/{userId}")
    public ResponseEntity<Map<String, Object>> getSubscriptionDaysRemaining(@PathVariable String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            long daysRemaining = subscriptionLimitService.getSubscriptionDaysRemaining(userId);
            Map<String, Object> response = Map.of("daysRemaining", daysRemaining);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if user can create a specific type of resource.
     *
     * @param userId The ID of the user
     * @param type The type of resource (content, playlist, device, group)
     * @return Map containing permission status and details
     */
    @GetMapping("/can-create/{userId}/{type}")
    public ResponseEntity<Map<String, Object>> canCreateResource(@PathVariable String userId, @PathVariable String type) {
        if (userId == null || userId.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean canCreate = false;
            String message = "";
            Map<String, Object> usageStats = subscriptionLimitService.getUserUsageStats(userId);

            switch (type.toLowerCase()) {
                case "content":
                    Map<String, Object> contentStats = (Map<String, Object>) usageStats.get("content");
                    canCreate = !"unlimited".equals(contentStats.get("remaining")) &&
                               ((Number) contentStats.get("remaining")).longValue() > 0;
                    message = canCreate ? "Can create content" : "Content limit reached";
                    break;
                case "playlist":
                    Map<String, Object> playlistStats = (Map<String, Object>) usageStats.get("playlists");
                    canCreate = !"unlimited".equals(playlistStats.get("remaining")) &&
                               ((Number) playlistStats.get("remaining")).longValue() > 0;
                    message = canCreate ? "Can create playlist" : "Playlist limit reached";
                    break;
                case "device":
                    Map<String, Object> deviceStats = (Map<String, Object>) usageStats.get("devices");
                    canCreate = !"unlimited".equals(deviceStats.get("remaining")) &&
                               ((Number) deviceStats.get("remaining")).longValue() > 0;
                    message = canCreate ? "Can create device" : "Device limit reached";
                    break;
                case "group":
                    Map<String, Object> groupStats = (Map<String, Object>) usageStats.get("groups");
                    canCreate = !"unlimited".equals(groupStats.get("remaining")) &&
                               ((Number) groupStats.get("remaining")).longValue() > 0;
                    message = canCreate ? "Can create group" : "Group limit reached";
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            Map<String, Object> response = Map.of(
                "canCreate", canCreate,
                "message", message,
                "type", type
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * DEBUG ENDPOINT: Get detailed subscription information for troubleshooting.
     * This endpoint shows exactly what's stored in the database.
     */
    @GetMapping("/debug/{userId}")
    public ResponseEntity<Map<String, Object>> debugSubscription(@PathVariable String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Map<String, Object> debug = new HashMap<>();

            // 1. Get active subscription from database
            Optional<Subscription> subOpt = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE");
            if (subOpt.isPresent()) {
                Subscription sub = subOpt.get();
                Map<String, Object> subInfo = new HashMap<>();
                subInfo.put("id", sub.getId());
                subInfo.put("userId", sub.getUserId());
                subInfo.put("planId", sub.getPlanId());
                subInfo.put("planType", sub.getPlanType());
                subInfo.put("status", sub.getStatus());
                subInfo.put("PriceId", sub.getPriceId());
                subInfo.put("SubscriptionId", sub.getSubscriptionId());
                subInfo.put("isTrial", sub.getIsTrial());
                subInfo.put("currentPeriodStart", sub.getCurrentPeriodStart());
                subInfo.put("currentPeriodEnd", sub.getCurrentPeriodEnd());
                debug.put("activeSubscription", subInfo);

                // 2. Try to find plan by Stripe Price ID (if exists)
                if (sub.getPriceId() != null && !sub.getPriceId().isEmpty()) {
                    Optional<SubscriptionPlan> planByPriceId = subscriptionPlanRepository.findByPriceId(sub.getPriceId());
                    if (planByPriceId.isPresent()) {
                        SubscriptionPlan plan = planByPriceId.get();
                        Map<String, Object> planInfo = new HashMap<>();
                        planInfo.put("id", plan.getId());
                        planInfo.put("name", plan.getName());
                        planInfo.put("maxContent", plan.getMaxContent());
                        planInfo.put("maxPlaylists", plan.getMaxPlaylists());
                        planInfo.put("maxDevices", plan.getMaxDevices());
                        planInfo.put("maxGroups", plan.getMaxGroups());
                        planInfo.put("PriceId", plan.getPriceId());
                        debug.put("planByPriceId", planInfo);
                        debug.put("planLookupMethod", "STRIPE_PRICE_ID");
                    } else {
                        debug.put("planByPriceId", "NOT_FOUND");
                        debug.put("warning", "Subscription has PriceId but no matching plan found!");
                    }
                }

                // 3. Try to find plan by Plan ID
                if (sub.getPlanId() != null && !sub.getPlanId().isEmpty()) {
                    Optional<SubscriptionPlan> planById = subscriptionPlanRepository.findById(sub.getPlanId());
                    if (planById.isPresent()) {
                        SubscriptionPlan plan = planById.get();
                        Map<String, Object> planInfo = new HashMap<>();
                        planInfo.put("id", plan.getId());
                        planInfo.put("name", plan.getName());
                        planInfo.put("maxContent", plan.getMaxContent());
                        planInfo.put("maxPlaylists", plan.getMaxPlaylists());
                        planInfo.put("maxDevices", plan.getMaxDevices());
                        planInfo.put("maxGroups", plan.getMaxGroups());
                        planInfo.put("PriceId", plan.getPriceId());
                        debug.put("planByPlanId", planInfo);
                        if (!debug.containsKey("planLookupMethod")) {
                            debug.put("planLookupMethod", "PLAN_ID");
                        }
                    } else {
                        debug.put("planByPlanId", "NOT_FOUND");
                    }
                }

            } else {
                debug.put("activeSubscription", "NONE");
                debug.put("message", "No active subscription found for this user");
            }

            // 4. Get all plans in database for reference
            List<SubscriptionPlan> allPlans = subscriptionPlanRepository.findAll();
            List<Map<String, Object>> plansList = new ArrayList<>();
            for (SubscriptionPlan plan : allPlans) {
                Map<String, Object> planInfo = new HashMap<>();
                planInfo.put("id", plan.getId());
                planInfo.put("planCode", plan.getPlanCode());
                planInfo.put("name", plan.getName());
                planInfo.put("PriceId", plan.getPriceId());
                planInfo.put("maxContent", plan.getMaxContent());
                planInfo.put("maxPlaylists", plan.getMaxPlaylists());
                planInfo.put("maxDevices", plan.getMaxDevices());
                planInfo.put("maxGroups", plan.getMaxGroups());
                plansList.add(planInfo);
            }
            debug.put("allPlansInDatabase", plansList);

            // 5. Get current usage stats
            Map<String, Object> usageStats = subscriptionLimitService.getUserUsageStats(userId);
            debug.put("currentUsageStats", usageStats);

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(error);
        }
    }
}
