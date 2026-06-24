package com.mediaserver.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "groups")
public class Group {
    @Id
    private String id;
    
    private String userId;
    
    private String groupName;
    
    private String status;
    
    private String location;
    
    private String notes;
    
    private List<String> currentPlaylistId;

    private String currentPlaylistName;
    
    private String playlistId;

    private List<String> devices;

    private List<String> schedules;

    private Boolean isVideoWall;

    private Integer gridRows;

    private Integer gridCols;

    @Field("create_date")
    private Date createDate;

    @Field("update_date")
    private Date updateDate;

    @Field("last_seen")
    private Date lastSeen;

    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getCurrentPlaylistId() {
        return currentPlaylistId;
    }

    public void setCurrentPlaylistId(List<String> currentPlaylistId) {
        this.currentPlaylistId = currentPlaylistId;
    }

    public String getCurrentPlaylistName() {
        return currentPlaylistName;
    }

    public void setCurrentPlaylistName(String currentPlaylistName) {
        this.currentPlaylistName = currentPlaylistName;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public List<String> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<String> schedules) {
        this.schedules = schedules;
    }

    public Boolean getIsVideoWall() {
        return isVideoWall;
    }

    public void setIsVideoWall(Boolean isVideoWall) {
        this.isVideoWall = isVideoWall;
    }

    public Integer getGridRows() {
        return gridRows;
    }

    public void setGridRows(Integer gridRows) {
        this.gridRows = gridRows;
    }

    public Integer getGridCols() {
        return gridCols;
    }

    public void setGridCols(Integer gridCols) {
        this.gridCols = gridCols;
    }

}

