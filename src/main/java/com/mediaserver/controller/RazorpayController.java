package com.mediaserver.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.service.RazorpayService;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/razorpay")
public class RazorpayController {

    @Autowired
    private RazorpayService razorpayService;

    /**
     * Create Razorpay order for checkout
     * POST /razorpay/create-order
     * Body: { "userId": "xxx", "planId": "xxx" }
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String planId = request.get("planId");

            if (userId == null || planId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId and planId are required"));
            }

            Map<String, Object> orderDetails = razorpayService.createOrder(userId, planId);
            return ResponseEntity.ok(orderDetails);

        } catch (RazorpayException e) {
            System.err.println("Razorpay order creation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create order: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Verify payment and activate subscription
     * POST /razorpay/verify-payment
     * Body: {
     *   "userId": "xxx",
     *   "orderId": "order_xxx",
     *   "paymentId": "pay_xxx",
     *   "paymentLinkId": "plink_xxx",
     *   "razorpay_payment_link_status": "paid"
     * }
     */
    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String orderId = (String) request.get("orderId");
            String paymentId = (String) request.get("paymentId");
            String paymentLinkId = (String) request.get("paymentLinkId");
            String paymentLinkStatus = (String) request.get("razorpay_payment_link_status");

            if (userId == null || paymentId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "userId and paymentId are required"
                ));
            }

            System.out.println("Verifying payment:");
            System.out.println("User ID: " + userId);
            System.out.println("Order ID: " + orderId);
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Payment Link ID: " + paymentLinkId);
            System.out.println("Payment Link Status: " + paymentLinkStatus);

            // Fetch payment details from Razorpay API
            Map<String, Object> paymentDetails = razorpayService.getPaymentDetails(paymentId);
            String status = (String) paymentDetails.get("status");

            System.out.println("Fetched Payment Status: " + status);

            // Check if payment is captured or authorized
            if (!"captured".equals(status) && !"authorized".equals(status)) {
                System.err.println("Payment not captured or authorized. Status: " + status);
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "error", "Payment not completed. Status: " + status
                ));
            }

            // Additional check for payment link status if provided
            if (paymentLinkStatus != null && !"paid".equals(paymentLinkStatus)) {
                System.err.println("Payment link status not paid. Status: " + paymentLinkStatus);
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "error", "Payment link not paid. Status: " + paymentLinkStatus
                ));
            }

            // Process the payment
            razorpayService.processSuccessfulPayment(userId, orderId, paymentLinkId, paymentId, null);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "payment processed"
            ));

        } catch (RazorpayException e) {
            System.err.println("Razorpay verification failed: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to verify payment: " + e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Internal server error"
            ));
        }
    }

    /**
     * Webhook endpoint for Razorpay events
     * POST /razorpay/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            razorpayService.processWebhookEvent(payload, signature);
            return ResponseEntity.ok("Webhook processed");
        } catch (SecurityException e) {
            System.err.println("Webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid signature");
        } catch (Exception e) {
            System.err.println("Webhook processing failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body("Webhook processing failed");
        }
    }

    /**
     * Get payment details
     * GET /razorpay/payment/{paymentId}
     */
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentDetails(@PathVariable String paymentId) {
        try {
            Map<String, Object> details = razorpayService.getPaymentDetails(paymentId);
            return ResponseEntity.ok(details);
        } catch (RazorpayException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Payment not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Refund a payment
     * POST /razorpay/refund
     * Body: { "paymentId": "pay_xxx", "amount": 199.00 }
     */
    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(@RequestBody Map<String, Object> request) {
        try {
            String paymentId = (String) request.get("paymentId");
            Double amount = request.get("amount") != null ?
                Double.parseDouble(request.get("amount").toString()) : null;

            if (paymentId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "paymentId is required"));
            }

            Map<String, Object> refundDetails = razorpayService.refundPayment(paymentId, amount);
            return ResponseEntity.ok(refundDetails);

        } catch (RazorpayException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Refund failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Create payment link for backend checkout
     * POST /razorpay/create-payment-link
     * Body: { "userId": "xxx", "planId": "xxx" }
     */
    @PostMapping("/create-payment-link")
    public ResponseEntity<Map<String, Object>> createPaymentLink(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String planId = request.get("planId");

            if (userId == null || planId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId and planId are required"));
            }

            Map<String, Object> paymentLinkDetails = razorpayService.createPaymentLink(userId, planId);
            return ResponseEntity.ok(paymentLinkDetails);

        } catch (RazorpayException e) {
            System.err.println("Razorpay payment link creation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create payment link: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get payment link details
     * GET /razorpay/payment-link/{paymentLinkId}
     */
    @GetMapping("/payment-link/{paymentLinkId}")
    public ResponseEntity<Map<String, Object>> getPaymentLinkDetails(@PathVariable String paymentLinkId) {
        try {
            Map<String, Object> details = razorpayService.getPaymentLinkDetails(paymentLinkId);
            return ResponseEntity.ok(details);
        } catch (RazorpayException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Payment link not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Cancel payment link
     * POST /razorpay/cancel-payment-link
     * Body: { "paymentLinkId": "plink_xxx" }
     */
    
    /**
     * Cancel payment link
     * POST /razorpay/cancel-payment-link
     * Body: { "paymentLinkId": "plink_xxx" }
     */
    @PostMapping("/cancel-payment-link")
    public ResponseEntity<Map<String, Object>> cancelPaymentLink(@RequestBody Map<String, String> request) {
        try {
            String paymentLinkId = request.get("paymentLinkId");

            if (paymentLinkId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "paymentLinkId is required"));
            }

            Map<String, Object> response = razorpayService.cancelPaymentLink(paymentLinkId);
            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to cancel payment link: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get all subscription plans
     * GET /razorpay/plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<Map<String, Object>>> getAllPlans() {
        try {
            List<Map<String, Object>> plans = razorpayService.getAllPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            System.err.println("Error fetching plans: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Create checkout session for Razorpay hosted payment page
     * POST /razorpay/create-checkout-session
     * Body: { "userId": "xxx", "planId": "xxx" }
     */
    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String planId = request.get("planId");

            if (userId == null || planId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId and planId are required"));
            }

            Map<String, Object> checkoutSession = razorpayService.createCheckoutSession(userId, planId);
            return ResponseEntity.ok(checkoutSession);

        } catch (RazorpayException e) {
            System.err.println("Razorpay checkout session creation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create checkout session: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Handle Razorpay payment success callback
     * GET /razorpay/payment-success
     * Query params: userId, planId, razorpay_payment_id, razorpay_payment_link_id, razorpay_signature
     *
     * NOTE: For payment link redirects, Razorpay does NOT provide a verifiable signature.
     * We verify the payment by fetching it from Razorpay API instead.
     */
    @GetMapping("/payment-success")
    public ResponseEntity<Map<String, Object>> handlePaymentSuccess(
            @RequestParam String userId,
            @RequestParam String planId,
            @RequestParam String razorpay_payment_id,
            @RequestParam(required = false) String razorpay_payment_link_id,
            @RequestParam(required = false) String razorpay_payment_link_status,
            @RequestParam(required = false) String razorpay_signature) {
        try {
            System.out.println("=== Payment Success Callback ===");
            System.out.println("User ID: " + userId);
            System.out.println("Plan ID: " + planId);
            System.out.println("Payment ID: " + razorpay_payment_id);
            System.out.println("Payment Link ID: " + razorpay_payment_link_id);
            System.out.println("Payment Link Status: " + razorpay_payment_link_status);

            // For payment link callbacks, verify by fetching payment details from Razorpay API
            // instead of signature verification (which doesn't work for payment link redirects)
            Map<String, Object> paymentDetails = razorpayService.getPaymentDetails(razorpay_payment_id);
            String paymentStatus = (String) paymentDetails.get("status");

            System.out.println("Payment Status from API: " + paymentStatus);

            if (!"captured".equals(paymentStatus) && !"authorized".equals(paymentStatus)) {
                System.err.println("Payment not successful. Status: " + paymentStatus);
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "error", "Payment not completed. Status: " + paymentStatus
                ));
            }

            // Process the successful payment
            razorpayService.processSuccessfulPayment(
                userId,
                null, // No order ID for payment links
                razorpay_payment_link_id,
                razorpay_payment_id,
                razorpay_signature
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment processed successfully"
            ));

        } catch (RazorpayException e) {
            System.err.println("Razorpay payment processing failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to process payment: " + e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Internal server error"
            ));
        }
    }
}  // End of RazorpayController class
