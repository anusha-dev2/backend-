// // OAuth2UserInfoFactory.java
// package com.mediaserver.security.oauth2;

// import com.mediaserver.exception.OAuth2AuthenticationProcessingException;
// import com.mediaserver.model.AuthProvider;

// import java.util.Map;

// public class OAuth2UserInfoFactory {

//     public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
//         if(registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
//             System.out.println(AuthProvider.GOOGLE.toString());
//             return new GoogleOAuth2UserInfo(attributes);
//         } else if (registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())) {
//             System.out.println(AuthProvider.GITHUB.toString());
//             return new GithubOAuth2UserInfo(attributes);
//         } else {
//             System.out.println("Sorry! Login with");

//             throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
//         }
//     }
// }

// OAuth2UserInfoFactory.java
// OAuth2UserInfoFactory.java
package com.mediaserver.security.oauth2;

import com.mediaserver.exception.OAuth2AuthenticationProcessingException;
import com.mediaserver.model.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OAuth2UserInfoFactory {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserInfoFactory.class);

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            logger.info("Creating GoogleOAuth2UserInfo for registrationId: {}", registrationId);
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())) {
            logger.info("Creating GithubOAuth2UserInfo for registrationId: {}", registrationId);
            return new GithubOAuth2UserInfo(attributes);
        } else {
            logger.warn("Unsupported registrationId: {}", registrationId);
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}