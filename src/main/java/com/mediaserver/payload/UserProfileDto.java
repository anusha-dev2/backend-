package com.mediaserver.payload;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class UserProfileDto {
    @NotBlank @Size(max = 50)
    private String firstName;
    
    @Size(max = 50)
    private String lastName;
    
    @NotBlank @Email
    private String email;
    
    private String jobTitle;
    private String phone;
    private String timezone;
    private String language;
    private String avatarUrl;

    // Company Info Fields
    private String companyName;
    private String companyIndustry;
    private String companySize;
    private String companyWebsite;
    private String companyLogoUrl;
    private String companyAddress;
    private String companyCity;
    private String companyState;
    private String companyZipCode;
    private String companyCountry;
}
