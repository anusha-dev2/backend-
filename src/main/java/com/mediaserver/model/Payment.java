// // Payment.java
// package com.mediaserver.model;

// import lombok.Data;
// import org.springframework.data.annotation.Id;
// import org.springframework.data.mongodb.core.mapping.Document;
// import java.time.LocalDateTime;

// @Data
// @Document(collection = "payments")
// public class Payment {
//     @Id
//     private String id;

//     private String userId;

//     private String subscriptionId; // Link to subscription for subscription payments

//     private Double amount;

//     private String currency;

//     private String paymentMethod;

//     private String status;

//     private String stripePaymentId;

//     private LocalDateTime createdAt;

//     // Getters and setters
//     public String getId() {
//         return id;
//     }

//     public void setId(String id) {
//         this.id = id;
//     }

//     public String getUserId() {
//         return userId;
//     }

//     public void setUserId(String userId) {
//         this.userId = userId;
//     }

//     public String getSubscriptionId() {
//         return subscriptionId;
//     }

//     public void setSubscriptionId(String subscriptionId) {
//         this.subscriptionId = subscriptionId;
//     }

//     public Double getAmount() {
//         return amount;
//     }

//     public void setAmount(Double amount) {
//         this.amount = amount;
//     }

//     public String getCurrency() {
//         return currency;
//     }

//     public void setCurrency(String currency) {
//         this.currency = currency;
//     }

//     public String getPaymentMethod() {
//         return paymentMethod;
//     }

//     public void setPaymentMethod(String paymentMethod) {
//         this.paymentMethod = paymentMethod;
//     }

//     public String getStatus() {
//         return status;
//     }

//     public void setStatus(String status) {
//         this.status = status;
//     }

//     public String getStripePaymentId() {
//         return stripePaymentId;
//     }

//     public void setStripePaymentId(String stripePaymentId) {
//         this.stripePaymentId = stripePaymentId;
//     }

//     public LocalDateTime getCreatedAt() {
//         return createdAt;
//     }

//     public void setCreatedAt(LocalDateTime createdAt) {
//         this.createdAt = createdAt;
//     }
// }


package com.mediaserver.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private ObjectId id;

    private String userId;

    private String subscriptionId; // Link to subscription for subscription payments (String hex for ref)

    private Double amount;

    private String currency;

    private String paymentMethod;

    private String status;

    private String stripePaymentId;

    private LocalDateTime createdAt;

    // Getters and setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStripePaymentId() {
        return stripePaymentId;
    }

    public void setStripePaymentId(String stripePaymentId) {
        this.stripePaymentId = stripePaymentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
