// PlaylistContent.java
package com.mediaserver.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "playlist_content")
public class PlaylistContent {
    @Id
    private String id;
    
    private String playlistId;
    
    private String contentId;
    
    private Integer displayOrder;
}
