// package com.mediaserver.security.oauth2;

// import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
// import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
// import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
// import org.springframework.stereotype.Component;
// import org.springframework.util.SerializationUtils;

// import javax.servlet.http.Cookie;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.util.Base64;
// import java.util.HashMap;
// import java.util.Map;

// @Component
// public class CustomStatelessAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

//     private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
//     private static final String REDIRECT_URI_PARAM_COOKIE_NAME = "oauth2_redirect_uri";  // For custom redirect if needed
//     private static final int COOKIE_EXPIRATION_SECONDS = 180;  // 3 minutes, matching typical state TTL

//     @Override
//     public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
//         String encodedRequest = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
//         if (encodedRequest == null) {
//             return null;
//         }
//         byte[] bytes = Base64.getUrlDecoder().decode(encodedRequest);
//         OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
//         return authorizationRequest;
//     }

//     @Override
//     public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
//         if (authorizationRequest == null) {
//             deleteCookies(response);
//             return;
//         }

//         byte[] bytes = SerializationUtils.serialize(authorizationRequest);
//         String encodedRequest = Base64.getUrlEncoder().encodeToString(bytes);
//         Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, encodedRequest);
//         cookie.setPath("/");
//         cookie.setHttpOnly(true);  // Secure against XSS
//         cookie.setSecure(false);  // Set to true for HTTPS in prod
//         cookie.setMaxAge(COOKIE_EXPIRATION_SECONDS);
//         response.addCookie(cookie);

//         // Optional: Store redirect URI if custom
//         String redirectUri = request.getParameter(OAuth2ParameterNames.REDIRECT_URI);
//         if (redirectUri != null) {
//             Cookie redirectUriCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
//             redirectUriCookie.setPath("/");
//             redirectUriCookie.setHttpOnly(true);
//             redirectUriCookie.setSecure(false);
//             redirectUriCookie.setMaxAge(COOKIE_EXPIRATION_SECONDS);
//             response.addCookie(redirectUriCookie);
//         }
//     }

//     @Override
//     @Deprecated
//     public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
//         return loadAuthorizationRequest(request);  // Load and implicitly delete via save(null)
//     }

//     private void deleteCookies(HttpServletResponse response) {
//         Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, null);
//         cookie.setPath("/");
//         cookie.setHttpOnly(true);
//         cookie.setSecure(false);
//         cookie.setMaxAge(0);
//         response.addCookie(cookie);

//         Cookie redirectUriCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, null);
//         redirectUriCookie.setPath("/");
//         redirectUriCookie.setHttpOnly(true);
//         redirectUriCookie.setSecure(false);
//         redirectUriCookie.setMaxAge(0);
//         response.addCookie(redirectUriCookie);
//     }

//     private String getCookie(HttpServletRequest request, String name) {
//         if (request.getCookies() != null) {
//             for (Cookie cookie : request.getCookies()) {
//                 if (name.equals(cookie.getName())) {
//                     return cookie.getValue();
//                 }
//             }
//         }
//         return null;
//     }
// }

// CustomStatelessAuthorizationRequestRepository.java
package com.mediaserver.security.oauth2;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomStatelessAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final String REDIRECT_URI_PARAM_COOKIE_NAME = "oauth2_redirect_uri";
    private static final int COOKIE_EXPIRATION_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String encodedRequest = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (encodedRequest == null) {
            return null;
        }
        byte[] bytes = Base64.getUrlDecoder().decode(encodedRequest);
        OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
        return authorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookies(response);
            return;
        }

        byte[] bytes = SerializationUtils.serialize(authorizationRequest);
        String encodedRequest = Base64.getUrlEncoder().encodeToString(bytes);
        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, encodedRequest);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);  // Set to true for HTTPS in prod
        cookie.setMaxAge(COOKIE_EXPIRATION_SECONDS);
        response.addCookie(cookie);

        String redirectUri = request.getParameter(OAuth2ParameterNames.REDIRECT_URI);
        if (redirectUri != null) {
            Cookie redirectUriCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
            redirectUriCookie.setPath("/");
            redirectUriCookie.setHttpOnly(true);
            redirectUriCookie.setSecure(false);
            redirectUriCookie.setMaxAge(COOKIE_EXPIRATION_SECONDS);
            response.addCookie(redirectUriCookie);
        }
    }

    @Override
    @Deprecated
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return loadAuthorizationRequest(request);
    }

    private void deleteCookies(HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Cookie redirectUriCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, null);
        redirectUriCookie.setPath("/");
        redirectUriCookie.setHttpOnly(true);
        redirectUriCookie.setSecure(false);
        redirectUriCookie.setMaxAge(0);
        response.addCookie(redirectUriCookie);
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}