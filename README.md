# Media Server Application

A Spring Boot application for media server management with Stripe payment integration.

## Features

- User authentication and authorization
- Content management
- Device management
- Playlist management
- Stripe payment processing for subscriptions
- Real-time notifications via WebSocket

## Payment Testing Flow

The payment system uses Stripe's hosted checkout page to handle payments. This bypasses frontend redirects and allows direct testing with test cards.

### Testing Steps

1. **Create a Checkout Session**
   - Use the `/payment/create-checkout-session` endpoint to create a Stripe checkout session.
   - Requires: `planId` (subscription plan ID) and `Authorization` header with JWT token.
   - Returns: `sessionId`

2. **Access Stripe Checkout**
   - Navigate to: `https://checkout.stripe.com/pay/{sessionId}`
   - This opens Stripe's hosted checkout page.

3. **Complete Payment**
   - Use Stripe test cards (e.g., 4242 4242 4242 4242 with any future expiry and CVC).
   - Complete the payment form.

4. **Post-Payment Redirect**
   - **Success**: Redirects to `/payment/success?session_id={CHECKOUT_SESSION_ID}`
     - This endpoint fetches session details from Stripe.
     - Calls `PaymentService.processOneTimePayment(userId, paymentIntentId)` to record the payment.
     - Returns success response with payment details.

   - **Cancel**: Redirects to `/payment/cancel`
     - Returns cancellation message.

### Test Cards

- **Success**: 4242 4242 4242 4242
- **Decline**: 4000 0000 0000 0002
- **Require Authentication**: 4000 0025 0000 3155

All test cards accept any future expiry date and any 3-digit CVC.

## Running the Application

1. Ensure you have Java 11+ and Maven installed.
2. Set up environment variables:
   - `STRIPE_API_KEY`: Your Stripe test/live API key
   - `APP_DOMAIN`: Your application domain (e.g., http://localhost:9000)
3. Run `mvn spring-boot:run`

## API Documentation

Use the provided Postman collections:
- `Stripe_Payment_API.postman_collection.json`
- `GroupEndpoints.postman_collection.json`
- `MediaServerApplication.java.postman_collection.json`
- `PaymentService_Postman_Collection.json`
- `Playlist_API.postman_collection.json`
- `Root_User_API.postman_collection.json`

## Database

The application uses MongoDB. Configure the connection in `application.properties`.
