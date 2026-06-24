package com.mediaserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.service.VideoWallService;

@RestController
@RequestMapping("/device-groups")
public class VideoWallController {

    @Autowired
    private VideoWallService videoWallService;

    @PostMapping("/{groupId}/play/{playlistId}")
    public ResponseEntity<?> playVideoWall(@PathVariable String groupId, @PathVariable String playlistId) {
        try {
            videoWallService.processVideoWallFromPlaylist(groupId, playlistId);
            return ResponseEntity.ok("Video Wall processing started successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing video wall: " + e.getMessage());
        }
    }

    @PostMapping("/{groupId}/disable")
    public ResponseEntity<?> disableVideoWall(@PathVariable String groupId) {
        try {
            videoWallService.disableVideoWall(groupId);
            return ResponseEntity.ok("Video Wall disabled successfully. Devices are resuming normal playback.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error disabling video wall: " + e.getMessage());
        }
    }
}
