package com.mediaserver.controller;

import com.mediaserver.model.Payment;
import com.mediaserver.model.Subscription;
import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam String userId, @RequestParam String planId) throws StripeException {
        Map<String, String> result = paymentService.createCheckoutSession(userId, planId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.processWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            System.err.println("Webhook processing failed: " + e.getMessage());
            return ResponseEntity.status(400).body("Webhook processing failed");
        }
    }

    @PostMapping("/process-successful-payment")
    public ResponseEntity<String> processSuccessfulPayment(@RequestBody Map<String, String> request) throws StripeException {
        String userId = request.get("userId");
        String subscriptionId = request.get("subscriptionId");
        paymentService.processSuccessfulPayment(userId, subscriptionId);
        return ResponseEntity.ok("Payment processed");
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        List<SubscriptionPlan> plans = paymentService.getAllSubscriptionPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/available")
    public ResponseEntity<List<SubscriptionPlan>> getAvailablePlans(@RequestParam String userId) {
        List<SubscriptionPlan> plans = paymentService.getAvailableSubscriptionPlans(userId);
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/plans")
    public ResponseEntity<SubscriptionPlan> createPlan(@RequestBody SubscriptionPlan plan) {
        SubscriptionPlan savedPlan = paymentService.createSubscriptionPlan(plan);
        return ResponseEntity.ok(savedPlan);
    }

    @GetMapping("/user/{userId}/payments")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable String userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}/subscription")
    public ResponseEntity<Subscription> getUserSubscription(@PathVariable String userId) {
        Subscription subscription = paymentService.getUserSubscription(userId);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/cancel-subscription")
    public ResponseEntity<String> cancelSubscription(@RequestBody Map<String, String> request) throws StripeException {
        String userId = request.get("userId");
        boolean success = paymentService.cancelSubscription(userId);
        return ResponseEntity.ok(success ? "Subscription cancelled" : "Failed to cancel");
    }

    @PostMapping("/billing-portal")
    public ResponseEntity<Map<String, String>> createBillingPortal(@RequestBody Map<String, String> request) throws StripeException {
        String userId = request.get("userId");
        String url = paymentService.createBillingPortalSession(userId);
        Map<String, String> result = Map.of("url", url);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<Subscription> getSubscriptionByStripeId(@PathVariable String subscriptionId) {
        Subscription subscription = paymentService.getSubscriptionByStripeId(subscriptionId);
        if (subscription != null) {
            return ResponseEntity.ok(subscription);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/success")
    public ResponseEntity<Map<String, Object>> handlePaymentSuccess(@RequestParam String session_id) throws StripeException {
        Map<String, Object> result = paymentService.handlePaymentSuccess(session_id);
        return ResponseEntity.ok(result);
    }

    // @GetMapping("/subscription-history")
    // public ResponseEntity<List<Subscription>> getSubscriptionHistory(@RequestParam String userId) {
    //     List<Subscription> history = paymentService.getSubscriptionHistory(userId);
    //     return ResponseEntity.ok(history);
    // }
}
