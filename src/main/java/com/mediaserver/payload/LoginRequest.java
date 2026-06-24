// LoginRequest.java
package com.mediaserver.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank
    private String username;
    // private String email;

    @NotBlank
    private String password;
}
