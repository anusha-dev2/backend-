package com.mediaserver.config;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.repository.SubscriptionPlanRepository;

/**
 * Initializes subscription plans in the database on application startup.
 * Creates or updates the Basic and Advanced monthly plans with defined limits.
 */
@Component
public class SubscriptionPlansInitializer implements CommandLineRunner {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if plans already exist to prevent creating duplicates on restarts
        if (subscriptionPlanRepository.count() > 0) {
            System.out.println("✓ Subscription plans already exist, skipping initialization");
            return;
        }
        initializeSubscriptionPlans();
    }

    private void initializeSubscriptionPlans() {
        // Free Plan (Post-Trial)
        createOrUpdatePlan(
            "free",
            "Free Plan",
            "Limited access after trial period expires",
            0.0,
            "monthly",
            3,    // content
            1,    // playlists
            1,    // devices
            1,    // groups
            Arrays.asList(
                "Limited to 3 contents",
                "1 playlist",
                "1 device",
                "1 group"
            )
        );

        // Basic Plan (Monthly) - 30 days - ₹199
        createOrUpdatePlan(
            "6916ba-3e1b6a-120296-908055",
            "Basic Plan (Monthly)",
            "Perfect for small businesses and individual users",
            199.0,
            "monthly",
            8,    // content
            4,    // playlists
            3,    // devices
            2,    // groups
            Arrays.asList(
                "Up to 8 contents",
                "4 playlists",
                "3 devices",
                "2 groups",
                "Basic scheduling",
                "Email support"
            )
        );

        // Advanced Plan (Monthly) - 60 days - ₹499
        createOrUpdatePlan(
            "6916ba-3e1b6a-120296-250101",
            "Advanced Plan (Monthly)",
            "For growing businesses with multiple devices",
            499.0,
            "monthly",
            25,   // content
            6,    // playlists
            4,    // devices
            4,    // groups
            Arrays.asList(
                "Up to 25 contents",
                "6 playlists",
                "4 devices",
                "4 groups",
                "Advanced scheduling",
                "Priority email support",
                "Custom branding"
            )
        );

        System.out.println("✓ Subscription plans initialized successfully");
    }

    private void createOrUpdatePlan(
            String planCode,
            String name,
            String description,
            Double price,
            String interval,
            Integer maxContent,
            Integer maxPlaylists,
            Integer maxDevices,
            Integer maxGroups,
            java.util.List<String> features) {

        Optional<SubscriptionPlan> existingPlan = subscriptionPlanRepository.findByPlanCode(planCode);

        SubscriptionPlan plan;
        String existingPriceId = null;
        if (existingPlan.isPresent()) {
            plan = existingPlan.get();
            existingPriceId = plan.getPriceId(); // Preserve existing Stripe price ID
            System.out.println("Updating existing plan: " + name);
        } else {
            plan = new SubscriptionPlan();
            System.out.println("Creating new plan: " + name);
        }

        plan.setId(planCode);
        plan.setPlanCode(planCode);
        plan.setName(name);
        plan.setDescription(description);
        plan.setPrice(price);
        plan.setInterval(interval);
        plan.setMaxContent(maxContent);
        plan.setMaxPlaylists(maxPlaylists);
        plan.setMaxDevices(maxDevices);
        plan.setMaxGroups(maxGroups);
        plan.setFeatures(features);

        // Preserve existing Stripe price ID if it exists
        if (existingPriceId != null && !existingPriceId.isEmpty()) {
            plan.setPriceId(existingPriceId);
        }

        subscriptionPlanRepository.save(plan);
    }
}
