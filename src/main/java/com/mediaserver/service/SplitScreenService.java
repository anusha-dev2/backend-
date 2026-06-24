package com.mediaserver.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediaserver.model.Content;
import com.mediaserver.model.SplitScreenSession;
import com.mediaserver.repository.SplitScreenRepository;
import com.mediaserver.repository.SplitScreenRepository.SplitScreenModel;

@Service
public class SplitScreenService {

    @Autowired
    private ContentService contentService;

    @Autowired
    private SplitScreenRepository splitScreenRepository;

    @Autowired
    private SplitScreenSessionService splitScreenSessionService;

    public SplitScreenModel getSplitScreenByUniqueIdOrThrow(String uniqueId) {
        // Case 1: encoded uniqueId -> contentId1|contentId2|layoutMode
        Optional<SplitScreenRepository.SplitScreenData> dataOpt = splitScreenRepository.findByUniqueId(uniqueId);
        if (dataOpt.isPresent()) {
            SplitScreenRepository.SplitScreenData data = dataOpt.get();

            Content content1 = contentService.getContentById(data.getContentId1())
                    .orElseThrow(() -> new IllegalArgumentException("Content not found with id: " + data.getContentId1()));
            Content content2 = contentService.getContentById(data.getContentId2())
                    .orElseThrow(() -> new IllegalArgumentException("Content not found with id: " + data.getContentId2()));

            // layoutMode from encoded uniqueId
            return splitScreenRepository.build(
                    uniqueId,
                    data.getLayoutMode(),
                    content1,
                    content2);
        }

        // Case 2: plain UUID uniqueId
        Optional<SplitScreenSession> sessionOpt = splitScreenSessionService.getSessionByUniqueId(uniqueId);
        if (sessionOpt.isPresent()) {
            SplitScreenSession session = sessionOpt.get();

            Content content1 = contentService.getContentById(session.getContentId1())
                    .orElseThrow(() -> new IllegalArgumentException("Content not found with id: " + session.getContentId1()));
            Content content2 = contentService.getContentById(session.getContentId2())
                    .orElseThrow(() -> new IllegalArgumentException("Content not found with id: " + session.getContentId2()));

            return splitScreenRepository.build(
                    uniqueId,
                    session.getLayoutMode(),
                    content1,
                    content2);
        }

        throw new IllegalArgumentException("SplitScreen not found for uniqueId (expected contentId1|contentId2|layoutMode or UUID): " + uniqueId);
    }


    /**
     * Helper for controller: convert to response payload.
     */
    public Map<String, Object> toResponsePayload(SplitScreenModel model) {
        String normalized = model.getLayoutMode() == null ? null : model.getLayoutMode().trim().toUpperCase();
        if (!"HORIZONTAL".equals(normalized) && !"VERTICAL".equals(normalized)) {
            throw new IllegalArgumentException("layoutMode must be either HORIZONTAL or VERTICAL");
        }

        List<Content> zone1 = model.getZone1();
        List<Content> zone2 = model.getZone2();

        Map<String, Object> zones = new java.util.LinkedHashMap<>();
        if ("HORIZONTAL".equals(normalized)) {
            zones.put("TOP", zone1);
            zones.put("BOTTOM", zone2);
        } else {
            zones.put("LEFT", zone1);
            zones.put("RIGHT", zone2);
        }

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("contentId1", model.getContentId1());
        response.put("contentId2", model.getContentId2());
        response.put("layoutMode", normalized);
        response.put("zones", zones);
        response.put("uniqueId", model.getUniqueId());
        return response;
    }
}

