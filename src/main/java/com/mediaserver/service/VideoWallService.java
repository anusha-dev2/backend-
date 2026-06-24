package com.mediaserver.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mediaserver.model.Content;
import com.mediaserver.model.Group;
import com.mediaserver.model.Device;
import com.mediaserver.model.Playlist;
import com.mediaserver.model.PlaylistContent;
import com.mediaserver.model.PlayListItems;
import com.mediaserver.model.VideoSegment;
import com.mediaserver.repository.ContentRepository;
import com.mediaserver.repository.DeviceRepository;
import com.mediaserver.repository.PlaylistRepository;
import com.mediaserver.repository.VideoSegmentRepository;

@Service
public class VideoWallService {

    @Value("${content.storage.location}")
    private String storageLocation;

    @Autowired
    private GroupService groupService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private VideoSegmentRepository videoSegmentRepository;

    @Autowired
    private GridCalculationService gridCalculationService;

    public void processVideoWallFromPlaylist(String groupId, String playlistId) throws Exception {
        Optional<Group> groupOpt = groupService.getGroupById(groupId);
        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }
        
        Group group = groupOpt.get();
        List<String> devices = group.getDevices();
        if (devices == null || devices.isEmpty()) {
            throw new IllegalArgumentException("No devices in group");
        }

