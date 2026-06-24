package com.mediaserver.dto;

import java.util.Map;

import lombok.Data;

@Data
public class WeeklyActivityData {
    private String username; // Added for root user access
    private Map<String, Double> contentPlays; // Key: Day (e.g., "Monday"), Value: Plays
    private Map<String, Double> deviceActivity; // Key: Day, Value: Activity (e.g., hits)

    public void setUsername(String username) {
        this.username = username;
    }

    public void setContentPlays(Map<String, Double> contentPlays) {
        this.contentPlays = contentPlays;
    }

    public void setDeviceActivity(Map<String, Double> deviceActivity) {
        this.deviceActivity = deviceActivity;
    }

    public Map<String, Double> getContentPlays() {
        return contentPlays;
    }

    public Map<String, Double> getDeviceActivity() {
        return deviceActivity;
    }
}
