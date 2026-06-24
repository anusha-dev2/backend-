package com.mediaserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mediaserver.service.VideoService;
import com.mediaserver.model.Video;
import com.mediaserver.security.SecurityUtil;
import java.io.IOException;



@RestController
@RequestMapping("/api/videos")
public class VideoController {
    @Autowired
    private VideoService videoService;

    @PostMapping("/upload")
    public ResponseEntity<Video> uploadVideo(@RequestParam("file") MultipartFile file,
                                             @RequestParam("title") String title,
                                             @RequestParam("description") String description)throws IOException {
        Video video = videoService.uploadVideo(file, title, description);
        return ResponseEntity.ok(video);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable String id) {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return videoService.getVideoById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
