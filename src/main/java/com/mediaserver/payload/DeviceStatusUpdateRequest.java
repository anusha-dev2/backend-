package com.mediaserver.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DeviceStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(online|offline|warning|maintenance)$",
             message = "Status must be one of: online, offline, warning, maintenance")
    private String status;

    private String ipAddress;
    private String currentPlaylist;
    private String location;
    private String connectionType;
    private String deviceInfo;

    // Manually add getters if Lombok is not working
    public String getStatus() {
        return status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getCurrentPlaylist() {
        return currentPlaylist;
    }

    public String getLocation() {
        return location;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }
}
