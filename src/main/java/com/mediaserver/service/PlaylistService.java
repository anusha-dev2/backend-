// // PlaylistService.java
// package com.mediaserver.service;

// import com.mediaserver.model.Content;
// import com.mediaserver.model.Device;
// import com.mediaserver.model.PlayListItems;
// import com.mediaserver.model.Playlist;
// import com.mediaserver.model.PlaylistContent;
// import com.mediaserver.model.Subscription;
// import com.mediaserver.model.SubscriptionPlan;
// import com.mediaserver.repository.PlaylistContentRepository;
// import com.mediaserver.repository.PlaylistRepository;
// import com.mediaserver.repository.ContentRepository;
// import com.mediaserver.repository.DeviceRepository;
// import com.mediaserver.repository.SubscriptionRepository;
// import com.mediaserver.repository.SubscriptionPlanRepository;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.mongodb.core.MongoTemplate;
// // add by priya 11/6
// import org.springframework.data.mongodb.core.query.Criteria;
// import org.springframework.data.mongodb.core.query.Query;
// import org.springframework.data.domain.Sort;
// //
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.time.DayOfWeek;
// import java.time.temporal.WeekFields;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Locale;
// import java.util.Optional;
// import java.util.Set;
// import java.util.stream.Collectors;
// import java.util.Map;
// import java.util.*;

// import com.mediaserver.repository.ContentRepository;

// @Service
// public class PlaylistService {

//     @Autowired
//     private PlaylistRepository playlistRepository;

//     @Autowired
//     private PlaylistContentRepository playlistContentRepository;

//     @Autowired
//     private ContentRepository contentRepository;

//     @Autowired
//     private MongoTemplate mongoTemplate;

//     // Autowire DeviceRepository if needed for symmetry
//     @Autowired
//     private DeviceRepository deviceRepository;

//     @Autowired
//     private SubscriptionRepository subscriptionRepository;

//     @Autowired
//     private SubscriptionPlanRepository subscriptionPlanRepository;

//     @Autowired
//     private SubscriptionLimitService subscriptionLimitService;



//     public List<Playlist> getAllPlaylists() {
//         return playlistRepository.findAll();
//     }

//     public List<Playlist> getPlaylistsByUserId(String userId) {
//         return playlistRepository.findByUserId(userId);
//     }

//     public Optional<Playlist> getPlaylistById(String id) {
//         return playlistRepository.findById(id);
//     }

//     // public Playlist createPlaylist(Playlist playlist) {
//     //     return playlistRepository.save(playlist);
//     // }

//     //done by priyangka
//     // Updated createPlaylist method with duplicate name validation and plan limits
//     public Playlist createPlaylist(Playlist playlist) {
//         // Check plan limits for maxPlaylists using centralized service
//         subscriptionLimitService.checkPlaylistLimit(playlist.getUserId());

//         // Check if playlist name already exists for the user
//         if (isPlaylistNameExists(playlist.getName(), playlist.getUserId(), null)) {
//             throw new IllegalArgumentException("Playlist with name \'" + playlist.getName() + "\' already exists for this user");
//         }

//         // Set creation timestamp
//         playlist.setCreatedDate(LocalDateTime.now());
//         playlist.setLastModifiedDate(LocalDateTime.now());


//         return playlistRepository.save(playlist);
//     }

//     // public Playlist updatePlaylist(Playlist playlist) {
//     //     return playlistRepository.save(playlist);
//     // }
    
//     // @Transactional
//     // public Playlist updatePlaylist(String id, Playlist updatedPlaylist) {
//     //     Optional<Playlist> existingPlaylistOpt = playlistRepository.findById(id);
        
//     //     if (!existingPlaylistOpt.isPresent()) {
//     //         throw new IllegalArgumentException("Playlist with ID " + id + " not found");
//     //     }
        
//     //     Playlist existingPlaylist = existingPlaylistOpt.get();
        
//     //     // Check for duplicate name if name is being changed
//     //     if (!existingPlaylist.getName().equals(updatedPlaylist.getName())) {
//     //         if (isPlaylistNameExists(updatedPlaylist.getName(), existingPlaylist.getUserId(), id)) {
//     //             throw new IllegalArgumentException("Playlist with name \'" + updatedPlaylist.getName() + "\' already exists for this user");
//     //         }
//     //     }
        
//     //     // Update only the fields that should be updatable
//     //     existingPlaylist.setName(updatedPlaylist.getName());
//     //     existingPlaylist.setDescription(updatedPlaylist.getDescription());
//     //     existingPlaylist.setThumbnailUrl(updatedPlaylist.getThumbnailUrl());
//     //     existingPlaylist.setVisibility(updatedPlaylist.getVisibility());
//     //     existingPlaylist.setCategory(updatedPlaylist.getCategory());
//     //     existingPlaylist.setTags(updatedPlaylist.getTags());
//     //     existingPlaylist.setIsActive(updatedPlaylist.getIsActive());
//     //     existingPlaylist.setDuration(updatedPlaylist.getDuration());
//     //     existingPlaylist.setGroupId(updatedPlaylist.getGroupId());
//     //     existingPlaylist.setDevices(updatedPlaylist.getDevices());
//     //     existingPlaylist.setItems(updatedPlaylist.getItems());
//     //     existingPlaylist.setDurationEnable(updatedPlaylist.getDurationEnable());
//     //     existingPlaylist.setTimeEnable(updatedPlaylist.getTimeEnable());
        
//     //     // Update settings if provided
//     //     if (updatedPlaylist.getSetting() != null) {
//     //         existingPlaylist.setSetting(updatedPlaylist.getSetting());
//     //     }
        
//     //     // Update last modified timestamp
//     //     existingPlaylist.setLastModifiedDate(LocalDateTime.now());
        
//     //     return playlistRepository.save(existingPlaylist);
//     // }


//     // 29.7.25
// /////////////////////////18/aug/25
// // @Transactional
// // public Playlist updatePlaylist(String id, Playlist updatedPlaylist) {
// //     Optional<Playlist> existingPlaylistOpt = playlistRepository.findById(id);
// //     if (!existingPlaylistOpt.isPresent()) {
// //         throw new IllegalArgumentException("Playlist with ID " + id + " not found");
// //     }

// //     Playlist existingPlaylist = existingPlaylistOpt.get();

// //     /* 1. duplicate-name guard */
// //     if (!existingPlaylist.getName().equals(updatedPlaylist.getName())) {
// //         if (isPlaylistNameExists(updatedPlaylist.getName(),
// //                                  existingPlaylist.getUserId(), id)) {
// //             throw new IllegalArgumentException(
// //                     "Playlist with name '" + updatedPlaylist.getName() +
// //                     "' already exists for this user");
// //         }
// //     }

// //     /* 2. copy simple fields */
// //     existingPlaylist.setName(updatedPlaylist.getName());
// //     existingPlaylist.setDescription(updatedPlaylist.getDescription());
// //     existingPlaylist.setThumbnailUrl(updatedPlaylist.getThumbnailUrl());
// //     existingPlaylist.setVisibility(updatedPlaylist.getVisibility());
// //     existingPlaylist.setCategory(updatedPlaylist.getCategory());
// //     existingPlaylist.setTags(updatedPlaylist.getTags());
// //     existingPlaylist.setIsActive(updatedPlaylist.getIsActive());
// //     existingPlaylist.setDuration(updatedPlaylist.getDuration());
// //     existingPlaylist.setGroupId(updatedPlaylist.getGroupId());
// //     existingPlaylist.setDevices(updatedPlaylist.getDevices());
// //     existingPlaylist.setDurationEnable(updatedPlaylist.getDurationEnable());
// //     existingPlaylist.setTimeEnable(updatedPlaylist.getTimeEnable());

// //     /* 3. settings */
// //     if (updatedPlaylist.getSetting() != null) {
// //         existingPlaylist.setSetting(updatedPlaylist.getSetting());
// //     }

// //     /* 4. APPEND NEW ITEMS & RE-INDEX */
// //     List<PlayListItems> items = existingPlaylist.getItems();
// //     if (items == null) items = new ArrayList<>();

// //     Set<String> existingIds = items.stream()
// //                                    .map(PlayListItems::getMediaId)
// //                                    .collect(Collectors.toSet());

// //     if (updatedPlaylist.getItems() != null) {
// //         for (PlayListItems in : updatedPlaylist.getItems()) {
// //             if (in.getMediaId() != null && !existingIds.contains(in.getMediaId())) {
// //                 in.setDisplayOrder(items.size() + 1);
// //                 items.add(in);
// //                 existingIds.add(in.getMediaId());
// //             }
// //         }
// //     }

// //     // 1,2,3… for the whole list
// //     for (int i = 0; i < items.size(); i++) items.get(i).setDisplayOrder(i + 1);
// //     existingPlaylist.setItems(items);

// //     /* 5. timestamp & save */
// //     existingPlaylist.setLastModifiedDate(LocalDateTime.now());
// //     Playlist saved = playlistRepository.save(existingPlaylist);

// //     /* 6. sync PlaylistContent – delete old, insert current */
// //     playlistContentRepository.deleteByPlaylistId(saved.getId());
// //     for (PlayListItems item : items) {
// //         PlaylistContent pc = new PlaylistContent();
// //         pc.setPlaylistId(saved.getId());
// //         pc.setContentId(item.getMediaId());
// //         pc.setDisplayOrder(item.getDisplayOrder());
// //         playlistContentRepository.save(pc);
// //     }

