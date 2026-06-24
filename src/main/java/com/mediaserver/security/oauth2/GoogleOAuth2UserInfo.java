// // GoogleOAuth2UserInfo.java
// package com.mediaserver.security.oauth2;

// import java.util.Map;

// public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

//     public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
//         super(attributes);
//     }

//     @Override
//     public String getId() {
//         return (String) attributes.get("sub");
//     }

//     @Override
//     public String getName() {
//         return (String) attributes.get("name");
//     }

//     @Override
//     public String getEmail() {
//         return (String) attributes.get("email");
//     }

//     @Override
//     public String getImageUrl() {
//         return (String) attributes.get("picture");
//     }
// }

// GoogleOAuth2UserInfo.java
package com.mediaserver.security.oauth2;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return attributes.get("name") != null ? (String) attributes.get("name") : null;
    }

    @Override
    public String getEmail() {
        return attributes.get("email") != null ? (String) attributes.get("email") : null;
    }

    @Override
    public String getImageUrl() {
        return attributes.get("picture") != null ? (String) attributes.get("picture") : null;
    }
}