
// OverView.java
package com.mediaserver.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@Document(collection = "group_device")
public class OverView {
    // @Id
    // private String id;
    // @Indexed(unique = true)
    private Summary summary;
    // private DevicesStatus devicestatus;

    @Data
    @AllArgsConstructor
    public static class Summary {
        private int deviceCount;
        private int onlineDeviceCount;
        private int MediaCount;
        private int playListCount;

    }

    @Data
    @AllArgsConstructor
    public static class DevicesStatus {
        private int online;
        private int offilne;
        private int warning;
    }

    public OverView(int deviceCount, int onlineDeviceCount, int MediaCount, int playListCount) {
        this.summary = new Summary(deviceCount, onlineDeviceCount, MediaCount, playListCount);
    }

    // public void DevicesStatusList(int online, int Offline, int Warning){
    //     this.devicestatus = new DevicesStatus(online, Offline, Warning);
    // }
}