// //     return saved;
// // }
// ////////////////////
// /// 
//     @Transactional
// public Playlist updatePlaylist(String id, Playlist updatedPlaylist) {
//     Optional<Playlist> existingPlaylistOpt = playlistRepository.findById(id);
//     if (!existingPlaylistOpt.isPresent()) {
//         throw new IllegalArgumentException("Playlist with ID " + id + " not found");
//     }

//     Playlist existingPlaylist = existingPlaylistOpt.get();

//     /* 1. duplicate-name guard */
//     if (!existingPlaylist.getName().equals(updatedPlaylist.getName())) {
//         if (isPlaylistNameExists(updatedPlaylist.getName(),
//                                 existingPlaylist.getUserId(), id)) {
//             throw new IllegalArgumentException(
//                     "Playlist with name '" + updatedPlaylist.getName() +
//                     "' already exists for this user");
//         }
//     }

//     /* 2. copy simple fields */
//     existingPlaylist.setName(updatedPlaylist.getName());
//     existingPlaylist.setDescription(updatedPlaylist.getDescription());
//     existingPlaylist.setThumbnailUrl(updatedPlaylist.getThumbnailUrl());
//     existingPlaylist.setVisibility(updatedPlaylist.getVisibility());
//     existingPlaylist.setCategory(updatedPlaylist.getCategory());
//     existingPlaylist.setTags(updatedPlaylist.getTags());
//     existingPlaylist.setIsActive(updatedPlaylist.getIsActive());
//     existingPlaylist.setDuration(updatedPlaylist.getDuration());
//     existingPlaylist.setGroupId(updatedPlaylist.getGroupId());
//     existingPlaylist.setDevices(updatedPlaylist.getDevices());
//     existingPlaylist.setDurationEnable(updatedPlaylist.getDurationEnable());
//     existingPlaylist.setTimeEnable(updatedPlaylist.getTimeEnable());

//     /* 3. settings */
//     if (updatedPlaylist.getSetting() != null) {
//         existingPlaylist.setSetting(updatedPlaylist.getSetting());
//     }

//     /* 4. Handle items reordering */
//     if (updatedPlaylist.getItems() != null && !updatedPlaylist.getItems().isEmpty()) {
//         // Create a map of mediaId to displayOrder from the updated playlist
//         Map<String, Integer> newOrderMap = updatedPlaylist.getItems().stream()
//             .collect(Collectors.toMap(PlayListItems::getMediaId, PlayListItems::getDisplayOrder));
        
//         // Update the existing items with new display orders
//         List<PlayListItems> existingItems = existingPlaylist.getItems();
//         if (existingItems != null) {
//             for (PlayListItems item : existingItems) {
//                 if (newOrderMap.containsKey(item.getMediaId())) {
//                     item.setDisplayOrder(newOrderMap.get(item.getMediaId()));
//                 }
//             }
            
//             // Sort the items based on the new display order
//             existingItems.sort(Comparator.comparingInt(PlayListItems::getDisplayOrder));
            
//             // Re-index to ensure continuous numbering (1,2,3...)
//             for (int i = 0; i < existingItems.size(); i++) {
//                 existingItems.get(i).setDisplayOrder(i + 1);
//             }
//         }
        
//         // Add any new items that don't exist in the current playlist
//         Set<String> existingMediaIds = existingItems.stream()
//             .map(PlayListItems::getMediaId)
//             .collect(Collectors.toSet());
            
//         for (PlayListItems newItem : updatedPlaylist.getItems()) {
//             if (!existingMediaIds.contains(newItem.getMediaId())) {
//                 // Add new item at the specified position
//                 int insertPosition = Math.min(newItem.getDisplayOrder() - 1, existingItems.size());
//                 existingItems.add(insertPosition, newItem);
//             }
//         }
        
//         // Final re-index after adding new items
//         for (int i = 0; i < existingItems.size(); i++) {
//             existingItems.get(i).setDisplayOrder(i + 1);
//         }
//     }

//     /* 5. timestamp & save */
//     existingPlaylist.setLastModifiedDate(LocalDateTime.now());
//     Playlist saved = playlistRepository.save(existingPlaylist);

//     /* 6. sync PlaylistContent - delete old, insert current */
//     playlistContentRepository.deleteByPlaylistId(saved.getId());
//     for (PlayListItems item : saved.getItems()) {
//         PlaylistContent pc = new PlaylistContent();
//         pc.setPlaylistId(saved.getId());
//         pc.setContentId(item.getMediaId());
//         pc.setDisplayOrder(item.getDisplayOrder());
//         playlistContentRepository.save(pc);
//     }

//     return saved;
// }

//     // @Transactional
//     // public Playlist schedulePlaylist(String userId, String playlistId, Playlist.PlaylistScheduleInfo scheduleInfo) {
//     //     Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        
//     //     if (!playlistOpt.isPresent()) {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//     //     }
        
//     //     Playlist playlist = playlistOpt.get();
//     //     if (!playlist.getUserId().equals(userId)) {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//     //     }
        
//     //     // Calculate next scheduled time
//     //     LocalDateTime nextScheduledTime = calculateNextScheduledTime(scheduleInfo);
//     //     scheduleInfo.setNextScheduledTime(nextScheduledTime);
//     //     scheduleInfo.setScheduled(true);
        
//     //     playlist.setScheduleInfo(scheduleInfo);
//     //     playlist.setLastModifiedDate(LocalDateTime.now());
        
//     //     return playlistRepository.save(playlist);
//     // }

//     // @Transactional
//     // public Playlist updatePlaylistSchedule(String userId, String playlistId, Playlist.PlaylistScheduleInfo scheduleInfo) {
//     //     Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        
//     //     if (!playlistOpt.isPresent()) {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//     //     }
        
//     //     Playlist playlist = playlistOpt.get();
//     //     if (!playlist.getUserId().equals(userId)) {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//     //     }
        
//     //     // Calculate next scheduled time
//     //     LocalDateTime nextScheduledTime = calculateNextScheduledTime(scheduleInfo);
//     //     scheduleInfo.setNextScheduledTime(nextScheduledTime);
//     //     scheduleInfo.setScheduled(true);

//     //     playlist.setScheduleInfo(scheduleInfo);
//     //     playlist.setLastModifiedDate(LocalDateTime.now());
        
//     //     return playlistRepository.save(playlist);
//     // }

//     // @Transactional
//     // public Playlist removePlaylistSchedule(String userId, String playlistId) {
//     //     Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        
//     //     if (!playlistOpt.isPresent()) {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//     //     }
        
//     //     Playlist playlist = playlistOpt.get();
//     //     if (!playlist.getUserId().equals(userId)) {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//     //     }
//     //     playlist.setScheduleInfo(null);
//     //     playlist.setLastModifiedDate(LocalDateTime.now());
        
//     //     return playlistRepository.save(playlist);
//     // }

//     // public List<Playlist> getScheduledPlaylists(String userId) {
//     //     return playlistRepository.findByUserIdAndScheduleInfo_Scheduled(userId, true);
//     // }

//     // public List<Playlist> getActivePlaylistsForDevice(String userId, String deviceId, LocalDateTime currentTime) {
//     //     List<Playlist> allPlaylists = playlistRepository.findByUserId(userId);
        
//     //     return allPlaylists.stream()
//     //             .filter(playlist -> {
//     //                 if (playlist.getScheduleInfo() == null || !playlist.getScheduleInfo().isScheduled()) {
//     //                     return false;
//     //                 }
//     //                 return playlist.getScheduleInfo().getScheduledDevices() != null &&
//     //                        playlist.getScheduleInfo().getScheduledDevices().contains(deviceId) &&
//     //                        isScheduleActiveAtTime(playlist.getScheduleInfo(), currentTime);
//     //             })
//     //             .collect(Collectors.toList());
//     // }

//     // public List<Playlist> getPlaylistsScheduledForTime(String userId, LocalDateTime targetTime) {
//     //     List<Playlist> allPlaylists = playlistRepository.findByUserIdAndScheduleInfo_Scheduled(userId, true); 
        
//     //     return allPlaylists.stream()
//     //             .filter(playlist -> {
//     //                 if (playlist.getScheduleInfo() == null || !playlist.getScheduleInfo().isScheduled()) {
//     //                     return false;
//     //                 }
//     //                 return isScheduleActiveAtTime(playlist.getScheduleInfo(), targetTime);
//     //             })
//     //             .collect(Collectors.toList());
//     // }

//     // public List<Playlist> getUpcomingScheduledPlaylists(String userId, int hours) {
//     //     LocalDateTime currentTime = LocalDateTime.now();
//     //     LocalDateTime futureTime = currentTime.plusHours(hours);
        
//     //     return playlistRepository.findByUserIdAndScheduleInfo_NextScheduledTimeBetween(userId, currentTime, futureTime);
//     // }

//     // private boolean isPlaylistActiveForDevice(Playlist playlist, String deviceId, LocalDateTime currentTime) {
//     //     if (playlist.getScheduleInfo() == null || !playlist.getScheduleInfo().isScheduled()) {
//     //         return false;
//     //     }
        
//     //     Playlist.PlaylistScheduleInfo schedule = playlist.getScheduleInfo();
        
