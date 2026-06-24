package com.mediaserver.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.security.RootJwtTokenProvider;

/**
 * Debug controller to help diagnose JWT token issues
 * Remove this in production!
 */
@RestController
@RequestMapping("/root/auth/debug")
public class RootAuthTestController {

    private static final Logger logger = LoggerFactory.getLogger(RootAuthTestController.class);

    @Autowired
    private RootJwtTokenProvider rootTokenProvider;

    /**
     * Test endpoint to verify token validity
     * GET /root/auth/debug/verify-token
     */
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract token from header
            String bearerToken = request.getHeader("Root-Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                response.put("error", "No Root-Authorization header found or invalid format");
                response.put("authenticated", false);
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = bearerToken.substring(7);
            response.put("tokenPresent", true);
            response.put("tokenLength", token.length());
            response.put("tokenPrefix", token.substring(0, Math.min(20, token.length())) + "...");
            
            // Validate token
            boolean isValid = rootTokenProvider.validateToken(token);
            response.put("isValid", isValid);
            
            if (isValid) {
                // Get token details
                String username = rootTokenProvider.getUsernameFromJWT(token);
                Date expiration = rootTokenProvider.getExpirationDateFromToken(token);
                Date issuedAt = rootTokenProvider.getIssuedAtDateFromToken(token);
                long remainingTime = rootTokenProvider.getRemainingValidityTime(token);
                
                response.put("username", username);
                response.put("issuedAt", issuedAt);
                response.put("expiresAt", expiration);
                response.put("currentTime", new Date());
                response.put("remainingValidityMinutes", remainingTime / 60000);
                response.put("isExpired", rootTokenProvider.isTokenExpired(token));
                
                // Check authentication context
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                response.put("authenticationSet", auth != null && auth.isAuthenticated());
                if (auth != null) {
                    response.put("authenticatedUser", auth.getName());
                    response.put("authorities", auth.getAuthorities());
                }
            } else {
                response.put("message", "Token validation failed. Check server logs for details.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in token verification: ", e);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Check current authentication status
     * GET /root/auth/debug/auth-status
     */
    @GetMapping("/auth-status")
    public ResponseEntity<?> checkAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("authenticated", auth != null && auth.isAuthenticated());
        
        if (auth != null) {
            response.put("principal", auth.getPrincipal().getClass().getSimpleName());
            response.put("name", auth.getName());
            response.put("authorities", auth.getAuthorities());
            response.put("details", auth.getDetails());
        } else {
            response.put("message", "No authentication found in security context");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all request headers (for debugging)
     * GET /root/auth/debug/headers
     */
    @GetMapping("/headers")
    public ResponseEntity<?> getAllHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Truncate authorization headers for security
            if (headerName.toLowerCase().contains("authorization")) {
                headers.put(headerName, headerValue.substring(0, Math.min(30, headerValue.length())) + "...");
            } else {
                headers.put(headerName, headerValue);
            }
        }
        
        return ResponseEntity.ok(headers);
    }

    /**
     * Test if root authentication is working
     * GET /root/auth/debug/test-root-access
     */
    @GetMapping("/test-root-access")
    public ResponseEntity<?> testRootAccess() {
        Map<String, Object> response = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }
        
        boolean hasRootRole = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ROOT"));
        
        response.put("success", hasRootRole);
        response.put("authenticated", true);
        response.put("username", auth.getName());
        response.put("hasRootRole", hasRootRole);
        response.put("authorities", auth.getAuthorities());
        response.put("message", hasRootRole ? "Root access confirmed!" : "No root role found");
        
        return ResponseEntity.ok(response);
    }
}