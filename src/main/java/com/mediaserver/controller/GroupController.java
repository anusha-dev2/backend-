package com.mediaserver.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.model.Device;
import com.mediaserver.model.Group;
import com.mediaserver.security.SecurityUtil;
import com.mediaserver.service.GroupService;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private com.mediaserver.service.VideoWallService videoWallService;



    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createGroup(@PathVariable String userId, @RequestBody Group group) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Group createdGroup = groupService.createGroup(userId, group);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    class GroupInfo {
        private String groupName;
        private String id;

        /**
         * Constructor for GroupInfo.
         * @param groupName The name of the group.
         * @param id The ID of the group.
         */
        public GroupInfo(String groupName, String id) {
            this.groupName = groupName;
            this.id = id;
        }

        // Getters
        public String getGroupName() {
            return groupName;
        }

        public String getId() {
            return id;
        }

        // Setters
        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public void setId(String id) {
            this.id = id;
        }

        /**
         * Overrides equals to ensure uniqueness based on the group ID.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupInfo that = (GroupInfo) o;
            return Objects.equals(id, that.id);
        }

        /**
         * Overrides hashCode to be consistent with equals, using the group ID.
         */
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<GroupInfo>> getDistinctCategoryAsString(@PathVariable String userId) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Group> groupsList = groupService.getGroupByUserId(userId);

        Set<GroupInfo> uniqueGroupInfos = groupsList.stream()
            .filter(group -> group.getGroupName() != null && !group.getGroupName().trim().isEmpty())
            .map(group -> new GroupInfo(group.getGroupName(), group.getId()))
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(GroupInfo::getId))));

        return ResponseEntity.ok(new ArrayList<>(uniqueGroupInfos));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(@PathVariable String groupId, @RequestBody Group group) {
        Optional<Group> existingGroup = groupService.getGroupById(groupId);
        if (existingGroup.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!SecurityUtil.isRoot() && !existingGroup.get().getUserId().equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Group updatedGroup = groupService.updateGroup(groupId, group);
            if (updatedGroup != null) {
                return ResponseEntity.ok(updatedGroup);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{groupId}/devices/add")
    public ResponseEntity<String> addDevicesToGroup(@PathVariable String groupId, @RequestBody Map<String, List<String>> payload) {
        Optional<Group> existingGroup = groupService.getGroupById(groupId);
        if (existingGroup.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!SecurityUtil.isRoot() && !existingGroup.get().getUserId().equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<String> deviceIds = payload.get("Devices");
        if (deviceIds == null) {
            return ResponseEntity.badRequest().body("Payload does not contain 'Devices' key.");
        }
        try {
            groupService.addDevicesToGroup(groupId, deviceIds);
            return ResponseEntity.ok("Devices added successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{groupId}/devices/remove")
    public ResponseEntity<Group> removeDevicesFromGroup(
            @PathVariable String groupId,
            @RequestBody List<String> deviceIds) {

        Optional<Group> existingGroup = groupService.getGroupById(groupId);
        if (existingGroup.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!SecurityUtil.isRoot() && !existingGroup.get().getUserId().equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        groupService.removeDevicesFromGroup(groupId, deviceIds);

        return groupService.getGroupById(groupId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroupById(@PathVariable String groupId) {
        Optional<Group> group = groupService.getGroupById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!SecurityUtil.isRoot() && !group.get().getUserId().equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(group.get());
    }

    @GetMapping("/user/{userId}/group/{groupName}")
    public ResponseEntity<Group> getGroupByUserIdAndGroupName(@PathVariable String userId, @PathVariable String groupName) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return groupService.getGroupByUserIdAndGroupName(userId, groupName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getGroupsByUserId(
            @PathVariable String userId,
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "location", required = false) String location) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<Group> groups = groupService.getGroupsByUserId(userId, groupName, status, location);
            return ResponseEntity.ok(groups);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((Object) e.getMessage());
        }
    }

    @GetMapping("/{groupId}/devices")
    public ResponseEntity<List<String>> getDevicesByGroupId(@PathVariable String groupId) {
        Optional<Group> group = groupService.getGroupById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!SecurityUtil.isRoot() && !group.get().getUserId().equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<String> devices = groupService.getDevicesByGroupId(groupId);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Group>> getAllGroups() {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Group> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @DeleteMapping("/user/{userId}/group/{groupId}")
    public ResponseEntity<?> deleteGroupByUserIdAndGroupId(@PathVariable String userId, @PathVariable String groupId) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            groupService.deleteGroupByUserIdAndGroupId(userId, groupId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/user/{userId}/group/{groupId}")
    public ResponseEntity<?> updateGroupByUserIdAndGroupId(@PathVariable String userId, @PathVariable String groupId, @RequestBody Group group) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Group updatedGroup = groupService.updateGroupByUserIdAndGroupId(userId, groupId, group);
            return ResponseEntity.ok(updatedGroup);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}/linked-devices")
    public ResponseEntity<?> getLinkedDevicesByGroupId(@PathVariable String groupId) {
        Optional<Group> group = groupService.getGroupById(groupId);
        if (group.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!SecurityUtil.isRoot() && !group.get().getUserId().equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<Device> devices = groupService.getLinkedDevicesByGroupId(groupId);
            if (devices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body("No linked devices found for group ID: " + groupId);
            }
            return ResponseEntity.ok(devices);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/filter/{userId}/{value}")
    public ResponseEntity<List<Group>> filterGroupsByValue(@PathVariable String userId, @PathVariable String value) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Group> groups = groupService.filterGroupsByValue(userId, value);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/user/{userId}/devices/{groupId}")
    public ResponseEntity<?> getDevicesByUserIdAndGroupId(@PathVariable String userId, @PathVariable String groupId) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<Device> devices = groupService.getDevicesByUserIdAndGroupId(userId, groupId);
            if (devices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No devices found for the specified group and user.");
            }
            return ResponseEntity.ok(devices);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{userId}/group/{groupId}/playlist/{playlistId}")
    public ResponseEntity<?> removePlaylistFromGroup(@PathVariable String userId, @PathVariable String groupId, @PathVariable String playlistId) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            groupService.removePlaylistFromGroup(userId, groupId, playlistId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}