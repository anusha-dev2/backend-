// package com.mediaserver;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// @SpringBootTest
// @AutoConfigureMockMvc
// public class PaymentFlowTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Test
//     public void testPaymentSuccessEndpoint() throws Exception {
//         // Mock Stripe session retrieval (in real test, this would be mocked properly)
//         // For this unit test, we're just verifying the endpoint exists and responds

//         // This test demonstrates the testing flow:
//         // 1. Create checkout session via API
//         // 2. Use https://checkout.stripe.com/pay/{sessionId} for payment
//         // 3. Redirect to /payment/success processes the payment

//         mockMvc.perform(get("/payment/success")
//                 .param("session_id", "cs_test_123"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("success"));
//     }

//     @Test
//     public void testPaymentCancelEndpoint() throws Exception {
//         mockMvc.perform(get("/payment/cancel")
//                 .param("session_id", "cs_test_123"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("cancelled"));
//     }
// }