//     //     // Check if device is in scheduled devices
//     //     if (schedule.getScheduledDevices() != null && 
//     //         !schedule.getScheduledDevices().isEmpty() && 
//     //         !schedule.getScheduledDevices().contains(deviceId)) {
//     //         return false;
//     //     }
        
//     //     return isScheduleActiveAtTime(schedule, currentTime);
//     // }

//     // private boolean isScheduleActiveAtTime(Playlist.PlaylistScheduleInfo schedule, LocalDateTime checkTime) {
//     //     // Check duration
//     //     if (schedule.getStartDate() != null && schedule.getEndDate() != null) {
//     //         if (checkTime.isBefore(schedule.getStartDate()) || checkTime.isAfter(schedule.getEndDate())) {
//     //             return false;
//     //         }
//     //     }

//     //     // Check selected days of week
//     //     if (schedule.getSelectedDaysOfWeek() != null && !schedule.getSelectedDaysOfWeek().isEmpty()) {
//     //         DayOfWeek currentDayOfWeek = checkTime.getDayOfWeek();
//     //         String currentDayAbbr = currentDayOfWeek.name().substring(0, 2).toUpperCase();
//     //         if (!schedule.getSelectedDaysOfWeek().contains(currentDayAbbr)) {
//     //             return false;
//     //         }
//     //     }
        
//     //     // Check selected dates of month
//     //     if (schedule.getSelectedDatesOfMonth() != null && !schedule.getSelectedDatesOfMonth().isEmpty()) {
//     //         int currentDayOfMonth = checkTime.getDayOfMonth();
//     //         if (!schedule.getSelectedDatesOfMonth().contains(currentDayOfMonth)) {
//     //             return false;
//     //         }
//     //     }
        
//     //     // Check specific time
//     //     if (schedule.isSpecificTimeEnabled()) {
//     //         LocalTime currentTime = checkTime.toLocalTime();
//     //         if (schedule.getSpecificStartTime() != null && schedule.getSpecificEndTime() != null) {
//     //             if (currentTime.isBefore(schedule.getSpecificStartTime()) || 
//     //                 currentTime.isAfter(schedule.getSpecificEndTime())) {
//     //                 return false;
//     //             }
//     //         }
//     //     }
        
//     //     // Check week cycle
//     //     if (schedule.isWeekCycleEnabled() && schedule.getWeekCycleType() != null) {
//     //         WeekFields weekFields = WeekFields.of(Locale.getDefault());
//     //         int weekOfYear = checkTime.get(weekFields.weekOfWeekBasedYear());
//     //         int cycleType = schedule.getWeekCycleType();
//     //         if (cycleType > 0 && (weekOfYear % cycleType != 0)) {
//     //             return false;
//     //         }
//     //     }
        
//     //     return true;
//     // }

//     // private LocalDateTime calculateNextScheduledTime(Playlist.PlaylistScheduleInfo scheduleInfo) {
//     //     LocalDateTime now = LocalDateTime.now();
        
//     //     if (scheduleInfo.getScheduledTime() != null) {
//     //         LocalDateTime scheduledTime = scheduleInfo.getScheduledTime();
            
//     //         if (scheduledTime.isAfter(now)) {
//     //             return scheduledTime;
//     //         } else if (scheduleInfo.isRecurring()) {
//     //             return calculateNextRecurringTime(scheduleInfo, now);
//     //         }
//     //     }
        
//     //     return null;
//     // }

//     // private LocalDateTime calculateNextRecurringTime(Playlist.PlaylistScheduleInfo scheduleInfo, LocalDateTime from) {
//     //     String recurring = scheduleInfo.getRecurring();
//     //     LocalDateTime next = from;
        
//     //     switch (recurring.toLowerCase()) {
//     //         case "daily":
//     //             next = from.plusDays(1);
//     //             break;
//     //         case "weekly":
//     //             next = from.plusWeeks(1);
//     //             break;
//     //         case "monthly":
//     //             next = from.plusMonths(1);
//     //             break;
//     //         default:
//     //             // Custom recurring logic based on other schedule parameters
//     //             next = calculateCustomRecurringTime(scheduleInfo, from);
//     //     }
        
//     //     return next;
//     // }

//     // private LocalDateTime calculateCustomRecurringTime(Playlist.PlaylistScheduleInfo scheduleInfo, LocalDateTime from) {
//     //     // Implement custom recurring logic based on selected days, week cycles, etc.
//     //     // This is a simplified version - you can expand based on your requirements
//     //     return from.plusDays(1);
//     // }

//     // // Mark playlist as executed
//     // @Transactional
//     // public void markPlaylistExecuted(String userId, String playlistId) {
//     //     Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        
//     //     if (playlistOpt.isPresent()) {
//     //         Playlist playlist = playlistOpt.get();
//     //         if (!playlist.getUserId().equals(userId)) {
//     //             throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//     //         }
//     //         if (playlist.getScheduleInfo() != null) {
//     //             playlist.getScheduleInfo().setLastExecuted(LocalDateTime.now());
                
//     //             // Calculate next scheduled time if recurring
//     //             if (playlist.getScheduleInfo().isRecurring()) {
//     //                 LocalDateTime nextTime = calculateNextRecurringTime(
//     //                     playlist.getScheduleInfo(), 
//     //                     LocalDateTime.now()
//     //                 );
//     //                 playlist.getScheduleInfo().setNextScheduledTime(nextTime);
//     //             }
                
//     //             playlistRepository.save(playlist);
//     //         }
//     //     } else {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//     //     }
//     // }

//     // public Playlist.PlaylistScheduleInfo getPlaylistScheduleInfo(String userId, String playlistId) {
//     //     Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
//     //     if (playlistOpt.isPresent()) {
//     //         Playlist playlist = playlistOpt.get();
//     //         if (!playlist.getUserId().equals(userId)) {
//     //             throw new IlleggyugalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//     //         }
//     //         return playlist.getScheduleInfo();
//     //     } else {
//     //         throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//     //     }
//     // }

//     // Modified method in PlaylistService.java to prevent creating a schedule if one already exists

// @Transactional
// public Playlist schedulePlaylist(String userId, String playlistId, Playlist.PlaylistScheduleInfo scheduleInfo) {
//     Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
    
//     if (!playlistOpt.isPresent()) {
//         throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//     }
    
//     Playlist playlist = playlistOpt.get();
//     if (!playlist.getUserId().equals(userId)) {
//         throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//     }
    
//     // Check if the playlist is already scheduled to prevent duplicates
//     if (playlist.getScheduleInfo() != null && playlist.getScheduleInfo().isScheduled()) {
//         throw new IllegalArgumentException("Playlist with ID " + playlistId + " is already scheduled. Use UPDATE to modify the existing schedule.");
//     }
    
//     // Calculate next scheduled time
//     LocalDateTime nextScheduledTime = calculateNextScheduledTime(scheduleInfo);
//     scheduleInfo.setNextScheduledTime(nextScheduledTime);
//     scheduleInfo.setScheduled(true);
    
//     playlist.setScheduleInfo(scheduleInfo);
//     playlist.setLastModifiedDate(LocalDateTime.now());
    
//     return playlistRepository.save(playlist);
// }

//     @Transactional
//     public Playlist updatePlaylistSchedule(String userId, String playlistId, Playlist.PlaylistScheduleInfo scheduleInfo) {
//         Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
//         if (!playlistOpt.isPresent()) {
//             throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//         }
//         Playlist playlist = playlistOpt.get();
//         if (!playlist.getUserId().equals(userId)) {
//             throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//         }
//         LocalDateTime nextScheduledTime = calculateNextScheduledTime(scheduleInfo);
//         scheduleInfo.setNextScheduledTime(nextScheduledTime);
//         scheduleInfo.setScheduled(true);
//         playlist.setScheduleInfo(scheduleInfo);
//         playlist.setLastModifiedDate(LocalDateTime.now());
//         return playlistRepository.save(playlist);
//     }

//     @Transactional
//     public Playlist removePlaylistSchedule(String userId, String playlistId) {
//         Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
//         if (!playlistOpt.isPresent()) {
//             throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//         }
//         Playlist playlist = playlistOpt.get();
//         if (!playlist.getUserId().equals(userId)) {
//             throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//         }
//         playlist.setScheduleInfo(null);
//         playlist.setLastModifiedDate(LocalDateTime.now());
//         return playlistRepository.save(playlist);
//     }

//     public List<Playlist> getScheduledPlaylists(String userId) {
//         return playlistRepository.findByUserIdAndScheduleInfo_Scheduled(userId, true);
//     }

//     public List<Playlist> getActivePlaylistsForDevice(String userId, String deviceId, LocalDateTime currentTime) {
//         List<Playlist> allPlaylists = playlistRepository.findByUserId(userId);
//         return allPlaylists.stream()
//                 .filter(playlist -> {
//                     if (playlist.getScheduleInfo() == null || !playlist.getScheduleInfo().isScheduled()) {
//                         return false;
//                     }
//                     return playlist.getScheduleInfo().getScheduledDevices() != null &&
//                            playlist.getScheduleInfo().getScheduledDevices().contains(deviceId) &&
//                            isScheduleActiveAtTime(playlist.getScheduleInfo(), currentTime);
//                 })
//                 .collect(Collectors.toList());
//     }

