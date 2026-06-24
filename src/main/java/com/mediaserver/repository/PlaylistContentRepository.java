// PlaylistContentRepository.java
package com.mediaserver.repository;

import com.mediaserver.model.PlaylistContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaylistContentRepository extends MongoRepository<PlaylistContent, String> {
    List<PlaylistContent> findByPlaylistIdOrderByDisplayOrder(String playlistId);
    void deleteByPlaylistIdAndContentId(String playlistId, String contentId);
    // add by 23/6/25
    boolean existsByPlaylistIdAndContentId(String playlistId, String contentId);
    List<PlaylistContent> findByContentId(String contentId);
    //add by priya 11/6
    List<PlaylistContent> findByPlaylistIdAndDisplayOrderBetween(String playlistId, Integer minOrder, Integer maxOrder);
    List<PlaylistContent> findByPlaylistIdAndDisplayOrderLessThanEqual(String playlistId, Integer minOrder);
    List<PlaylistContent> findByPlaylistIdAndDisplayOrderGreaterThanEqual(String playlistId, Integer maxOrder);
    //void deleteByPlaylistIdAndContentId(String playlistId, String contentId);
    
    /* --- NEW --- */
    void deleteByPlaylistId(String playlistId);
    void deleteByContentId(String contentId);
}