package com.mediaserver.payload;


import com.mediaserver.model.Device;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class DeviceAuthResponse {
    private String token;
    private String deviceId;
    private String deviceName;
    private String macAddress;
    private String status;
    private String currentPlaylist;
    private Device.Setting setting;
}
