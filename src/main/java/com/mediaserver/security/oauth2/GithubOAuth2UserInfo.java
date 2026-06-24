// // GithubOAuth2UserInfo.java
// package com.mediaserver.security.oauth2;

// import java.util.Map;

// public class GithubOAuth2UserInfo extends OAuth2UserInfo {

//     public GithubOAuth2UserInfo(Map<String, Object> attributes) {
//         super(attributes);
//     }

//     @Override
//     public String getId() {
//         return ((Integer) attributes.get("id")).toString();
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
//         return (String) attributes.get("avatar_url");
//     }
// }

// GithubOAuth2UserInfo.java
package com.mediaserver.security.oauth2;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return attributes.get("id") != null ? ((Integer) attributes.get("id")).toString() : null;
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
        return attributes.get("avatar_url") != null ? (String) attributes.get("avatar_url") : null;
    }
}