package com.mediaserver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Document(collection = "root_user")
public class RootUser {

    @Id
    private String id;

    @NotBlank
    private String name;

    @Indexed(unique = true)
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Indexed(unique = true)
    @Email
    private String email;

    private String imageUrl;

    private Boolean emailVerified = false;

    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    // Optional fields if needed
    private String resetPasswordToken;

    private java.time.Instant resetPasswordTokenExpiry;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getEmailVerified() { return emailVerified; }
    public AuthProvider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public String getResetPasswordToken() { return resetPasswordToken; }
    public java.time.Instant getResetPasswordTokenExpiry() { return resetPasswordTokenExpiry; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }
    public void setResetPasswordTokenExpiry(java.time.Instant resetPasswordTokenExpiry) { this.resetPasswordTokenExpiry = resetPasswordTokenExpiry; }
}
