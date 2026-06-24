

// MediaServerApplication.java
package com.mediaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;

@SpringBootApplication
public class MediaServerApplication {
    public static void main(String[] args) {
        // Create content directory if it doesn't exist
        File contentDir = new File("content");
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }
        SpringApplication.run(MediaServerApplication.class, args);
    }
}