//     public List<Playlist> getPlaylistsScheduledForTime(String userId, LocalDateTime targetTime) {
//         List<Playlist> allPlaylists = playlistRepository.findByUserIdAndScheduleInfo_Scheduled(userId, true);
//         return allPlaylists.stream()
//                 .filter(playlist -> {
//                     if (playlist.getScheduleInfo() == null || !playlist.getScheduleInfo().isScheduled()) {
//                         return false;
//                     }
//                     return isScheduleActiveAtTime(playlist.getScheduleInfo(), targetTime);
//                 })
//                 .collect(Collectors.toList());
//     }

//     public List<Playlist> getUpcomingScheduledPlaylists(String userId, int hours) {
//         LocalDateTime currentTime = LocalDateTime.now();
//         LocalDateTime futureTime = currentTime.plusHours(hours);
//         return playlistRepository.findByUserIdAndScheduleInfo_NextScheduledTimeBetween(userId, currentTime, futureTime);
//     }

//     @Transactional
//     public void markPlaylistExecuted(String userId, String playlistId) {
//         Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
//         if (playlistOpt.isPresent()) {
//             Playlist playlist = playlistOpt.get();
//             if (!playlist.getUserId().equals(userId)) {
//                 throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//             }
//             if (playlist.getScheduleInfo() != null) {
//                 playlist.getScheduleInfo().setLastExecuted(LocalDateTime.now());
//                 if (playlist.getScheduleInfo().isRecurring()) {
//                     LocalDateTime nextTime = calculateNextRecurringTime(playlist.getScheduleInfo(), LocalDateTime.now());
//                     playlist.getScheduleInfo().setNextScheduledTime(nextTime);
//                 }
//                 playlistRepository.save(playlist);
//             }
//         } else {
//             throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//         }
//     }

//     public Playlist.PlaylistScheduleInfo getPlaylistScheduleInfo(String userId, String playlistId) {
//         Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
//         if (playlistOpt.isPresent()) {
//             Playlist playlist = playlistOpt.get();
//             if (!playlist.getUserId().equals(userId)) {
//                 throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
//             }
//             return playlist.getScheduleInfo();
//         } else {
//             throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
//         }
//     }

//     private boolean isScheduleActiveAtTime(Playlist.PlaylistScheduleInfo schedule, LocalDateTime checkTime) {
//         if (schedule.getStartDate() != null && schedule.getEndDate() != null) {
//             if (checkTime.isBefore(schedule.getStartDate()) || checkTime.isAfter(schedule.getEndDate())) {
//                 return false;
//             }
//         }
//         if (schedule.getSelectedDaysOfWeek() != null && !schedule.getSelectedDaysOfWeek().isEmpty()) {
//             DayOfWeek currentDayOfWeek = checkTime.getDayOfWeek();
//             String currentDayAbbr = currentDayOfWeek.name().substring(0, 2).toUpperCase();
//             if (!schedule.getSelectedDaysOfWeek().contains(currentDayAbbr)) {
//                 return false;
//             }
//         }
//         if (schedule.getSelectedDatesOfMonth() != null && !schedule.getSelectedDatesOfMonth().isEmpty()) {
//             int currentDayOfMonth = checkTime.getDayOfMonth();
//             if (!schedule.getSelectedDatesOfMonth().contains(currentDayOfMonth)) {
//                 return false;
//             }
//         }
//         if (schedule.isSpecificTimeEnabled()) {
//             LocalTime currentTime = checkTime.toLocalTime();
//             if (schedule.getSpecificStartTime() != null && schedule.getSpecificEndTime() != null) {
//                 if (currentTime.isBefore(schedule.getSpecificStartTime()) || 
//                     currentTime.isAfter(schedule.getSpecificEndTime())) {
//                     return false;
//                 }
//             }
//         }
//         if (schedule.isWeekCycleEnabled() && schedule.getWeekCycleType() != null) {
//             WeekFields weekFields = WeekFields.of(Locale.getDefault());
//             int weekOfYear = checkTime.get(weekFields.weekOfWeekBasedYear());
//             int cycleType = schedule.getWeekCycleType();
//             if (cycleType > 0 && (weekOfYear % cycleType != 0)) {
//                 return false;
//             }
//         }
//         return true;
//     }

//     private LocalDateTime calculateNextScheduledTime(Playlist.PlaylistScheduleInfo scheduleInfo) {
//         LocalDateTime now = LocalDateTime.now();
//         if (scheduleInfo.getScheduledTime() != null) {
//             LocalDateTime scheduledTime = scheduleInfo.getScheduledTime();
//             if (scheduledTime.isAfter(now)) {
//                 return scheduledTime;
//             } else if (scheduleInfo.isRecurring()) {
//                 return calculateNextRecurringTime(scheduleInfo, now);
//             }
//         }
//         return now; // Default to now if no valid scheduled time
//     }

//     private LocalDateTime calculateNextRecurringTime(Playlist.PlaylistScheduleInfo scheduleInfo, LocalDateTime from) {
//         String recurring = scheduleInfo.getRecurring();
//         LocalDateTime next = from;
//         if (recurring == null) {
//             return next;
//         }
//         switch (recurring.toLowerCase()) {
//             case "daily":
//                 next = from.plusDays(1);
//                 if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
//                     next = next.with(scheduleInfo.getSpecificStartTime());
//                 }
//                 break;
//             case "weekly":
//                 next = from.plusWeeks(1);
//                 if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
//                     next = next.with(scheduleInfo.getSpecificStartTime());
//                 }
//                 break;
//             case "monthly":
//                 next = from.plusMonths(1);
//                 if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
//                     next = next.with(scheduleInfo.getSpecificStartTime());
//                 }
//                 break;
//             case "custom":
//                 next = calculateCustomRecurringTime(scheduleInfo, from);
//                 break;
//         }
//         return next;
//     }

//     private LocalDateTime calculateCustomRecurringTime(Playlist.PlaylistScheduleInfo scheduleInfo, LocalDateTime from) {
//         LocalDateTime next = from.plusDays(1);
//         if (scheduleInfo.getSelectedDaysOfWeek() != null && !scheduleInfo.getSelectedDaysOfWeek().isEmpty()) {
//             DayOfWeek currentDay = from.getDayOfWeek();
//             int daysToAdd = 1;
//             while (true) {
//                 next = from.plusDays(daysToAdd);
//                 String nextDayAbbr = next.getDayOfWeek().name().substring(0, 2).toUpperCase();
//                 if (scheduleInfo.getSelectedDaysOfWeek().contains(nextDayAbbr)) {
//                     break;
//                 }
//                 daysToAdd++;
//             }
//         }
//         if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
//             next = next.with(scheduleInfo.getSpecificStartTime());
//         }
//         return next;
//     }

//     // Other methods (createPlaylist, updatePlaylist, etc.) remain unchanged


//     // Helper method to check if playlist name exists for a user
//     private boolean isPlaylistNameExists(String name, String userId, String excludePlaylistId) {
//         List<Playlist> existingPlaylists = playlistRepository.findByUserId(userId);
        
//         return existingPlaylists.stream()
//                 .anyMatch(playlist -> 
//                     playlist.getName().equalsIgnoreCase(name) && 
//                     (excludePlaylistId == null || !playlist.getId().equals(excludePlaylistId))
//                 );
//     }

//     // Alternative method using repository query (more efficient for large datasets)
//     public boolean isPlaylistNameExistsForUser(String name, String userId) {
//         Optional<Playlist> existingPlaylist = playlistRepository.findByNameAndUserId(name, userId);
//         return existingPlaylist.isPresent();
//     }


//     public void deletePlaylist(String id) {
//         playlistRepository.deleteById(id);
//     }

//     public List<PlaylistContent> getPlaylistContents(String playlistId) {
//         return playlistContentRepository.findByPlaylistIdOrderByDisplayOrder(playlistId);
//     }

//     @Transactional
//     public PlaylistContent addContentToPlaylist(String playlistId, String contentId, Integer displayOrder) {
//         PlaylistContent playlistContent = new PlaylistContent();
//         playlistContent.setPlaylistId(playlistId);
//         playlistContent.setContentId(contentId);
//         playlistContent.setDisplayOrder(displayOrder);
//         return playlistContentRepository.save(playlistContent);
//     }

//    @Transactional
//     public PlayListItems addContentToPlaylistItems(String playlistId, String contentId, Integer displayOrder) {

//         Integer playlistCount = playlistRepository.findById(playlistId).get().getItems().size() + 1;


//         return playlistRepository.findById(playlistId)
//                 .map(playlist -> contentRepository.findById(contentId)
//                         .map(content -> {
//                             PlayListItems playListItemsData = new PlayListItems();
//                             playListItemsData.setMediaId(contentId);
//                             playListItemsData.setTitle(content.getTitle());
//                             playListItemsData.setFileType(content.getFileType());
//                             playListItemsData.setUrl(content.getUrl());
//                             playListItemsData.setDuration(content.getDuration());
//                             playListItemsData.setThumbnail(content.getThumbnail());
//                             playListItemsData.setDisplayOrder(playlistCount);

//                             // Ensure the playlist has an items array initialized
//                             if (playlist.getItems() == null) {
//                                 playlist.setItems(new ArrayList<>());
//                             }

//                             // Add the new PlayListItems to the existing list
//                             playlist.getItems().add(playListItemsData);

//                             // Save the updated playlist
//                             playlistRepository.save(playlist);

//                             return playListItemsData;
//                         })
//                         .orElseThrow(() -> new IllegalArgumentException("Content not found")))
//                 .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));
//     }


