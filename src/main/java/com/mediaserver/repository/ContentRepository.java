package com.mediaserver.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mediaserver.dto.MonthlyAggregate;
import com.mediaserver.dto.WeeklyAggregate;
import com.mediaserver.model.Content;

@Repository
public interface ContentRepository extends MongoRepository<Content, String> {
    List<Content> findByUserId(String userId);
    Optional<Content> findById(String id);
    List<Content> findByUserIdOrderByUploadDateDesc(String userId);
    // 16/6/24
    // List<Content> findByTypeAndUserId(String type, String userId);
    // List<Content> findByFileSizeBetween(Long minSize, Long maxSize);
    // List<Content> findByTypeAndFileSizeBetween(String type, Long minSize, Long maxSize);
    // // Add the missing methods
    // List<Content> findByTypeAndUserIdAndFileSizeBetween(String type, String userId, Long minSize, Long maxSize);
    // List<Content> findByType(String type);

    List<Content> findByTitleContainingIgnoreCase(String title);
    List<Content> findByUploadDateBetween(LocalDateTime fromDate, LocalDateTime toDate);
    List<Content> findByFileType(String fileType);
    List<Content> findByFileSizeBetween(Long minSize, Long maxSize);
    List<Content> findByThumbnail(String thumbnail);

    // already exists – just add the following methods
    List<Content> findByTagsContainingIgnoreCase(String tag);
    List<Content> findByUserIdAndTagsContainingIgnoreCase(String userId, String tag);
    Optional<Content> findByUniqueId(String uniqueId);
    
    // New methods to ignore system-generated content
    List<Content> findByUserIdAndSystemGeneratedFalseOrderByUploadDateDesc(String userId);
    List<Content> findByUserIdAndSystemGeneratedFalseAndTagsContainingIgnoreCase(String userId, String tag);
    List<Content> findByUserIdAndSystemGeneratedFalseAndUploadDateBetweenOrderByUploadDateDesc(String userId, LocalDateTime fromDate, LocalDateTime toDate);

    // New method for date range filter by user
    List<Content> findByUserIdAndUploadDateBetweenOrderByUploadDateDesc(String userId, LocalDateTime fromDate, LocalDateTime toDate);

    /* Weekly aggregation pipeline */
    @Aggregation(pipeline = {
        "{ $match: { userId: ?0, uploadDate : { $gte : ?1 } } }",
        "{ $group: { _id : { $dayOfWeek : '$uploadDate' }, contentPlays : { $sum : 1 }, bandwidthGb : { $sum : { $divide : ['$fileSize', 1073741824] } } } }"
    })
    List<WeeklyAggregate> getWeeklyStats(String userId, LocalDateTime since);

    @Aggregation(pipeline = {
        "{ $match: { userId: ?0, uploadDate : { $gte : ?1 } } }",
        "{ $group: { _id : { year: { $year : '$uploadDate' }, month: { $month : '$uploadDate' } }, contentPlays : { $sum : 1 }, bandwidthGb : { $sum : { $divide : ['$fileSize', 1073741824] } } } }",
        "{ $project: { year: '$_id.year', month: '$_id.month', contentPlays: 1, bandwidthGb: 1, _id: 0 } }",
        "{ $sort: { year : 1, month : 1 } }"
    })
    List<MonthlyAggregate> getMonthlyStats(String userId, LocalDateTime since);

    @Aggregation(pipeline = {
    "{ $match: { userId: ?0, uploadDate : { $gte : ?1, $lte : ?2 } } }",
    "{ $group: { _id : { $dayOfWeek : '$uploadDate' }, contentPlays : { $sum : 1 }, bandwidthGb : { $sum : { $divide : ['$fileSize', 1073741824] } } } }"
})
List<WeeklyAggregate> getWeeklyStats(String userId, LocalDateTime from, LocalDateTime to);

    @Aggregation(pipeline = {
    "{ $match: { userId: ?0, uploadDate : { $gte : ?1, $lte : ?2 } } }",
    "{ $group: { _id : { year: { $year : '$uploadDate' }, month: { $month : '$uploadDate' } }, contentPlays : { $sum : 1 }, bandwidthGb : { $sum : { $divide : ['$fileSize', 1073741824] } } } }",
    "{ $project: { year: '$_id.year', month: '$_id.month', contentPlays: 1, bandwidthGb: 1, _id: 0 } }",
    "{ $sort: { year : 1, month : 1 } }"
})
List<MonthlyAggregate> getMonthlyStats(String userId, LocalDateTime from, LocalDateTime to);

@Aggregation(pipeline = {
    "{ $match: { uploadDate : { $gte : ?0, $lte : ?1 } } }",
    "{ $group: { _id : { $dayOfWeek : '$uploadDate' }, contentPlays : { $sum : 1 }, bandwidthGb : { $sum : { $divide : ['$fileSize', 1073741824] } } } }"
})
List<WeeklyAggregate> getWeeklyStatsForAllUsers(LocalDateTime from, LocalDateTime to);

@Aggregation(pipeline = {
    "{ $match: { uploadDate : { $gte : ?0, $lte : ?1 } } }",
    "{ $group: { _id : { year: { $year : '$uploadDate' }, month: { $month : '$uploadDate' } }, contentPlays : { $sum : 1 }, bandwidthGb : { $sum : { $divide : ['$fileSize', 1073741824] } } } }",
    "{ $project: { year: '$_id.year', month: '$_id.month', contentPlays: 1, bandwidthGb: 1, _id: 0 } }",
    "{ $sort: { year : 1, month : 1 } }"
})
List<MonthlyAggregate> getMonthlyStatsForAllUsers(LocalDateTime from, LocalDateTime to);
}