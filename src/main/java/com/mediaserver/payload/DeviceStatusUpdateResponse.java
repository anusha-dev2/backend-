package com.mediaserver.payload;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
public class DeviceStatusUpdateResponse {
    private String deviceId;
    private String status;
    private String lastSeen;
    private String ipAddress;
    private String currentPlaylist;
    private String location;
    private String message;

    // Manually add constructor since Lombok is not working
    public DeviceStatusUpdateResponse(String deviceId, String status, String lastSeen, String ipAddress, String currentPlaylist, String location, String message) {
        this.deviceId = deviceId;
        this.status = status;
        this.lastSeen = lastSeen;
        this.ipAddress = ipAddress;
        this.currentPlaylist = currentPlaylist;
        this.location = location;
        this.message = message;
    }
}
