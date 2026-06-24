package com.mediaserver.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

import com.mediaserver.model.PlayListSetting;
import com.mediaserver.model.PlayListItems;

@Data
@Document(collection = "playlists")
public class Playlist {
    @Id
    private String id;
    
    private String name;
    
    private String description;
    
    private LocalDateTime createdDate = LocalDateTime.now();
    
    private String userId;
    
    // Optional thumbnail image for the playlist
    private String thumbnailUrl;
    
    // Optional visibility setting (public, private, etc.)
    private String visibility = "private";
    
    // Optional category or genre
    private String category;
    
    // Optional tags
    private String[] tags;
    
    // Statistics
    private Integer viewCount = 0;
    
    // Last modified timestamp
    private LocalDateTime lastModifiedDate = LocalDateTime.now();

    private PlayListSetting setting;

    private List<PlayListItems> items;

    private Boolean isActive;

    private String duration;

    // add by priya 11/6

    private String groupId; // Ensure this field is present

   // private List<String> devices; // Ensure this field is present

    // Contents of the playlist
    private List<PlaylistContent> contents;

    // Playlist.java
    private List<String> devices = new ArrayList<>();  // Add if missing

    // set time for playlist

    // Embedded schedule information
    private PlaylistScheduleInfo scheduleInfo;

    private boolean durationEnable;
    private boolean timeEnable;

    // Helper method to check if playlist is currently scheduled
    public boolean isCurrentlyScheduled() {
        return scheduleInfo != null && scheduleInfo.isScheduled();
    }

    // Helper method to get next scheduled time
    public LocalDateTime getNextScheduledTime() {
        return scheduleInfo != null ? scheduleInfo.getNextScheduledTime() : null;
    }

    // Inner class for schedule information
    @Data
    public static class PlaylistScheduleInfo {
        private boolean scheduled = false;
        private LocalDateTime scheduledTime;
        private String recurring; // daily, weekly, monthly, custom
        
        // Duration fields
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        
        // Only on Selected Days
        private List<String> selectedDaysOfWeek; // ["SU", "MO", "TU"]
        private List<Integer> selectedDatesOfMonth; // [1, 15, 30]
        
        // At Specified Time
        private boolean specificTimeEnabled;
        private LocalTime specificStartTime;
        private LocalTime specificEndTime;
        
        // Week cycle scheduling
        private boolean weekCycleEnabled;
        private Integer weekCycleType; // 3, 4, or 5
        
        // Device-specific scheduling
        private List<String> scheduledDevices;
        
        // Schedule status
        private boolean enabled = true;
        private LocalDateTime lastExecuted;
        private LocalDateTime nextScheduledTime;
        
        // Helper methods
        public boolean isScheduled() {
            return scheduled && enabled;
        }

        public boolean isRecurring() {
            return recurring != null && !recurring.isEmpty() && !recurring.equals("none");
        }

        public LocalDateTime getNextScheduledTime() {
            return nextScheduledTime;
        }

        public void setNextScheduledTime(LocalDateTime nextScheduledTime) {
            this.nextScheduledTime = nextScheduledTime;
        }

        public LocalTime getSpecificStartTime() {
            return specificStartTime;
        }

        public void setSpecificStartTime(LocalTime specificStartTime) {
            this.specificStartTime = specificStartTime;
        }

        public LocalTime getSpecificEndTime() {
            return specificEndTime;
        }

        public void setSpecificEndTime(LocalTime specificEndTime) {
            this.specificEndTime = specificEndTime;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
        }

        public List<String> getSelectedDaysOfWeek() {
            return selectedDaysOfWeek;
        }

        public void setSelectedDaysOfWeek(List<String> selectedDaysOfWeek) {
            this.selectedDaysOfWeek = selectedDaysOfWeek;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public LocalDateTime getScheduledTime() {
            return scheduledTime;
        }

        public void setScheduledTime(LocalDateTime scheduledTime) {
            this.scheduledTime = scheduledTime;
        }

        public String getRecurring() {
            return recurring;
        }

        public void setRecurring(String recurring) {
            this.recurring = recurring;
        }

        public boolean isSpecificTimeEnabled() {
            return specificTimeEnabled;
        }

        public void setSpecificTimeEnabled(boolean specificTimeEnabled) {
            this.specificTimeEnabled = specificTimeEnabled;
        }

        public boolean isWeekCycleEnabled() {
            return weekCycleEnabled;
        }

        public void setWeekCycleEnabled(boolean weekCycleEnabled) {
            this.weekCycleEnabled = weekCycleEnabled;
        }

        public Integer getWeekCycleType() {
            return weekCycleType;
        }

        public void setWeekCycleType(Integer weekCycleType) {
            this.weekCycleType = weekCycleType;
        }
    }
    // Getters and Setters for durationEnable and timeEnable
    public boolean getDurationEnable() {
        return durationEnable;
    }

    public void setDurationEnable(boolean durationEnable) {
        this.durationEnable = durationEnable;
    }

    public boolean getTimeEnable() {
        return timeEnable;
    }

    public void setTimeEnable(boolean timeEnable) {
        this.timeEnable = timeEnable;
    }

    // Additional getters and setters if needed
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PlayListItems> getItems() {
        return items;
    }

    public void setItems(List<PlayListItems> items) {
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PlayListSetting getSetting() {
        return setting;
    }

    public void setSetting(PlayListSetting setting) {
        this.setting = setting;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public PlaylistScheduleInfo getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(PlaylistScheduleInfo scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

}