//     // @Transactional
//     // public void removeContentFromPlaylist(String playlistId, String contentId) {
//     //     playlistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);
//     // }
//     //31.7.25
//     @Transactional
// public void removeContentFromPlaylist(String playlistId, String contentId) {
//     // 1. delete the mapping table row
//     playlistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);

//     // 2. remove from the embedded list
//     Playlist playlist = playlistRepository.findById(playlistId)
//             .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

//     if (playlist.getItems() != null) {
//         playlist.getItems().removeIf(item -> contentId.equals(item.getMediaId()));

//         // 3. re-index displayOrder
//         for (int i = 0; i < playlist.getItems().size(); i++) {
//             playlist.getItems().get(i).setDisplayOrder(i + 1);
//         }
//     }

//     playlist.setLastModifiedDate(LocalDateTime.now());
//     playlistRepository.save(playlist);
// }
// // add by priya 11/6

//     public void updatePlaylistGroupId(String playlistId, String groupId) {
//         Optional<Playlist> playlist = playlistRepository.findById(playlistId);
//         if (playlist.isPresent()) {
//             Playlist updatedPlaylist = playlist.get();
//             updatedPlaylist.setGroupId(groupId);
//             updatedPlaylist.setLastModifiedDate(LocalDateTime.now());
//             playlistRepository.save(updatedPlaylist);
//         } else {
//             throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not exist.");
//         }
//     }

//     public void deletePlaylistByGroupId(String groupId) {
//         List<Playlist> playlists = playlistRepository.findByGroupId(groupId);
//         for (Playlist playlist : playlists) {
//             playlist.setGroupId(null);
//             playlist.setLastModifiedDate(LocalDateTime.now());
//             playlistRepository.save(playlist);
//         }
//     }

//     public List<Playlist> findByGroupId(String groupId) {
//         return playlistRepository.findByGroupId(groupId);
//     }

//     //11/6/25

//     public List<Playlist> getPlaylistsByUserIdWithFilters(
//         String userId,
//         String name,
//         String category,
//         String visibility,
//         String groupId,
//         String sortBy,
//         String sortOrder) {
    
//     // Create query criteria
//     Criteria criteria = Criteria.where("userId").is(userId);
    
//     if (name != null && !name.isEmpty()) {
//         criteria.and("name").regex(name, "i"); // case insensitive search
//     }
    
//     if (category != null && !category.isEmpty()) {
//         criteria.and("category").is(category);
//     }
    
//     if (visibility != null && !visibility.isEmpty()) {
//         criteria.and("visibility").is(visibility);
//     }
    
//     if (groupId != null && !groupId.isEmpty()) {
//         criteria.and("groupId").is(groupId);
//     }
    
//     // Create query with criteria
//     Query query = new Query(criteria);
    
//     // Add sorting if specified
//     if (sortBy != null && !sortBy.isEmpty()) {
//         Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
//         query.with(Sort.by(direction, sortBy));
//     }
    
//     return mongoTemplate.find(query, Playlist.class);
// }

//  //11/6/25
//     // In PlaylistService.java add this method
//     public List<PlaylistContent> getPlaylistContentsWithFilters(
//         String playlistId,
//         String contentId,
//         Integer minOrder,
//         Integer maxOrder,
//         String sortBy,
//         String sortOrder) {
    
//     Criteria criteria = Criteria.where("playlistId").is(playlistId);
    
//     if (contentId != null && !contentId.isEmpty()) {
//         criteria.and("contentId").is(contentId);
//     }
    
//     if (minOrder != null && maxOrder != null) {
//         criteria.and("displayOrder").gte(minOrder).lte(maxOrder);
//     } else if (minOrder != null) {
//         criteria.and("displayOrder").gte(minOrder);
//     } else if (maxOrder != null) {
//         criteria.and("displayOrder").lte(maxOrder);
//     }
    
//     Query query = new Query(criteria);
    
//     // Add sorting if specified
//     if (sortBy != null && !sortBy.isEmpty()) {
//         Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
//         query.with(Sort.by(direction, sortBy));
//     }
    
//     return mongoTemplate.find(query, PlaylistContent.class);
// }

// // added
// // public List<Playlist> filterByName(String name) {
// //         Criteria criteria = Criteria.where("name").regex(".*" + name + ".*", "i");
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

// //     public List<Playlist> filterByViewCount(Integer viewCount) {
// //         Criteria criteria = Criteria.where("viewCount").is(viewCount);
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

// //     public List<Playlist> filterByIsActive(Boolean isActive) {
// //         Criteria criteria = Criteria.where("isActive").is(isActive);
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

// //     public List<Playlist> filterByVisibility(String visibility) {
// //         Criteria criteria = Criteria.where("visibility").is(visibility);
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

// //     public List<Playlist> filterByNameAndIsActive(String name, Boolean isActive) {
// //         Criteria criteria = Criteria.where("name").regex(".*" + name + ".*", "i").and("isActive").is(isActive);
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

// //     public List<Playlist> filterByNameAndVisibility(String name, String visibility) {
// //         Criteria criteria = Criteria.where("name").regex(".*" + name + ".*", "i").and("visibility").is(visibility);
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

// //     public List<Playlist> filterByIsActiveAndVisibility(Boolean isActive, String visibility) {
// //         Criteria criteria = Criteria.where("isActive").is(isActive).and("visibility").is(visibility);
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

// //     public List<Playlist> filterByNameViewCountIsActiveVisibility(String name, Integer viewCount, Boolean isActive, String visibility) {
// //         Criteria criteria = Criteria.where("name").regex(".*" + name + ".*", "i")
// //                                     .and("viewCount").is(viewCount)
// //                                     .and("isActive").is(isActive)
// //                                     .and("visibility").is(visibility);
// //         Query query = new Query(criteria);
// //         return mongoTemplate.find(query, Playlist.class);
// //     }

//     // added the url 1 filter for all playlist

//     // public List<Playlist> filterPlaylistByValue(String value) {
//     //     String lowerCaseValue = value != null ? value.toLowerCase() : null;
//     //     List<Playlist> allPlaylist = playlistRepository.findAll();

//     //     if (lowerCaseValue != null) {
//     //         return allPlaylist.stream()
//     //                 .filter(device -> 
//     //                     (Playlist.getname() != null && Playlist.getname().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (Playlist.getvisibility() != null && Playlist.getvisibility().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (Playlist.getisActive() != null && Playlist.getisActive().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (Playlist.getcategory() != null && Playlist.getcategory().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (Playlist.getviewCount() != null && Playlist.getviewCount().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (Playlist.gettags() != null && Playlist.gettags().toLowerCase().startsWith(lowerCaseValue))
                        
//     //                    // (Playlist.getDeviceType() != null && device.getDeviceType().toLowerCase().startsWith(lowerCaseValue)) ||
                        
//     //                 )
//     //                 .collect(Collectors.toList());
//     //     } else {
//     //         return allPlaylist;
//     //     }
//     // }


//     // PlaylistService.java

// // ... existing imports and class definition ...

//     // public List<Playlist> filterPlaylistByValue(String value) {
//     //     String lowerCaseValue = value != null ? value.toLowerCase() : null;
//     //     List<Playlist> allPlaylists = playlistRepository.findAll();

//     //     if (lowerCaseValue != null) {
//     //         return allPlaylists.stream()
//     //                 .filter(playlist -> 
//     //                     (playlist.getName() != null && playlist.getName().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (playlist.getDescription() != null && playlist.getDescription().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (playlist.getCategory() != null && playlist.getCategory().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (playlist.getVisibility() != null && playlist.getVisibility().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     //(playlist.getTags() != null && java.util.Arrays.stream(playlist.getTags()).anyMatch(tag -> tag.toLowerCase().startsWith(lowerCaseValue))) ||
//     //                     (playlist.getUserId() != null && playlist.getUserId().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (playlist.getGroupId() != null && playlist.getGroupId().toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (playlist.getIsActive() != null && String.valueOf(playlist.getIsActive()).toLowerCase().startsWith(lowerCaseValue)) ||
//     //                     (playlist.getViewCount() != null && String.valueOf(playlist.getViewCount()).toLowerCase().startsWith(lowerCaseValue))
//     //                 )
//     //                 .collect(Collectors.toList());
//     //     } else {
//     //         return allPlaylists;
//     //     }
//     // }
//     // Service Method
//     public List<Playlist> filterPlaylistByValue(String userId, String value) {
//     String lowerCaseValue = value != null ? value.toLowerCase() : null;
//     List<Playlist> allPlaylists = playlistRepository.findAll();

//     if (lowerCaseValue != null && !lowerCaseValue.isEmpty()) {
//         return allPlaylists.stream()
//                 .filter(playlist -> 
//                     playlist.getUserId() != null && playlist.getUserId().equals(userId) && (
//                         (playlist.getName() != null && playlist.getName().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (playlist.getDescription() != null && playlist.getDescription().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (playlist.getCategory() != null && playlist.getCategory().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (playlist.getVisibility() != null && playlist.getVisibility().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (playlist.getGroupId() != null && playlist.getGroupId().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (playlist.getIsActive() != null && String.valueOf(playlist.getIsActive()).toLowerCase().startsWith(lowerCaseValue)) ||
//                         (playlist.getViewCount() != null && String.valueOf(playlist.getViewCount()).toLowerCase().startsWith(lowerCaseValue))
//                     )
//                 )
//                 .collect(Collectors.toList());
//     } else {
//         return allPlaylists.stream()
//                 .filter(playlist -> playlist.getUserId() != null && playlist.getUserId().equals(userId))
//                 .collect(Collectors.toList());
//     }
// }


