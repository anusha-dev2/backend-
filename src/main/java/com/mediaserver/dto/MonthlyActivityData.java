package com.mediaserver.dto;

import java.util.Map;

import lombok.Data;

@Data
public class MonthlyActivityData {
    private Map<String, Double> contentPlays;
    private Map<String, Double> bandwidthGb;
    private Map<String, Double> storageGb;
    private Map<String, Double> deviceActivity;

    public Map<String, Double> getContentPlays() {
        return contentPlays;
    }

    public void setContentPlays(Map<String, Double> contentPlays) {
        this.contentPlays = contentPlays;
    }

    public Map<String, Double> getBandwidthGb() {
        return bandwidthGb;
    }

    public void setBandwidthGb(Map<String, Double> bandwidthGb) {
        this.bandwidthGb = bandwidthGb;
    }

    public Map<String, Double> getStorageGb() {
        return storageGb;
    }

    public void setStorageGb(Map<String, Double> storageGb) {
        this.storageGb = storageGb;
    }

    public Map<String, Double> getDeviceActivity() {
        return deviceActivity;
    }

    public void setDeviceActivity(Map<String, Double> deviceActivity) {
        this.deviceActivity = deviceActivity;
    }
}
