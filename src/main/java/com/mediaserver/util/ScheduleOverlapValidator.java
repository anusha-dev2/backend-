package com.mediaserver.util;

import com.mediaserver.model.Playlist;
import com.mediaserver.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ScheduleOverlapValidator {

    @Autowired
    private PlaylistRepository playlistRepository;

    /**
     * Validates that the given playlist IDs don't have overlapping schedules
     *
     * @param playlistIds List of playlist IDs to validate
     * @param groupId     The group ID for context in error messages
     * @throws IllegalArgumentException if overlapping schedules are found
     */
    public void validateNoScheduleOverlaps(List<String> playlistIds, String groupId) {
        if (playlistIds == null || playlistIds.size() <= 1) {
            return; // No overlap possible with 0 or 1 playlist
        }

        // 1) Get all playlists with their schedule information
        List<Playlist> playlists = new ArrayList<>();
        playlistRepository.findAllById(playlistIds).forEach(playlists::add);

        // 2) Filter only scheduled playlists
        List<Playlist> scheduledPlaylists = playlists.stream()
                .filter(p -> p.getScheduleInfo() != null && p.getScheduleInfo().isScheduled())
                .collect(Collectors.toList());

        if (scheduledPlaylists.size() <= 1) {
            return;
        }

        // 3) Check for overlaps between each pair of scheduled playlists
        for (int i = 0; i < scheduledPlaylists.size(); i++) {
            for (int j = i + 1; j < scheduledPlaylists.size(); j++) {
                Playlist p1 = scheduledPlaylists.get(i);
                Playlist p2 = scheduledPlaylists.get(j);

                if (hasScheduleOverlap(p1, p2)) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Schedule overlap detected between playlists '%s' and '%s' in group %s. "
                                            + "Multiple playlists cannot be scheduled for the same time and date.",
                                    p1.getName(), p2.getName(), groupId));
                }
            }
        }
    }

    /**
     * Validates that adding a new playlist to existing schedules won't create overlaps
     */
    public void validateAddingPlaylistToSchedules(String newPlaylistId,
                                                  List<String> existingPlaylistIds,
                                                  String groupId) {
        if (existingPlaylistIds == null || existingPlaylistIds.isEmpty()) {
            return;
        }

        Optional<Playlist> newOpt = playlistRepository.findById(newPlaylistId);
        if (!newOpt.isPresent()) {
            throw new IllegalArgumentException("Playlist with ID " + newPlaylistId + " not found");
        }

        Playlist newPlaylist = newOpt.get();
        if (newPlaylist.getScheduleInfo() == null || !newPlaylist.getScheduleInfo().isScheduled()) {
            return;
        }

        // Load existing playlists
        List<Playlist> existing = new ArrayList<>();
        playlistRepository.findAllById(existingPlaylistIds).forEach(existing::add);

        for (Playlist e : existing) {
            if (e.getScheduleInfo() != null
                    && e.getScheduleInfo().isScheduled()
                    && hasScheduleOverlap(newPlaylist, e)) {

                throw new IllegalArgumentException(
                        String.format(
                                "Schedule overlap detected between new playlist '%s' and existing playlist '%s' in group %s. "
                                        + "Multiple playlists cannot be scheduled for the same time and date.",
                                newPlaylist.getName(), e.getName(), groupId));
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* ------------------  PRIVATE HELPER METHODS  ------------------------- */
    /* --------------------------------------------------------------------- */

    private boolean hasScheduleOverlap(Playlist p1, Playlist p2) {
        Playlist.PlaylistScheduleInfo s1 = p1.getScheduleInfo();
        Playlist.PlaylistScheduleInfo s2 = p2.getScheduleInfo();

        if (s1 == null || s2 == null || !s1.isScheduled() || !s2.isScheduled()) {
            return false;
        }

        return dateRangesOverlap(s1, s2)
                && selectedDaysOverlap(s1, s2)
                && selectedDatesOverlap(s1, s2)
                && timeRangesOverlap(s1, s2);
    }

    private boolean dateRangesOverlap(Playlist.PlaylistScheduleInfo s1,
                                      Playlist.PlaylistScheduleInfo s2) {
        LocalDateTime start1 = s1.getStartDate();
        LocalDateTime end1 = s1.getEndDate();
        LocalDateTime start2 = s2.getStartDate();
        LocalDateTime end2 = s2.getEndDate();

        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return true;
        }
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    private boolean selectedDaysOverlap(Playlist.PlaylistScheduleInfo s1,
                                        Playlist.PlaylistScheduleInfo s2) {
        List<String> days1 = s1.getSelectedDaysOfWeek();
        List<String> days2 = s2.getSelectedDaysOfWeek();

        if (days1 == null || days1.isEmpty() || days2 == null || days2.isEmpty()) {
            return true;
        }
        Set<String> set1 = new HashSet<>(days1);
        return days2.stream().anyMatch(set1::contains);
    }

    private boolean selectedDatesOverlap(Playlist.PlaylistScheduleInfo s1,
                                         Playlist.PlaylistScheduleInfo s2) {
        List<Integer> dates1 = s1.getSelectedDatesOfMonth();
        List<Integer> dates2 = s2.getSelectedDatesOfMonth();

        if (dates1 == null || dates1.isEmpty() || dates2 == null || dates2.isEmpty()) {
            return true;
        }
        Set<Integer> set1 = new HashSet<>(dates1);
        return dates2.stream().anyMatch(set1::contains);
    }

    private boolean timeRangesOverlap(Playlist.PlaylistScheduleInfo s1,
                                      Playlist.PlaylistScheduleInfo s2) {
        if (!s1.isSpecificTimeEnabled() || !s2.isSpecificTimeEnabled()) {
            return true;
        }

        LocalTime start1 = s1.getSpecificStartTime();
        LocalTime end1 = s1.getSpecificEndTime();
        LocalTime start2 = s2.getSpecificStartTime();
        LocalTime end2 = s2.getSpecificEndTime();

        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return true;
        }

        // Handle midnight crossings
        if (end1.isBefore(start1)) {          // schedule 1 crosses midnight
            if (end2.isBefore(start2)) {      // schedule 2 also crosses midnight
                return true;
            } else {
                return !start2.isAfter(end1) || !start1.isAfter(end2);
            }
        } else if (end2.isBefore(start2)) {   // only schedule 2 crosses midnight
            return !start1.isAfter(end2) || !start2.isAfter(end1);
        } else {                              // neither crosses midnight
            return !start1.isAfter(end2) && !start2.isAfter(end1);
        }
    }
}