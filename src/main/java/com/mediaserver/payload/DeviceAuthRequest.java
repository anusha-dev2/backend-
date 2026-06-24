package com.mediaserver.payload;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceAuthRequest {
    @NotBlank(message = "MAC address is required")
    private String macAddress;
    
    private String ipAddress;
    private String deviceInfo; // Optional device information
}
