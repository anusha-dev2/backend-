package com.mediaserver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
// import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Document(collection = "videos")
public class Video {
    @Id
    private String id;
    private String title;
    private String description;
    private String videoUrl;

    // Getters and Setters
}
