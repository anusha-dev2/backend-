// User.java
package com.mediaserver.model;

import java.time.Instant;


import org.apache.tika.config.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document; 

import lombok.Data;
import ucar.nc2.stream.NcStreamProto.EnumTypedef.EnumType; 


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    private String name;

    private String razorpayCustomerId;

    private String status = "ACTIVE";

    private Instant lastLoginDate;

    private String suspensionReason;

    private Instant suspensionDate;
    
    // setting page

    private String lastName;
    private String jobTitle;
    private String phone;
    private String timezone;
    private String language;

    // Company Info
    private String companyName;
    private String companyIndustry;
    private String companySize;
    private String companyWebsite;
    private String companyLogoUrl;
    
    // Company Address
    private String companyAddress;
    private String companyCity;
    private String companyState;
    private String companyZipCode;
    private String companyCountry;

    
    @Indexed(unique = true)
    private String username;

    private String password;
    
    @Indexed(unique = true)
    private String email;
    
    private String imageUrl; // This is the user's avatarUrl

    private Boolean emailVerified = false;
    
    // private AuthProvider provider = AuthProvider.GOOGLE;
    private AuthProvider provider = AuthProvider.LOCAL;
    
    private String providerId;
    
    private String stripeCustomerId;
    
    // Optional subscription tier that can be used for feature control
    private String subscriptionTier = "FREE"; // FREE, BASIC, PREMIUM, etc.

    // Track if user has already used the free plan once
    private Boolean hasUsedFreePlan = false;

    private String resetPasswordToken;
    
   private Instant resetPasswordTokenExpiry;
  
    // Soft deletion field
    // private Instant deletionDate;

    // Soft deletion fields
    private Boolean isDeleted = false;
    private Instant deletionDate;
    private String deletionReason;

    // Helper methods for soft deletion
    public boolean isAccountMarkedForDeletion() {
        return Boolean.TRUE.equals(isDeleted) && deletionDate != null;
    }

    public boolean isPermanentDeletionDue() {
        if (!isAccountMarkedForDeletion()) {
            return false;
        }
        // Check if 15 days have passed since deletion date
        Instant fifteenDaysAfterDeletion = deletionDate.plus(15, java.time.temporal.ChronoUnit.DAYS);
        return Instant.now().isAfter(fifteenDaysAfterDeletion);
    }

    public void markForDeletion(String reason) {
        this.isDeleted = true;
        this.deletionDate = Instant.now();
        this.deletionReason = reason != null ? reason : "User requested account deletion";
    }

    public void recoverAccount() {
        this.isDeleted = false;
        this.deletionDate = null;
        this.deletionReason = null;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Suspension helper methods
    public boolean isSuspended() {
        return "SUSPENDED".equals(this.status);
    }

    public boolean isInactive() {
        return "INACTIVE".equals(this.status);
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public void suspend(String reason) {
        this.status = "SUSPENDED";
        this.suspensionDate = Instant.now();
        this.suspensionReason = reason;
    }

    public void unsuspend() {
        this.status = "ACTIVE";
        this.suspensionDate = null;
        this.suspensionReason = null;
    }

    public void markInactive() {
        this.status = "INACTIVE";
    }

    // Getters and setters for new fields
    public Instant getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Instant lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getSuspensionReason() {
        return suspensionReason;
    }

    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    public Instant getSuspensionDate() {
        return suspensionDate;
    }

    public void setSuspensionDate(Instant suspensionDate) {
        this.suspensionDate = suspensionDate;
    }

}


