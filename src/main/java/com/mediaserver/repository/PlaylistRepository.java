// PlaylistRepository.java
package com.mediaserver.repository;

import com.mediaserver.dto.MonthlyAggregate;
import com.mediaserver.dto.WeeklyAggregate;
import com.mediaserver.model.Playlist;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface PlaylistRepository extends MongoRepository<Playlist, String> {
    List<Playlist> findByUserId(String userId);
    Optional<Playlist> findById(String PlaylistId);
    // add by priya 11/6
    void deleteByGroupId(String groupId);
    List<Playlist> findByGroupId(String groupId);

    //added 19.6.24

    List<Playlist> findByNameIgnoreCase(String name);
    List<Playlist> findByViewCount(Integer viewCount);
    List<Playlist> findByIsActive(Boolean isActive);
    List<Playlist> findByVisibility(String visibility);
    List<Playlist> findByNameIgnoreCaseAndIsActive(String name, Boolean isActive);
    List<Playlist> findByNameIgnoreCaseAndVisibility(String name, String visibility);
    List<Playlist> findByIsActiveAndVisibility(Boolean isActive, String visibility);
    List<Playlist> findByNameIgnoreCaseAndViewCountAndIsActiveAndVisibility(String name, Integer viewCount, Boolean isActive, String visibility);

    Optional<Playlist> findByName(String name);

    Optional<Playlist> findByNameAndUserId(String name, String userId);
    Optional<Playlist> findByNameIgnoreCaseAndUserId(String name, String userId);
    List<Playlist> findByUserIdAndNameIgnoreCase(String userId, String name);


    //set scheduled playlist

    // Add these methods to your existing PlaylistRepository.java

// Find playlists with schedule info
@Query("{\'scheduleInfo.scheduled\': true}")
List<Playlist> findScheduledPlaylists();

// Find playlists with schedule info for a specific user
@Query("{\'userId\': ?0, \'scheduleInfo.scheduled\': true}")
List<Playlist> findScheduledPlaylistsByUserId(String userId);

// Find playlists scheduled for specific devices
@Query("{\'scheduleInfo.scheduled\': true, \'scheduleInfo.scheduledDevices\': ?0}")
List<Playlist> findScheduledPlaylistsByDevice(String deviceId);

// Find playlists with schedule info enabled
@Query("{\'scheduleInfo.enabled\': true}")
List<Playlist> findEnabledScheduledPlaylists();

// Find playlists with next scheduled time before a specific time
@Query("{\'scheduleInfo.nextScheduledTime\': {\'$lte\': ?0}}")
List<Playlist> findPlaylistsWithNextScheduledTimeBefore(LocalDateTime time);

// Find playlists with next scheduled time between two times
@Query("{\'scheduleInfo.nextScheduledTime\': {\'$gte\': ?0, \'$lte\': ?1}}")
List<Playlist> findPlaylistsWithNextScheduledTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

// Find playlists with recurring schedules
@Query("{\'scheduleInfo.recurring\': {\'$ne\': null, \'$ne\': \'\'}}")
List<Playlist> findRecurringPlaylists();

// Find playlists scheduled for specific days of week
@Query("{\'scheduleInfo.selectedDaysOfWeek\': {\'$in\': ?0}}")
List<Playlist> findPlaylistsByDaysOfWeek(List<String> daysOfWeek);

// Find playlists scheduled for specific dates of month
@Query("{\'scheduleInfo.selectedDatesOfMonth\': {\'$in\': ?0}}")
List<Playlist> findPlaylistsByDatesOfMonth(List<Integer> datesOfMonth);

// Find playlists with specific time enabled
@Query("{\'scheduleInfo.specificTimeEnabled\': true}")
List<Playlist> findPlaylistsWithSpecificTime();

// Find playlists with week cycle enabled
@Query("{\'scheduleInfo.weekCycleEnabled\': true}")
List<Playlist> findPlaylistsWithWeekCycle();

// Find playlists scheduled within a date range
@Query("{\'scheduleInfo.startDate\': {\'$lte\': ?1}, \'scheduleInfo.endDate\': {\'$gte\': ?0}}")
List<Playlist> findPlaylistsScheduledInDateRange(LocalDateTime startDate, LocalDateTime endDate);

// Find playlists by user and schedule status
List<Playlist> findByUserIdAndScheduleInfo_Scheduled(String userId, boolean scheduled);

// Find playlists that were last executed before a specific time
@Query("{\'scheduleInfo.lastExecuted\': {\'$lt\': ?0}}")
List<Playlist> findPlaylistsLastExecutedBefore(LocalDateTime time);

// Find playlists with schedule info not null
@Query("{\'scheduleInfo\': {\'$ne\': null}}")
List<Playlist> findPlaylistsWithScheduleInfo();

// Complex query for active schedules at current time
@Query("{\'scheduleInfo.scheduled\': true, \'scheduleInfo.enabled\': true, " +
       "\'scheduleInfo.startDate\': {\'$lte\': ?0}, " +
       "\'scheduleInfo.endDate\': {\'$gte\': ?0}}")
List<Playlist> findActiveScheduledPlaylistsAtTime(LocalDateTime currentTime);

List<Playlist> findByUserIdAndScheduleInfo_NextScheduledTimeBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);





    @Aggregation(pipeline = {
        "{ $match: { createdDate : { $gte : ?0 } } }",
        "{ $group: { _id : { $dayOfWeek : '$createdDate' }, contentPlays : { $sum : 1 } } }"
    })
    List<WeeklyAggregate> getWeeklyStats(LocalDateTime since);

    @Aggregation(pipeline = {
        "{ $match: { createdDate : { $gte : ?0 } } }",
        "{ $group: { _id : { $month : '$createdDate' }, contentPlays : { $sum : 1 } } }"
    })
    List<MonthlyAggregate> getMonthlyStats(LocalDateTime since);

}
