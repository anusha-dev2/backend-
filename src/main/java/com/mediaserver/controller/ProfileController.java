package com.mediaserver.controller;

import com.mediaserver.payload.ApiResponse;
import com.mediaserver.payload.UserProfileDto;
import com.mediaserver.service.UserService;
import com.mediaserver.security.RootUserPrincipal;
import com.mediaserver.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/settings" ) // A single base URL for all settings
public class ProfileController {

    private static final String ACCOUNT_DELETION_MESSAGE = "Account has been marked for deletion. You have 15 days to recover your account using your email and password.";

    @Autowired
    private UserService userService;

    // Updated authentication methods similar to DeviceController
    private boolean isRootUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"));
        }
        return false;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    /**
     * GET /api/settings/profile
     * Fetches all profile and company information for the current user.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getFullUserProfile() {
        if (isRootUser()) {
            // Root users do not have profiles in the regular user system
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } else {
            String username = getCurrentUsername();
            return ResponseEntity.ok(userService.getFullProfile(username));
        }
    }

    /**
     * GET /api/settings/profile/{userId}
     * Fetches all profile and company information for a specific user (root only).
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getFullUserProfile(@PathVariable String userId) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
        Optional<com.mediaserver.model.User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(userService.getFullProfile(user.get().getUsername()));
        } else {
            return ResponseEntity.notFound().build();
        }
        } catch (Exception e) {
            // Handle exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/settings/profile
     * Updates all profile and company information for the current user.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateFullUserProfile(@Valid @RequestBody UserProfileDto dto) {
        if (!isRootUser()) {
            String username = getCurrentUsername();
            return ResponseEntity.ok(userService.updateFullProfile(username, dto));
        } else {
            // For root user, allow updating any user's profile by userId
            // But since no userId is provided, perhaps update root user's own profile
            String username = getCurrentUsername();
            return ResponseEntity.ok(userService.updateFullProfile(username, dto));
        }
    }

    /**
     * PUT /api/settings/profile/{userId}
     * Updates all profile and company information for a specific user (root only).
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> updateFullUserProfile(@PathVariable String userId, @Valid @RequestBody UserProfileDto dto) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
        Optional<com.mediaserver.model.User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(userService.updateFullProfile(user.get().getUsername(), dto));
        } else {
            return ResponseEntity.notFound().build();
        }
        } catch (Exception e) {
            // Handle exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/settings/profile
     * Marks the user account for deletion (soft delete).
     * The account will be retained for 15 days before permanent deletion.
     * Expects a JSON body like: { "reason": "User requested account deletion" }
     */
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse> deleteUserAccount(@RequestBody(required = false) Map<String, String> request) {
        try {
            if (!isRootUser()) {
                String username = getCurrentUsername();
                String reason = (request != null) ? request.get("reason") : "User requested account deletion";
                userService.markAccountForDeletion(username, reason);
                return ResponseEntity.ok(new ApiResponse(true, ACCOUNT_DELETION_MESSAGE));
            } else {
                // For root user, allow deleting any user's account by userId
                // But since no userId is provided, perhaps delete root user's own account
                String username = getCurrentUsername();
                String reason = (request != null) ? request.get("reason") : "Root user requested account deletion";
                userService.markAccountForDeletion(username, reason);
                return ResponseEntity.ok(new ApiResponse(true, ACCOUNT_DELETION_MESSAGE));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to delete account: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/settings/profile/{userId}
     * Marks a specific user account for deletion (root only).
     * Expects a JSON body like: { "reason": "Admin requested account deletion" }
     */
    @DeleteMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse> deleteUserAccount(@PathVariable String userId, @RequestBody(required = false) Map<String, String> request) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Optional<com.mediaserver.model.User> user = userService.getUserById(userId);
            if (!user.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            String reason = (request != null) ? request.get("reason") : "Root user requested account deletion";
            userService.markAccountForDeletion(user.get().getUsername(), reason);
            return ResponseEntity.ok(new ApiResponse(true, ACCOUNT_DELETION_MESSAGE));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to delete account: " + e.getMessage()));
        }
    }

    /**
     * POST /api/settings/profile/recover
     * Recovers a user account that was marked for deletion.
     * This is only possible within the 15-day retention period.
     * Expects a JSON body like: { "email": "user@example.com", "password": "userpassword" }
     */
    @PostMapping("/profile/recover")
    public ResponseEntity<ApiResponse> recoverUserAccount(@RequestBody Map<String, String> request) {
        try {
            if (!isRootUser()) {
                String email = request.get("email");
                String password = request.get("password");

                if (email == null || password == null) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false,
                        "Email and password are required for account recovery"));
                }

                boolean recovered = userService.recoverAccount(email, password);

                if (recovered) {
                    return ResponseEntity.ok(new ApiResponse(true,
                        "Account has been successfully recovered. You can now log in normally."));
                } else {
                    return ResponseEntity.badRequest().body(new ApiResponse(false,
                        "Account recovery failed. Please check your credentials."));
                }
            } else {
                // For root user, allow recovering any user's account
                String email = request.get("email");
                String password = request.get("password");

                if (email == null || password == null) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false,
                        "Email and password are required for account recovery"));
                }

                boolean recovered = userService.recoverAccount(email, password);

                if (recovered) {
                    return ResponseEntity.ok(new ApiResponse(true,
                        "Account has been successfully recovered. You can now log in normally."));
                } else {
                    return ResponseEntity.badRequest().body(new ApiResponse(false,
                        "Account recovery failed. Please check your credentials."));
                }
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse(false,
                "Account recovery failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/settings/profile/recover/{userId}
     * Recovers a specific user account that was marked for deletion (root only).
     * Expects a JSON body like: { "email": "user@example.com", "password": "userpassword" }
     */
    @PostMapping("/profile/recover/{userId}")
    public ResponseEntity<ApiResponse> recoverUserAccount(@PathVariable String userId, @RequestBody Map<String, String> request) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Optional<com.mediaserver.model.User> user = userService.getUserById(userId);
            if (!user.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false,
                    "Email and password are required for account recovery"));
            }

            boolean recovered = userService.recoverAccount(email, password);

            if (recovered) {
                return ResponseEntity.ok(new ApiResponse(true,
                    "Account has been successfully recovered. You can now log in normally."));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(false,
                    "Account recovery failed. Please check your credentials."));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse(false,
                "Account recovery failed: " + e.getMessage()));
        }
    }

    /**
     * GET /api/settings/profile/deletion-status
     * Gets the deletion status and remaining days for recovery for the current user.
     */
    @GetMapping("/profile/deletion-status")
    public ResponseEntity<Map<String, Object>> getAccountDeletionStatus() {
        try {
            if (!isRootUser()) {
                String username = getCurrentUsername();
                Map<String, Object> status = userService.getAccountDeletionStatus(username);
                return ResponseEntity.ok(status);
            } else {
                // For root user, allow getting deletion status for any user
                String username = getCurrentUsername();
                Map<String, Object> status = userService.getAccountDeletionStatus(username);
                return ResponseEntity.ok(status);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", true,
                "message", "Failed to get deletion status: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/settings/profile/deletion-status/{userId}
     * Gets the deletion status for a specific user (root only).
     */
    @GetMapping("/profile/deletion-status/{userId}")
    public ResponseEntity<Map<String, Object>> getAccountDeletionStatus(@PathVariable String userId) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Optional<com.mediaserver.model.User> user = userService.getUserById(userId);
            if (!user.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            Map<String, Object> status = userService.getAccountDeletionStatus(user.get().getUsername());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", true,
                "message", "Failed to get deletion status: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/settings/profiles
     * Fetches all user profiles (root only).
     */
    @GetMapping("/profiles")
    public ResponseEntity<List<UserProfileDto>> getAllUserProfiles() {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<com.mediaserver.model.User> users = userService.getAllUsers();
            List<UserProfileDto> profiles = users.stream()
                .filter(user -> !user.isAccountMarkedForDeletion())
                .map(user -> {
                    UserProfileDto dto = new UserProfileDto();
                    dto.setFirstName(user.getName());
                    dto.setLastName(user.getLastName());
                    dto.setEmail(user.getEmail());
                    dto.setJobTitle(user.getJobTitle());
                    dto.setPhone(user.getPhone());
                    dto.setTimezone(user.getTimezone());
                    dto.setLanguage(user.getLanguage());
                    dto.setAvatarUrl(user.getImageUrl());
                    dto.setCompanyName(user.getCompanyName());
                    dto.setCompanyIndustry(user.getCompanyIndustry());
                    dto.setCompanySize(user.getCompanySize());
                    dto.setCompanyWebsite(user.getCompanyWebsite());
                    dto.setCompanyLogoUrl(user.getCompanyLogoUrl());
                    dto.setCompanyAddress(user.getCompanyAddress());
                    dto.setCompanyCity(user.getCompanyCity());
                    dto.setCompanyState(user.getCompanyState());
                    dto.setCompanyZipCode(user.getCompanyZipCode());
                    dto.setCompanyCountry(user.getCompanyCountry());
                    return dto;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/settings/update-password
     * Updates the password for the current user.
     * Expects a JSON body like: { "currentPassword": "...", "newPassword": "..." }
     */
    // @PostMapping("/update-password")
    // public ResponseEntity<ApiResponse> updatePassword(@RequestBody Map<String, String> passwordRequest) {
    //     try {
    //         String username = getCurrentUsername();
    //         userService.updateUserPassword(username, passwordRequest);
    //         return ResponseEntity.ok(new ApiResponse(true, "Password updated successfully"));
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    //     } catch (BadCredentialsException e) {
    //         return ResponseEntity.badRequest().body(new ApiResponse(false, "Incorrect current password"));
    //     } catch (IllegalStateException e) {
    //         return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().body(new ApiResponse(false, "Failed to update password"));
    //     }
    // }


}
