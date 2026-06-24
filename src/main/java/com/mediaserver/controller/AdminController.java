package com.mediaserver.controller;

import com.mediaserver.model.User;
import com.mediaserver.payload.ApiResponse;
import com.mediaserver.service.UserService;
import com.mediaserver.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    
    /**
     * Suspend a user account.
     * Only accessible by root/admin users.
     */
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable String userId, @RequestBody Map<String, String> request) {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied. Admin privileges required."));
        }

        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Suspension reason is required."));
        }

        try {
            userService.suspendUser(userId, reason);
            return ResponseEntity.ok(new ApiResponse(true, "User suspended successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to suspend user: " + e.getMessage()));
        }
    }

    /**
     * Unsuspend a user account.
     * Only accessible by root/admin users.
     */
    @PostMapping("/users/{userId}/unsuspend")
    public ResponseEntity<?> unsuspendUser(@PathVariable String userId) {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied. Admin privileges required."));
        }

        try {
            userService.unsuspendUser(userId);
            return ResponseEntity.ok(new ApiResponse(true, "User unsuspended successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to unsuspend user: " + e.getMessage()));
        }
    }

    /**
     * Get user suspension status.
     * Only accessible by root/admin users.
     */
    @GetMapping("/users/{userId}/suspension-status")
    public ResponseEntity<?> getUserSuspensionStatus(@PathVariable String userId) {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied. Admin privileges required."));
        }

        try {
            Map<String, Object> status = userService.getUserSuspensionStatus(userId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to get suspension status: " + e.getMessage()));
        }
    }

    /**
     * Get all suspended users.
     * Only accessible by root/admin users.
     */
    @GetMapping("/users/suspended")
    public ResponseEntity<?> getSuspendedUsers() {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied. Admin privileges required."));
        }

        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> suspendedUsers = allUsers.stream()
                    .filter(user -> user.isSuspended() || user.isInactive())
                    .toList();

            return ResponseEntity.ok(suspendedUsers);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to retrieve suspended users: " + e.getMessage()));
        }
    }

    /**
     * Manually trigger suspension of inactive users.
     * Only accessible by root/admin users.
     */
    @PostMapping("/maintenance/suspend-inactive")
    public ResponseEntity<?> suspendInactiveUsers() {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied. Admin privileges required."));
        }

        try {
            userService.suspendInactiveUsers();
            return ResponseEntity.ok(new ApiResponse(true, "Inactive users suspension check completed."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to suspend inactive users: " + e.getMessage()));
        }
    }
}
