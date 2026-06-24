
package com.mediaserver.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mediaserver.dto.MonthlyActivityData;
import com.mediaserver.dto.WeeklyActivityData;
import com.mediaserver.model.Content;
import com.mediaserver.model.Content.MediaType;
import com.mediaserver.model.PlayListItems;
import com.mediaserver.model.Playlist;
import com.mediaserver.repository.ContentRepository;
import com.mediaserver.repository.PlaylistRepository;
import com.mediaserver.repository.SubscriptionPlanRepository;
import com.mediaserver.repository.SubscriptionRepository;

@Service
public class ContentService {

    @Value("${app.content.base-url:http://192.168.0.158:9000}")
    private String baseUrl;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionLimitService subscriptionLimitService;

    public List<Content> getAllContent() {
        return contentRepository.findAll();
    }

    public List<Content> getContentByUserId(String userId) {
        return contentRepository.findByUserIdAndSystemGeneratedFalseOrderByUploadDateDesc(userId);
    }

    public Optional<Content> getContentById(String id) {
        return contentRepository.findById(id);
    }
    public List<Content> getContentWidthOutExistingInPlayList(String id, String userId) {
        Optional<Playlist> playListData = playlistRepository.findById(id);
        List<Content> contentList = contentRepository.findByUserId(userId).stream()
                .filter(c -> !c.isSystemGenerated())
                .collect(Collectors.toList());
        if (playListData.isPresent()) {
            List<PlayListItems> playListItemsData = playListData.get().getItems();
            return contentList.stream()
                    .filter(mediaItem -> playListItemsData.stream()
                            .noneMatch(playlistItem -> playlistItem.getMediaId().equals(mediaItem.getId())))
                    .collect(Collectors.toList());
        } else {
            return contentList;
        }
    }

