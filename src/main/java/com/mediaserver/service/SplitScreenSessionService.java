package com.mediaserver.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediaserver.model.SplitScreenSession;
import com.mediaserver.repository.SplitScreenSessionRepository;

@Service
public class SplitScreenSessionService {

    @Autowired
    private SplitScreenSessionRepository splitScreenSessionRepository;

    public SplitScreenSession saveSession(String uniqueId, String layoutMode, String contentId1, String contentId2) {
        SplitScreenSession session = new SplitScreenSession(uniqueId, layoutMode, contentId1, contentId2);
        return splitScreenSessionRepository.save(session);
    }

    public Optional<SplitScreenSession> getSessionByUniqueId(String uniqueId) {
        return splitScreenSessionRepository.findByUniqueId(uniqueId);
    }
}
