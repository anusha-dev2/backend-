package com.mediaserver.payload;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class RecoverAccountRequest {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String password;
}