    /**
     * ✅ MAIN UPLOAD METHOD WITH SUBSCRIPTION LIMIT CHECK
     */
    public Content uploadContent(MultipartFile file, String title, String userId, String tags) throws IOException {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("🔍 [uploadContent] Starting content upload");
        System.out.println("   User ID: " + userId);
        System.out.println("   Title: " + title);
        System.out.println("   File Size: " + (file.getSize() / 1024) + " KB");
        System.out.println("════════════════════════════════════════");

        // ✅ CHECK SUBSCRIPTION LIMITS FIRST - BEFORE ANY FILE OPERATIONS
        try {
            subscriptionLimitService.checkContentLimit(userId);
            System.out.println("✅ Subscription limit check PASSED");
        } catch (SubscriptionLimitService.SubscriptionLimitExceededException e) {
            System.out.println("❌ Subscription limit check FAILED");
            System.out.println("   Error: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }

        // Check for duplicate title
        boolean duplicate = contentRepository.findByUserId(userId)
                .stream()
                .anyMatch(c -> c.getTitle().equalsIgnoreCase(title.trim()));
        if (duplicate) {
            System.out.println("❌ Duplicate title found: " + title);
            throw new IllegalArgumentException("Content with title '" + title + "' already exists.");
        }

        System.out.println("✅ Title is unique");

        // Store file and generate metadata
        String filePath = storageService.store(file);
        System.out.println("✅ File stored at: " + filePath);

        String thumbnailPath = null;
        String duration = null;
        MediaType mediaType = determineMediaType(file.getContentType());

        System.out.println("📝 Creating content object...");
        System.out.println("   Media Type: " + mediaType);

        Content content = new Content();
        content.setUniqueId(java.util.UUID.randomUUID().toString());
        content.setTitle(title.trim());
        content.setFilePath(filePath);
        content.setFileSize(file.getSize());
        content.setFileType(file.getContentType());
        content.setUserId(userId);
        content.setMediaType(mediaType);
        content.setUploadDate(LocalDateTime.now());

        if (tags != null && !tags.isEmpty()) {
            content.setTags(Arrays.asList(tags.split(",")));
            System.out.println("   Tags: " + tags);
        }

        // Handle different media types
        System.out.println("🎬 Processing media type: " + mediaType);
        switch (mediaType) {
            case VIDEO:
                System.out.println("   Generating video thumbnail...");
                try {
                    ThumbnailResult result = storageService.generateThumbnail(filePath);
                    if (result.getThumbnailPath() != null) {
                        thumbnailPath = result.getThumbnailPath();
                        content.setThumbnail(thumbnailPath);
                    }
                    if (result.getDuration() != null) {
                        duration = result.getDuration();
                        content.setDuration(duration);
                    }
                    System.out.println("   ✅ Video processed - Duration: " + duration);
                } catch (Exception e) {
                    System.out.println("   ⚠️ Video thumbnail skipped: " + e.getMessage());
                }
                break;

            case IMAGE:
                System.out.println("   Processing image...");
                try (InputStream inputStream = file.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image != null) {
                        content.setWidth(image.getWidth());
                        content.setHeight(image.getHeight());
                        System.out.println("   ✅ Image dimensions: " + image.getWidth() + "x" + image.getHeight());
                    }
                }
                String imageFormat = extractImageFormat(file.getOriginalFilename());
                content.setImageFormat(imageFormat);
                thumbnailPath = storageService.generateImageThumbnail(filePath);
                content.setThumbnail(thumbnailPath);
                System.out.println("   ✅ Image processed");
                break;

            case PDF:
                System.out.println("   Processing PDF...");
                int pageCount = storageService.getPdfPageCount(filePath);
                content.setPageCount(pageCount);
                thumbnailPath = storageService.generatePdfThumbnail(filePath);
                content.setThumbnail(thumbnailPath);
                System.out.println("   ✅ PDF processed - Pages: " + pageCount);
                break;

            default:
                System.out.println("   Document type not requiring special processing");
                break;
        }

        content.setUrl(baseUrl + "/content/file/" + filePath);
        if (thumbnailPath != null) {
            content.setThumbnail(thumbnailPath);
        }

        // Save to database
        Content saved = contentRepository.save(content);

        System.out.println("════════════════════════════════════════");
        System.out.println("✅ CONTENT UPLOADED SUCCESSFULLY");
        System.out.println("   Content ID: " + saved.getId());
        System.out.println("   Title: " + saved.getTitle());
        System.out.println("════════════════════════════════════════\n");

        return saved;
    }

    public Content addWebLink(String url, String title, String userId, String tags) throws IOException {
        System.out.println("\n🔍 [addWebLink] Adding web link for user: " + userId);

        // CHECK SUBSCRIPTION LIMITS FIRST
        try {
            subscriptionLimitService.checkContentLimit(userId);
            System.out.println("✅ Subscription limit check passed");
        } catch (SubscriptionLimitService.SubscriptionLimitExceededException e) {
            System.out.println("❌ Subscription limit check failed: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }

        boolean duplicate = contentRepository.findByUserId(userId)
                .stream()
                .anyMatch(c -> c.getTitle().equalsIgnoreCase(title.trim()));
        if (duplicate) {
            throw new IllegalArgumentException("Content with title '" + title + "' already exists.");
        }

        try {
            new URL(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        Content content = new Content();
        content.setTitle(title.trim());
        content.setWebUrl(url);
        content.setOriginalUrl(url);
        content.setFileType("text/html");
        content.setUserId(userId);
        content.setMediaType(MediaType.WEB_LINK);
        content.setUrl(url);
        content.setUploadDate(LocalDateTime.now());

        if (tags != null && !tags.isEmpty()) {
            content.setTags(Arrays.asList(tags.split(",")));
        }

        try {
            String thumbnailPath = storageService.generateWebThumbnail(url);
            content.setThumbnail(thumbnailPath);
        } catch (Exception e) {
            System.out.println("Failed to generate thumbnail for web link: " + e.getMessage());
        }

        Content saved = contentRepository.save(content);
        System.out.println("✅ Web link added successfully");
        return saved;
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType == null) {
            return MediaType.DOCUMENT;
        }

        if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        } else if (contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        } else if (contentType.equals("application/pdf")) {
            return MediaType.PDF;
        } else {
            return MediaType.DOCUMENT;
        }
    }

    private String extractImageFormat(String filename) {
        if (filename == null)
            return "unknown";

        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "unknown";
    }

    public Optional<Content> updateContent(String contentId, Content incoming) throws IOException {
        return contentRepository.findById(contentId).map(existing -> {
            if (incoming.getTitle() != null) {
                existing.setTitle(incoming.getTitle());
            }
            if (incoming.getDescription() != null) {
                existing.setDescription(incoming.getDescription());
            }
            if (incoming.getThumbnail() != null) {
                existing.setThumbnail(incoming.getThumbnail());
            }
            if (incoming.getDuration() != null) {
                existing.setDuration(incoming.getDuration());
            }
            if (incoming.getWebUrl() != null) {
                existing.setWebUrl(incoming.getWebUrl());
                existing.setOriginalUrl(incoming.getWebUrl());
            }
            if (incoming.getTags() != null) {
                existing.setTags(incoming.getTags());
            }
            return contentRepository.save(existing);
        });
    }

    public void deleteContent(String id) throws IOException {
        Optional<Content> contentOpt = contentRepository.findById(id);
        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();

            if (content.getMediaType() != MediaType.WEB_LINK) {
                if (content.getFilePath() != null) {
                    storageService.delete(content.getFilePath());
                }
            }

            if (content.getThumbnail() != null) {
                storageService.delete(content.getThumbnail());
            }

            contentRepository.delete(content);
        }
    }

    public List<Content> filterContent(String userId, String value) {
        String lowerCaseValue = value != null ? value.toLowerCase() : null;
        List<Content> allContent = contentRepository.findAll();
        if (lowerCaseValue != null && !lowerCaseValue.isEmpty()) {
            return allContent.stream()
                    .filter(content -> !content.isSystemGenerated())
                    .filter(content -> content.getUserId() != null && content.getUserId().equals(userId)
                            && ((content.getFileType() != null
                                    && content.getFileType().toLowerCase().startsWith(lowerCaseValue)) ||
                                    (content.getTitle() != null
                                            && content.getTitle().toLowerCase().startsWith(lowerCaseValue))
                                    ||
                                    (content.getFilePath() != null
                                            && content.getFilePath().toLowerCase().startsWith(lowerCaseValue))
                                    ||
                                    (content.getDescription() != null
                                            && content.getDescription().toLowerCase().startsWith(lowerCaseValue))
                                    ||
                                    (content.getDuration() != null
                                            && content.getDuration().toLowerCase().startsWith(lowerCaseValue))
                                    ||
                                    (content.getWebUrl() != null
                                            && content.getWebUrl().toLowerCase().contains(lowerCaseValue))
                                    ||
                                    (content.getMediaType() != null
                                            && content.getMediaType().toString().toLowerCase().contains(lowerCaseValue))
                                    ||
                                    (content.getTags() != null && content.getTags().stream()
                                            .anyMatch(tag -> tag.toLowerCase().contains(lowerCaseValue)))))
                    .collect(Collectors.toList());
        } else {
            return allContent.stream()
                    .filter(content -> !content.isSystemGenerated())
                    .filter(content -> content.getUserId() != null && content.getUserId().equals(userId))
                    .collect(Collectors.toList());
        }
    }

    public List<Content> getContentByMediaType(String userId, MediaType mediaType) {
        return contentRepository.findByUserId(userId).stream()
                .filter(content -> !content.isSystemGenerated())
                .filter(content -> content.getMediaType() == mediaType)
                .collect(Collectors.toList());
    }

    public List<Content> filterByTag(String userId, String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return contentRepository.findByUserIdAndSystemGeneratedFalseOrderByUploadDateDesc(userId);
        }
        return contentRepository.findByUserIdAndSystemGeneratedFalseAndTagsContainingIgnoreCase(userId, tag.trim());
    }

    public List<Content> getContentByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return contentRepository.findByUserIdAndSystemGeneratedFalseAndUploadDateBetweenOrderByUploadDateDesc(userId, startDate, endDate);
    }

