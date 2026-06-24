package com.mediaserver.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.model.Content;
import com.mediaserver.model.PlayListItems;
import com.mediaserver.model.PlayListSetting;
import com.mediaserver.model.Playlist;
import com.mediaserver.model.PlaylistContent;
import com.mediaserver.model.ScheduleRequest;
import com.mediaserver.model.User;
import com.mediaserver.security.RootUserPrincipal;
import com.mediaserver.security.UserPrincipal;
import com.mediaserver.service.ContentService;
import com.mediaserver.service.PlaylistService;
import com.mediaserver.service.UserService;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private UserService userService;

    @Autowired
    private ContentService contentService;

    private boolean isRootUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"));
        }
        return false;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        } else if (authentication != null && authentication.getPrincipal() instanceof RootUserPrincipal) {
            RootUserPrincipal rootUserPrincipal = (RootUserPrincipal) authentication.getPrincipal();
            return rootUserPrincipal.getId();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists() {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(playlistService.getAllPlaylists());
    }
// cmd by priya 11/6
    // @GetMapping("/user/{userId}")
    // public ResponseEntity<List<Playlist>> getPlaylistsByUser(@PathVariable String userId) {
    //     Optional<User> user = userService.getUserById(userId);
        
    //     if (user.isPresent()) {
    //         return ResponseEntity.ok(playlistService.getPlaylistsByUserId(userId));
    //     } else {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

    @GetMapping("/user/{userId}")
public ResponseEntity<List<Playlist>> getPlaylistsByUser(
        @PathVariable String userId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String visibility,
        @RequestParam(required = false) String groupId,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false, defaultValue = "asc") String sortOrder) {

    if (!isRootUser() && !getCurrentUserId().equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Optional<User> user = userService.getUserById(userId);

    if (user.isPresent()) {
        List<Playlist> playlists = playlistService.getPlaylistsByUserIdWithFilters(
            userId,
            name,
            category,
            visibility,
            //description,
            groupId,
            sortBy,
            sortOrder
        );
        return ResponseEntity.ok(playlists);
    } else {
        return ResponseEntity.notFound().build();
    }
}

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable String id) {
        Optional<Playlist> playlist = playlistService.getPlaylistById(id);
        if (playlist.isPresent()) {
            if (!isRootUser() && !playlist.get().getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(playlist.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //add by priya 11/6

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Playlist>> getPlaylistsByGroupId(@PathVariable String groupId) {
        List<Playlist> playlists = playlistService.findByGroupId(groupId);
        if (playlists.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Allow root users full access, normal users only if playlists belong to them
        if (!isRootUser()) {
            for (Playlist p : playlists) {
                if (!p.getUserId().equals(getCurrentUserId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }
        return ResponseEntity.ok(playlists);
    }

    // @GetMapping("/{id}/contents")
    // public ResponseEntity<List<PlaylistContent>> getPlaylistContents(@PathVariable String id) {
    //     Optional<Playlist> playlist = playlistService.getPlaylistById(id);
        
    //     if (playlist.isPresent()) {
    //         return ResponseEntity.ok(playlistService.getPlaylistContents(id));
    //     } else {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

    //added by priya 7/7/25

    class PlaylistInfo {
    private String name;
    private String id;

    /**
     * Constructor for PlaylistInfo.
     * @param name The name of the playlist.
     * @param id The ID of the playlist.
     */
    public  PlaylistInfo    (String name, String id) {
        this.name = name;
        this.id = id;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    // Setters (optional, depending on immutability needs)
    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Overrides equals to ensure uniqueness based on the playlist ID.
     * This is crucial if you plan to use this DTO in a Set to filter by ID.
     * @param o The object to compare with.
     * @return True if objects are equal based on ID, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistInfo that = (PlaylistInfo) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Overrides hashCode to be consistent with equals, using the playlist ID.
     * @return The hash code based on the playlist ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<PlaylistInfo>> getDistinctCategoryAsString(@PathVariable String userId) {
        if (!isRootUser() && !getCurrentUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Fetch all playlists for the given user
        List<Playlist> playlistList = playlistService.getPlaylistsByUserId(userId);

        // Extract playlist name and ID, filter out null/empty names,
        // and collect into a TreeSet<PlaylistInfo> to ensure uniqueness by ID
        // and maintain a consistent order (sorted by ID).
        Set<PlaylistInfo> uniquePlaylistInfos = playlistList.stream()
            .filter(playlist -> playlist.getName() != null && !playlist.getName().trim().isEmpty()) // Filter out invalid names
            .map(playlist -> new PlaylistInfo(playlist.getName(), playlist.getId())) // Map to PlaylistInfo DTO
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(PlaylistInfo::getId)))); // Collect into a TreeSet, ensuring uniqueness by ID and sorting by ID

        // Convert the Set back to a List and return
        return ResponseEntity.ok(new ArrayList<>(uniquePlaylistInfos));
    }

    @GetMapping("/{id}/contents")
    public ResponseEntity<List<PlaylistContent>> getPlaylistContents(
        @PathVariable String id,
        @RequestParam(required = false) String contentId,
        @RequestParam(required = false) Integer minOrder,
        @RequestParam(required = false) Integer maxOrder,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false, defaultValue = "asc") String sortOrder) {

    Optional<Playlist> playlist = playlistService.getPlaylistById(id);
    if (playlist.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    if (!isRootUser() && !playlist.get().getUserId().equals(getCurrentUserId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    List<PlaylistContent> contents = playlistService.getPlaylistContentsWithFilters(
        id,
        contentId,
        minOrder,
        maxOrder,
        sortBy,
        sortOrder
    );
    return ResponseEntity.ok(contents);
}


    // @PostMapping("/user/{userId}")
    // public ResponseEntity<?> createPlaylist(@PathVariable String userId, @Valid @RequestBody Playlist playlist) {
    //     Optional<User> user = userService.getUserById(userId);
        
    //     if (user.isPresent()) {
    //         playlist.setUserId(userId);
    //         PlayListSetting setting = new PlayListSetting();
    //         setting.setLoop(false);
    //         setting.setAutoPlay(false);
    //         setting.setTransitionType("fade");
    //         setting.setTransitionDuration("1.0");
    //         playlist.setSetting(setting);
    //         playlist.setIsActive(false);
    //         playlist.setDuration("00:30");
    //         playlist.setItems(new ArrayList<>());
    //         Playlist createdPlaylist = playlistService.createPlaylist(playlist);
    //         return ResponseEntity.status(HttpStatus.CREATED).body(createdPlaylist);
    //     } else {
    //         return ResponseEntity.notFound().build();
    //     }
    // }
// 24/6/25

    // @PostMapping("/user/{userId}")
    // public ResponseEntity<?> createPlaylist(@PathVariable String userId, @Valid @RequestBody Playlist playlist) {
    //     Optional<User> user = userService.getUserById(userId);
        
    //     if (user.isPresent()) {
    //         try {
    //             playlist.setUserId(userId);
    //             PlayListSetting setting = new PlayListSetting();
    //             setting.setLoop(false);
    //             setting.setAutoPlay(false);
    //             setting.setTransitionType("fade");
    //             setting.setTransitionDuration("1.0");
    //             playlist.setSetting(setting);
    //             playlist.setIsActive(false);
    //             playlist.setDuration("00:30");
    //             playlist.setItems(new ArrayList<>());
                
    //             Playlist createdPlaylist = playlistService.createPlaylist(playlist);
    //             return ResponseEntity.status(HttpStatus.CREATED).body(createdPlaylist);
    //         } catch (IllegalArgumentException e) {
    //             // Return error response for duplicate name
    //             Map<String, String> errorResponse = new HashMap<>();
    //             errorResponse.put("error", e.getMessage());
    //             return ResponseEntity.badRequest().body(errorResponse);
    //         }
    //     } else {
    //         return ResponseEntity.notFound().build();
    //     }
    // }
    
    @PostMapping("/user/{userId}")
public ResponseEntity<?> createPlaylist(@PathVariable String userId,
                                        @Valid @RequestBody Playlist playlist) {

    if (!isRootUser() && !userId.equals(getCurrentUserId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Optional<User> user = userService.getUserById(userId);
    if (user.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    /* basic duplicate-name guard */
    if (playlistService.isPlaylistNameExistsForUser(playlist.getName(), userId)) {
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Playlist with name '" + playlist.getName() + "' already exists for this user"));
    }

    /* ----------  defaults applied only when missing  ---------- */
    if (playlist.getSetting() == null) {
        playlist.setSetting(new PlayListSetting());
    }
    PlayListSetting s = playlist.getSetting();
    if (s.getLoop() == null) s.setLoop(false);          // <-- kept only if client did not send
    if (s.getAutoPlay() == null) s.setAutoPlay(false);
    if (s.getTransitionType() == null) s.setTransitionType("fade");
    if (s.getTransitionDuration() == null) s.setTransitionDuration("1.0");

    if (playlist.getIsActive() == null) playlist.setIsActive(false);
    if (playlist.getDuration() == null) playlist.setDuration("00:30");
    if (playlist.getItems() == null) playlist.setItems(new ArrayList<>());

    /* ---------------------------------------------------------- */

    playlist.setUserId(userId);
    Playlist saved = playlistService.createPlaylist(playlist);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlaylist(@PathVariable String id, @Valid @RequestBody Playlist playlist) {
        Optional<Playlist> existing = playlistService.getPlaylistById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!isRootUser() && !existing.get().getUserId().equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Playlist updatedPlaylist = playlistService.updatePlaylist(id, playlist);
            return ResponseEntity.ok(updatedPlaylist);
        } catch (IllegalArgumentException e) {
            // Return error response for validation errors (duplicate name, not found, etc.)
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
    }
    

    @PostMapping("/{playlistId}/content/{contentId}")
    public ResponseEntity<?> addContentToPlaylist(
            @PathVariable String playlistId,
            @PathVariable String contentId,
            @RequestParam Integer displayOrder) {

        Optional<Playlist> playlist = playlistService.getPlaylistById(playlistId);
        if (playlist.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Playlist not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (!isRootUser() && !playlist.get().getUserId().equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Content> content = contentService.getContentById(contentId);

        if (content.isPresent()) {

            PlaylistContent playlistContent = playlistService.addContentToPlaylist(playlistId, contentId, displayOrder);

        //   PlayListItems playListItems = playlistService.addContentToPlaylistItems(playlistId, contentId, displayOrder);
        PlayListItems playListItems = playlistService.addContentToPlaylistItems(playlistId, contentId, displayOrder);



            return ResponseEntity.status(HttpStatus.CREATED).body(playlistContent);
        } else {
            // // return ResponseEntity.notFound().build();
            // return ResponseEntity.status(HttpStatus.CREATED).body("Not found!");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Content not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{playlistId}/content/{contentId}")
    public ResponseEntity<String> removeContentFromPlaylist(
            @PathVariable String playlistId,
            @PathVariable String contentId) {

        Optional<Playlist> playlist = playlistService.getPlaylistById(playlistId);
        if (playlist.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist not found.");
        }
        if (!isRootUser() && !playlist.get().getUserId().equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Content> content = contentService.getContentById(contentId);

        if (content.isPresent()) {
            playlistService.removeContentFromPlaylist(playlistId, contentId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Content not found.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlaylist(@PathVariable String id) {
        Optional<Playlist> playlist = playlistService.getPlaylistById(id);

        if (playlist.isPresent()) {
            if (!isRootUser() && !playlist.get().getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            playlistService.deletePlaylist(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist is incorrect.");
        }
    }

//
//added

    // @GetMapping("/filter/{value}")
    // public ResponseEntity<List<Playlist>> filterPlaylistByValue(@PathVariable String value) {
    //     List<Playlist> playlist = playlistService.filterPlaylistByValue(value);
    //     return ResponseEntity.ok(playlist);
    // }

    // PlaylistController.java

// ... existing imports and class definition ...

    // @GetMapping("/filter/{value}")
    // public ResponseEntity<List<Playlist>> filterPlaylistByValue(@PathVariable String value) {
    //     List<Playlist> playlist = playlistService.filterPlaylistByValue(value);
    //     return ResponseEntity.ok(playlist);
    // }

    @GetMapping("/filter/{userId}/{value}")
public ResponseEntity<List<Playlist>> filterPlaylistByValue(@PathVariable String userId, @PathVariable String value) {
    if (!isRootUser() && !getCurrentUserId().equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    List<Playlist> playlists = playlistService.filterPlaylistByValue(userId, value);
    return ResponseEntity.ok(playlists);
}




// @PostMapping("/user/{userId}/schedule")
//     public ResponseEntity<?> handleScheduleRequest(
//             @PathVariable String userId,
//             @Valid @RequestBody ScheduleRequest request) {
//         Optional<User> user = userService.getUserById(userId);
//         if (!user.isPresent()) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", "User with ID " + userId + " not found");
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//         }

//         try {
//             // Validate action
//             if (request.getAction() == null || request.getAction().isEmpty()) {
//                 throw new IllegalArgumentException("Action is required");
//             }

//             switch (request.getAction().toUpperCase()) {
//                 case "CREATE":
//                     if (request.getPlaylistId() == null || request.getScheduleInfo() == null) {
//                         throw new IllegalArgumentException("Playlist ID and schedule info are required for CREATE action");
//                     }
//                     validateScheduleInfo(request.getScheduleInfo());
//                     Playlist createdSchedule = playlistService.schedulePlaylist(userId, request.getPlaylistId(), request.getScheduleInfo());
//                     return ResponseEntity.ok(createdSchedule);

//                 case "UPDATE":
//                     if (request.getPlaylistId() == null || request.getScheduleInfo() == null) {
//                         throw new IllegalArgumentException("Playlist ID and schedule info are required for UPDATE action");
//                     }
//                     validateScheduleInfo(request.getScheduleInfo());
//                     Playlist updatedSchedule = playlistService.updatePlaylistSchedule(userId, request.getPlaylistId(), request.getScheduleInfo());
//                     return ResponseEntity.ok(updatedSchedule);

//                 case "DELETE":
//                     if (request.getPlaylistId() == null) {
//                         throw new IllegalArgumentException("Playlist ID is required for DELETE action");
//                     }
//                     Playlist deletedSchedule = playlistService.removePlaylistSchedule(userId, request.getPlaylistId());
//                     return ResponseEntity.ok(deletedSchedule);

//                 case "GET_ACTIVE":
//                     if (request.getDeviceId() == null) {
//                         throw new IllegalArgumentException("Device ID is required for GET_ACTIVE action");
//                     }
//                     List<Playlist> activePlaylists = playlistService.getActivePlaylistsForDevice(userId, request.getDeviceId(), LocalDateTime.now());
//                     return ResponseEntity.ok(activePlaylists);

//                 case "GET_UPCOMING":
//                     int hours = request.getHours() != null ? request.getHours() : 24;
//                     if (hours <= 0) {
//                         throw new IllegalArgumentException("Hours must be positive for GET_UPCOMING action");
//                     }
//                     List<Playlist> upcomingPlaylists = playlistService.getUpcomingScheduledPlaylists(userId, hours);
//                     return ResponseEntity.ok(upcomingPlaylists);

//                 case "GET_SCHEDULED":
//                     List<Playlist> scheduledPlaylists = playlistService.getScheduledPlaylists(userId);
//                     return ResponseEntity.ok(scheduledPlaylists);

//                 case "GET_SCHEDULE":
//                     if (request.getPlaylistId() == null) {
//                         throw new IllegalArgumentException("Playlist ID is required for GET_SCHEDULE action");
//                     }
//                     Playlist.PlaylistScheduleInfo scheduleInfo = playlistService.getPlaylistScheduleInfo(userId, request.getPlaylistId());
//                     if (scheduleInfo != null) {
//                         return ResponseEntity.ok(scheduleInfo);
//                     } else {
//                         Map<String, String> response = new HashMap<>();
//                         response.put("message", "No schedule found for this playlist");
//                         return ResponseEntity.ok(response);
//                     }

//                 case "GET_SCHEDULED_FOR_TIME":
//                     if (request.getTargetTime() == null) {
//                         throw new IllegalArgumentException("Target time is required for GET_SCHEDULED_FOR_TIME action");
//                     }
//                     List<Playlist> scheduledForTime = playlistService.getPlaylistsScheduledForTime(userId, request.getTargetTime());
//                     return ResponseEntity.ok(scheduledForTime);

//                 case "MARK_EXECUTED":
//                     if (request.getPlaylistId() == null) {
//                         throw new IllegalArgumentException("Playlist ID is required for MARK_EXECUTED action");
//                     }
//                     playlistService.markPlaylistExecuted(userId, request.getPlaylistId());
//                     return ResponseEntity.ok().build();

//                 default:
//                     throw new IllegalArgumentException("Invalid action: " + request.getAction());
//             }
//         } catch (IllegalArgumentException e) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", e.getMessage());
//             return ResponseEntity.badRequest().body(errorResponse);
//         } catch (Exception e) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", "An error occurred: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//         }
//     }

//     // Helper method to validate PlaylistScheduleInfo
//     private void validateScheduleInfo(Playlist.PlaylistScheduleInfo scheduleInfo) {
//         if (scheduleInfo == null) {
//             throw new IllegalArgumentException("Schedule info cannot be null");
//         }
//         // Validate required fields for scheduling
//         if (scheduleInfo.isScheduled() && scheduleInfo.getScheduledTime() == null && !scheduleInfo.isRecurring()) {
//             throw new IllegalArgumentException("Scheduled time or recurring setting is required when scheduling is enabled");
//         }
//         if (scheduleInfo.isSpecificTimeEnabled() && (scheduleInfo.getSpecificStartTime() == null || scheduleInfo.getSpecificEndTime() == null)) {
//             throw new IllegalArgumentException("Specific start and end times are required when specific time is enabled");
//         }
//         if (scheduleInfo.isWeekCycleEnabled() && (scheduleInfo.getWeekCycleType() == null || scheduleInfo.getWeekCycleType() < 3 || scheduleInfo.getWeekCycleType() > 5)) {
//             throw new IllegalArgumentException("Week cycle type must be 3, 4, or 5 when week cycle is enabled");
//         }
//         if (scheduleInfo.isRecurring() && (scheduleInfo.getRecurring() == null || !List.of("daily", "weekly", "monthly", "custom").contains(scheduleInfo.getRecurring().toLowerCase()))) {
//             throw new IllegalArgumentException("Recurring type must be daily, weekly, monthly, or custom");
//         }
//     }

@PostMapping("/user/{userId}/schedule")
    public ResponseEntity<?> handleScheduleRequest(
            @PathVariable String userId,
            @Valid @RequestBody ScheduleRequest request) {
        Optional<User> user = userService.getUserById(userId);
        if (!user.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User with ID " + userId + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        if (!isRootUser() && !getCurrentUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // Validate action
            if (request.getAction() == null || request.getAction().isEmpty()) {
                throw new IllegalArgumentException("Action is required");
            }

            switch (request.getAction().toUpperCase()) {
                case "CREATE":
                    if (request.getPlaylistId() == null || request.getScheduleInfo() == null) {
                        throw new IllegalArgumentException("Playlist ID and schedule info are required for CREATE action");
                    }
                    validateScheduleInfo(request.getScheduleInfo());

                    // Check for duplicate or overlapping schedule
                    if (hasConflictingSchedule(userId, request.getPlaylistId(), request.getScheduleInfo())) {
                        throw new IllegalArgumentException("A schedule with the same or overlapping time and days already exists for this user.");
                    }

                    Playlist createdSchedule = playlistService.schedulePlaylist(userId, request.getPlaylistId(), request.getScheduleInfo());
                    return ResponseEntity.ok(createdSchedule);

                case "UPDATE":
                    if (request.getPlaylistId() == null || request.getScheduleInfo() == null) {
                        throw new IllegalArgumentException("Playlist ID and schedule info are required for UPDATE action");
                    }
                    validateScheduleInfo(request.getScheduleInfo());
                    Playlist updatedSchedule = playlistService.updatePlaylistSchedule(userId, request.getPlaylistId(), request.getScheduleInfo());
                    return ResponseEntity.ok(updatedSchedule);

                case "DELETE":
                    if (request.getPlaylistId() == null) {
                        throw new IllegalArgumentException("Playlist ID is required for DELETE action");
                    }
                    Playlist deletedSchedule = playlistService.removePlaylistSchedule(userId, request.getPlaylistId());
                    return ResponseEntity.ok(deletedSchedule);

                case "GET_ACTIVE":
                    if (request.getDeviceId() == null) {
                        throw new IllegalArgumentException("Device ID is required for GET_ACTIVE action");
                    }
                    List<Playlist> activePlaylists = playlistService.getActivePlaylistsForDevice(userId, request.getDeviceId(), LocalDateTime.now());
                    return ResponseEntity.ok(activePlaylists);

                case "GET_UPCOMING":
                    int hours = request.getHours() != null ? request.getHours() : 24;
                    if (hours <= 0) {
                        throw new IllegalArgumentException("Hours must be positive for GET_UPCOMING action");
                    }
                    List<Playlist> upcomingPlaylists = playlistService.getUpcomingScheduledPlaylists(userId, hours);
                    return ResponseEntity.ok(upcomingPlaylists);

                case "GET_SCHEDULED":
                    List<Playlist> scheduledPlaylists = playlistService.getScheduledPlaylists(userId);
                    return ResponseEntity.ok(scheduledPlaylists);

                case "GET_SCHEDULE":
                    if (request.getPlaylistId() == null) {
                        throw new IllegalArgumentException("Playlist ID is required for GET_SCHEDULE action");
                    }
                    Playlist.PlaylistScheduleInfo scheduleInfo = playlistService.getPlaylistScheduleInfo(userId, request.getPlaylistId());
                    if (scheduleInfo != null) {
                        return ResponseEntity.ok(scheduleInfo);
                    } else {
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "No schedule found for this playlist");
                        return ResponseEntity.ok(response);
                    }

                case "GET_SCHEDULED_FOR_TIME":
                    if (request.getTargetTime() == null) {
                        throw new IllegalArgumentException("Target time is required for GET_SCHEDULED_FOR_TIME action");
                    }
                    List<Playlist> scheduledForTime = playlistService.getPlaylistsScheduledForTime(userId, request.getTargetTime());
                    return ResponseEntity.ok(scheduledForTime);

                case "MARK_EXECUTED":
                    if (request.getPlaylistId() == null) {
                        throw new IllegalArgumentException("Playlist ID is required for MARK_EXECUTED action");
                    }
                    playlistService.markPlaylistExecuted(userId, request.getPlaylistId());
                    return ResponseEntity.ok().build();

                default:
                    throw new IllegalArgumentException("Invalid action: " + request.getAction());
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper method to validate PlaylistScheduleInfo
    private void validateScheduleInfo(Playlist.PlaylistScheduleInfo scheduleInfo) {
        if (scheduleInfo == null) {
            throw new IllegalArgumentException("Schedule info cannot be null");
        }
        if (scheduleInfo.isScheduled() && scheduleInfo.getScheduledTime() == null && !scheduleInfo.isRecurring()) {
            throw new IllegalArgumentException("Scheduled time or recurring setting is required when scheduling is enabled");
        }
        if (scheduleInfo.isSpecificTimeEnabled() && (scheduleInfo.getSpecificStartTime() == null || scheduleInfo.getSpecificEndTime() == null)) {
            throw new IllegalArgumentException("Specific start and end times are required when specific time is enabled");
        }
        if (scheduleInfo.isWeekCycleEnabled() && (scheduleInfo.getWeekCycleType() == null || scheduleInfo.getWeekCycleType() < 1 || scheduleInfo.getWeekCycleType() > 7)) {
            throw new IllegalArgumentException("Week cycle type must be 1 to 7 when week cycle is enabled");
        }
        if (scheduleInfo.isRecurring() && (scheduleInfo.getRecurring() == null || !List.of("daily", "weekly", "monthly", "custom").contains(scheduleInfo.getRecurring().toLowerCase()))) {
            throw new IllegalArgumentException("Recurring type must be daily, weekly, monthly, or custom");
        }
    }

    // New helper method to check for conflicting schedules
    private boolean hasConflictingSchedule(String userId, String playlistId, Playlist.PlaylistScheduleInfo newSchedule) {
        // Use PlaylistService to get scheduled playlists for the user
        List<Playlist> scheduledPlaylists = playlistService.getScheduledPlaylists(userId);

        for (Playlist playlist : scheduledPlaylists) {
            // Skip the current playlist being scheduled (if updating)
            if (playlist.getId().equals(playlistId)) {
                continue;
            }

            Playlist.PlaylistScheduleInfo existingSchedule = playlist.getScheduleInfo();
            if (existingSchedule != null && existingSchedule.isScheduled() && existingSchedule.isEnabled()) {
                // Check for exact match or overlapping schedule
                boolean isSameTime = existingSchedule.getSpecificStartTime().equals(newSchedule.getSpecificStartTime()) &&
                                     existingSchedule.getSpecificEndTime().equals(newSchedule.getSpecificEndTime());
                boolean isSameDays = existingSchedule.getSelectedDaysOfWeek() != null &&
                                     newSchedule.getSelectedDaysOfWeek() != null &&
                                     existingSchedule.getSelectedDaysOfWeek().containsAll(newSchedule.getSelectedDaysOfWeek()) &&
                                     newSchedule.getSelectedDaysOfWeek().containsAll(existingSchedule.getSelectedDaysOfWeek());
                boolean isSameDateRange = existingSchedule.getStartDate().equals(newSchedule.getStartDate()) &&
                                          existingSchedule.getEndDate().equals(newSchedule.getEndDate());

                // Check for overlap in time ranges
                boolean isOverlappingTime = newSchedule.getSpecificStartTime().isBefore(existingSchedule.getSpecificEndTime()) &&
                                            newSchedule.getSpecificEndTime().isAfter(existingSchedule.getSpecificStartTime());

                if ((isSameTime && isSameDays && isSameDateRange) || (isOverlappingTime && isSameDays && isSameDateRange)) {
                    return true; // Conflict found
                }
            }
        }
        return false; // No conflict
    }

    // ... other existing methods in PlaylistController.java ...


}
