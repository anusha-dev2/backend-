// package com.mediaserver.dto;


// public class GoogleAuthRequest {
//     private String email;
//     private String username;
//     private Boolean isGoogleUser;
//     private String token;

//     // getters and setters
// }


package com.mediaserver.dto;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String email;
    private String username;
    private Boolean isGoogleUser;
    private String token;
}
