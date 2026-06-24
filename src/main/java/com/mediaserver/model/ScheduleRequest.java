package com.mediaserver.model;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleRequest {
    private String action; // CREATE, UPDATE, DELETE, GET_ACTIVE, GET_UPCOMING, GET_SCHEDULED, GET_SCHEDULE, MARK_EXECUTED
    private String playlistId; // Required for CREATE, UPDATE, DELETE, GET_SCHEDULE, MARK_EXECUTED
    private String deviceId; // Required for GET_ACTIVE
    private Playlist.PlaylistScheduleInfo scheduleInfo; // Required for CREATE, UPDATE
    private LocalDateTime targetTime; // Optional for GET_SCHEDULED_FOR_TIME
    private Integer hours;
    
    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Playlist.PlaylistScheduleInfo getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(Playlist.PlaylistScheduleInfo scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    public LocalDateTime getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(LocalDateTime targetTime) {
        this.targetTime = targetTime;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }
}
