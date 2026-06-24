package com.mediaserver.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mediaserver.model.SplitScreenSession;

@Repository
public interface SplitScreenSessionRepository extends MongoRepository<SplitScreenSession, String> {
    Optional<SplitScreenSession> findByUniqueId(String uniqueId);
}
