
// // OAuth2AuthenticationSuccessHandler.java
// package com.mediaserver.security.oauth2;

// import com.mediaserver.security.JwtTokenProvider;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
// import org.springframework.stereotype.Component;
// import org.springframework.web.util.UriComponentsBuilder;

// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;

// @Component
// public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

//     @Autowired
//     private JwtTokenProvider tokenProvider;

//     @Value("${app.oauth2.redirectUri}")
//     private String redirectUri;

//     @Override
//     public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//         String targetUrl = determineTargetUrl(request, response, authentication);

//         if (response.isCommitted()) {
//             logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
//             return;
//         }

//         getRedirectStrategy().sendRedirect(request, response, targetUrl);
//     }

//     protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//         //String token = tokenProvider.generateToken(authentication);


//         // OLD
// //String token = tokenProvider.generateToken(authentication);

// // NEW – pass the e-mail as subject
// String email = ((org.springframework.security.oauth2.core.user.OAuth2User)
//                  authentication.getPrincipal())
//                  .getAttribute("email");
// String token = tokenProvider.generateToken(email);  
//         return UriComponentsBuilder.fromUriString(redirectUri)
//                 .queryParam("token", token)
//                 .build().toUriString();
//     }
// }

// today 16-9-2025

// OAuth2AuthenticationSuccessHandler.java
// OAuth2AuthenticationSuccessHandler.java
package com.mediaserver.security.oauth2;

import com.mediaserver.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${app.oauth2.redirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            String targetUrl = determineTargetUrl(request, response, authentication);

            if (response.isCommitted()) {
                logger.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
                return;
            }

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            logger.error("Failed to handle authentication success: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication success handling failed");
        }
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String email = ((org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal())
                    .getAttribute("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalStateException("Email not found in OAuth2 user attributes");
            }

            String token = tokenProvider.generateToken(email);
            logger.debug("Generated JWT token for email: {}", email);

            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", token)
                    .build().toUriString();
        } catch (Exception e) {
            logger.error("Error determining target URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate redirect URL", e);
        }
    }
}