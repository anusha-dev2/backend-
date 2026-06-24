// package com.mediaserver.security;

// import java.security.Key;
// import java.util.Date;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.ExpiredJwtException;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.MalformedJwtException;
// import io.jsonwebtoken.SignatureException;
// import io.jsonwebtoken.UnsupportedJwtException;
// import io.jsonwebtoken.security.Keys;

// @Component
// public class RootJwtTokenProvider {

//     private static final Logger logger = LoggerFactory.getLogger(RootJwtTokenProvider.class);

//     @Value("${app.root.jwt.secret}")
//     private String rootJwtSecret;

//     @Value("${app.root.jwt.expiration}")
//     private int rootJwtExpirationInMs;

//     public String generateToken(Authentication authentication) {
//         UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

//         Date now = new Date();
//         Date expiryDate = new Date(now.getTime() + rootJwtExpirationInMs);

//         Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());

//         return Jwts.builder()
//                 .setSubject(userPrincipal.getUsername())
//                 .setIssuedAt(now)
//                 .setExpiration(expiryDate)
//                 .signWith(key)
//                 .compact();
//     }

//     public String generateToken(String username) {
//         Date now = new Date();
//         Date expiryDate = new Date(now.getTime() + rootJwtExpirationInMs);
//         Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());

//         return Jwts.builder()
//                 .setSubject(username)
//                 .setIssuedAt(now)
//                 .setExpiration(expiryDate)
//                 .signWith(key)
//                 .compact();
//     }

//     public String getUsernameFromJWT(String token) {
//         Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());

//         Claims claims = Jwts.parserBuilder()
//                 .setSigningKey(key)
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();

//         return claims.getSubject();
//     }

//     public boolean validateToken(String authToken) {
//         try {
//             Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());
//             Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
//             return true;
//         } catch (SignatureException ex) {
//             logger.error("Invalid Root JWT signature");
//         } catch (MalformedJwtException ex) {
//             logger.error("Invalid Root JWT token");
//         } catch (ExpiredJwtException ex) {
//             logger.error("Expired Root JWT token");
//         } catch (UnsupportedJwtException ex) {
//             logger.error("Unsupported Root JWT token");
//         } catch (IllegalArgumentException ex) {
//             logger.error("Root JWT claims string is empty");
//         }
//         return false;
//     }
// }

package com.mediaserver.security;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

import javax.annotation.PostConstruct;

@Component
public class RootJwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(RootJwtTokenProvider.class);

    @Value("${app.root.jwt.secret}")
    private String rootJwtSecret;

    @Value("${app.root.jwt.expiration}")
    private int rootJwtExpirationInMs;

    /**
     * Validates the JWT secret on application startup
     */
    @PostConstruct
    public void init() {
        if (rootJwtSecret == null || rootJwtSecret.isEmpty()) {
            logger.error("Root JWT secret is not configured! Check your application.properties or environment variables.");
            throw new IllegalStateException("Root JWT secret must be configured");
        }
        
        // Check if secret is strong enough (at least 256 bits = 32 bytes = 43 base64 chars minimum)
        if (rootJwtSecret.length() < 32) {
            logger.warn("Root JWT secret is too short! It should be at least 256 bits (32 characters) for HS256.");
        }
        
        logger.info("Root JWT Token Provider initialized successfully");
        logger.debug("Root JWT Secret length: {} characters", rootJwtSecret.length());
        logger.debug("Root JWT Expiration: {} ms ({} hours)", rootJwtExpirationInMs, rootJwtExpirationInMs / 3600000);
    }

    /**
     * Generate JWT token from Authentication object
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateToken(userPrincipal.getUsername());
    }

    /**
     * Generate JWT token from username
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + rootJwtExpirationInMs);
        
        Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();

        logger.debug("Generated root JWT token for user: {}", username);
        logger.debug("Token issued at: {}, expires at: {}", now, expiryDate);

        return token;
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromJWT(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            logger.debug("Extracted username from root JWT: {}", username);
            
            return username;
        } catch (Exception e) {
            logger.error("Failed to extract username from root JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate JWT token with enhanced debugging
     */
    public boolean validateToken(String authToken) {
        try {
            Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());
            
            logger.debug("Validating root JWT token...");
            logger.debug("Root JWT secret length: {} bytes", rootJwtSecret.getBytes().length);
            logger.debug("Token length: {} characters", authToken != null ? authToken.length() : 0);
            
            // Parse and validate the token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();
            
            // Additional validation checks
            Date now = new Date();
            Date expiration = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();
            String subject = claims.getSubject();
            
            logger.debug("Root JWT validation SUCCESS");
            logger.debug("Subject: {}", subject);
            logger.debug("Issued at: {}", issuedAt);
            logger.debug("Expires at: {}", expiration);
            logger.debug("Current time: {}", now);
            logger.debug("Token is valid for: {} more minutes", (expiration.getTime() - now.getTime()) / 60000);
            
            return true;
            
        } catch (SignatureException ex) {
            logger.error("Invalid Root JWT signature - The token signature does not match!");
            logger.error("This usually means the token was signed with a different secret key");
            logger.debug("Token prefix: {}", authToken != null && authToken.length() > 20 ? authToken.substring(0, 20) + "..." : "null");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid Root JWT token - Malformed token structure");
            logger.error("Token might be corrupted or not a valid JWT format");
            logger.debug("Error details: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired Root JWT token");
            logger.error("Token was issued at: {}", ex.getClaims().getIssuedAt());
            logger.error("Token expired at: {}", ex.getClaims().getExpiration());
            logger.error("Current time: {}", new Date());
            logger.error("Token expired {} minutes ago", 
                (new Date().getTime() - ex.getClaims().getExpiration().getTime()) / 60000);
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported Root JWT token - Token format not supported");
            logger.debug("Error details: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Root JWT claims string is empty or null");
            logger.debug("Token value: {}", authToken != null ? "present" : "null");
        } catch (Exception ex) {
            logger.error("Unexpected error validating Root JWT token: {}", ex.getClass().getSimpleName());
            logger.error("Error message: {}", ex.getMessage());
            logger.debug("Stack trace: ", ex);
        }
        
        return false;
    }

    /**
     * Get expiration date from token (useful for debugging)
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("Failed to extract expiration date from root JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if token is expired (without throwing exception)
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return true;
            }
            boolean expired = expiration.before(new Date());
            logger.debug("Token expired status: {}", expired);
            return expired;
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Get remaining validity time in milliseconds
     */
    public long getRemainingValidityTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return 0;
            }
            long remaining = expiration.getTime() - new Date().getTime();
            return Math.max(0, remaining);
        } catch (Exception e) {
            logger.error("Error calculating remaining validity time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get issued at date from token
     */
    public Date getIssuedAtDateFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(rootJwtSecret.getBytes());
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getIssuedAt();
        } catch (Exception e) {
            logger.error("Failed to extract issued at date from root JWT token: {}", e.getMessage());
            return null;
        }
    }
}