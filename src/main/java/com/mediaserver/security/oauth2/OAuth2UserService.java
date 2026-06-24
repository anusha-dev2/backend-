// // OAuth2UserService.java
// package com.mediaserver.security.oauth2;

// import com.mediaserver.exception.OAuth2AuthenticationProcessingException;
// import com.mediaserver.model.AuthProvider;
// import com.mediaserver.model.User;
// import com.mediaserver.repository.UserRepository;
// import com.mediaserver.security.UserPrincipal;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.InternalAuthenticationServiceException;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
// import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
// import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.stereotype.Service;
// import org.springframework.util.StringUtils;


// import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
// // import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
// import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// import org.springframework.security.oauth2.core.user.OAuth2User;


// import java.util.Optional;

// @Service
// public class OAuth2UserService extends DefaultOAuth2UserService {

//     @Autowired
//     private UserRepository userRepository;

//     @Override
//     public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
//         OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

//         try {
//             return processOAuth2User(oAuth2UserRequest, oAuth2User);
//         } catch (AuthenticationException ex) {
//             throw ex;
//         } catch (Exception ex) {
//             throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
//         }
//     }

//     private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
//         String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
//         OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
//         if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
//             throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
//         }

//         Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
//         User user;
        
//         if(userOptional.isPresent()) {
//             user = userOptional.get();
            
//             if(!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
//                 throw new OAuth2AuthenticationProcessingException("You're signed up with " + 
//                     user.getProvider() + ". Please use your " + user.getProvider() + " account to login.");
//             }
            
//             user = updateExistingUser(user, oAuth2UserInfo);
//         } else {
//             user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
//         }

//         return UserPrincipal.create(user, oAuth2User.getAttributes());
//     }

//     private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
//         User user = new User();

//         user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()));
//         user.setProviderId(oAuth2UserInfo.getId());
//         user.setName(oAuth2UserInfo.getName());
//         user.setEmail(oAuth2UserInfo.getEmail());
//         user.setImageUrl(oAuth2UserInfo.getImageUrl());
//         user.setUsername(oAuth2UserInfo.getEmail());
        
//         return userRepository.save(user);
//     }

//     private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
//         existingUser.setName(oAuth2UserInfo.getName());
//         existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        
//         return userRepository.save(existingUser);
//     }
// }

// OAuth2UserService.java
// OAuth2UserService.java
// OAuth2UserService.java
// OAuth2UserService.java
// OAuth2UserService.java
package com.mediaserver.security.oauth2;

import com.mediaserver.exception.OAuth2AuthenticationProcessingException;
import com.mediaserver.model.AuthProvider;
import com.mediaserver.model.User;
import com.mediaserver.repository.UserRepository;
import com.mediaserver.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User; // Added correct import
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(oAuth2UserRequest);
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            logger.error("Authentication exception: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("Internal authentication service exception: {}", ex.getMessage(), ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        try {
            String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

            if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
                logger.error("Email not found from OAuth2 provider for registrationId: {}", registrationId);
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
            }

            Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
            User user;

            if (userOptional.isPresent()) {
                user = updateExistingUser(userOptional.get(), oAuth2UserInfo);
            } else {
                user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
            }

            logger.debug("Processed OAuth2 user saved/updated with email: {}", user.getEmail());
            return UserPrincipal.create(user, oAuth2User.getAttributes());
        } catch (Exception e) {
            logger.error("Error processing OAuth2 user: {}", e.getMessage(), e);
            OAuth2Error oauth2Error = new OAuth2Error("server_error", "Failed to process OAuth2 user", null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        try {
            User user = new User();
            user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()));
            user.setProviderId(oAuth2UserInfo.getId());
            user.setName(oAuth2UserInfo.getName());
            user.setEmail(oAuth2UserInfo.getEmail());
            user.setImageUrl(oAuth2UserInfo.getImageUrl());
            String baseUsername = oAuth2UserInfo.getEmail().split("@")[0];
            String username = baseUsername;
            int suffix = 1;
            while (userRepository.findByUsername(username).isPresent()) {
                username = baseUsername + suffix++;
            }
            user.setUsername(username);

            User savedUser = userRepository.save(user); // Save new user
            logger.debug("Registered new user with email: {} and ID: {}", savedUser.getEmail(), savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to register new user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register new user", e);
        }
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        try {
            existingUser.setName(oAuth2UserInfo.getName());
            existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
            User updatedUser = userRepository.save(existingUser); // Update existing user
            logger.debug("Updated existing user with email: {} and ID: {}", updatedUser.getEmail(), updatedUser.getId());
            return updatedUser;
        } catch (Exception e) {
            logger.error("Failed to update existing user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update existing user", e);
        }
    }
}