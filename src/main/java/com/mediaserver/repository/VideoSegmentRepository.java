package com.mediaserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mediaserver.model.VideoSegment;
import java.util.List;

@Repository
public interface VideoSegmentRepository extends MongoRepository<VideoSegment, String> {
    List<VideoSegment> findByDeviceGroupId(String deviceGroupId);
    List<VideoSegment> findByPlaylistId(String playlistId);
    List<VideoSegment> findByDeviceId(String deviceId);
}
