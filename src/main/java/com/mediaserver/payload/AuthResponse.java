package com.mediaserver.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private User user; // Renamed to follow Java naming conventions

    @Data
    @AllArgsConstructor
    public static class User {
        private String username;
        private String email;
        private String profileImage;
        private String userId;
    }

    public AuthResponse(String accessToken, String username, String email, String profileImage, String userId) {
        this.accessToken = accessToken;
        this.user = new User(username, email, profileImage, userId);
    }
}