package com.mediaserver.model;

import java.util.List;

/**
 * Model used for screen split requests.
 * Kept separate from the service/repository payload objects.
 */
public class SplitScreenRequestModel {

    private String contentId1;
    private String contentId2;
    private String layoutMode; // HORIZONTAL | VERTICAL

    // Optional: include zones if caller wants full payload.
    private List<Content> zone1;
    private List<Content> zone2;

    public String getContentId1() {
        return contentId1;
    }

    public void setContentId1(String contentId1) {
        this.contentId1 = contentId1;
    }

    public String getContentId2() {
        return contentId2;
    }

    public void setContentId2(String contentId2) {
        this.contentId2 = contentId2;
    }

    public String getLayoutMode() {
        return layoutMode;
    }

    public void setLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
    }

    public List<Content> getZone1() {
        return zone1;
    }

    public void setZone1(List<Content> zone1) {
        this.zone1 = zone1;
    }

    public List<Content> getZone2() {
        return zone2;
    }

    public void setZone2(List<Content> zone2) {
        this.zone2 = zone2;
    }
}

