
package com.mediaserver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.mediaserver.dto.MonthlyActivityData;
import com.mediaserver.model.Content;
import com.mediaserver.model.Playlist;
import com.mediaserver.model.User;
import com.mediaserver.repository.PlaylistContentRepository;
import com.mediaserver.repository.PlaylistRepository;
import com.mediaserver.security.RootUserPrincipal;
import com.mediaserver.security.UserPrincipal;
import com.mediaserver.service.ContentService;
import com.mediaserver.service.DeviceService;
import com.mediaserver.service.PlaylistService;
import com.mediaserver.service.SplitScreenService;
import com.mediaserver.service.SplitScreenSessionService;

import com.mediaserver.service.StorageService;
import com.mediaserver.service.UserService;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistContentRepository playlistContentRepository;

    @Autowired
    private SplitScreenService splitScreenService;

    @Autowired
    private SplitScreenSessionService splitScreenSessionService;

    @Value("${content.storage.location}")
    private String storageLocation;

    private boolean isRootUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ROOT"));
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        } else if (principal instanceof RootUserPrincipal) {
            return null; // root can access all
        }
        return null;
    }

    private boolean isAuthorized(String targetUserId) {
        if (isRootUser()) return true;
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(targetUserId);
    }

    @GetMapping
    public ResponseEntity<List<Content>> getAllContent() {
        if (isRootUser()) {
            return ResponseEntity.ok(contentService.getAllContent());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Content>> getContentByUser(@PathVariable String userId) {
        if (isAuthorized(userId)) {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(contentService.getContentByUserId(userId));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/filter/{userId}/{value}")
    public ResponseEntity<List<Content>> filterByTitle(@PathVariable String userId, @PathVariable String value) {
        if (isAuthorized(userId)) {
            return ResponseEntity.ok(contentService.filterContent(userId, value));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Content> getContentById(@PathVariable String id) {
        Optional<Content> content = contentService.getContentById(id);
        if (content.isPresent()) {
            if (isAuthorized(content.get().getUserId())) {
                return ResponseEntity.ok(content.get());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/playlist/filter/{playListId}/{userId}")
    public ResponseEntity<List<Content>> getContentWidthOutExistingInPlayList(@PathVariable String playListId,
            @PathVariable String userId) {
        if (isAuthorized(userId)) {
            return ResponseEntity.ok(contentService.getContentWidthOutExistingInPlayList(playListId, userId));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/upload/user/{userId}")
    public ResponseEntity<?> uploadContent(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "tags", required = false) String tags) {
        if (isAuthorized(userId)) {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                try {
                    Content uploadedContent = contentService.uploadContent(file, title, userId, tags);
                    return ResponseEntity.status(HttpStatus.CREATED).body(uploadedContent);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to upload content: " + e.getMessage());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/weblink/user/{userId}")
    public ResponseEntity<?> addWebLink(
            @PathVariable String userId,
            @RequestParam("url") String url,
            @RequestParam("title") String title,
            @RequestParam(value = "tags", required = false) String tags) {
        if (isAuthorized(userId)) {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                try {
                    Content webLinkContent = contentService.addWebLink(url, title, userId, tags);
                    return ResponseEntity.status(HttpStatus.CREATED).body(webLinkContent);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to add web link: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(e.getMessage());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/user/{userId}/type/{mediaType}")
    public ResponseEntity<List<Content>> getContentByMediaType(
            @PathVariable String userId,
            @PathVariable String mediaType) {
        if (isAuthorized(userId)) {
            try {
                com.mediaserver.model.Content.MediaType myType = com.mediaserver.model.Content.MediaType
                        .valueOf(mediaType.toUpperCase());
                List<Content> content = contentService.getContentByMediaType(userId, myType);
                return ResponseEntity.ok(content);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/update/{contentId}")
    public ResponseEntity<Content> updateContent(@PathVariable String contentId, @RequestBody Content content)
            throws IOException {
        Optional<Content> existingContent = contentService.getContentById(contentId);
        if (existingContent.isPresent()) {
            if (isAuthorized(existingContent.get().getUserId())) {
                return contentService.updateContent(contentId, content)
                        .map(updated -> ResponseEntity.ok(updated))
                        .orElseGet(() -> ResponseEntity.notFound().build());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/streaming")
    public ResponseEntity<Resource> streamingContent(@PathVariable String id) {
        Optional<Content> contentOpt = contentService.getContentById(id);
        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();
            if (isAuthorized(content.getUserId())) {
                try {
                    Resource resource = storageService.loadAsResource(content.getFilePath());
                    if (resource.exists() && resource.isReadable()) {
                        String mimeType = URLConnection.guessContentTypeFromName(resource.getFilename());
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType(mimeType));
                        headers.setContentLength(resource.contentLength());
                        headers.setContentDispositionFormData("inline", resource.getFilename());
                        return ResponseEntity.ok()
                                .headers(headers)
                                .body(resource);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable String id) {
        Optional<Content> contentOpt = contentService.getContentById(id);
        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();
            if (isAuthorized(content.getUserId())) {
                File file = new File(storageLocation + "/" + content.getFilePath());
                if (!file.exists()) {
                    return ResponseEntity.notFound().build();
                }
                StreamingResponseBody stream = outputStream -> {
                    try (InputStream inputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            try {
                                outputStream.write(buffer, 0, bytesRead);
                            } catch (IOException e) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                        .body(stream);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<StreamingResponseBody> file(@PathVariable String id) {
        Optional<Content> contentOpt = contentService.getContentById(id);
        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();
            if (isAuthorized(content.getUserId())) {
                File file = new File(storageLocation + "/" + content.getFilePath());
                if (!file.exists()) {
                    return ResponseEntity.notFound().build();
                }
                StreamingResponseBody stream = outputStream -> {
                    try (InputStream inputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            try {
                                outputStream.write(buffer, 0, bytesRead);
                            } catch (IOException e) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                        .body(stream);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String id) {
        Optional<Content> contentOpt = contentService.getContentById(id);
        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();
            if (isAuthorized(content.getUserId())) {
                if (content.getThumbnail() == null) {
                    return ResponseEntity.notFound().build();
                }
                try {
                    Resource resource = storageService.loadAsResource(content.getThumbnail());
                    if (resource.exists() && resource.isReadable()) {
                        String mimeType = URLConnection.guessContentTypeFromName(resource.getFilename());
                        if (mimeType == null) {
                            mimeType = "image/png";
                        }
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType(mimeType));
                        headers.setContentLength(resource.contentLength());
                        headers.setContentDispositionFormData("inline", resource.getFilename());
                        return ResponseEntity.ok()
                                .headers(headers)
                                .body(resource);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadContent(@PathVariable String id) {
        Optional<Content> contentOpt = contentService.getContentById(id);
        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();
            if (isAuthorized(content.getUserId())) {
                try {
                    Resource resource = storageService.loadAsResource(content.getFilePath());
                    if (resource.exists() && resource.isReadable()) {
                        String fileName = content.getTitle() + getExtensionFromPath(content.getFilePath());
                        return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(content.getFileType()))
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                                .body(resource);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable String id) {
        Optional<Content> content = contentService.getContentById(id);
        if (content.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Content not found with ID: " + id);
        }
        if (isAuthorized(content.get().getUserId())) {
            try {
                contentService.deleteContent(id);
                List<Playlist> allPlaylists = playlistService.getPlaylistsByUserId(content.get().getUserId());
                for (Playlist playlist : allPlaylists) {
                    boolean modified = playlist.getItems()
                            .removeIf(item -> item.getMediaId().equals(id));
                    if (modified) {
                        playlistRepository.save(playlist);
                    }
                }
                playlistContentRepository.deleteByContentId(id);
                return ResponseEntity.ok("Content deleted successfully and removed from playlists.");
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete content due to an internal server error.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private String getExtensionFromPath(String filePath) {
        if (filePath != null && filePath.contains(".")) {
            return filePath.substring(filePath.lastIndexOf("."));
        }
        return "";
    }

    private boolean isValidFileType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith("video/") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("application/pdf");
    }

    @GetMapping("/user/{userId}/tags/{tag}")
    public ResponseEntity<List<Content>> filterByTag(
            @PathVariable String userId,
            @PathVariable String tag) {
        if (isAuthorized(userId)) {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                List<Content> content = contentService.filterByTag(userId, tag);
                return ResponseEntity.ok(content);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // @GetMapping("/filter/userId/{userId}/{value}")
    // public ResponseEntity<List<Content>> filterContent(
    // @PathVariable String userId,
    // @PathVariable String value) {
    // Optional<User> user = userService.getUserById(userId);
    // if (user.isPresent()) {
    // return ResponseEntity.ok(contentService.filterContent(userId, value));
    // } else {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    // }
    // }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<Content>> getContentByDateRange(
            @PathVariable String userId,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {
        if (isAuthorized(userId)) {
            try {
                // Assuming date format is yyyy-MM-dd
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate startLocalDate = LocalDate.parse(startDateStr, formatter);
                LocalDate endLocalDate = LocalDate.parse(endDateStr, formatter);

                // Convert to LocalDateTime: start of day to end of day (inclusive)
                LocalDateTime startDate = startLocalDate.atStartOfDay();
                LocalDateTime endDate = endLocalDate.atTime(LocalTime.MAX);

                Optional<User> user = userService.getUserById(userId);
                if (user.isPresent()) {
                    List<Content> content = contentService.getContentByUserIdAndDateRange(userId, startDate, endDate);
                    return ResponseEntity.ok(content);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Add this to an existing ContentController.java or create a new one if not
    // present
    // Assuming ContentController exists; add the following method

    // @GetMapping("/user/{userId}/usage/monthly")
    // public ResponseEntity<MonthlyActivityData> getMonthlyUsage(@PathVariable
    // String userId) {
    // return ResponseEntity.ok(contentService.getMonthlyContentActivity(userId));
    // }

    @GetMapping("/user/{userId}/usage/monthly")
    public ResponseEntity<MonthlyActivityData> getMonthlyUsage(@PathVariable String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (isAuthorized(userId)) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = from != null ? LocalDate.parse(from, dateFormatter)
                    : now.toLocalDate().minusYears(1).plusDays(1);
            LocalDate endDate = to != null ? LocalDate.parse(to, dateFormatter) : now.toLocalDate();
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59, 999999999);
            return ResponseEntity.ok(contentService.getMonthlyContentActivity(userId, start, end));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/usage/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyUsageForAllUsers(@RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // pass-through

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = from != null ? LocalDate.parse(from, dateFormatter)
                : now.toLocalDate().minusMonths(11).withDayOfMonth(1);
        LocalDate endDate = to != null ? LocalDate.parse(to, dateFormatter) : now.toLocalDate();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59, 999999999);

        MonthlyActivityData contentData = contentService.getMonthlyContentActivityForAllUsers(start, end);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("contentPlays", contentData.getContentPlays());
        response.put("bandwidthGb", contentData.getBandwidthGb());
        response.put("storageGb", contentData.getStorageGb());

        return ResponseEntity.ok(response);
    }

    // ============================
    // Playback / Screen payload
    // GET /content/screen/layoutMode
    // ============================

    @GetMapping("/screen/layoutMode")
    public ResponseEntity<?> getScreenLayoutMode(
            @RequestParam String contentId1,
            @RequestParam String contentId2,
            @RequestParam String layoutMode) {

        try {
            if (contentId1 == null || contentId1.isBlank()) {
                return ResponseEntity.badRequest().body("contentId1 is required");
            }
            if (contentId2 == null || contentId2.isBlank()) {
                return ResponseEntity.badRequest().body("contentId2 is required");
            }

            Optional<com.mediaserver.model.Content> content1Opt = contentService.getContentById(contentId1);
            if (content1Opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Content not found with id1: " + contentId1);
            }

            Optional<com.mediaserver.model.Content> content2Opt = contentService.getContentById(contentId2);
            if (content2Opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Content not found with id2: " + contentId2);
            }

            // If either content belongs to a different user, block for non-root.
            if (!isRootUser()) {
                String currentUserId = getCurrentUserId();
                if (currentUserId == null ||
                        (content1Opt.get().getUserId() != null && !content1Opt.get().getUserId().equals(currentUserId))
                        ||
                        (content2Opt.get().getUserId() != null
                                && !content2Opt.get().getUserId().equals(currentUserId))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            if (layoutMode == null || layoutMode.isBlank()) {
                return ResponseEntity.badRequest().body("layoutMode is required");
            }

            String normalized = layoutMode.trim().toUpperCase();
            if (!"HORIZONTAL".equals(normalized) && !"VERTICAL".equals(normalized)) {
                return ResponseEntity.badRequest().body("layoutMode must be either HORIZONTAL or VERTICAL");
            }

            List<com.mediaserver.model.Content> zone1 = new java.util.ArrayList<>();
            zone1.add(content1Opt.get());

            List<com.mediaserver.model.Content> zone2 = new java.util.ArrayList<>();
            zone2.add(content2Opt.get());

            Map<String, Object> zones = new LinkedHashMap<>();
            if ("VERTICAL".equals(layoutMode)) {
                zones.put("TOP", zone1);
                zones.put("BOTTOM", zone2);
            } else {
                // HORIZONTAL
                zones.put("LEFT", zone1);
                zones.put("RIGHT", zone2);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("contentId1", contentId1);
            response.put("contentId2", contentId2);
            response.put("layoutMode", layoutMode);
            response.put("zones", zones);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching screen layoutMode: " + e.getMessage());
        }
    }

    @PostMapping("/screen/layoutMode")
    public ResponseEntity<?> updateScreenLayoutMode(
            @RequestBody com.mediaserver.model.Device.ScreenLayoutModeRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("Request body is missing");
            }

            if (request.getContentId1() == null || request.getContentId1().isBlank()) {
                return ResponseEntity.badRequest().body("contentId1 is required");
            }
            if (request.getContentId2() == null || request.getContentId2().isBlank()) {
                return ResponseEntity.badRequest().body("contentId2 is required");
            }

            if (request.getLayoutMode() == null || request.getLayoutMode().isBlank()) {
                return ResponseEntity.badRequest().body("layoutMode is required");
            }

            String normalized = request.getLayoutMode().trim().toUpperCase();
            if (!"HORIZONTAL".equals(normalized) && !"VERTICAL".equals(normalized)) {
                return ResponseEntity.badRequest().body("layoutMode must be either HORIZONTAL or VERTICAL");
            }
            // Split into two zones (still returned, but layoutMode is now saved)
            java.util.Optional<com.mediaserver.model.Content> content1Opt = contentService
                    .getContentById(request.getContentId1());
            if (content1Opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Content not found with id1: " + request.getContentId1());
            }

            java.util.Optional<com.mediaserver.model.Content> content2Opt = contentService
                    .getContentById(request.getContentId2());
            if (content2Opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Content not found with id2: " + request.getContentId2());
            }

            List<com.mediaserver.model.Content> zone1 = new java.util.ArrayList<>();
            zone1.add(content1Opt.get());
            List<com.mediaserver.model.Content> zone2 = new java.util.ArrayList<>();
            zone2.add(content2Opt.get());

            Map<String, Object> zones = new LinkedHashMap<>();
            if ("HORIZONTAL".equals(normalized)) {
                zones.put("TOP", zone1);
                zones.put("BOTTOM", zone2);
            } else {
                // VERTICAL
                zones.put("LEFT", zone1);
                zones.put("RIGHT", zone2);
            }

            // Generate a real UUID for persistent session storage
            String uniqueId = UUID.randomUUID().toString();

            // ✅ Persist uniqueId + layoutMode in MongoDB (so streaming can validate later)
            splitScreenSessionService.saveSession(
                    uniqueId,
                    normalized,
                    request.getContentId1(),
                    request.getContentId2());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("uniqueId", uniqueId);
            response.put("contentId1", request.getContentId1());
            response.put("contentId2", request.getContentId2());
            response.put("layoutMode", normalized);
            response.put("zones", zones);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating screen layoutMode: " + e.getMessage());
        }
    }

    // GET /api/content/split-streamByUniqueId/{uniqueId}?zone=1|2
    // Streams the selected content bytes (like video streaming).
    @GetMapping("/split-streamByUniqueId/{uniqueId}")
    public ResponseEntity<?> splitStreamByUniqueId(
            @PathVariable String uniqueId,
            @RequestParam(required = false) String zone,
            @RequestHeader(name = "Range", required = false) String range) {
        try {
            uniqueId = uniqueId.trim();
            com.mediaserver.repository.SplitScreenRepository.SplitScreenModel model = splitScreenService
                    .getSplitScreenByUniqueIdOrThrow(uniqueId);

            List<Content> zone1 = model.getZone1();
            List<Content> zone2 = model.getZone2();

            String normalized = zone == null ? "" : zone.trim().toLowerCase();

            // 1. Return JSON Metadata if requested
            if ("info".equals(normalized) || "json".equals(normalized)) {
                return ResponseEntity.ok(splitScreenService.toResponsePayload(model));
            }

            // 2. Return a Visual HTML Split-Screen Preview only if explicitly requested
            if ("preview".equals(normalized) || "html".equals(normalized)) {
                String layout = model.getLayoutMode(); // HORIZONTAL or VERTICAL
                // HORIZONTAL = Top/Bottom cut -> Flex Column
                // VERTICAL = Left/Right cut -> Flex Row
                String flexDir = "HORIZONTAL".equalsIgnoreCase(layout) ? "column" : "row";

                String html = "<!DOCTYPE html><html>" +
                        "<body style='margin:0; background:black; display:flex; flex-direction:" + flexDir
                        + "; height:100vh; width:100vw;'>" +
                        "<video style='flex:1; object-fit:contain; min-height:0; min-width:0;' src='/api/content/split-streamByUniqueId/"
                        + uniqueId + "?zone=1' autoplay loop muted controls></video>" +
                        "<video style='flex:1; object-fit:contain; min-height:0; min-width:0;' src='/api/content/split-streamByUniqueId/"
                        + uniqueId + "?zone=2' autoplay loop muted controls></video>" +
                        "</body></html>";

                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(html);
            }

            // 2.5 Dynamic FFmpeg Merge Stream when no zone is specified
            if (normalized.isEmpty()) {
                if (zone1 == null || zone1.isEmpty() || zone2 == null || zone2.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                Content c1 = zone1.get(0);
                Content c2 = zone2.get(0);
                Resource r1 = storageService.loadAsResource(c1.getFilePath());
                Resource r2 = storageService.loadAsResource(c2.getFilePath());
                if (!r1.exists() || !r2.exists()) return ResponseEntity.notFound().build();

                boolean isHorizontal = "HORIZONTAL".equalsIgnoreCase(model.getLayoutMode());
                // For HORIZONTAL layout (Top/Bottom stack), we need identical widths -> scale to width 1280, auto-height
                // For VERTICAL layout (Left/Right stack), we need identical heights -> scale to auto-width, height 720
                String filterComplex = isHorizontal
                        ? "[0:v]scale=1280:-2[v0];[1:v]scale=1280:-2[v1];[v0][v1]vstack=inputs=2[v]"
                        : "[0:v]scale=-2:720[v0];[1:v]scale=-2:720[v1];[v0][v1]hstack=inputs=2[v]";

                String p1 = r1.getFile().getAbsolutePath();
                String p2 = r2.getFile().getAbsolutePath();

                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg", "-i", p1, "-i", p2,
                        "-filter_complex", filterComplex,
                        "-map", "[v]", "-c:v", "libx264", "-preset", "ultrafast",
                        "-f", "mp4", "-movflags", "frag_keyframe+empty_moov", "pipe:1"
                );

                try {
                    Process process = pb.start();
                    org.springframework.core.io.InputStreamResource streamResource = new org.springframework.core.io.InputStreamResource(process.getInputStream()) {
                        @Override
                        public java.io.InputStream getInputStream() throws java.io.IOException, IllegalStateException {
                            return new java.io.FilterInputStream(super.getInputStream()) {
                                @Override
                                public void close() throws java.io.IOException {
                                    try {
                                        super.close();
                                    } finally {
                                        process.destroyForcibly();
                                    }
                                }
                            };
                        }
                        
                        @Override
                        public long contentLength() {
                            return -1; // Unknown length for live streams
                        }
                    };
                    
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                            .body(streamResource);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            // 3. Otherwise, stream the actual video bytes for zone 1 or 2
            Content selected;
            if ("2".equals(normalized)) {
                if (zone2 == null || zone2.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                selected = zone2.get(0);
            } else {
                if (zone1 == null || zone1.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                selected = zone1.get(0);
            }

            if (selected == null || selected.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            // NOTE: We bypass the `isAuthorized` check here because the UUID `uniqueId` acts as a secure, unguessable token.
            // HTML <video> tags cannot send JWT headers, so enforcing auth here would block the video from playing in the browser.

            // Prefer Range-aware streaming for MP4.
            Resource resource = storageService.loadAsResource(selected.getFilePath());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String mimeType = URLConnection.guessContentTypeFromName(resource.getFilename());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            // Spring's ResourceHttpMessageConverter natively supports byte-range streaming, Accept-Ranges, and partial content!
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (java.io.IOException ex) {
            System.err.println("File not found on disk: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleExceptions(Exception ex) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "Unknown Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
