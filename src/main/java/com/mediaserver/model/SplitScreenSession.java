package com.mediaserver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "split_screen_sessions")
public class SplitScreenSession {

    @Id
    private String id;
    
    private String uniqueId;
    private String layoutMode;
    private String contentId1;
    private String contentId2;

    public SplitScreenSession() {
    }

    public SplitScreenSession(String uniqueId, String layoutMode, String contentId1, String contentId2) {
        this.uniqueId = uniqueId;
        this.layoutMode = layoutMode;
        this.contentId1 = contentId1;
        this.contentId2 = contentId2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getLayoutMode() {
        return layoutMode;
    }

    public void setLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
    }

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
}
