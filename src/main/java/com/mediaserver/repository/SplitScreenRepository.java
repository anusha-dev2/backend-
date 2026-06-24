package com.mediaserver.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.mediaserver.model.Content;

/**
 * Repository for split-screen by uniqueId.
 *
 * Current implementation is stateless: uniqueId is encoded as
 * contentId1|contentId2|layoutMode.
 *
 * If you later introduce persistent storage, this repository is the place to
 * switch to DB-backed lookup.
 */
@Repository
public class SplitScreenRepository {

    /**
     * Stateless lookup.
     */
    public Optional<SplitScreenData> findByUniqueId(String uniqueId) {
        if (uniqueId == null || uniqueId.isBlank()) {
            return Optional.empty();
        }

        // format: contentId1|contentId2|layoutMode
        String[] parts = uniqueId.split("\\|");
        if (parts.length != 3) {
            // If uniqueId is already a plain UUID (single-part), decoding is not possible in this stateless
            // repository. The service layer will handle any session-backed decoding.
            return Optional.empty();
        }

        String contentId1 = parts[0];
        String contentId2 = parts[1];
        String layoutMode = parts[2];

        return Optional.of(new SplitScreenData(uniqueId, contentId1, contentId2, layoutMode));
    }

    /**
     * DTO representing decoded uniqueId.
     */
    public static class SplitScreenData {
        private final String uniqueId;
        private final String contentId1;
        private final String contentId2;
        private final String layoutMode;

        public SplitScreenData(String uniqueId, String contentId1, String contentId2, String layoutMode) {
            this.uniqueId = uniqueId;
            this.contentId1 = contentId1;
            this.contentId2 = contentId2;
            this.layoutMode = layoutMode;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public String getContentId1() {
            return contentId1;
        }

        public String getContentId2() {
            return contentId2;
        }

        public String getLayoutMode() {
            return layoutMode;
        }
    }

    /**
     * Build split-screen zones from two Content objects.
     */
    public SplitScreenModel build(String uniqueId, String layoutMode, Content content1, Content content2) {
        java.util.List<Content> zone1 = new java.util.ArrayList<>();
        zone1.add(content1);

        java.util.List<Content> zone2 = new java.util.ArrayList<>();
        zone2.add(content2);

        return new SplitScreenModel(uniqueId, layoutMode, content1.getId(), content2.getId(), zone1, zone2);
    }

    /**
     * Domain model returned by repository/service.
     */
    public static class SplitScreenModel {
        private final String uniqueId;
        private final String layoutMode;
        private final String contentId1;
        private final String contentId2;
        private final java.util.List<Content> zone1;
        private final java.util.List<Content> zone2;

        public SplitScreenModel(String uniqueId, String layoutMode, String contentId1, String contentId2,
                java.util.List<Content> zone1, java.util.List<Content> zone2) {
            this.uniqueId = uniqueId;
            this.layoutMode = layoutMode;
            this.contentId1 = contentId1;
            this.contentId2 = contentId2;
            this.zone1 = zone1;
            this.zone2 = zone2;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public String getLayoutMode() {
            return layoutMode;
        }

        public String getContentId1() {
            return contentId1;
        }

        public String getContentId2() {
            return contentId2;
        }

        public java.util.List<Content> getZone1() {
            return zone1;
        }

        public java.util.List<Content> getZone2() {
            return zone2;
        }
    }
}