// // ... rest of the class ...

// // public void schedulePlaylist(String playlistId, String deviceId, LocalDateTime scheduledTime, String recurring) {
// //         PlaylistSchedule schedule = new PlaylistSchedule(playlistId, deviceId, scheduledTime, recurring);
// //         scheduleRepository.save(schedule);
// //     }

// //     public Playlist getActivePlaylistForDevice(String deviceId, LocalDateTime currentTime) {
// //         List<PlaylistSchedule> schedules = scheduleRepository
// //                 .findByDeviceIdAndScheduledTimeLessThanEqualOrderByScheduledTimeDesc(deviceId, currentTime);

// //         if (!schedules.isEmpty()) {
// //             String playlistId = schedules.get(0).getPlaylistId();
// //             return playlistRepository.findById(playlistId).orElse(null);
// //         }
// //         return null;
// //     }

// //     public boolean setScheduledTime(String userId, String playlistId, String scheduledTime) {
// //         Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);
// //         if (playlistOptional.isPresent()) {
// //             Playlist playlist = playlistOptional.get();
// //                 if (playlist.getId().equals(userId)) {
// //                 playlist.setScheduledTime(LocalDateTime.parse(scheduledTime));
// //                 playlistRepository.save(playlist);
// //                 return true;
// //             }
// //         }
// //         return false;
// //     }

// }

package com.mediaserver.service;

import com.mediaserver.model.Content;
import com.mediaserver.model.Device;
import com.mediaserver.model.PlayListItems;
import com.mediaserver.model.Playlist;
import com.mediaserver.model.PlaylistContent;
import com.mediaserver.model.Subscription;
import com.mediaserver.model.SubscriptionPlan;
import com.mediaserver.repository.PlaylistContentRepository;
import com.mediaserver.repository.PlaylistRepository;
import com.mediaserver.repository.ContentRepository;
import com.mediaserver.repository.DeviceRepository;
import com.mediaserver.repository.SubscriptionRepository;
import com.mediaserver.repository.SubscriptionPlanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.*;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistContentRepository playlistContentRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionLimitService subscriptionLimitService;



    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    public List<Playlist> getPlaylistsByUserId(String userId) {
        return playlistRepository.findByUserId(userId);
    }

    public Optional<Playlist> getPlaylistById(String id) {
        return playlistRepository.findById(id);
    }

    /**
     * ✅ MAIN CREATE PLAYLIST METHOD WITH SUBSCRIPTION LIMIT CHECK
     */
    @Transactional
    public Playlist createPlaylist(Playlist playlist) {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("🔍 [createPlaylist] Starting playlist creation");
        System.out.println("   User ID: " + playlist.getUserId());
        System.out.println("   Name: " + playlist.getName());
        System.out.println("════════════════════════════════════════");
        
        // ✅ CHECK SUBSCRIPTION LIMITS FIRST
        try {
            subscriptionLimitService.checkPlaylistLimit(playlist.getUserId());
            System.out.println("✅ Subscription limit check PASSED");
        } catch (SubscriptionLimitService.SubscriptionLimitExceededException e) {
            System.out.println("❌ Subscription limit check FAILED");
            System.out.println("   Error: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }

        // Check if playlist name already exists for the user
        if (isPlaylistNameExistsForUser(playlist.getName(), playlist.getUserId())) {
            String error = "Playlist with name '" + playlist.getName() + "' already exists for this user";
            System.out.println("❌ " + error);
            throw new IllegalArgumentException(error);
        }

        System.out.println("✅ Playlist name is unique");

        // Set creation timestamp
        playlist.setCreatedDate(LocalDateTime.now());
        playlist.setLastModifiedDate(LocalDateTime.now());
        
        if (playlist.getItems() == null) {
            playlist.setItems(new ArrayList<>());
        }

        Playlist saved = playlistRepository.save(playlist);
        
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ PLAYLIST CREATED SUCCESSFULLY");
        System.out.println("   Playlist ID: " + saved.getId());
        System.out.println("   Name: " + saved.getName());
        System.out.println("════════════════════════════════════════\n");
        
        return saved;
    }

    @Transactional
    public Playlist updatePlaylist(String id, Playlist updatedPlaylist) {
        Optional<Playlist> existingPlaylistOpt = playlistRepository.findById(id);
        if (!existingPlaylistOpt.isPresent()) {
            throw new IllegalArgumentException("Playlist with ID " + id + " not found");
        }

        Playlist existingPlaylist = existingPlaylistOpt.get();

        /* 1. duplicate-name guard */
        if (!existingPlaylist.getName().equals(updatedPlaylist.getName())) {
            if (isPlaylistNameExists(updatedPlaylist.getName(),
                                    existingPlaylist.getUserId(), id)) {
                throw new IllegalArgumentException(
                        "Playlist with name '" + updatedPlaylist.getName() +
                        "' already exists for this user");
            }
        }

        /* 2. copy simple fields */
        existingPlaylist.setName(updatedPlaylist.getName());
        existingPlaylist.setDescription(updatedPlaylist.getDescription());
        existingPlaylist.setThumbnailUrl(updatedPlaylist.getThumbnailUrl());
        existingPlaylist.setVisibility(updatedPlaylist.getVisibility());
        existingPlaylist.setCategory(updatedPlaylist.getCategory());
        existingPlaylist.setTags(updatedPlaylist.getTags());
        existingPlaylist.setIsActive(updatedPlaylist.getIsActive());
        existingPlaylist.setDuration(updatedPlaylist.getDuration());
        existingPlaylist.setGroupId(updatedPlaylist.getGroupId());
        existingPlaylist.setDevices(updatedPlaylist.getDevices());
        existingPlaylist.setDurationEnable(updatedPlaylist.getDurationEnable());
        existingPlaylist.setTimeEnable(updatedPlaylist.getTimeEnable());

        /* 3. settings */
        if (updatedPlaylist.getSetting() != null) {
            existingPlaylist.setSetting(updatedPlaylist.getSetting());
        }

        /* 4. Handle items reordering */
        if (updatedPlaylist.getItems() != null && !updatedPlaylist.getItems().isEmpty()) {
            Map<String, Integer> newOrderMap = updatedPlaylist.getItems().stream()
                .collect(Collectors.toMap(PlayListItems::getMediaId, PlayListItems::getDisplayOrder));
            
            List<PlayListItems> existingItems = existingPlaylist.getItems();
            if (existingItems != null) {
                for (PlayListItems item : existingItems) {
                    if (newOrderMap.containsKey(item.getMediaId())) {
                        item.setDisplayOrder(newOrderMap.get(item.getMediaId()));
                    }
                }
                
                existingItems.sort(Comparator.comparingInt(PlayListItems::getDisplayOrder));
                
                for (int i = 0; i < existingItems.size(); i++) {
                    existingItems.get(i).setDisplayOrder(i + 1);
                }
            }
            
            Set<String> existingMediaIds = existingItems.stream()
                .map(PlayListItems::getMediaId)
                .collect(Collectors.toSet());
                
            for (PlayListItems newItem : updatedPlaylist.getItems()) {
                if (!existingMediaIds.contains(newItem.getMediaId())) {
                    int insertPosition = Math.min(newItem.getDisplayOrder() - 1, existingItems.size());
                    existingItems.add(insertPosition, newItem);
                }
            }
            
            for (int i = 0; i < existingItems.size(); i++) {
                existingItems.get(i).setDisplayOrder(i + 1);
            }
        }

        /* 5. timestamp & save */
        existingPlaylist.setLastModifiedDate(LocalDateTime.now());
        Playlist saved = playlistRepository.save(existingPlaylist);

        /* 6. sync PlaylistContent - delete old, insert current */
        playlistContentRepository.deleteByPlaylistId(saved.getId());
        for (PlayListItems item : saved.getItems()) {
            PlaylistContent pc = new PlaylistContent();
            pc.setPlaylistId(saved.getId());
            pc.setContentId(item.getMediaId());
            pc.setDisplayOrder(item.getDisplayOrder());
            playlistContentRepository.save(pc);
        }

        return saved;
    }

@Transactional
    public Playlist schedulePlaylist(String userId, String playlistId, Playlist.PlaylistScheduleInfo scheduleInfo) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        
        if (!playlistOpt.isPresent()) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
        }
        
        Playlist playlist = playlistOpt.get();
        if (!playlist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
        }
        
        if (playlist.getScheduleInfo() != null && playlist.getScheduleInfo().isScheduled()) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " is already scheduled. Use UPDATE to modify the existing schedule.");
        }

        // ✅ NEW: Check for schedule conflicts with other playlists
        if (hasScheduleConflicts(userId, playlistId, scheduleInfo)) {
            throw new IllegalArgumentException(
                "Cannot schedule this playlist: it conflicts with existing schedule(s). " +
                "Please choose different times, days, or date range."
            );
        }
        
        LocalDateTime nextScheduledTime = calculateNextScheduledTime(scheduleInfo);
        scheduleInfo.setNextScheduledTime(nextScheduledTime);
        scheduleInfo.setScheduled(true);
        
        playlist.setScheduleInfo(scheduleInfo);
        playlist.setLastModifiedDate(LocalDateTime.now());
        
        return playlistRepository.save(playlist);
    }