    public WeeklyActivityData getWeeklyContentActivity(String userId, LocalDateTime from, LocalDateTime to) {
        WeeklyActivityData data = new WeeklyActivityData();
        Map<String, Double> contentPlays = new LinkedHashMap<>();
        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        for (String day : days) {
            contentPlays.put(day, 0.0);
        }

        contentRepository.getWeeklyStats(userId, from, to).forEach(stat -> {
            int dayOfWeek = stat.getId() - 1;
            if (dayOfWeek >= 0 && dayOfWeek < 7) {
                contentPlays.put(days[dayOfWeek], stat.getContentPlays().doubleValue());
            }
        });

        data.setContentPlays(contentPlays);
        return data;
    }

    public WeeklyActivityData getWeeklyContentActivityForAllUsers(LocalDateTime from, LocalDateTime to) {
        WeeklyActivityData data = new WeeklyActivityData();
        Map<String, Double> contentPlays = new LinkedHashMap<>();
        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        for (String day : days) {
            contentPlays.put(day, 0.0);
        }

        contentRepository.getWeeklyStatsForAllUsers(from, to).forEach(stat -> {
            int index = (stat.getId() + 5) % 7;
            contentPlays.put(days[index], stat.getContentPlays().doubleValue());
        });

        data.setContentPlays(contentPlays);
        return data;
    }

