package com.mediaserver.service;

import com.mediaserver.model.Payment;
import com.mediaserver.model.Subscription;
import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.model.User;
import com.mediaserver.repository.PaymentRepository;
import com.mediaserver.repository.SubscriptionPlanRepository;
import com.mediaserver.repository.SubscriptionRepository;
import com.mediaserver.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class RazorpayService {

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

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${app.frontend.domain:http://localhost:3000}")
    private String frontendDomain;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        System.out.println("=== Razorpay Configuration Debug ===");
        System.out.println("Razorpay Key ID: " + (razorpayKeyId != null ?
            "[" + razorpayKeyId.substring(0, Math.min(12, razorpayKeyId.length())) + "...] (length: " + razorpayKeyId.length() + ")" :
            "NULL"));

        // Clean up the keys if they have quotes
        if (razorpayKeyId != null) {
            razorpayKeyId = razorpayKeyId.trim();
            if (razorpayKeyId.startsWith("\"") && razorpayKeyId.endsWith("\"")) {
                razorpayKeyId = razorpayKeyId.substring(1, razorpayKeyId.length() - 1);
            }
        }

        if (razorpayKeySecret != null) {
            razorpayKeySecret = razorpayKeySecret.trim();
            if (razorpayKeySecret.startsWith("\"") && razorpayKeySecret.endsWith("\"")) {
                razorpayKeySecret = razorpayKeySecret.substring(1, razorpayKeySecret.length() - 1);
            }
        }

        // Validate
        if (razorpayKeyId == null || razorpayKeyId.isEmpty()) {
            throw new IllegalStateException("Razorpay Key ID is not configured. Check RAZORPAY_KEY_ID environment variable.");
        }

        if (razorpayKeySecret == null || razorpayKeySecret.isEmpty()) {
            throw new IllegalStateException("Razorpay Key Secret is not configured. Check RAZORPAY_KEY_SECRET environment variable.");
        }

        try {
            razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            System.out.println("Razorpay initialized successfully!");
        } catch (RazorpayException e) {
            throw new IllegalStateException("Failed to initialize Razorpay client: " + e.getMessage(), e);
        }
        System.out.println("====================================");
    }

    /**
     * Create Razorpay order for checkout
     */
    public Map<String, Object> createOrder(String userId, String planId) throws RazorpayException {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByHexId(planId);

        if (userOpt.isEmpty() || planOpt.isEmpty()) {
            throw new IllegalArgumentException("User or plan not found");
        }

        User user = userOpt.get();
        SubscriptionPlan plan = planOpt.get();

        // Validate plan price
        if (plan.getPrice() < 0) {
            throw new IllegalArgumentException("Invalid plan price");
        }

        // Create order in Razorpay
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", Math.round(plan.getPrice() * 100)); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());
        
        JSONObject notes = new JSONObject();
        notes.put("userId", userId);
        notes.put("planId", planId);
        notes.put("planName", plan.getName());
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);

        System.out.println("Razorpay order created:");
        System.out.println("Order ID: " + order.get("id"));
        System.out.println("Amount: " + order.get("amount"));
        System.out.println("Currency: " + order.get("currency"));

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", plan.getPrice() / 100.0); // Convert from paise to rupees for client
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);
        response.put("userName", user.getName());
        response.put("userEmail", user.getEmail());
        response.put("planName", plan.getName());

        return response;
    }

    /**
     * Verify Razorpay payment signature for orders
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String generatedSignature = generateSignature(payload, razorpayKeySecret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            System.err.println("Error verifying signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify Razorpay payment link signature
     */
    public boolean verifyPaymentLinkSignature(String paymentLinkId, String paymentId, String signature) {
        try {
            // For payment links, the signature is generated as: payment_id|payment_link_id
            String payload = paymentId + "|" + paymentLinkId;
            String generatedSignature = generateSignature(payload, razorpayKeySecret);

            System.out.println("Payment Link Signature Verification:");
            System.out.println("Payment Link ID: " + paymentLinkId);
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Payload: " + payload);
            System.out.println("Expected Signature: " + generatedSignature);
            System.out.println("Received Signature: " + signature);
            System.out.println("Match: " + generatedSignature.equals(signature));

            return generatedSignature.equals(signature);
        } catch (Exception e) {
            System.err.println("Error verifying payment link signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify Razorpay checkout signature for regular checkout (not payment links)
     */
    public boolean verifyCheckoutSignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
        } catch (Exception e) {
            System.err.println("Error verifying checkout signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate HMAC SHA256 signature
     */
    private String generateSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Process successful payment and activate subscription
     */
    @Transactional
    public void processSuccessfulPayment(String userId, String orderId, String paymentLinkId, String paymentId, String signature) throws RazorpayException {
        System.out.println("=== PROCESSING RAZORPAY PAYMENT ===");
        System.out.println("User ID: " + userId);
        System.out.println("Order ID: " + orderId);
        System.out.println("Payment Link ID: " + paymentLinkId);
        System.out.println("Payment ID: " + paymentId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        String planId;
        if (paymentLinkId != null && !paymentLinkId.isEmpty()) {
            // For payment links, fetch planId from payment link notes
            com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.fetch(paymentLinkId);
            JSONObject notes = paymentLink.get("notes");
            planId = notes.getString("planId");
            System.out.println("Plan ID from payment link: " + planId);
        } else if (orderId != null && !orderId.isEmpty()) {
            // For orders, fetch planId from order notes
            Order order = razorpayClient.orders.fetch(orderId);
            JSONObject notes = order.get("notes");
            planId = notes.getString("planId");
            System.out.println("Plan ID from order: " + planId);
        } else {
            throw new IllegalArgumentException("Either orderId or paymentLinkId must be provided");
        }

        System.out.println("Plan ID from order: " + planId);

        // Find the matching plan
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByHexId(planId);
        if (planOpt.isEmpty()) {
            throw new IllegalArgumentException("Plan not found: " + planId);
        }

        SubscriptionPlan plan = planOpt.get();
        System.out.println("✅ Found plan: " + plan.getName());
        System.out.println("   Max Content: " + plan.getMaxContent());
        System.out.println("   Max Playlists: " + plan.getMaxPlaylists());
        System.out.println("   Max Devices: " + plan.getMaxDevices());
        System.out.println("   Max Groups: " + plan.getMaxGroups());

        // Create/update subscription using SubscriptionService
        System.out.println("Creating subscription...");
        Subscription subscription = subscriptionService.changePlan(
            userId, 
            plan.getId(), 
            null, // No Stripe price ID for Razorpay
            "razorpay_" + paymentId // Use Razorpay payment ID as subscription reference
        );

        System.out.println("✅ Subscription created:");
        System.out.println("   Subscription ID: " + subscription.getId());
        System.out.println("   Plan ID: " + subscription.getPlanId());
        System.out.println("   Status: " + subscription.getStatus());

        // Record payment
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setSubscriptionId(subscription.getPlanId());
        payment.setAmount(plan.getPrice());
        payment.setCurrency("INR");
        payment.setPaymentMethod("razorpay");
        payment.setStatus("completed");
        payment.setStripePaymentId(paymentId); // Reusing field for Razorpay payment ID
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        System.out.println("✅ Payment recorded");

        // Send real-time notifications
        webSocketService.notifyPaymentStatus(userId, "completed", payment);
        webSocketService.notifySubscriptionUpdate(userId, "activated", subscription);

        System.out.println("=== RAZORPAY PAYMENT PROCESSING COMPLETE ===\n");
    }

    /**
     * Handle Razorpay webhook events
     */
    public void processWebhookEvent(String payload, String signature) throws Exception {
        // Verify webhook signature
        if (!verifyWebhookSignature(payload, signature)) {
            throw new SecurityException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        System.out.println("Processing Razorpay webhook: " + eventType);

        switch (eventType) {
            case "payment.captured":
                handlePaymentCaptured(event);
                break;
            case "payment.failed":
                handlePaymentFailed(event);
                break;
            case "order.paid":
                handleOrderPaid(event);
                break;
            default:
                System.out.println("Unhandled event type: " + eventType);
        }
    }

    /**
     * Verify webhook signature
     */
    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String generatedSignature = generateSignature(payload, razorpayKeySecret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            System.err.println("Error verifying webhook signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle payment.captured event
     */
    private void handlePaymentCaptured(JSONObject event) {
        try {
            JSONObject payload = event.getJSONObject("payload");
            JSONObject payment = payload.getJSONObject("payment");
            JSONObject entity = payment.getJSONObject("entity");
            
            String paymentId = entity.getString("id");
            String orderId = entity.getString("order_id");
            
            System.out.println("Payment captured: " + paymentId + " for order: " + orderId);
            
            // Fetch order details to get userId and planId
            Order order = razorpayClient.orders.fetch(orderId);
            JSONObject notes = order.get("notes");
            String userId = notes.getString("userId");
            
            // Payment is already captured, just log it
            System.out.println("Payment successfully captured for user: " + userId);
            
        } catch (Exception e) {
            System.err.println("Failed to handle payment.captured: " + e.getMessage());
        }
    }

    /**
     * Handle payment.failed event
     */
    private void handlePaymentFailed(JSONObject event) {
        try {
            JSONObject payload = event.getJSONObject("payload");
            JSONObject payment = payload.getJSONObject("payment");
            JSONObject entity = payment.getJSONObject("entity");
            
            String paymentId = entity.getString("id");
            String orderId = entity.getString("order_id");
            
            System.err.println("Payment failed: " + paymentId + " for order: " + orderId);
            
            // Fetch order details to get userId
            Order order = razorpayClient.orders.fetch(orderId);
            JSONObject notes = order.get("notes");
            String userId = notes.getString("userId");
            
            // Notify user
            webSocketService.notifyPaymentStatus(userId, "failed", null);
            
            // Log failed payment
            Payment failedPayment = new Payment();
            failedPayment.setUserId(userId);
            failedPayment.setAmount(entity.getDouble("amount") / 100.0);
            failedPayment.setCurrency("INR");
            failedPayment.setPaymentMethod("razorpay");
            failedPayment.setStatus("failed");
            failedPayment.setStripePaymentId(paymentId);
            failedPayment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(failedPayment);
            
        } catch (Exception e) {
            System.err.println("Failed to handle payment.failed: " + e.getMessage());
        }
    }

    /**
     * Handle order.paid event
     */
    private void handleOrderPaid(JSONObject event) {
        try {
            JSONObject payload = event.getJSONObject("payload");
            JSONObject order = payload.getJSONObject("order");
            JSONObject entity = order.getJSONObject("entity");
            
            String orderId = entity.getString("id");
            System.out.println("Order paid: " + orderId);
            
        } catch (Exception e) {
            System.err.println("Failed to handle order.paid: " + e.getMessage());
        }
    }

    /**
     * Fetch payment details
     */
    public Map<String, Object> getPaymentDetails(String paymentId) throws RazorpayException {
        com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("id", payment.get("id"));
        details.put("orderId", payment.get("order_id"));
        details.put("amount", payment.get("amount"));
        details.put("currency", payment.get("currency"));
        details.put("status", payment.get("status"));
        details.put("method", payment.get("method"));
        details.put("email", payment.get("email"));
        details.put("contact", payment.get("contact"));
        details.put("createdAt", payment.get("created_at"));
        
        return details;
    }

    /**
     * Refund a payment
     */
    public Map<String, Object> refundPayment(String paymentId, Double amount) throws RazorpayException {
        JSONObject refundRequest = new JSONObject();

        if (amount != null) {
            refundRequest.put("amount", Math.round(amount * 100)); // Amount in paise
        }

        com.razorpay.Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("refundId", refund.get("id"));
        response.put("paymentId", refund.get("payment_id"));
        response.put("amount", refund.get("amount"));
        response.put("status", refund.get("status"));

        return response;
    }

    /**
     * Create payment link for backend checkout
     */
    public Map<String, Object> createPaymentLink(String userId, String planId) throws RazorpayException {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByHexId(planId);

        if (userOpt.isEmpty() || planOpt.isEmpty()) {
            throw new IllegalArgumentException("User or plan not found");
        }

        User user = userOpt.get();
        SubscriptionPlan plan = planOpt.get();

        // Create payment link request
        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", Math.round(plan.getPrice() * 100)); // Amount in paise
        paymentLinkRequest.put("currency", "INR");
        paymentLinkRequest.put("description", "Subscription for " + plan.getName());

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("name", user.getName());
        customer.put("email", user.getEmail());
        paymentLinkRequest.put("customer", customer);

        // Callback URL and method
        JSONObject notify = new JSONObject();
        notify.put("email", true);
        notify.put("sms", false);
        paymentLinkRequest.put("notify", notify);

        // Custom notes
        JSONObject notes = new JSONObject();
        notes.put("userId", userId);
        notes.put("planId", planId);
        notes.put("planName", plan.getName());
        paymentLinkRequest.put("notes", notes);

        // Create payment link
        com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("paymentLinkId", paymentLink.get("id"));
        response.put("shortUrl", paymentLink.get("short_url"));
        response.put("amount", plan.getPrice() / 100.0); // Convert to rupees
        response.put("currency", "INR");
        response.put("status", paymentLink.get("status"));
        response.put("description", paymentLink.get("description"));
        response.put("userName", user.getName());
        response.put("userEmail", user.getEmail());
        response.put("planName", plan.getName());

        return response;
    }

    /**
     * Get payment link details
     */
    public Map<String, Object> getPaymentLinkDetails(String paymentLinkId) throws RazorpayException {
        com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.fetch(paymentLinkId);

        Map<String, Object> details = new HashMap<>();
        details.put("id", paymentLink.get("id"));
        details.put("shortUrl", paymentLink.get("short_url"));
        details.put("amount", paymentLink.get("amount"));
        details.put("currency", paymentLink.get("currency"));
        details.put("status", paymentLink.get("status"));
        details.put("description", paymentLink.get("description"));
        details.put("createdAt", paymentLink.get("created_at"));

        return details;
    }

    /**
     * Cancel payment link
     */
    public Map<String, Object> cancelPaymentLink(String paymentLinkId) throws RazorpayException {
        com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.cancel(paymentLinkId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", paymentLink.get("id"));
        response.put("status", paymentLink.get("status"));
        response.put("cancelledAt", paymentLink.get("cancelled_at"));

        return response;
    }

    /**
     * Get all subscription plans
     */
    public List<Map<String, Object>> getAllPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
        List<Map<String, Object>> planList = new ArrayList<>();

        for (SubscriptionPlan plan : plans) {
            Map<String, Object> planInfo = new HashMap<>();
            planInfo.put("id", plan.getId());
            planInfo.put("planCode", plan.getPlanCode());
            planInfo.put("name", plan.getName());
            planInfo.put("description", plan.getDescription());
            planInfo.put("price", plan.getPrice());
            planInfo.put("interval", plan.getInterval());
            planInfo.put("features", plan.getFeatures());
            planInfo.put("maxContent", plan.getMaxContent());
            planInfo.put("maxPlaylists", plan.getMaxPlaylists());
            planInfo.put("maxDevices", plan.getMaxDevices());
            planInfo.put("maxGroups", plan.getMaxGroups());
            planList.add(planInfo);
        }

        return planList;
    }

    /**
     * Create checkout session for Razorpay hosted payment page
     */
    public Map<String, Object> createCheckoutSession(String userId, String planId) throws RazorpayException {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByHexId(planId);

        if (userOpt.isEmpty() || planOpt.isEmpty()) {
            throw new IllegalArgumentException("User or plan not found");
        }

        User user = userOpt.get();
        SubscriptionPlan plan = planOpt.get();

        // Create payment link for checkout session
        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", Math.round(plan.getPrice() * 100)); // Amount in paise
        paymentLinkRequest.put("currency", "INR");
        paymentLinkRequest.put("description", "Subscription for " + plan.getName());

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("name", user.getName());
        customer.put("email", user.getEmail());
        paymentLinkRequest.put("customer", customer);

        // Callback URLs
        JSONObject notify = new JSONObject();
        notify.put("email", true);
        notify.put("sms", false);
        paymentLinkRequest.put("notify", notify);

        // Custom callback URL with userId and planId
        String callbackUrl = frontendDomain + "/payment-success?userId=" + userId + "&planId=" + planId;
        paymentLinkRequest.put("callback_url", callbackUrl);
        paymentLinkRequest.put("callback_method", "get");

        // Custom notes
        JSONObject notes = new JSONObject();
        notes.put("userId", userId);
        notes.put("planId", planId);
        notes.put("planName", plan.getName());
        paymentLinkRequest.put("notes", notes);

        // Create payment link (acts as checkout session)
        com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("checkoutUrl", paymentLink.get("short_url"));
        response.put("paymentLinkId", paymentLink.get("id"));
        response.put("amount", plan.getPrice());
        response.put("currency", "INR");
        response.put("status", paymentLink.get("status"));
        response.put("description", paymentLink.get("description"));
        response.put("userName", user.getName());
        response.put("userEmail", user.getEmail());
        response.put("planName", plan.getName());
        response.put("callbackUrl", callbackUrl);

        return response;
    }
}