@Transactional
    public Playlist updatePlaylistSchedule(String userId, String playlistId, Playlist.PlaylistScheduleInfo scheduleInfo) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (!playlistOpt.isPresent()) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
        }
        Playlist playlist = playlistOpt.get();
        if (!playlist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
        }

        // ✅ NEW: Check for schedule conflicts with other playlists (exclude current playlistId)
        if (hasScheduleConflicts(userId, playlistId, scheduleInfo)) {
            throw new IllegalArgumentException(
                "Cannot update this schedule: it conflicts with existing schedule(s). " +
                "Please choose different times, days, or date range."
            );
        }

        LocalDateTime nextScheduledTime = calculateNextScheduledTime(scheduleInfo);
        scheduleInfo.setNextScheduledTime(nextScheduledTime);
        scheduleInfo.setScheduled(true);
        playlist.setScheduleInfo(scheduleInfo);
        playlist.setLastModifiedDate(LocalDateTime.now());
        return playlistRepository.save(playlist);
    }

    @Transactional
    public Playlist removePlaylistSchedule(String userId, String playlistId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (!playlistOpt.isPresent()) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
        }
        Playlist playlist = playlistOpt.get();
        if (!playlist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
        }
        playlist.setScheduleInfo(null);
        playlist.setLastModifiedDate(LocalDateTime.now());
        return playlistRepository.save(playlist);
    }

    public List<Playlist> getScheduledPlaylists(String userId) {
        return playlistRepository.findByUserIdAndScheduleInfo_Scheduled(userId, true);
    }

    public List<Playlist> getActivePlaylistsForDevice(String userId, String deviceId, LocalDateTime currentTime) {
        List<Playlist> allPlaylists = playlistRepository.findByUserId(userId);
        return allPlaylists.stream()
                .filter(playlist -> {
                    if (playlist.getScheduleInfo() == null || !playlist.getScheduleInfo().isScheduled()) {
                        return false;
                    }
                    return playlist.getScheduleInfo().getScheduledDevices() != null &&
                           playlist.getScheduleInfo().getScheduledDevices().contains(deviceId) &&
                           isScheduleActiveAtTime(playlist.getScheduleInfo(), currentTime);
                })
                .collect(Collectors.toList());
    }

    public List<Playlist> getPlaylistsScheduledForTime(String userId, LocalDateTime targetTime) {
        List<Playlist> allPlaylists = playlistRepository.findByUserIdAndScheduleInfo_Scheduled(userId, true);
        return allPlaylists.stream()
                .filter(playlist -> {
                    if (playlist.getScheduleInfo() == null || !playlist.getScheduleInfo().isScheduled()) {
                        return false;
                    }
                    return isScheduleActiveAtTime(playlist.getScheduleInfo(), targetTime);
                })
                .collect(Collectors.toList());
    }

    public List<Playlist> getUpcomingScheduledPlaylists(String userId, int hours) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime futureTime = currentTime.plusHours(hours);
        return playlistRepository.findByUserIdAndScheduleInfo_NextScheduledTimeBetween(userId, currentTime, futureTime);
    }

    @Transactional
    public void markPlaylistExecuted(String userId, String playlistId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            if (!playlist.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
            }
            if (playlist.getScheduleInfo() != null) {
                playlist.getScheduleInfo().setLastExecuted(LocalDateTime.now());
                if (playlist.getScheduleInfo().isRecurring()) {
                    LocalDateTime nextTime = calculateNextRecurringTime(playlist.getScheduleInfo(), LocalDateTime.now());
                    playlist.getScheduleInfo().setNextScheduledTime(nextTime);
                }
                playlistRepository.save(playlist);
            }
        } else {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
        }
    }

    public Playlist.PlaylistScheduleInfo getPlaylistScheduleInfo(String userId, String playlistId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            if (!playlist.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not belong to user " + userId);
            }
            return playlist.getScheduleInfo();
        } else {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
        }
    }

    private boolean isScheduleActiveAtTime(Playlist.PlaylistScheduleInfo schedule, LocalDateTime checkTime) {
        if (schedule.getStartDate() != null && schedule.getEndDate() != null) {
            if (checkTime.isBefore(schedule.getStartDate()) || checkTime.isAfter(schedule.getEndDate())) {
                return false;
            }
        }
        if (schedule.getSelectedDaysOfWeek() != null && !schedule.getSelectedDaysOfWeek().isEmpty()) {
            DayOfWeek currentDayOfWeek = checkTime.getDayOfWeek();
            String currentDayAbbr = currentDayOfWeek.name().substring(0, 2).toUpperCase();
            if (!schedule.getSelectedDaysOfWeek().contains(currentDayAbbr)) {
                return false;
            }
        }
        if (schedule.getSelectedDatesOfMonth() != null && !schedule.getSelectedDatesOfMonth().isEmpty()) {
            int currentDayOfMonth = checkTime.getDayOfMonth();
            if (!schedule.getSelectedDatesOfMonth().contains(currentDayOfMonth)) {
                return false;
            }
        }
        if (schedule.isSpecificTimeEnabled()) {
            LocalTime currentTime = checkTime.toLocalTime();
            if (schedule.getSpecificStartTime() != null && schedule.getSpecificEndTime() != null) {
                if (currentTime.isBefore(schedule.getSpecificStartTime()) || 
                    currentTime.isAfter(schedule.getSpecificEndTime())) {
                    return false;
                }
            }
        }
        if (schedule.isWeekCycleEnabled() && schedule.getWeekCycleType() != null) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int weekOfYear = checkTime.get(weekFields.weekOfWeekBasedYear());
            int cycleType = schedule.getWeekCycleType();
            if (cycleType > 0 && (weekOfYear % cycleType != 0)) {
                return false;
            }
        }
        return true;
    }

    private LocalDateTime calculateNextScheduledTime(Playlist.PlaylistScheduleInfo scheduleInfo) {
        LocalDateTime now = LocalDateTime.now();
        if (scheduleInfo.getScheduledTime() != null) {
            LocalDateTime scheduledTime = scheduleInfo.getScheduledTime();
            if (scheduledTime.isAfter(now)) {
                return scheduledTime;
            } else if (scheduleInfo.isRecurring()) {
                return calculateNextRecurringTime(scheduleInfo, now);
            }
        }
        return now;
    }

    private LocalDateTime calculateNextRecurringTime(Playlist.PlaylistScheduleInfo scheduleInfo, LocalDateTime from) {
        String recurring = scheduleInfo.getRecurring();
        LocalDateTime next = from;
        if (recurring == null) {
            return next;
        }
        switch (recurring.toLowerCase()) {
            case "daily":
                next = from.plusDays(1);
                if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
                    next = next.with(scheduleInfo.getSpecificStartTime());
                }
                break;
            case "weekly":
                next = from.plusWeeks(1);
                if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
                    next = next.with(scheduleInfo.getSpecificStartTime());
                }
                break;
            case "monthly":
                next = from.plusMonths(1);
                if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
                    next = next.with(scheduleInfo.getSpecificStartTime());
                }
                break;
            case "custom":
                next = calculateCustomRecurringTime(scheduleInfo, from);
                break;
        }
        return next;
    }

    private LocalDateTime calculateCustomRecurringTime(Playlist.PlaylistScheduleInfo scheduleInfo, LocalDateTime from) {
        LocalDateTime next = from.plusDays(1);
        if (scheduleInfo.getSelectedDaysOfWeek() != null && !scheduleInfo.getSelectedDaysOfWeek().isEmpty()) {
            DayOfWeek currentDay = from.getDayOfWeek();
            int daysToAdd = 1;
            while (true) {
                next = from.plusDays(daysToAdd);
                String nextDayAbbr = next.getDayOfWeek().name().substring(0, 2).toUpperCase();
                if (scheduleInfo.getSelectedDaysOfWeek().contains(nextDayAbbr)) {
                    break;
                }
                daysToAdd++;
            }
        }
        if (scheduleInfo.isSpecificTimeEnabled() && scheduleInfo.getSpecificStartTime() != null) {
            next = next.with(scheduleInfo.getSpecificStartTime());
        }
        return next;
    }

    private boolean isPlaylistNameExists(String name, String userId, String excludePlaylistId) {
        List<Playlist> existingPlaylists = playlistRepository.findByUserId(userId);
        
        return existingPlaylists.stream()
                .anyMatch(playlist -> 
                    playlist.getName().equalsIgnoreCase(name) && 
                    (excludePlaylistId == null || !playlist.getId().equals(excludePlaylistId))
                );
    }

    public boolean isPlaylistNameExistsForUser(String name, String userId) {
        Optional<Playlist> existingPlaylist = playlistRepository.findByNameAndUserId(name, userId);
        return existingPlaylist.isPresent();
    }

    public void deletePlaylist(String id) {
        playlistRepository.deleteById(id);
    }

    public List<PlaylistContent> getPlaylistContents(String playlistId) {
        return playlistContentRepository.findByPlaylistIdOrderByDisplayOrder(playlistId);
    }

    @Transactional
    public PlaylistContent addContentToPlaylist(String playlistId, String contentId, Integer displayOrder) {
        PlaylistContent playlistContent = new PlaylistContent();
        playlistContent.setPlaylistId(playlistId);
        playlistContent.setContentId(contentId);
        playlistContent.setDisplayOrder(displayOrder);
        return playlistContentRepository.save(playlistContent);
    }

    @Transactional
    public PlayListItems addContentToPlaylistItems(String playlistId, String contentId, Integer displayOrder) {
        Integer playlistCount = playlistRepository.findById(playlistId).get().getItems().size() + 1;

        return playlistRepository.findById(playlistId)
                .map(playlist -> contentRepository.findById(contentId)
                        .map(content -> {
                            PlayListItems playListItemsData = new PlayListItems();
                            playListItemsData.setMediaId(contentId);
                            playListItemsData.setTitle(content.getTitle());
                            playListItemsData.setFileType(content.getFileType());
                            playListItemsData.setUrl(content.getUrl());
                            playListItemsData.setDuration(content.getDuration());
                            playListItemsData.setThumbnail(content.getThumbnail());
                            playListItemsData.setDisplayOrder(playlistCount);

                            if (playlist.getItems() == null) {
                                playlist.setItems(new ArrayList<>());
                            }

                            playlist.getItems().add(playListItemsData);
                            playlistRepository.save(playlist);

                            return playListItemsData;
                        })
                        .orElseThrow(() -> new IllegalArgumentException("Content not found")))
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));
    }

    @Transactional
    public void removeContentFromPlaylist(String playlistId, String contentId) {
        playlistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

        if (playlist.getItems() != null) {
            playlist.getItems().removeIf(item -> contentId.equals(item.getMediaId()));

            for (int i = 0; i < playlist.getItems().size(); i++) {
                playlist.getItems().get(i).setDisplayOrder(i + 1);
            }
        }

        playlist.setLastModifiedDate(LocalDateTime.now());
        playlistRepository.save(playlist);
    }

    public void updatePlaylistGroupId(String playlistId, String groupId) {
        Optional<Playlist> playlist = playlistRepository.findById(playlistId);
        if (playlist.isPresent()) {
            Playlist updatedPlaylist = playlist.get();
            updatedPlaylist.setGroupId(groupId);
            updatedPlaylist.setLastModifiedDate(LocalDateTime.now());
            playlistRepository.save(updatedPlaylist);
        } else {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " does not exist.");
        }
    }

    public void deletePlaylistByGroupId(String groupId) {
        List<Playlist> playlists = playlistRepository.findByGroupId(groupId);
        for (Playlist playlist : playlists) {
            playlist.setGroupId(null);
            playlist.setLastModifiedDate(LocalDateTime.now());
            playlistRepository.save(playlist);
        }
    }

    public List<Playlist> findByGroupId(String groupId) {
        return playlistRepository.findByGroupId(groupId);
    }

    public List<Playlist> getPlaylistsByUserIdWithFilters(
        String userId,
        String name,
        String category,
        String visibility,
        String groupId,
        String sortBy,
        String sortOrder) {
    
        Criteria criteria = Criteria.where("userId").is(userId);
        
        if (name != null && !name.isEmpty()) {
            criteria.and("name").regex(name, "i");
        }
        
        if (category != null && !category.isEmpty()) {
            criteria.and("category").is(category);
        }
        
        if (visibility != null && !visibility.isEmpty()) {
            criteria.and("visibility").is(visibility);
        }
        
        if (groupId != null && !groupId.isEmpty()) {
            criteria.and("groupId").is(groupId);
        }
        
        Query query = new Query(criteria);
        
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(Sort.by(direction, sortBy));
        }
        
        return mongoTemplate.find(query, Playlist.class);
    }

    public List<PlaylistContent> getPlaylistContentsWithFilters(
        String playlistId,
        String contentId,
        Integer minOrder,
        Integer maxOrder,
        String sortBy,
        String sortOrder) {
    
        Criteria criteria = Criteria.where("playlistId").is(playlistId);
        
        if (contentId != null && !contentId.isEmpty()) {
            criteria.and("contentId").is(contentId);
        }
        
        if (minOrder != null && maxOrder != null) {
            criteria.and("displayOrder").gte(minOrder).lte(maxOrder);
        } else if (minOrder != null) {
            criteria.and("displayOrder").gte(minOrder);
        } else if (maxOrder != null) {
            criteria.and("displayOrder").lte(maxOrder);
        }
        
        Query query = new Query(criteria);
        
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(Sort.by(direction, sortBy));
        }
        
        return mongoTemplate.find(query, PlaylistContent.class);
    }

    public List<Playlist> filterPlaylistByValue(String userId, String value) {
        String lowerCaseValue = value != null ? value.toLowerCase() : null;
        List<Playlist> allPlaylists = playlistRepository.findAll();

        if (lowerCaseValue != null && !lowerCaseValue.isEmpty()) {
            return allPlaylists.stream()
                    .filter(playlist -> 
                        playlist.getUserId() != null && playlist.getUserId().equals(userId) && (
                            (playlist.getName() != null && playlist.getName().toLowerCase().startsWith(lowerCaseValue)) ||
                            (playlist.getDescription() != null && playlist.getDescription().toLowerCase().startsWith(lowerCaseValue)) ||
                            (playlist.getCategory() != null && playlist.getCategory().toLowerCase().startsWith(lowerCaseValue)) ||
                            (playlist.getVisibility() != null && playlist.getVisibility().toLowerCase().startsWith(lowerCaseValue)) ||
                            (playlist.getGroupId() != null && playlist.getGroupId().toLowerCase().startsWith(lowerCaseValue)) ||
                            (playlist.getIsActive() != null && String.valueOf(playlist.getIsActive()).toLowerCase().startsWith(lowerCaseValue)) ||
                            (playlist.getViewCount() != null && String.valueOf(playlist.getViewCount()).toLowerCase().startsWith(lowerCaseValue))
                        )
                    )
                    .collect(Collectors.toList());
        } else {
            return allPlaylists.stream()
                    .filter(playlist -> playlist.getUserId() != null && playlist.getUserId().equals(userId))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Checks if the new schedule conflicts with any existing schedules for the user
     * @param userId User attempting to schedule
     * @param excludePlaylistId Playlist ID to exclude (for updates)
     * @param newSchedule Proposed schedule info
     * @return true if conflict exists, false otherwise
     */
    public boolean hasScheduleConflicts(String userId, String excludePlaylistId, Playlist.PlaylistScheduleInfo newSchedule) {
        if (newSchedule == null || !newSchedule.isScheduled()) {
            return false;
        }

        // Get all scheduled playlists for this user, excluding the target playlist
        List<Playlist> userScheduledPlaylists = playlistRepository.findScheduledPlaylistsByUserId(userId).stream()
            .filter(p -> !p.getId().equals(excludePlaylistId))
            .filter(p -> p.getScheduleInfo() != null && p.getScheduleInfo().isScheduled() && p.getScheduleInfo().isEnabled())
            .collect(Collectors.toList());

        // Early return if no other schedules
        if (userScheduledPlaylists.isEmpty()) {
            return false;
        }

        // Check date range overlap first (quick filter using repo query)
        if (newSchedule.getStartDate() != null && newSchedule.getEndDate() != null) {
            List<Playlist> dateOverlapping = playlistRepository.findPlaylistsScheduledInDateRange(
                newSchedule.getStartDate(), newSchedule.getEndDate());
            userScheduledPlaylists.retainAll(dateOverlapping);
            if (userScheduledPlaylists.isEmpty()) {
                return false;
            }
        }

        // Detailed conflict checking for remaining candidates
        for (Playlist existingPlaylist : userScheduledPlaylists) {
            Playlist.PlaylistScheduleInfo existing = existingPlaylist.getScheduleInfo();

            // Skip if same playlist (shouldn't happen but safety check)
            if (existingPlaylist.getId().equals(excludePlaylistId)) continue;

            // 1. Date range overlap check
            if (!rangesOverlap(newSchedule.getStartDate(), newSchedule.getEndDate(),
                              existing.getStartDate(), existing.getEndDate())) {
                continue;
            }

            // 2. Days of week intersection
            if (!daysOfWeekOverlap(newSchedule.getSelectedDaysOfWeek(), existing.getSelectedDaysOfWeek())) {
                continue;
            }

            // 3. Specific time overlap (if both use specific times)
            if (newSchedule.isSpecificTimeEnabled() && existing.isSpecificTimeEnabled()) {
                if (!timesOverlap(newSchedule.getSpecificStartTime(), newSchedule.getSpecificEndTime(),
                                 existing.getSpecificStartTime(), existing.getSpecificEndTime())) {
                    continue;
                }
            }

            // If all checks pass - CONFLICT!
            return true;
        }

        return false;
    }

    /**
     * Check if two date ranges overlap
     */
    private boolean rangesOverlap(LocalDateTime start1, LocalDateTime end1, 
                                 LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) return false;
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }

    /**
     * Check if two sets of days have intersection
     */
    private boolean daysOfWeekOverlap(List<String> days1, List<String> days2) {
        if ((days1 == null || days1.isEmpty()) && (days2 == null || days2.isEmpty())) {
            return true; // Both unspecified = overlap
        }
        if (days1 == null || days1.isEmpty() || days2 == null || days2.isEmpty()) {
            return true; // One unspecified = overlap with anything
        }
        return days1.stream().anyMatch(days2::contains);
    }

    /**
     * Check if two time ranges overlap
     */
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) return false;
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }
}

