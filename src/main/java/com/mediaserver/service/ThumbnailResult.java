package com.mediaserver.service;

public class ThumbnailResult {
    private String thumbnailPath;
    private String duration;

    // Constructor
    public ThumbnailResult(String thumbnailPath, String duration) {
        this.thumbnailPath = thumbnailPath;
        this.duration = duration;
    }

    // Getters and setters
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}