    public MonthlyActivityData getMonthlyContentActivity(String userId, LocalDateTime from, LocalDateTime to) {
        MonthlyActivityData data = new MonthlyActivityData();
        Map<String, Double> contentPlays = new LinkedHashMap<>();
        Map<String, Double> storageGb = new LinkedHashMap<>();
        Map<String, Double> bandwidthGb = new LinkedHashMap<>();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        LocalDateTime currentMonth = from.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (!currentMonth.isAfter(to)) {
            String monthKey = currentMonth.format(monthFormatter);
            contentPlays.put(monthKey, 0.0);
            storageGb.put(monthKey, 0.0);
            bandwidthGb.put(monthKey, 0.0);
            currentMonth = currentMonth.plusMonths(1);
        }

        contentRepository.getMonthlyStats(userId, from, to).forEach(stat -> {
            LocalDateTime monthDate = LocalDateTime.of(stat.getYear(), stat.getMonth(), 1, 0, 0);
            String monthKey = monthDate.format(monthFormatter);

            if (contentPlays.containsKey(monthKey)) {
                contentPlays.put(monthKey, (double) stat.getContentPlays());
                bandwidthGb.put(monthKey, stat.getBandwidthGb());
                storageGb.put(monthKey, stat.getBandwidthGb());
            }
        });

        data.setContentPlays(contentPlays);
        data.setBandwidthGb(bandwidthGb);
        data.setStorageGb(storageGb);
        return data;
    }

    public MonthlyActivityData getMonthlyContentActivityForAllUsers(LocalDateTime from, LocalDateTime to) {
        MonthlyActivityData data = new MonthlyActivityData();
        Map<String, Double> contentPlays = new LinkedHashMap<>();
        Map<String, Double> storageGb = new LinkedHashMap<>();
        Map<String, Double> bandwidthGb = new LinkedHashMap<>();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        LocalDateTime currentMonth = from.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (!currentMonth.isAfter(to)) {
            String monthKey = currentMonth.format(monthFormatter);
            contentPlays.put(monthKey, 0.0);
            storageGb.put(monthKey, 0.0);
            bandwidthGb.put(monthKey, 0.0);
            currentMonth = currentMonth.plusMonths(1);
        }

        contentRepository.getMonthlyStatsForAllUsers(from, to).forEach(stat -> {
            LocalDateTime monthDate = LocalDateTime.of(stat.getYear(), stat.getMonth(), 1, 0, 0);
            String monthKey = monthDate.format(monthFormatter);

            if (contentPlays.containsKey(monthKey)) {
                contentPlays.put(monthKey, (double) stat.getContentPlays());
                bandwidthGb.put(monthKey, stat.getBandwidthGb());
                storageGb.put(monthKey, stat.getBandwidthGb());
            }
        });

        data.setContentPlays(contentPlays);
        data.setBandwidthGb(bandwidthGb);
        data.setStorageGb(storageGb);
        return data;
    }
}