package com.mediaserver.repository;

import com.mediaserver.model.Video;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoRepository extends MongoRepository<Video, String> {
    
}
