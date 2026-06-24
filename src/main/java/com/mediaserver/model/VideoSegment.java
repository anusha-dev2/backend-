package com.mediaserver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "video_segments")
public class VideoSegment {
    @Id
    private String id;
    
    private String originalVideoId;
    private String playlistId;
    private String deviceGroupId;
    private String deviceId;
    private String segmentPath;
    
    private int gridRow;
    private int gridColumn;
    
    private String cropX;
    private String cropY;
    private String cropWidth;
    private String cropHeight;
    
    private String status;
}
