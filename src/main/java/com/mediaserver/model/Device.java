// Device.java
package com.mediaserver.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "devices")
public class Device {

    @Id
    private String id;

    private boolean rebootStatus = false;

    // Add this new field for rate-limiting
    private LocalDateTime rebootRequestedAt;

    // private LocalDateTime lastSeen;
    @Field("lastSeen")
    private LocalDateTime lastSeen;

    // @Indexed(unique = true)
    private String macAddress;
    
    private String deviceName;
    
    private boolean enabled = true;
    
    private String userId;

    //newlly added
    private String status;
    private String location;
    private String group;
    private String ip;
    private String currentPlaylist;
    private String deviceType;
    private String connectionType;
    private LocalDateTime registrationDate = LocalDateTime.now();
    private String notes;
    private String groupId;
    private String groupName;
    private String description;
    
    // New fields for Video Wall grid mapping
    private String deviceGroupId;
    private Integer gridRow;
    private Integer gridColumn;
    private Integer totalRows;
    private Integer totalColumns;
    private String sliceContentId;
    private java.util.Map<String, String> sliceMappings = new java.util.HashMap<>();
    
    private List<String> assignedPlaylists = new ArrayList<>();
    private Setting setting;

    // Manually add getters and setters if Lombok is not working
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRebootStatus() {
        return rebootStatus;
    }

    public void setRebootStatus(boolean rebootStatus) {
        this.rebootStatus = rebootStatus;
    }

    public LocalDateTime getRebootRequestedAt() {
        return rebootRequestedAt;
    }

    public void setRebootRequestedAt(LocalDateTime rebootRequestedAt) {
        this.rebootRequestedAt = rebootRequestedAt;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCurrentPlaylist() {
        return currentPlaylist;
    }

    public void setCurrentPlaylist(String currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceGroupId() {
        return deviceGroupId;
    }

    public void setDeviceGroupId(String deviceGroupId) {
        this.deviceGroupId = deviceGroupId;
    }

    public Integer getGridRow() {
        return gridRow;
    }

    public void setGridRow(Integer gridRow) {
        this.gridRow = gridRow;
    }

    public Integer getGridColumn() {
        return gridColumn;
    }

    public void setGridColumn(Integer gridColumn) {
        this.gridColumn = gridColumn;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getTotalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(Integer totalColumns) {
        this.totalColumns = totalColumns;
    }

    public String getSliceContentId() {
        return sliceContentId;
    }

    public void setSliceContentId(String sliceContentId) {
        this.sliceContentId = sliceContentId;
    }

    public java.util.Map<String, String> getSliceMappings() {
        return sliceMappings;
    }

    public void setSliceMappings(java.util.Map<String, String> sliceMappings) {
        this.sliceMappings = sliceMappings;
    }

    public List<String> getAssignedPlaylists() {
        return assignedPlaylists;
    }

    public void setAssignedPlaylists(List<String> assignedPlaylists) {
        this.assignedPlaylists = assignedPlaylists;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    @Data
    public static class Setting {
        private String assignedGroup;
        private List<String> categories;
        private String timezoneOfPlayer;
        private String playerNotes;
        private List<String> additionalPlaylists;

        // Split-screen layout mode for two-way split screen
        // Allowed values: HORIZONTAL (default) or VERTICAL
        private String layoutMode;

        // Manually add getters and setters if Lombok is not working
        public String getAssignedGroup() {
            return assignedGroup;
        }

        public void setAssignedGroup(String assignedGroup) {
            this.assignedGroup = assignedGroup;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public String getTimezoneOfPlayer() {
            return timezoneOfPlayer;
        }

        public void setTimezoneOfPlayer(String timezoneOfPlayer) {
            this.timezoneOfPlayer = timezoneOfPlayer;
        }

        public String getPlayerNotes() {
            return playerNotes;
        }

        public void setPlayerNotes(String playerNotes) {
            this.playerNotes = playerNotes;
        }

        public List<String> getAdditionalPlaylists() {
            return additionalPlaylists;
        }

        public void setAdditionalPlaylists(List<String> additionalPlaylists) {
            this.additionalPlaylists = additionalPlaylists;
        }

        public String getLayoutMode() {
            return layoutMode;
        }

        public void setLayoutMode(String layoutMode) {
            this.layoutMode = layoutMode;
        }
    }

    // Request payload for updating split-screen layout mode for content zones
    @Data
    public static class ScreenLayoutModeRequest {
        @javax.validation.constraints.NotBlank(message = "contentId1 is required")
        private String contentId1;

        @javax.validation.constraints.NotBlank(message = "contentId2 is required")
        private String contentId2;

        @javax.validation.constraints.NotBlank(message = "layoutMode is required")
        private String layoutMode;
    }

}

