// package com.mediaserver.security;

// import java.io.IOException;

// import javax.servlet.FilterChain;
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.util.StringUtils;
// import org.springframework.web.filter.OncePerRequestFilter;

// public class RootJwtAuthenticationFilter extends OncePerRequestFilter {

//     @Autowired
//     @Qualifier("rootUserDetailsService")
//     private UserDetailsService rootUserDetailsService;

//     private final RootJwtTokenProvider rootTokenProvider;

//     private static final Logger logger = LoggerFactory.getLogger(RootJwtAuthenticationFilter.class);

//     public RootJwtAuthenticationFilter(RootJwtTokenProvider rootTokenProvider) {
//         this.rootTokenProvider = rootTokenProvider;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//             throws ServletException, IOException {
//         try {
//             String jwt = getJwtFromRequest(request);
//             logger.debug("Root JWT from request: {}", jwt != null ? "present" : "null");

//             if (StringUtils.hasText(jwt) && rootTokenProvider.validateToken(jwt)) {
//                 String username = rootTokenProvider.getUsernameFromJWT(jwt);

//                 UserDetails userDetails = rootUserDetailsService.loadUserByUsername(username);
//                 UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                         userDetails, null, userDetails.getAuthorities());
//                 authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

//                 SecurityContextHolder.getContext().setAuthentication(authentication);
//                 logger.info("Set root user authentication for user: {}", username);
//             }
//         } catch (Exception ex) {
//             logger.error("Could not set root user authentication in security context", ex);
//         }

//         filterChain.doFilter(request, response);
//     }

//     private String getJwtFromRequest(HttpServletRequest request) {
//         String bearerToken = request.getHeader("Root-Authorization");
//         if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//             return bearerToken.substring(7);
//         }
//         return null;
//     }
// }
package com.mediaserver.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class RootJwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("rootUserDetailsService")
    private UserDetailsService rootUserDetailsService;

    private final RootJwtTokenProvider rootTokenProvider;

    private static final Logger logger = LoggerFactory.getLogger(RootJwtAuthenticationFilter.class);
    private static final String HEADER_NAME = "Root-Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    public RootJwtAuthenticationFilter(RootJwtTokenProvider rootTokenProvider) {
        this.rootTokenProvider = rootTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (jwt == null) {
                logger.debug("No Root JWT token found in request to {} {}", method, requestURI);
            } else {
                logger.debug("Root JWT token found in request to {} {}", method, requestURI);
                logger.debug("Token length: {} characters", jwt.length());
                logger.debug("Token prefix: {}...", jwt.substring(0, Math.min(20, jwt.length())));
            }

            if (StringUtils.hasText(jwt)) {
                logger.debug("Attempting to validate Root JWT token...");
                
                // Check if token is expired first (without throwing exception)
                if (rootTokenProvider.isTokenExpired(jwt)) {
                    logger.warn("Root JWT token is expired for request to {} {}", method, requestURI);
                    long expiredMinutesAgo = -rootTokenProvider.getRemainingValidityTime(jwt) / 60000;
                    logger.warn("Token expired {} minutes ago", expiredMinutesAgo);
                } else {
                    long remainingMinutes = rootTokenProvider.getRemainingValidityTime(jwt) / 60000;
                    logger.debug("Token is valid for {} more minutes", remainingMinutes);
                }
                
                if (rootTokenProvider.validateToken(jwt)) {
                    logger.debug("Root JWT token validation successful");
                    
                    String username = rootTokenProvider.getUsernameFromJWT(jwt);
                    logger.debug("Extracted username from token: {}", username);

                    UserDetails userDetails = rootUserDetailsService.loadUserByUsername(username);
                    logger.debug("Loaded user details for: {}", username);
                    logger.debug("User authorities: {}", userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("✓ Set root user authentication for user: {} on {} {}", username, method, requestURI);
                    logger.debug("Authentication object: {}", authentication);
                } else {
                    logger.warn("✗ Root JWT token validation failed for {} {}", method, requestURI);
                }
            } else {
                logger.trace("No Root JWT token present in request to {} {}", method, requestURI);
            }
        } catch (Exception ex) {
            logger.error("✗ Could not set root user authentication in security context for {} {}", method, requestURI);
            logger.error("Error type: {}", ex.getClass().getSimpleName());
            logger.error("Error message: {}", ex.getMessage());
            logger.debug("Full stack trace: ", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from request header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        
        logger.trace("Checking for {} header", HEADER_NAME);
        
        if (bearerToken != null) {
            logger.trace("{} header value: {}", HEADER_NAME, 
                bearerToken.substring(0, Math.min(30, bearerToken.length())) + "...");
        }
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            String token = bearerToken.substring(TOKEN_PREFIX.length());
            logger.trace("Extracted token (length: {})", token.length());
            return token;
        }
        
        logger.trace("No valid token found in {} header", HEADER_NAME);
        return null;
    }

    /**
     * Log all request headers (for debugging purposes only)
     */
    private void logAllHeaders(HttpServletRequest request) {
        logger.debug("=== All Request Headers ===");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if (headerName.toLowerCase().contains("authorization")) {
                logger.debug("{}: {}...", headerName, 
                    headerValue.substring(0, Math.min(30, headerValue.length())));
            } else {
                logger.debug("{}: {}", headerName, headerValue);
            }
        }
        logger.debug("===========================");
    }
}