        // Update group with playlistId
        group.setIsVideoWall(true);
        group.setPlaylistId(playlistId);
        
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            throw new IllegalArgumentException("Playlist not found");
        }
        Playlist playlist = playlistOpt.get();
        if (playlist.getItems() == null || playlist.getItems().isEmpty()) {
            throw new IllegalArgumentException("Playlist is empty");
        }
        
        int deviceCount = devices.size();
        
        // Calculate optimal grid using reusable service
        GridCalculationService.GridResult grid = gridCalculationService.calculateGrid(deviceCount);
        int cols = grid.getColumns();
        int rows = grid.getRows();

        group.setGridRows(rows);
        group.setGridCols(cols);
        groupService.updateGroup(groupId, group);

        long syncStartTime = System.currentTimeMillis() + 5000; // Play in exactly 5 seconds

        for (int itemIndex = 0; itemIndex < playlist.getItems().size(); itemIndex++) {
            String videoId = playlist.getItems().get(itemIndex).getMediaId();

            Optional<Content> originalContentOpt = contentRepository.findById(videoId);
            if (originalContentOpt.isEmpty()) {
                System.out.println("Original video content not found in database for ID: " + videoId);
                continue;
            }
            Content originalContent = originalContentOpt.get();
            String originalVideoFilename = originalContent.getFilePath();

            Path storagePath = Paths.get(storageLocation);
            Path inputVideoPath = storagePath.resolve(originalVideoFilename);

            if (!Files.exists(inputVideoPath)) {
                System.out.println("Original video file not found in storage: " + originalVideoFilename);
                continue;
            }

            List<VideoSegment> generatedSegments = new ArrayList<>();

            // Process FFmpeg slices for each device
            for (int i = 0; i < deviceCount; i++) {
                int row = i / cols;
                int col = i % cols;

                String extension = "";
                if (originalVideoFilename.contains(".")) {
                    extension = originalVideoFilename.substring(originalVideoFilename.lastIndexOf("."));
                }
                String outputFilename = "slice_" + row + "_" + col + "_" + UUID.randomUUID().toString() + extension;
                Path outputPath = storagePath.resolve(outputFilename);

                // Using iw (input width) and ih (input height) directly in ffmpeg crop filter
                String cropW = "iw/" + cols;
                String cropH = "ih/" + rows;
                String cropX = "(" + cropW + ")*" + col;
                String cropY = "(" + cropH + ")*" + row;
                String cropFilter = "crop=" + cropW + ":" + cropH + ":" + cropX + ":" + cropY;

                ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", inputVideoPath.toString(),
                    "-filter:v", cropFilter,
                    "-c:a", "copy",
                    outputPath.toString()
                );
                pb.redirectErrorStream(true);
                
                System.out.println("Executing FFmpeg for device " + i + " (Row " + row + ", Col " + col + "): " + pb.command());
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {}
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("FFmpeg processing failed for slice " + i + " with exit code " + exitCode);
                }

                String deviceId = devices.get(i);
                
                // Create VideoSegment
                VideoSegment segment = new VideoSegment();
                segment.setOriginalVideoId(originalContent.getId());
                segment.setPlaylistId(playlistId);
                segment.setDeviceGroupId(groupId);
                segment.setDeviceId(deviceId);
                segment.setSegmentPath(outputFilename);
                segment.setGridRow(row);
                segment.setGridColumn(col);
                segment.setCropX(cropX);
                segment.setCropY(cropY);
                segment.setCropWidth(cropW);
                segment.setCropHeight(cropH);
                segment.setStatus("COMPLETED");
                
                segment = videoSegmentRepository.save(segment);
                generatedSegments.add(segment);
                
                // Save grid mapping and slice mappings to Device model
                Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
                if (deviceOpt.isPresent()) {
                    Device device = deviceOpt.get();
                    if (itemIndex == 0) {
                        device.setDeviceGroupId(groupId);
                        device.setGridRow(row);
                        device.setGridColumn(col);
                        device.setTotalRows(rows);
                        device.setTotalColumns(cols);
                    }
                    // Save or append to sliceMappings for every item
                    if (device.getSliceMappings() == null) {
                        device.setSliceMappings(new java.util.HashMap<>());
                    }
                    // This map will store the relation between original video and sliced video
                    deviceRepository.save(device);
                }
            }

            for (VideoSegment segment : generatedSegments) {
                String deviceId = segment.getDeviceId();
                
                Content slicedContent = new Content();
                // Requested naming format: [Original Video Title] (Slice R{row}-C{col}) using 1-based indexing
                slicedContent.setTitle(originalContent.getTitle() + " (Slice R" + (segment.getGridRow() + 1) + "-C" + (segment.getGridColumn() + 1) + ")");
                slicedContent.setFilePath(segment.getSegmentPath());
                slicedContent.setFileType(originalContent.getFileType());
                slicedContent.setMediaType(originalContent.getMediaType());
                slicedContent.setUserId(originalContent.getUserId());
                slicedContent.setSystemGenerated(true);
                slicedContent = contentRepository.save(slicedContent);
                
                Optional<Device> updateDeviceOpt = deviceRepository.findById(deviceId);
                if (updateDeviceOpt.isPresent()) {
                    Device device = updateDeviceOpt.get();
                    if (itemIndex == 0) {
                        device.setSliceContentId(slicedContent.getId());
                    }
                    if (device.getSliceMappings() == null) {
                        device.setSliceMappings(new java.util.HashMap<>());
                    }
                    device.getSliceMappings().put(originalContent.getId(), slicedContent.getId());
                    deviceRepository.save(device);
                }
                
                // Only send PLAY_SYNC for the FIRST video in the playlist
                if (itemIndex == 0) {
                    String payload = String.format("{\"action\":\"PLAY_SYNC\",\"contentId\":\"%s\",\"startTime\":%d}", slicedContent.getId(), syncStartTime);
                    webSocketService.sendMessageToDevice(deviceId, payload);
                    System.out.println("Sent sync playback signal to device " + deviceId + " for VideoSegment " + segment.getId());
                }
            }
        }
    }

    public void disableVideoWall(String groupId) throws Exception {
        Optional<Group> groupOpt = groupService.getGroupById(groupId);
        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }

        Group group = groupOpt.get();

        // 1. Revert Group settings
        group.setIsVideoWall(false);
        group.setGridRows(null);
        group.setGridCols(null);
        groupService.updateGroup(groupId, group);

        // 2. Revert Device settings and notify them
        List<String> devices = group.getDevices();
        if (devices != null && !devices.isEmpty()) {
            for (String deviceId : devices) {
                Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
                if (deviceOpt.isPresent()) {
                    Device device = deviceOpt.get();
                    
                    // Clear video wall metadata
                    device.setGridRow(null);
                    device.setGridColumn(null);
                    device.setTotalRows(null);
                    device.setTotalColumns(null);
                    device.setSliceContentId(null);
                    device.setSliceMappings(new java.util.HashMap<>());
                    deviceRepository.save(device);

                    // 3. Broadcast to device to resume playing the full playlist
                    // If group has a playlistId, use it; otherwise fallback to currentPlaylistId if available
                    String playlistId = group.getPlaylistId();
                    if (playlistId == null && group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
                        playlistId = group.getCurrentPlaylistId().get(0);
                    }

                    if (playlistId != null) {
                        String payload = String.format("{\"action\":\"PLAY_PLAYLIST\",\"playlistId\":\"%s\"}", playlistId);
                        webSocketService.sendMessageToDevice(deviceId, payload);
                        System.out.println("Sent play normal playlist signal to device " + deviceId);
                    } else {
                        // If no playlist is found, you might want to send a STOP or REFRESH action
                        String payload = "{\"action\":\"REFRESH\"}";
                        webSocketService.sendMessageToDevice(deviceId, payload);
                    }
                }
            }
        }
    }
}
