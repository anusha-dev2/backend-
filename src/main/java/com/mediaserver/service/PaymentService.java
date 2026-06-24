// PaymentService.java
package com.mediaserver.service;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediaserver.model.Payment;
import com.mediaserver.model.Subscription;
import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.model.User;
import com.mediaserver.repository.PaymentRepository;
import com.mediaserver.repository.SubscriptionPlanRepository;
import com.mediaserver.repository.SubscriptionRepository;
import com.mediaserver.repository.UserRepository;
import com.mediaserver.security.JwtTokenProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.domain}")
    private String appDomain;

    @Value("${app.frontend.domain:http://localhost:3000}")
    private String frontendDomain;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        // Debug logging
        System.out.println("=== Stripe Configuration Debug ===");
        System.out.println("Stripe API Key: " + (stripeApiKey != null ?
            "[" + stripeApiKey.substring(0, Math.min(12, stripeApiKey.length())) + "...] (length: " + stripeApiKey.length() + ")" :
            "NULL"));

        // Clean up the key if it has quotes
        if (stripeApiKey != null) {
            stripeApiKey = stripeApiKey.trim();
            if (stripeApiKey.startsWith("\"") && stripeApiKey.endsWith("\"")) {
                stripeApiKey = stripeApiKey.substring(1, stripeApiKey.length() - 1);
                System.out.println("Removed quotes from API key");
            }
        }

        // Validate
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            throw new IllegalStateException("Stripe API key is not configured. Check STRIPE_API_KEY environment variable.");
        }

        if (!stripeApiKey.startsWith("sk_test_") && !stripeApiKey.startsWith("sk_live_")) {
            throw new IllegalStateException("Invalid Stripe API key format. Key should start with 'sk_test_' or 'sk_live_'");
        }

        Stripe.apiKey = stripeApiKey;
        System.out.println("Stripe initialized successfully!");
        System.out.println("==================================");
    }

    public Map<String, String> createCheckoutSession(String userId, String planId) throws StripeException {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByHexId(planId);

        if (userOpt.isEmpty() || planOpt.isEmpty()) {
            throw new IllegalArgumentException("User or plan not found");
        }

        User user = userOpt.get();
        SubscriptionPlan plan = planOpt.get();

        // Validate plan price
        if (plan.getPrice() <= 0) {
            throw new IllegalArgumentException("Plan price must be greater than 0");
        }

        // Create or retrieve customer
        String customerId;
        if (user.getStripeCustomerId() == null) {
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("email", user.getEmail());
            customerParams.put("name", user.getName());

            Customer customer = Customer.create(customerParams);
            customerId = customer.getId();

            // Save customer ID to user
            user.setStripeCustomerId(customerId);
            userRepository.save(user);
        } else {
            customerId = user.getStripeCustomerId();
        }

        // Check if plan has existing Stripe price ID
        String priceId = plan.getPriceId();
        if (priceId == null || priceId.isEmpty()) {
            // Create a new price in Stripe
            Map<String, Object> priceParams = new HashMap<>();
            priceParams.put("unit_amount", Math.round(plan.getPrice() * 100)); // Convert to paise for INR
            priceParams.put("currency", "inr");
            priceParams.put("product_data", Map.of("name", plan.getName()));

            // If plan has interval, make it recurring
            if (plan.getInterval() != null && !plan.getInterval().isEmpty()) {
                Map<String, Object> recurring = new HashMap<>();
                String stripeInterval = plan.getInterval().toUpperCase();
                if ("MONTHLY".equals(stripeInterval)) {
                    recurring.put("interval", "month");
                } else if ("YEARLY".equals(stripeInterval)) {
                    recurring.put("interval", "year");
                } else if ("WEEKLY".equals(stripeInterval)) {
                    recurring.put("interval", "week");
                } else if ("DAILY".equals(stripeInterval)) {
                    recurring.put("interval", "day");
                } else {
                    recurring.put("interval", "month"); // default to month
                }
                priceParams.put("recurring", recurring);
            }

            Price price = Price.create(priceParams);
            priceId = price.getId();

            // Save the price ID to the plan
            plan.setPriceId(priceId);
            subscriptionPlanRepository.save(plan);
        }

        // Determine session mode based on whether price is recurring
        Price price = Price.retrieve(priceId);
        SessionCreateParams.Mode mode = (price.getRecurring() != null) ?
            SessionCreateParams.Mode.SUBSCRIPTION : SessionCreateParams.Mode.PAYMENT;

        // Build checkout session using existing price ID
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setCustomer(customerId)
                .setClientReferenceId(userId)
                .setMode(mode)
                .setSuccessUrl(appDomain + "/payment/success/{CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendDomain + "/payment/cancel");

        // Add line items using existing price ID
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPrice(priceId)
                .setQuantity(1L)
                .build();

        builder.addLineItem(lineItem);

        // Create session
        SessionCreateParams params = builder.build();
        Session session = Session.create(params);

        // Log session details for debugging
        System.out.println("Checkout session created:");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Session URL: " + session.getUrl());
        System.out.println("Success URL: " + appDomain + "/payment/success?session_id={CHECKOUT_SESSION_ID}");
        System.out.println("Cancel URL: " + frontendDomain + "/payment/cancel");

        Map<String, String> result = new HashMap<>();
        result.put("id", session.getId());
        result.put("url", session.getUrl());
        return result;
    }

    public void processOneTimePayment(String userId, String paymentIntentId) throws StripeException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        // Retrieve payment intent to get amount
        com.stripe.model.PaymentIntent paymentIntent = com.stripe.model.PaymentIntent.retrieve(paymentIntentId);
        double amount = paymentIntent.getAmount() / 100.0; // Convert from paise

        // Record payment
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setCurrency("INR");
        payment.setPaymentMethod("stripe");
        payment.setStatus("completed");
        payment.setStripePaymentId(paymentIntentId);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Send notification
        webSocketService.notifyPaymentStatus(userId, "completed", payment);
    }

    @Transactional
    public void processSuccessfulPayment(String userId, String subscriptionId) throws StripeException {
        System.out.println("=== PROCESSING SUCCESSFUL PAYMENT ===");
        System.out.println("User ID: " + userId);
        System.out.println("Stripe Subscription ID: " + subscriptionId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        // Get subscription details from Stripe
        com.stripe.model.Subscription stripeSubscription;
        try {
            stripeSubscription = com.stripe.model.Subscription.retrieve(subscriptionId);
        } catch (com.stripe.exception.StripeException e) {
            throw new IllegalArgumentException("Invalid subscription ID: " + subscriptionId + ". Please ensure the subscription exists in Stripe.");
        }
        String priceId = stripeSubscription.getItems().getData().get(0).getPrice().getId();

        System.out.println("Stripe Price ID from subscription: " + priceId);

        // Find the matching plan by Stripe price ID
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByPriceId(priceId);
        SubscriptionPlan plan;

        if (planOpt.isPresent()) {
            plan = planOpt.get();
            System.out.println("✅ Found existing plan by Stripe Price ID");
            System.out.println("   Plan ID: " + plan.getId());
            System.out.println("   Plan Name: " + plan.getName());
            System.out.println("   Max Content: " + plan.getMaxContent());
            System.out.println("   Max Playlists: " + plan.getMaxPlaylists());
            System.out.println("   Max Devices: " + plan.getMaxDevices());
            System.out.println("   Max Groups: " + plan.getMaxGroups());
        } else {
            System.out.println("⚠️ No plan found with Stripe Price ID: " + priceId);
            System.out.println("Creating fallback plan from Stripe data...");

            // Create a default plan based on Stripe data
            Price price = Price.retrieve(priceId);

            plan = new SubscriptionPlan();
            plan.setName("Subscription Plan");
            plan.setPrice((double) price.getUnitAmount() / 100); // Convert from paise
            plan.setPriceId(priceId);

            if (price.getRecurring() != null) {
                plan.setInterval(price.getRecurring().getInterval().toUpperCase());
            }

            // Set default limits for fallback plan
            plan.setMaxContent(10);
            plan.setMaxPlaylists(5);
            plan.setMaxDevices(3);
            plan.setMaxGroups(2);

            plan = subscriptionPlanRepository.save(plan);
            System.out.println("✅ Created fallback plan with ID: " + plan.getId());
        }

        // ✅ CRITICAL: Use SubscriptionService.changePlan to handle subscription creation/update
        // This ensures the PriceId is properly saved to the subscription
        System.out.println("Calling SubscriptionService.changePlan...");
        Subscription subscription = subscriptionService.changePlan(userId, plan.getId(), priceId, subscriptionId);

        System.out.println("✅ Subscription created/updated:");
        System.out.println("   Subscription ID (DB): " + subscription.getId());
        System.out.println("   Plan ID: " + subscription.getPlanId());
        System.out.println("   Stripe Price ID: " + subscription.getPriceId());
        System.out.println("   Stripe Subscription ID: " + subscription.getSubscriptionId());
        System.out.println("   Status: " + subscription.getStatus());

        // Record payment
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setSubscriptionId(subscription.getPlanId());
        payment.setAmount(plan.getPrice());
        payment.setCurrency("INR");
        payment.setPaymentMethod("stripe");
        payment.setStatus("completed");
        payment.setStripePaymentId(stripeSubscription.getLatestInvoice());
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        System.out.println("✅ Payment recorded");

        // Send real-time notifications
        webSocketService.notifyPaymentStatus(userId, "completed", payment);
        webSocketService.notifySubscriptionUpdate(userId, "activated", subscription);

        System.out.println("=== PAYMENT PROCESSING COMPLETE ===\n");
    }

    public List<Payment> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Subscription getUserSubscription(String userId) {
        return subscriptionRepository.findByUserId(userId).orElse(null);
    }
    
    public boolean cancelSubscription(String userId) throws StripeException {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByUserId(userId);
        if (subscriptionOpt.isEmpty() || subscriptionOpt.get().getSubscriptionId() == null) {
            return false;
        }
        
        Subscription subscription = subscriptionOpt.get();
        
        // Cancel in Stripe
        com.stripe.model.Subscription stripeSubscription = 
                com.stripe.model.Subscription.retrieve(subscription.getSubscriptionId());
        stripeSubscription.cancel();
        
        // Update our record
        subscription.setStatus("canceled");
        subscriptionRepository.save(subscription);

        // Send real-time notification
        webSocketService.notifySubscriptionUpdate(userId, "cancelled", subscription);

        return true;
    }
    
    // New methods to resolve compilation errors
    
    public String processRecurringPayment(String customerId, String subscriptionId, String invoiceId, double amount) {
        try {
            // Find the user by their Stripe customer ID
            Optional<User> userOpt = userRepository.findByStripeCustomerId(customerId);
            if (userOpt.isEmpty()) {
                return "user_not_found";
            }
            
            User user = userOpt.get();
            
            // Record the payment
            Payment payment = new Payment();
            payment.setUserId(user.getId());
            payment.setSubscriptionId(subscriptionId); // Link to subscription for recurring payments
            payment.setAmount(amount);
            payment.setCurrency("INR");
            payment.setPaymentMethod("stripe");
            payment.setStatus("completed");
            payment.setStripePaymentId(invoiceId);
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Send real-time notification
            webSocketService.notifyPaymentStatus(user.getId(), "recurring_completed", payment);

            return "payment_processed";
        } catch (Exception e) {
            return "error_processing_payment";
        }
    }
    
    public void handleSubscriptionCancelled(String subscriptionId) {
        // First, find the subscription by Stripe subscription ID
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId);

        if (subscription != null) {
            subscription.setStatus("cancelled");
            subscriptionRepository.save(subscription);

            // Send real-time notification
            webSocketService.notifySubscriptionUpdate(subscription.getUserId(), "cancelled", subscription);
        }
    }

    public List<SubscriptionPlan> getAllSubscriptionPlans() {
        return subscriptionPlanRepository.findAll();
    }

    public List<SubscriptionPlan> getAvailableSubscriptionPlans(String userId) {
        List<SubscriptionPlan> allPlans = subscriptionPlanRepository.findAll();
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if user has an active subscription (any type)
            Subscription activeSubscription = getUserSubscription(userId);
            boolean hasActiveSubscription = activeSubscription != null &&
                "ACTIVE".equals(activeSubscription.getStatus());

            if (hasActiveSubscription) {
                // Filter out the free plan if user has an active subscription
                return allPlans.stream()
                        .filter(plan -> !"free".equals(plan.getId()))
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        return allPlans;
    }

    public SubscriptionPlan createSubscriptionPlan(SubscriptionPlan plan) {
        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);
        // Send real-time notification to all users
        webSocketService.broadcastPlanUpdate("created", savedPlan);
        return savedPlan;
    }

    public SubscriptionPlan updateSubscriptionPlan(String planId, SubscriptionPlan updatedPlan) {
        Optional<SubscriptionPlan> existingPlanOpt = subscriptionPlanRepository.findById(planId);
        if (existingPlanOpt.isPresent()) {
            SubscriptionPlan existingPlan = existingPlanOpt.get();
            existingPlan.setName(updatedPlan.getName());
            existingPlan.setDescription(updatedPlan.getDescription());
            existingPlan.setPrice(updatedPlan.getPrice());
            existingPlan.setInterval(updatedPlan.getInterval());
            existingPlan.setPriceId(updatedPlan.getPriceId());
            existingPlan.setFeatures(updatedPlan.getFeatures());
            SubscriptionPlan savedPlan = subscriptionPlanRepository.save(existingPlan);
            // Send real-time notification to all users
            webSocketService.broadcastPlanUpdate("updated", savedPlan);
            return savedPlan;
        }
        return null;
    }

    public boolean deleteSubscriptionPlan(String planId) {
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(planId);
        if (planOpt.isPresent()) {
            SubscriptionPlan plan = planOpt.get();
            subscriptionPlanRepository.deleteById(planId);
            // Send real-time notification to all users
            webSocketService.broadcastPlanUpdate("deleted", plan);
            return true;
        }
        return false;
    }
    
    public Subscription updateSubscription(String userId, String newPlanId) throws StripeException {
        Optional<Subscription> subOpt = subscriptionRepository.findByUserId(userId);
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(newPlanId);
        
        if (subOpt.isEmpty() || planOpt.isEmpty()) {
            return null;
        }
        
        Subscription subscription = subOpt.get();
        SubscriptionPlan newPlan = planOpt.get();
        
        // Update the subscription in Stripe - simplified implementation
        // You would need to retrieve the Stripe subscription and update it
        
        // Update local record
        subscription.setPlanId(newPlanId);
        subscription = subscriptionRepository.save(subscription);

        // Send real-time notification
        webSocketService.notifySubscriptionUpdate(userId, "plan_changed", subscription);

        return subscription;
    }
    
    public String createBillingPortalSession(String userId) throws StripeException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getStripeCustomerId() == null) {
            throw new IllegalArgumentException("User not found or no Stripe customer ID");
        }

        User user = userOpt.get();

        // Create billing portal session
        Map<String, Object> params = new HashMap<>();
        params.put("customer", user.getStripeCustomerId());
        params.put("return_url", frontendDomain + "/account");

        try {
            com.stripe.model.billingportal.Session portalSession = com.stripe.model.billingportal.Session.create(params);
            return portalSession.getUrl();
        } catch (Exception e) {
            // Fallback: try to create a checkout session for management
            throw new RuntimeException("Billing portal not available, please contact support");
        }
    }

    public String getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid token format");
        }
        String jwtToken = token.substring(7); // Remove "Bearer " prefix
        String username = jwtTokenProvider.getUsernameFromJWT(jwtToken);
        System.out.println("Extracted username from token: " + username);

        // Find user by username (since JWT subject is username)
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Found user: username=" + user.getUsername() + ", ID=" + user.getId());
            return user.getId();
        }

        // If not found, throw exception
        throw new IllegalArgumentException("User not found for username: " + username);
    }

    public String getStripeApiKeyPrefix() {
        return stripeApiKey != null && stripeApiKey.length() >= 12 ? stripeApiKey.substring(0, 12) : "null";
    }

    public int getStripeApiKeyLength() {
        return stripeApiKey != null ? stripeApiKey.length() : 0;
    }

    public boolean isStripeApiKeyValid() {
        return stripeApiKey != null && stripeApiKey.startsWith("sk_test_");
    }

    public Subscription getSubscriptionByStripeId(String subscriptionId) {
        return subscriptionRepository.findBySubscriptionId(subscriptionId);
    }

    public Map<String, Object> handlePaymentSuccess(String sessionId) throws StripeException {
        System.out.println("=== HANDLING PAYMENT SUCCESS ===");
        System.out.println("Session ID: " + sessionId);

        // Retrieve the checkout session from Stripe
        com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.retrieve(sessionId);

        String subscriptionId = session.getSubscription();
        String userId = session.getClientReferenceId();

        System.out.println("Stripe Subscription ID: " + subscriptionId);
        System.out.println("User ID: " + userId);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("userId", userId);

        if (subscriptionId != null) {
            // Get subscription details from our database
            Subscription subscription = getSubscriptionByStripeId(subscriptionId);
            if (subscription != null) {
                result.put("subscription", subscription);
                result.put("subscriptionId", subscriptionId);
                System.out.println("✅ Found subscription in database");
            } else {
                result.put("error", "Subscription not found in database");
                System.out.println("❌ Subscription not found in database");
            }
        } else {
            result.put("error", "No subscription ID in session");
            System.out.println("❌ No subscription ID in session");
        }

        System.out.println("=== PAYMENT SUCCESS HANDLING COMPLETE ===");
        return result;
    }

    public void processWebhookEvent(String payload, String sigHeader) throws Exception {
        // Import required Stripe classes
        com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "invoice.payment_succeeded":
                handleInvoicePaymentSucceeded(event);
                break;
            case "invoice.payment_failed":
                handleInvoicePaymentFailed(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(com.stripe.model.Event event) {
        com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) event.getData().getObject();
        String userId = session.getClientReferenceId();
        String subscriptionId = session.getSubscription();

        if (subscriptionId != null && userId != null) {
            try {
                // Log the Stripe subscription details for debugging
                com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.retrieve(subscriptionId);
                String priceId = stripeSubscription.getItems().getData().get(0).getPrice().getId();
                System.out.println("=== STRIPE WEBHOOK DEBUG ===");
                System.out.println("User ID: " + userId);
                System.out.println("Stripe Subscription ID: " + subscriptionId);
                System.out.println("Stripe Price ID: " + priceId);
                System.out.println("Price Amount: " + stripeSubscription.getItems().getData().get(0).getPrice().getUnitAmount());
                System.out.println("============================");

                processSuccessfulPayment(userId, subscriptionId);
                System.out.println("Checkout session completed for user: " + userId);
            } catch (Exception e) {
                System.err.println("Failed to process successful payment: " + e.getMessage());
            }
        }
    }

    private void handleInvoicePaymentSucceeded(com.stripe.model.Event event) {
        com.stripe.model.Invoice invoice = (com.stripe.model.Invoice) event.getData().getObject();
        String subscriptionId = invoice.getSubscription();
        String customerId = invoice.getCustomer();

        if (subscriptionId != null && customerId != null) {
            try {
                processRecurringPayment(customerId, subscriptionId, invoice.getId(), invoice.getAmountPaid() / 100.0);
                System.out.println("Recurring payment succeeded for subscription: " + subscriptionId);
            } catch (Exception e) {
                System.err.println("Failed to process recurring payment: " + e.getMessage());
            }
        }
    }

    private void handleInvoicePaymentFailed(com.stripe.model.Event event) {
        com.stripe.model.Invoice invoice = (com.stripe.model.Invoice) event.getData().getObject();
        String subscriptionId = invoice.getSubscription();
        String customerId = invoice.getCustomer();

        if (subscriptionId != null && customerId != null) {
            try {
                Optional<User> userOpt = userRepository.findByStripeCustomerId(customerId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.err.println("Payment failed for user: " + user.getId() + ", subscription: " + subscriptionId);

                    // Notify user via WebSocket
                    webSocketService.notifyPaymentStatus(user.getId(), "failed", null);

                    // Log the failure
                    Payment failedPayment = new Payment();
                    failedPayment.setUserId(user.getId());
                    failedPayment.setAmount(invoice.getAmountDue() / 100.0);
                    failedPayment.setCurrency("INR");
                    failedPayment.setPaymentMethod("stripe");
                    failedPayment.setStatus("failed");
                    failedPayment.setStripePaymentId(invoice.getId());
                    failedPayment.setCreatedAt(LocalDateTime.now());
                    paymentRepository.save(failedPayment);
                }
            } catch (Exception e) {
                System.err.println("Failed to handle payment failure: " + e.getMessage());
            }
        }
    }

    private void handleSubscriptionDeleted(com.stripe.model.Event event) {
        com.stripe.model.Subscription subscription = (com.stripe.model.Subscription) event.getData().getObject();
        String subscriptionId = subscription.getId();

        try {
            handleSubscriptionCancelled(subscriptionId);
            System.out.println("Subscription cancelled: " + subscriptionId);
        } catch (Exception e) {
            System.err.println("Failed to handle subscription cancellation: " + e.getMessage());
        }
    }
}
