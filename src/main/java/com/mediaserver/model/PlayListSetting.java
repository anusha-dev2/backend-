package com.mediaserver.model;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "playlists_setting")
public class PlayListSetting {
   
        // private boolean loop;
        // private boolean autoPlay;
    private Boolean loop;      // was boolean
    private Boolean autoPlay;
        private String  transitionType;
        private String  transitionDuration;

    // Explicit getters and setters if Lombok not working
    public Boolean getLoop() {
        return loop;
    }

    public void setLoop(Boolean loop) {
        this.loop = loop;
    }

    public Boolean getAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(Boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public String getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(String transitionType) {
        this.transitionType = transitionType;
    }

    public String getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(String transitionDuration) {
        this.transitionDuration = transitionDuration;
    }
    
}
