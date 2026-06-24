// // package com.mediaserver.service;

// // import java.util.ArrayList;
// // import java.util.Collections;
// // import java.util.Date;
// // import java.util.HashSet;
// // import java.util.List;
// // import java.util.Optional;
// // import java.util.Set;
// // import java.util.stream.Collectors;

// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.stereotype.Service;

// // import com.mediaserver.model.Device;
// // import com.mediaserver.model.Group;
// // import com.mediaserver.repository.DeviceRepository;
// // import com.mediaserver.repository.GroupRepository;
// // import com.mediaserver.repository.UserRepository;
// // import com.mediaserver.util.ScheduleOverlapValidator;


// // @Service
// // public class GroupService {

// //     @Autowired
// //     private GroupRepository groupRepository;

// //     @Autowired
// //     private DeviceRepository deviceRepository;

// //     @Autowired
// //     private UserRepository userRepository;

// //     @Autowired
// //     private UserService userService;

// //     @Autowired
// //     private DeviceService deviceService;

// //     @Autowired
// //     private PlaylistService playlistService;

// //     @Autowired
// //     private ScheduleOverlapValidator scheduleOverlapValidator;

// //     // public Group createGroup(String userId, Group group) {
// //     //     if (group.getId() != null && !group.getId().isEmpty()) {
// //     //         throw new IllegalArgumentException("Group ID must be null or empty when creating a new group.");
// //     //     }
// //     //     // group.setUserId(userId);
// //     //     // return groupRepository.save(group);

// //     //     Optional<Group> existingGroup = groupRepository.findByUserIdAndGroupName(userId, group.getGroupName());
// //     //     if (existingGroup.isPresent()) {
// //     //         throw new IllegalArgumentException("Group name already exists for this user.");
// //     //     }

// //     //     group.setUserId(userId);
// //     //     group.setCreateDate(new Date());
// //     //     group.setUpdateDate(new Date());
// //     //     group.setLastSeen(new Date());
// //     //     return groupRepository.save(group);
// //     // }

// //     //add by priya 11/6

// //     public Group createGroup(String userId, Group group) {
// //     if (group.getId() != null && !group.getId().isEmpty()) {
// //         throw new IllegalArgumentException("Group ID must be null or empty when creating a new group.");
// //     }
// //     if (!userRepository.existsById(userId)) {
// //         throw new IllegalArgumentException("The userId is not present in the database.");
// //     }
// //     Optional<Group> existingGroup = groupRepository.findByUserIdAndGroupName(userId, group.getGroupName());
// //     if (existingGroup.isPresent()) {
// //         throw new IllegalArgumentException("Group name already exists for this user.");
// //     }
// //     group.setUserId(userId);
// //     group.setCreateDate(new Date());
// //     group.setUpdateDate(new Date());
// //     group.setLastSeen(new Date());

// //     // REMOVE or MOVE this line: group.setDevices(new ArrayList<>());

// //     if (group.getDevices() != null && !group.getDevices().isEmpty()) {
// //         Set<String> uniqueDevices = new HashSet<>(group.getDevices());
// //         if (uniqueDevices.size() != group.getDevices().size()) {
// //             throw new IllegalArgumentException("Duplicate devices found in the provided list.");
// //         }
// //         for (String deviceId : group.getDevices()) {
// //             if (!deviceRepository.existsById(deviceId)) {
// //                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
// //             }
// //         }
// //         group.setDevices(new ArrayList<>(uniqueDevices));
// //     } else {
// //         // If no devices are provided, ensure the list is initialized as empty to avoid NullPointerException
// //         group.setDevices(new ArrayList<>());
// //     }

// //     // Handle schedules field
// //     if (group.getSchedules() != null && !group.getSchedules().isEmpty()) {
// //         Set<String> uniqueSchedules = new HashSet<>(group.getSchedules());
// //         if (uniqueSchedules.size() != group.getSchedules().size()) {
// //             throw new IllegalArgumentException("Duplicate schedules found in the provided list.");
// //         }
        
// //         // Validate no schedule overlaps
// //         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(uniqueSchedules), "new group");
        
// //         group.setSchedules(new ArrayList<>(uniqueSchedules));
// //     } else {
// //         // If no schedules are provided, ensure the list is initialized as empty
// //         group.setSchedules(new ArrayList<>());
// //     }

// //     Group savedGroup = groupRepository.save(group);

// //     if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
// //         playlistService.updatePlaylistGroupId(group.getCurrentPlaylistId(), savedGroup.getId());
// //     }

// //     return savedGroup;
// // }



// //     // public Group updateGroup(String groupId, Group group) {

// //     //     if (groupId == null || groupId.isEmpty()) {
// //     //         throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
// //     //     }

// //     //     // Check if groupName is null or empty
// //     //     if (group.getGroupName() == null || group.getGroupName().isEmpty()) {
// //     //         throw new IllegalArgumentException("Group name cannot be null or empty for update.");
// //     //     }
// //     //     Optional<Group> existingGroup = groupRepository.findById(groupId);
// //     //     if (existingGroup.isPresent()) {
// //     //         Group updatedGroup = existingGroup.get();
// //     //         updatedGroup.setGroupName(group.getGroupName());
// //     //         updatedGroup.setStatus(group.getStatus());
// //     //         updatedGroup.setLocation(group.getLocation());
// //     //         updatedGroup.setNotes(group.getNotes());
// //     //         updatedGroup.setCurrentPlaylistId(group.getCurrentPlaylistId());
// //     //         updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());
// //     //         updatedGroup.setDevices(group.getDevices());
// //     //         updatedGroup.setUpdateDate(new Date());
// //     //         updatedGroup.setLastSeen(new Date());
// //     //         return groupRepository.save(updatedGroup);
// //     //     }
// //     //     return null;
// //     // }
// //     //11/6
// //     public Group updateGroup(String groupId, Group group) {
// //     if (groupId == null || groupId.isEmpty()) {
// //         throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
// //     }

// //     if (group.getGroupName() == null || group.getGroupName().isEmpty()) {
// //         throw new IllegalArgumentException("Group name cannot be null or empty for update.");
// //     }

// //     Optional<Group> existingGroup = groupRepository.findById(groupId);
// //     if (!existingGroup.isPresent()) {
// //         throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
// //     }

// //     // Check if the userId exists in the database
// //     if (!userRepository.existsById(existingGroup.get().getUserId())) {
// //         throw new IllegalArgumentException("The userId is not present in the database.");
// //     }

// //     Group updatedGroup = existingGroup.get();
// //     updatedGroup.setGroupName(group.getGroupName());
// //     updatedGroup.setStatus(group.getStatus());
// //     updatedGroup.setLocation(group.getLocation());
// //     updatedGroup.setNotes(group.getNotes());
// //     updatedGroup.setCurrentPlaylistId(group.getCurrentPlaylistId());
// //     updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());

// //     // --- MODIFIED LOGIC FOR DEVICES ---
// //     if (group.getDevices() != null) { // Only update devices if the input provides a non-null list
// //         Set<String> newDevices = new HashSet<>();
// //         for (String deviceId : group.getDevices()) {
// //             if (!deviceRepository.existsById(deviceId)) {
// //                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
// //             }
// //             newDevices.add(deviceId);
// //         }
// //         updatedGroup.setDevices(new ArrayList<>(newDevices));
// //     } 
// //     // If group.getDevices() is null, the existing devices on updatedGroup remain unchanged.
// //     // If group.getDevices() is an empty list, the devices on updatedGroup will be set to an empty list.
// //     // --- END MODIFIED LOGIC ---

// //     // --- MODIFIED LOGIC FOR SCHEDULES ---
// //     if (group.getSchedules() != null) { // Only update schedules if the input provides a non-null list
// //         Set<String> newSchedules = new HashSet<>(group.getSchedules());
// //         if (newSchedules.size() != group.getSchedules().size()) {
// //             throw new IllegalArgumentException("Duplicate schedules found in the provided list.");
// //         }
        
// //         // Validate no schedule overlaps
// //         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(newSchedules), groupId);
        
// //         updatedGroup.setSchedules(new ArrayList<>(newSchedules));
// //     }
// //     // If group.getSchedules() is null, the existing schedules on updatedGroup remain unchanged.
// //     // If group.getSchedules() is an empty list, the schedules on updatedGroup will be set to an empty list.
// //     // --- END MODIFIED LOGIC FOR SCHEDULES ---

// //     updatedGroup.setUpdateDate(new Date());
// //     updatedGroup.setLastSeen(new Date());

// //     // Update the playlist with the new groupId
// //     if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
// //         playlistService.updatePlaylistGroupId(group.getCurrentPlaylistId(), group.getId());
// //     }
// //     return groupRepository.save(updatedGroup);
// // }

// //     // public void addDevicesToGroup(String groupId, List<String> deviceIds) {
// //     //     Optional<Group> existingGroup = groupRepository.findById(groupId);
// //     //     // * if (existingGroup.isPresent()) {
// //     //     //     Group group = existingGroup.get();
// //     //     //     if (group.getDevices() == null) {
// //     //     //         group.setDevices(deviceIds);
// //     //     //     } else {
// //     //     //         group.getDevices().addAll(deviceIds);
// //     //     //     }
// //     //     //     groupRepository.save(group);
// //     //     // *}

// //     //     if (existingGroup.isPresent()) {
// //     //         Group group = existingGroup.get();
// //     //         List<String> existingDeviceIds = group.getDevices() != null ? group.getDevices() : new ArrayList<>();
// //     //         // Set<String> uniqueDeviceIds = new HashSet<>(existingDeviceIds);
// //     //         // Check for duplicates within the group
// //     //         Set<String> uniqueDeviceIds = new HashSet<>(existingDeviceIds);
// //     //         for (String deviceId : deviceIds) {
// //     //             if (uniqueDeviceIds.contains(deviceId)) {
// //     //                 throw new IllegalArgumentException("Device ID " + deviceId + " is already part of this group.");
// //     //             }
// //     //             uniqueDeviceIds.add(deviceId);

// //     //             // Check if the device ID is already part of another group
// //     //             Optional<Group> groupWithDevice = groupRepository.findByDevices(deviceId);
// //     //             if (groupWithDevice.isPresent() && !groupWithDevice.get().getId().equals(groupId)) {
// //     //                 throw new IllegalArgumentException("Device ID " + deviceId + " is already part of another group.");
// //     //             }
// //     //         }

// //     //         // Add new device IDs to the group
// //     //         existingDeviceIds.addAll(deviceIds);
// //     //         group.setDevices(existingDeviceIds);
// //     //         groupRepository.save(group);
// //     //     }
// //     // }

// //     // public void addDevicesToGroup(String groupId, List<String> deviceIds) {
// //     //     Optional<Group> existingGroup = groupRepository.findById(groupId);
// //     //     if (existingGroup.isPresent()) {
// //     //         Group group = existingGroup.get();
// //     //         Set<String> existingDeviceIds = new HashSet<>(group.getDevices() != null ? group.getDevices() : Collections.emptyList());

// //     //         for (String deviceId : deviceIds) {
// //     //             // Check for duplicates within the group

// //     //             if(deviceRepository.existsById(groupId)){
// //     //             if (existingDeviceIds.contains(deviceId)) {
// //     //                 throw new IllegalArgumentException("Device ID " + deviceId + " is already part of this group.");
// //     //             }

// //     //             Optional<Device> device = deviceRepository.findById(deviceId);
// //     //             System.out.println(device);
// //     //             if (device.get().getGroupId() == null) {
// //     //                 existingDeviceIds.add(deviceId);
// //     //             }

// //     //             // Check if the device ID is already part of another group
// //     //             Optional<Group> groupWithDevice = groupRepository.findByDevices(deviceId);
// //     //             if (groupWithDevice.isPresent() && !groupWithDevice.get().getId().equals(groupId)) {
// //     //                 throw new IllegalArgumentException("Device ID " + deviceId + " is already part of another group.");
// //     //             }
// //     //         }
// //     //         }

// //     //         // Add new device IDs to the group
// //     //         group.getDevices().addAll(deviceIds);
// //     //         groupRepository.save(group);
// //     //     }
// //     // }

// //     public void addDevicesToGroup(String groupId, List<String> deviceIds) {
// //     Optional<Group> groupOpt = groupRepository.findById(groupId);
// //     if (!groupOpt.isPresent()) {
// //         throw new IllegalArgumentException("Group not found");
// //     }

// //     Group group = groupOpt.get();
// //     List<String> existingDevices = group.getDevices() != null ? group.getDevices() : new ArrayList<>();
// //     Set<String> uniqueDevices = new HashSet<>(existingDevices);

// //     for (String deviceId : deviceIds) {
// //         Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
// //         if (!deviceOpt.isPresent()) {
// //             throw new IllegalArgumentException("Device " + deviceId + " not found");
// //         }

// //         Device device = deviceOpt.get();
        
// //         // Check if the device is already associated with another group
// //         if (device.getGroupId() != null && !device.getGroupId().equals(groupId)) {
// //             throw new IllegalArgumentException("Device " + deviceId + " is already associated with another group: " + device.getGroupId());
// //         }

// //         // Update device's groupId
// //         device.setGroupId(groupId);
// //         deviceRepository.save(device);

// //         // Add to group's devices list if not already present
// //         if (!uniqueDevices.contains(deviceId)) {
// //             uniqueDevices.add(deviceId);
// //         }
// //     }

// //     // Update group's devices list
// //     group.setDevices(new ArrayList<>(uniqueDevices));
// //     group.setUpdateDate(new Date());
// //     groupRepository.save(group);
// // }

// //     public void removeDevicesFromGroup(String groupId, List<String> deviceIds) {
// //     Optional<Group> existingGroup = groupRepository.findById(groupId);
// //     if (existingGroup.isPresent()) {
// //         Group group = existingGroup.get();
// //         if (group.getDevices() != null) {
// //             for (String deviceId : deviceIds) {
// //                 // 1. remove the device from the group's list
// //                 group.getDevices().remove(deviceId);

// //                 // 2. clear the groupId on the device side
// //                 Optional<Device> device = deviceRepository.findById(deviceId);
// //                 device.ifPresent(d -> {
// //                     d.setGroupId(null);
// //                     deviceService.updateDevice(d);
// //                 });
// //             }
// //             group.setUpdateDate(new Date());
// //             group.setLastSeen(new Date());

// //             // 3. *** save the updated group ***
// //             groupRepository.save(group);
// //         }
// //     }
// // }

// //     public Optional<Group> getGroupById(String groupId) {
// //         return groupRepository.findById(groupId);
// //     }

// //     public List<Group> getGroupByUserId(String userId) {
// //         return groupRepository.findByUserId(userId);
// //     }

// //     public Optional<Group> getGroupByUserIdAndGroupName(String userId, String groupName) {
// //         return groupRepository.findByUserIdAndGroupName(userId, groupName);
// //     }

// //     // public List<Group> getGroupsByUserId(String userId) { // New method
// //     //     return groupRepository.findByUserId(userId);
// //     // } cmd priya

// //     public List<String> getDevicesByGroupId(String groupId) {
// //         Optional<Group> group = groupRepository.findById(groupId);
// //         return group.map(Group::getDevices).orElse(null);
// //     }

// //     public List<Group> getAllGroups() {
// //         return groupRepository.findAll();
// //     }

// //     // added newly line may 26

// //     public void deleteGroupByUserIdAndGroupId(String userId, String groupId) {
// //     if (groupId == null || groupId.isEmpty()) {
// //         throw new IllegalArgumentException("Group ID cannot be null or empty.");
// //     }

// //     if (userId == null || userId.isEmpty()) {
// //         throw new IllegalArgumentException("User ID cannot be null or empty.");
// //     }

// //     Optional<Group> existingGroup = groupRepository.findById(groupId);
// //     if (!existingGroup.isPresent()) {
// //         throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
// //     }

// //     Group group = existingGroup.get();
// //     if (!group.getUserId().equals(userId)) {
// //         throw new IllegalArgumentException("User ID does not match the group's user ID.");
// //     }

// //     // Remove groupId from all playlists associated with this group
// //     //11/6
// //     playlistService.deletePlaylistByGroupId(groupId);

// //     groupRepository.deleteById(groupId);
// // }


// //     public Group updateGroupByUserIdAndGroupId(String userId, String groupId, Group group) {
// //     if (groupId == null || groupId.isEmpty()) {
// //         throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
// //     }

// //     if (group.getGroupName() == null || group.getGroupName().isEmpty()) {
// //         throw new IllegalArgumentException("Group name cannot be null or empty for update.");
// //     }

// //     Optional<Group> existingGroup = groupRepository.findById(groupId);
// //     if (!existingGroup.isPresent()) {
// //         throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
// //     }

// //     Group updatedGroup = existingGroup.get();

// //     // Check if the userId matches the group's userId
// //     if (!updatedGroup.getUserId().equals(userId)) {
// //         throw new IllegalArgumentException("User ID does not match the group's user ID.");
// //     }

// //     // Update the group fields
// //     updatedGroup.setGroupName(group.getGroupName());
// //     updatedGroup.setStatus(group.getStatus());
// //     updatedGroup.setLocation(group.getLocation());
// //     updatedGroup.setNotes(group.getNotes());
// //     updatedGroup.setCurrentPlaylistId(group.getCurrentPlaylistId());
// //     updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());

// //     // Merge existing devices with new devices
// //     Set<String> allDevices = new HashSet<>();
// //     if (updatedGroup.getDevices() != null) {
// //         allDevices.addAll(updatedGroup.getDevices());
// //     }
// //     if (group.getDevices() != null) {
// //         for (String deviceId : group.getDevices()) {
// //             // Check if the device exists in the database
// //             if (!deviceRepository.existsById(deviceId)) {
// //                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the database.");
// //             }

// //             // Check for duplicates
// //             if (!allDevices.add(deviceId)) {
// //                 throw new IllegalArgumentException("Duplicate device ID " + deviceId + " found.");
// //             }
// //         }
// //     }

// //     updatedGroup.setDevices(new ArrayList<>(allDevices));
    
// //     // Handle schedules field
// //     Set<String> allSchedules = new HashSet<>();
// //     if (updatedGroup.getSchedules() != null) {
// //         allSchedules.addAll(updatedGroup.getSchedules());
// //     }
// //     if (group.getSchedules() != null) {
// //         for (String scheduleId : group.getSchedules()) {
// //             // Check for duplicates
// //             if (!allSchedules.add(scheduleId)) {
// //                 throw new IllegalArgumentException("Duplicate schedule ID " + scheduleId + " found.");
// //             }
// //         }
// //     }
    
// //     // Validate no schedule overlaps for the final combined list
// //     if (!allSchedules.isEmpty()) {
// //         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(allSchedules), groupId);
// //     }
    
// //     updatedGroup.setSchedules(new ArrayList<>(allSchedules));
    
// //     updatedGroup.setUpdateDate(new Date());
// //     updatedGroup.setLastSeen(new Date());

// //     return groupRepository.save(updatedGroup);
// // }

// // // add by priya 11/6/25


// // public List<Device> getLinkedDevicesByGroupId(String groupId) {
// //     // Verify group exists first
// //     if (!groupRepository.existsById(groupId)) {
// //         throw new IllegalArgumentException("Group not found");
// //     }

// //     // Get devices by groupId from devices collection
// //     List<Device> devices = deviceRepository.findByGroupId(groupId);
    
// //     // Also verify against group's devices list for consistency
// //     Optional<Group> group = groupRepository.findById(groupId);
// //     if (group.isPresent() && group.get().getDevices() != null) {
// //         Set<String> groupDeviceIds = new HashSet<>(group.get().getDevices());
// //         devices = devices.stream()
// //             .filter(device -> groupDeviceIds.contains(device.getId()))
// //             .collect(Collectors.toList());
// //     }
    
// //     return devices;
// // }
// // // add by priya 11/6

// // public List<Group> getGroupsByUserId(String userId, String groupName, String status, String location) {
// //     if (!userService.isUserIdExists(userId)) {
// //         throw new IllegalArgumentException("User ID does not exist.");
// //     }
    
// //     List<Group> groups = groupRepository.findByUserId(userId);
    
// //     if (groupName != null && !groupName.isEmpty()) {
// //         groups = groupRepository.findByUserIdAndGroupNameContaining(userId, groupName);
// //     }
    
// //     if (status != null && !status.isEmpty()) {
// //         groups = groupRepository.findByUserIdAndStatus(userId, status);
// //     }
    
// //     if (location != null && !location.isEmpty()) {
// //         groups = groupRepository.findByUserIdAndLocation(userId, location);
// //     }
    
// //     return groups;
// // }

// // public List<Group> filterGroupsByValue(String userId, String value) {
// //     String lowerCaseValue = value != null ? value.toLowerCase() : null;
// //     List<Group> allGroups = groupRepository.findByUserId(userId);

// //     if (lowerCaseValue != null && !lowerCaseValue.isEmpty()) {
// //         return allGroups.stream()
// //                 .filter(group ->
// //                     (group.getGroupName() != null && group.getGroupName().toLowerCase().startsWith(lowerCaseValue)) ||
// //                     (group.getStatus() != null && group.getStatus().toLowerCase().startsWith(lowerCaseValue)) ||
// //                     (group.getLocation() != null && group.getLocation().toLowerCase().startsWith(lowerCaseValue)) ||
// //                     (group.getNotes() != null && group.getNotes().toLowerCase().startsWith(lowerCaseValue)) ||
// //                     (group.getCurrentPlaylistName() != null && group.getCurrentPlaylistName().toLowerCase().startsWith(lowerCaseValue))
// //                 )
// //                 .collect(Collectors.toList());
// //     } else {
// //         return allGroups;
// //     }
// // }

// // //added 2/7
// //  public List<Device> getDevicesByUserIdAndGroupId(String userId, String groupId) {
// //         // 1. Validate userId exists
// //         if (!userRepository.existsById(userId)) {
// //             throw new IllegalArgumentException("User ID does not exist.");
// //         }

// //         // 2. Find the group by groupId
// //         Optional<Group> groupOpt = groupRepository.findById(groupId);
// //         if (!groupOpt.isPresent()) {
// //             throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
// //         }
// //         Group group = groupOpt.get();

// //         // 3. Verify the group belongs to the specified userId
// //         if (!group.getUserId().equals(userId)) {
// //             throw new IllegalArgumentException("Group with ID " + groupId + " does not belong to user ID " + userId + ".");
// //         }

// //         // 4. Get the list of device IDs from the group
// //         List<String> deviceIds = group.getDevices();
// //         if (deviceIds == null || deviceIds.isEmpty()) {
// //             return Collections.emptyList(); // Return empty list if no devices are linked
// //         }

// //         // 5. Fetch each device using deviceRepository.findById and collect them
// //         List<Device> devices = new ArrayList<>();
// //         for (String deviceId : deviceIds) {
// //             Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
// //             deviceOpt.ifPresent(devices::add);
// //         }

// //         return devices;

// // }

// // }

// package com.mediaserver.service;

// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Date;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Objects;
// import java.util.Optional;
// import java.util.Set;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.mediaserver.model.Device;
// import com.mediaserver.model.Group;
// import com.mediaserver.model.Playlist;
// import com.mediaserver.repository.DeviceRepository;
// import com.mediaserver.repository.GroupRepository;
// import com.mediaserver.repository.UserRepository;
// import com.mediaserver.util.ScheduleOverlapValidator;

// @Service
// public class GroupService {

//     @Autowired
//     private GroupRepository groupRepository;

//     @Autowired
//     private DeviceRepository deviceRepository;

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private UserService userService;

//     @Autowired
//     private DeviceService deviceService;

//     @Autowired
//     private PlaylistService playlistService;

//     @Autowired
//     private ScheduleOverlapValidator scheduleOverlapValidator;

//     @Autowired
//     private SubscriptionLimitService subscriptionLimitService;

//     // public Group createGroup(String userId, Group group) {
//     //     if (group.getId() != null && !group.getId().isEmpty()) {
//     //         throw new IllegalArgumentException("Group ID must be null or empty when creating a new group.");
//     //     }
//     //     if (!userRepository.existsById(userId)) {
//     //         throw new IllegalArgumentException("The userId is not present in the database.");
//     //     }
//     //     Optional<Group> existingGroup = groupRepository.findByUserIdAndGroupName(userId, group.getGroupName());
//     //     if (existingGroup.isPresent()) {
//     //         throw new IllegalArgumentException("Group name already exists for this user.");
//     //     }
//     //     group.setUserId(userId);
//     //     group.setCreateDate(new Date());
//     //     group.setUpdateDate(new Date());
//     //     group.setLastSeen(new Date());

//     //     if (group.getDevices() != null && !group.getDevices().isEmpty()) {
//     //         Set<String> uniqueDevices = new HashSet<>(group.getDevices());
//     //         if (uniqueDevices.size() != group.getDevices().size()) {
//     //             throw new IllegalArgumentException("Duplicate devices found in the provided list.");
//     //         }
//     //         for (String deviceId : group.getDevices()) {
//     //             if (!deviceRepository.existsById(deviceId)) {
//     //                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
//     //             }
//     //         }
//     //         group.setDevices(new ArrayList<>(uniqueDevices));
//     //     } else {
//     //         group.setDevices(new ArrayList<>());
//     //     }

//     //     if (group.getSchedules() != null && !group.getSchedules().isEmpty()) {
//     //         Set<String> uniqueSchedules = new HashSet<>(group.getSchedules());
//     //         if (uniqueSchedules.size() != group.getSchedules().size()) {
//     //             throw new IllegalArgumentException("Duplicate schedules found in the provided list.");
//     //         }
//     //         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(uniqueSchedules), "new group");
//     //         group.setSchedules(new ArrayList<>(uniqueSchedules));
//     //     } else {
//     //         group.setSchedules(new ArrayList<>());
//     //     }

//     //     // Handle currentPlaylistId as a list (moved after save to use savedGroup.getId())
//     //     if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
//     //         Set<String> uniquePlaylists = new HashSet<>(group.getCurrentPlaylistId());
//     //         if (uniquePlaylists.size() != group.getCurrentPlaylistId().size()) {
//     //             throw new IllegalArgumentException("Duplicate playlist IDs found in the provided list.");
//     //         }
//     //         group.setCurrentPlaylistId(new ArrayList<>(uniquePlaylists));
//     //     } else {
//     //         group.setCurrentPlaylistId(new ArrayList<>());
//     //     }

//     //     Group savedGroup = groupRepository.save(group);

//     //     // Update playlists after saving the group
//     //     if (savedGroup.getCurrentPlaylistId() != null && !savedGroup.getCurrentPlaylistId().isEmpty()) {
//     //         for (String playlistId : savedGroup.getCurrentPlaylistId()) {
//     //             playlistService.updatePlaylistGroupId(playlistId, savedGroup.getId());
//     //         }
//     //     }

//     //     return savedGroup;
//     // }
//     // GroupService.java
// public Group createGroup(String userId, Group group) {
//     if (group.getId() != null && !group.getId().isEmpty()) {
//         throw new IllegalArgumentException("Group ID must be null or empty when creating a new group.");
//     }
//     if (!userRepository.existsById(userId)) {
//         throw new IllegalArgumentException("The userId is not present in the database.");
//     }
//     Optional<Group> existingGroup = groupRepository.findByUserIdAndGroupName(userId, group.getGroupName());
//     if (existingGroup.isPresent()) {
//         throw new IllegalArgumentException("Group name already exists for this user.");
//     }

//     // Check subscription limits for groups
//     subscriptionLimitService.checkGroupLimit(userId);

//     group.setUserId(userId);
//     group.setCreateDate(new Date());
//     group.setUpdateDate(new Date());
//     group.setLastSeen(new Date());

//     // Handle devices (deduplicate and validate)
//     if (group.getDevices() != null && !group.getDevices().isEmpty()) {
//         Set<String> uniqueDevices = new HashSet<>(group.getDevices());
//         if (uniqueDevices.size() != group.getDevices().size()) {
//             throw new IllegalArgumentException("Duplicate devices found in the provided list.");
//         }
//         for (String deviceId : group.getDevices()) {
//             if (!deviceRepository.existsById(deviceId)) {
//                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
//             }
//         }
//         group.setDevices(new ArrayList<>(uniqueDevices));
//     } else {
//         group.setDevices(new ArrayList<>());
//     }

//     // Handle schedules (deduplicate and validate)
//     if (group.getSchedules() != null && !group.getSchedules().isEmpty()) {
//         Set<String> uniqueSchedules = new HashSet<>(group.getSchedules());
//         if (uniqueSchedules.size() != group.getSchedules().size()) {
//             throw new IllegalArgumentException("Duplicate schedules found in the provided list.");
//         }
//         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(uniqueSchedules), "new group");
//         group.setSchedules(new ArrayList<>(uniqueSchedules));
//     } else {
//         group.setSchedules(new ArrayList<>());
//     }

//     // Handle currentPlaylistId (deduplicate and validate schedules)
//     Set<String> allPlaylists = new HashSet<>();
//     if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
//         List<Playlist> playlists = new ArrayList<>();
//         for (String playlistId : group.getCurrentPlaylistId()) {
//             if (!allPlaylists.add(playlistId)) {
//                 throw new IllegalArgumentException("Duplicate playlist ID " + playlistId + " found in currentPlaylistId.");
//             }
//             Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
//             if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(userId)) {
//                 throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + userId);
//             }
//             playlists.add(playlistOpt.get());
//         }
//         // Validate no identical schedules among playlists
//         validatePlaylistScheduleOverlaps(playlists, group.getDevices());
//         group.setCurrentPlaylistId(new ArrayList<>(allPlaylists));
//     }

//     // Handle currentPlaylistName
//     if (group.getCurrentPlaylistName() != null) {
//         group.setCurrentPlaylistName(group.getCurrentPlaylistName());
//     } else if (allPlaylists.size() == 1) {
//         String singlePlaylistId = allPlaylists.iterator().next();
//         Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
//         group.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
//     } else {
//         group.setCurrentPlaylistName(null);
//     }

//     Group savedGroup = groupRepository.save(group);

//     // Update playlist groupId for each unique playlist
//     if (!allPlaylists.isEmpty()) {
//         for (String playlistId : allPlaylists) {
//             playlistService.updatePlaylistGroupId(playlistId, savedGroup.getId());
//         }
//     }

//     return savedGroup;
// }

// public Group updateGroup(String groupId, Group group) {
//     if (groupId == null || groupId.isEmpty()) {
//         throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
//     }

//     Optional<Group> existingGroupOpt = groupRepository.findById(groupId);
//     if (!existingGroupOpt.isPresent()) {
//         throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
//     }

//     Group updatedGroup = existingGroupOpt.get();

//     // Validate groupName
//     if (group.getGroupName() != null && !group.getGroupName().equals(updatedGroup.getGroupName())) {
//         Optional<Group> nameConflict = groupRepository.findByUserIdAndGroupName(updatedGroup.getUserId(), group.getGroupName());
//         if (nameConflict.isPresent() && !nameConflict.get().getId().equals(groupId)) {
//             throw new IllegalArgumentException("Group name already exists for this user.");
//         }
//         updatedGroup.setGroupName(group.getGroupName());
//     }

//     // Update simple fields
//     if (group.getStatus() != null) {
//         updatedGroup.setStatus(group.getStatus());
//     }
//     if (group.getLocation() != null) {
//         updatedGroup.setLocation(group.getLocation());
//     }
//     if (group.getNotes() != null) {
//         updatedGroup.setNotes(group.getNotes());
//     }

//     // Handle devices
//     Set<String> allDevices = new HashSet<>();
//     if (updatedGroup.getDevices() != null) {
//         allDevices.addAll(updatedGroup.getDevices());
//     }
//     if (group.getDevices() != null) {
//         for (String deviceId : group.getDevices()) {
//             if (!allDevices.add(deviceId)) {
//                 throw new IllegalArgumentException("Duplicate device ID " + deviceId + " found.");
//             }
//             if (!deviceRepository.existsById(deviceId)) {
//                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist.");
//             }
//         }
//     }
//     updatedGroup.setDevices(new ArrayList<>(allDevices));

//     // Handle schedules
//     Set<String> allSchedules = new HashSet<>();
//     if (updatedGroup.getSchedules() != null) {
//         allSchedules.addAll(updatedGroup.getSchedules());
//     }
//     if (group.getSchedules() != null) {
//         for (String scheduleId : group.getSchedules()) {
//             if (!allSchedules.add(scheduleId)) {
//                 throw new IllegalArgumentException("Duplicate schedule ID " + scheduleId + " found.");
//             }
//         }
//         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(allSchedules), "update group");
//     }
//     updatedGroup.setSchedules(new ArrayList<>(allSchedules));

//     // Handle currentPlaylistId (deduplicate and validate schedules)
//     Set<String> allPlaylists = new HashSet<>();
//     if (updatedGroup.getCurrentPlaylistId() != null) {
//         allPlaylists.addAll(updatedGroup.getCurrentPlaylistId());
//     }
//     if (group.getCurrentPlaylistId() != null) {
//         List<Playlist> playlists = new ArrayList<>();
//         for (String playlistId : group.getCurrentPlaylistId()) {
//             if (!allPlaylists.add(playlistId)) {
//                 throw new IllegalArgumentException("Duplicate playlist ID " + playlistId + " found.");
//             }
//             Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
//             if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(updatedGroup.getUserId())) {
//                 throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + updatedGroup.getUserId());
//             }
//             playlists.add(playlistOpt.get());
//         }
//         // Validate no identical schedules among playlists
//         validatePlaylistScheduleOverlaps(playlists, updatedGroup.getDevices());
//     }
//     updatedGroup.setCurrentPlaylistId(new ArrayList<>(allPlaylists));

//     // Handle currentPlaylistName
//     if (group.getCurrentPlaylistName() != null) {
//         updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());
//     } else if (allPlaylists.size() == 1) {
//         String singlePlaylistId = allPlaylists.iterator().next();
//         Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
//         updatedGroup.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
//     } else {
//         updatedGroup.setCurrentPlaylistName(null);
//     }

//     // Update playlist groupId
//     for (String playlistId : allPlaylists) {
//         playlistService.updatePlaylistGroupId(playlistId, groupId);
//     }

//     updatedGroup.setUpdateDate(new Date());
//     updatedGroup.setLastSeen(new Date());

//     return groupRepository.save(updatedGroup);
// }

// // Updated helper method to check for identical schedules
// private void validatePlaylistScheduleOverlaps(List<Playlist> playlists, List<String> groupDevices) {
//     for (int i = 0; i < playlists.size(); i++) {
//         Playlist p1 = playlists.get(i);
//         if (p1.getScheduleInfo() == null || !p1.getScheduleInfo().isScheduled()) {
//             continue; // Skip unscheduled playlists
//         }
//         for (int j = i + 1; j < playlists.size(); j++) {
//             Playlist p2 = playlists.get(j);
//             if (p2.getScheduleInfo() == null || !p2.getScheduleInfo().isScheduled()) {
//                 continue;
//             }
//             // Check if schedules are identical for shared devices
//             if (haveCommonDevices(p1.getScheduleInfo().getScheduledDevices(), p2.getScheduleInfo().getScheduledDevices(), groupDevices)) {
//                 if (areSchedulesIdentical(p1.getScheduleInfo(), p2.getScheduleInfo())) {
//                     throw new IllegalArgumentException("Playlists " + p1.getId() + " and " + p2.getId() + " have identical schedules.");
//                 }
//             }
//         }
//     }
// }

// // Helper to check for common devices
// private boolean haveCommonDevices(List<String> devices1, List<String> devices2, List<String> groupDevices) {
//     Set<String> commonDevices = new HashSet<>(devices1 != null ? devices1 : groupDevices);
//     commonDevices.retainAll(devices2 != null ? devices2 : groupDevices);
//     return !commonDevices.isEmpty();
// }

// // Helper to check if two playlist schedules are identical
// private boolean areSchedulesIdentical(Playlist.PlaylistScheduleInfo s1, Playlist.PlaylistScheduleInfo s2) {
//     // Compare key schedule fields
//     return Objects.equals(s1.isScheduled(), s2.isScheduled()) &&
//            Objects.equals(s1.getRecurring(), s2.getRecurring()) &&
//            Objects.equals(s1.getStartDate(), s2.getStartDate()) &&
//            Objects.equals(s1.getEndDate(), s2.getEndDate()) &&
//            Objects.equals(s1.getSelectedDaysOfWeek(), s2.getSelectedDaysOfWeek()) &&
//            Objects.equals(s1.getSelectedDatesOfMonth(), s2.getSelectedDatesOfMonth()) &&
//            Objects.equals(s1.isSpecificTimeEnabled(), s2.isSpecificTimeEnabled()) &&
//            Objects.equals(s1.getSpecificStartTime(), s2.getSpecificStartTime()) &&
//            Objects.equals(s1.getSpecificEndTime(), s2.getSpecificEndTime()) &&
//            Objects.equals(s1.isWeekCycleEnabled(), s2.isWeekCycleEnabled()) &&
//            Objects.equals(s1.getWeekCycleType(), s2.getWeekCycleType()) &&
//            Objects.equals(s1.getScheduledDevices(), s2.getScheduledDevices()) &&
//            Objects.equals(s1.isEnabled(), s2.isEnabled());
// }


//     // public Group updateGroup(String groupId, Group group) {
//     //     if (groupId == null || groupId.isEmpty()) {
//     //         throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
//     //     }

//     //     if (group.getGroupName() == null || group.getGroupName().isEmpty()) {
//     //         throw new IllegalArgumentException("Group name cannot be null or empty for update.");
//     //     }

//     //     Optional<Group> existingGroup = groupRepository.findById(groupId);
//     //     if (!existingGroup.isPresent()) {
//     //         throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
//     //     }

//     //     if (!userRepository.existsById(existingGroup.get().getUserId())) {
//     //         throw new IllegalArgumentException("The userId is not present in the database.");
//     //     }

//     //     Group updatedGroup = existingGroup.get();
//     //     updatedGroup.setGroupName(group.getGroupName());
//     //     updatedGroup.setStatus(group.getStatus());
//     //     updatedGroup.setLocation(group.getLocation());
//     //     updatedGroup.setNotes(group.getNotes());
//     //     updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());

//     //     if (group.getDevices() != null) {
//     //         Set<String> newDevices = new HashSet<>();
//     //         for (String deviceId : group.getDevices()) {
//     //             if (!deviceRepository.existsById(deviceId)) {
//     //                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
//     //             }
//     //             newDevices.add(deviceId);
//     //         }
//     //         updatedGroup.setDevices(new ArrayList<>(newDevices));
//     //     }

//     //     if (group.getSchedules() != null) {
//     //         Set<String> newSchedules = new HashSet<>(group.getSchedules());
//     //         if (newSchedules.size() != group.getSchedules().size()) {
//     //             throw new IllegalArgumentException("Duplicate schedules found in the provided list.");
//     //         }
//     //         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(newSchedules), groupId);
//     //         updatedGroup.setSchedules(new ArrayList<>(newSchedules));
//     //     }

//     //     // Handle currentPlaylistId as a list
//     //     if (group.getCurrentPlaylistId() != null) {
//     //         Set<String> newPlaylists = new HashSet<>(group.getCurrentPlaylistId());
//     //         if (newPlaylists.size() != group.getCurrentPlaylistId().size()) {
//     //             throw new IllegalArgumentException("Duplicate playlist IDs found in the provided list.");
//     //         }
//     //         updatedGroup.setCurrentPlaylistId(new ArrayList<>(newPlaylists));
//     //         for (String playlistId : newPlaylists) {
//     //             playlistService.updatePlaylistGroupId(playlistId, groupId);
//     //         }
//     //     }

//     //     updatedGroup.setUpdateDate(new Date());
//     //     updatedGroup.setLastSeen(new Date());

//     //     // Removed incorrect call to playlistService.updatePlaylistGroupId with List<String>

//     //     return groupRepository.save(updatedGroup);
//     // }

//     public void addDevicesToGroup(String groupId, List<String> deviceIds) {
//         Optional<Group> groupOpt = groupRepository.findById(groupId);
//         if (!groupOpt.isPresent()) {
//             throw new IllegalArgumentException("Group not found");
//         }

//         Group group = groupOpt.get();
//         List<String> existingDevices = group.getDevices() != null ? group.getDevices() : new ArrayList<>();
//         Set<String> uniqueDevices = new HashSet<>(existingDevices);

//         for (String deviceId : deviceIds) {
//             Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
//             if (!deviceOpt.isPresent()) {
//                 throw new IllegalArgumentException("Device " + deviceId + " not found");
//             }

//             Device device = deviceOpt.get();
            
//             // Check if the device is already associated with another group
//             if (device.getGroupId() != null && !device.getGroupId().equals(groupId)) {
//                 throw new IllegalArgumentException("Device " + deviceId + " is already associated with another group: " + device.getGroupId());
//             }

//             // Update device's groupId
//             device.setGroupId(groupId);
//             deviceRepository.save(device);

//             // Add to group's devices list if not already present
//             if (!uniqueDevices.contains(deviceId)) {
//                 uniqueDevices.add(deviceId);
//             }
//         }

//         // Update group's devices list
//         group.setDevices(new ArrayList<>(uniqueDevices));
//         group.setUpdateDate(new Date());
//         groupRepository.save(group);
//     }

//     public void removeDevicesFromGroup(String groupId, List<String> deviceIds) {
//         Optional<Group> existingGroup = groupRepository.findById(groupId);
//         if (existingGroup.isPresent()) {
//             Group group = existingGroup.get();
//             if (group.getDevices() != null) {
//                 for (String deviceId : deviceIds) {
//                     // 1. remove the device from the group's list
//                     group.getDevices().remove(deviceId);

//                     // 2. clear the groupId on the device side
//                     Optional<Device> device = deviceRepository.findById(deviceId);
//                     device.ifPresent(d -> {
//                         d.setGroupId(null);
//                         deviceService.updateDevice(d);
//                     });
//                 }
//                 group.setUpdateDate(new Date());
//                 group.setLastSeen(new Date());

//                 // 3. *** save the updated group ***
//                 groupRepository.save(group);
//             }
//         }
//     }

//     public Optional<Group> getGroupById(String groupId) {
//         return groupRepository.findById(groupId);
//     }

//     public List<Group> getGroupByUserId(String userId) {
//         return groupRepository.findByUserId(userId);
//     }

//     public Optional<Group> getGroupByUserIdAndGroupName(String userId, String groupName) {
//         return groupRepository.findByUserIdAndGroupName(userId, groupName);
//     }

//     public List<String> getDevicesByGroupId(String groupId) {
//         Optional<Group> group = groupRepository.findById(groupId);
//         return group.map(Group::getDevices).orElse(null);
//     }

//     public List<Group> getAllGroups() {
//         return groupRepository.findAll();
//     }

//     public void deleteGroupByUserIdAndGroupId(String userId, String groupId) {
//         if (groupId == null || groupId.isEmpty()) {
//             throw new IllegalArgumentException("Group ID cannot be null or empty.");
//         }

//         if (userId == null || userId.isEmpty()) {
//             throw new IllegalArgumentException("User ID cannot be null or empty.");
//         }

//         Optional<Group> existingGroup = groupRepository.findById(groupId);
//         if (!existingGroup.isPresent()) {
//             throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
//         }

//         Group group = existingGroup.get();
//         if (!group.getUserId().equals(userId)) {
//             throw new IllegalArgumentException("User ID does not match the group's user ID.");
//         }

//         playlistService.deletePlaylistByGroupId(groupId);
//         groupRepository.deleteById(groupId);
//     }

//     // public Group updateGroupByUserIdAndGroupId(String userId, String groupId, Group group) {
//     //     if (groupId == null || groupId.isEmpty()) {
//     //         throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
//     //     }

//     //     if (group.getGroupName() == null || group.getGroupName().isEmpty()) {
//     //         throw new IllegalArgumentException("Group name cannot be null or empty for update.");
//     //     }

//     //     Optional<Group> existingGroup = groupRepository.findById(groupId);
//     //     if (!existingGroup.isPresent()) {
//     //         throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
//     //     }

//     //     Group updatedGroup = existingGroup.get();

//     //     // Check if the userId matches the group's userId
//     //     if (!updatedGroup.getUserId().equals(userId)) {
//     //         throw new IllegalArgumentException("User ID does not match the group's user ID.");
//     //     }

//     //     // Update the group fields
//     //     updatedGroup.setGroupName(group.getGroupName());
//     //     updatedGroup.setStatus(group.getStatus());
//     //     updatedGroup.setLocation(group.getLocation());
//     //     updatedGroup.setNotes(group.getNotes());
//     //     updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());

//     //     // Merge existing devices with new devices
//     //     Set<String> allDevices = new HashSet<>();
//     //     if (updatedGroup.getDevices() != null) {
//     //         allDevices.addAll(updatedGroup.getDevices());
//     //     }
//     //     if (group.getDevices() != null) {
//     //         for (String deviceId : group.getDevices()) {
//     //             // Check if the device exists in the database
//     //             if (!deviceRepository.existsById(deviceId)) {
//     //                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the database.");
//     //             }

//     //             // Check for duplicates
//     //             if (!allDevices.add(deviceId)) {
//     //                 throw new IllegalArgumentException("Duplicate device ID " + deviceId + " found.");
//     //             }
//     //         }
//     //     }

//     //     updatedGroup.setDevices(new ArrayList<>(allDevices));
        
//     //     // Handle schedules field
//     //     Set<String> allSchedules = new HashSet<>();
//     //     if (updatedGroup.getSchedules() != null) {
//     //         allSchedules.addAll(updatedGroup.getSchedules());
//     //     }
//     //     if (group.getSchedules() != null) {
//     //         for (String scheduleId : group.getSchedules()) {
//     //             // Check for duplicates
//     //             if (!allSchedules.add(scheduleId)) {
//     //                 throw new IllegalArgumentException("Duplicate schedule ID " + scheduleId + " found.");
//     //             }
//     //         }
//     //     }
        
//     //     // Validate no schedule overlaps for the final combined list
//     //     if (!allSchedules.isEmpty()) {
//     //         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(allSchedules), groupId);
//     //     }
        
//     //     updatedGroup.setSchedules(new ArrayList<>(allSchedules));
        

//     //     // Handle currentPlaylistId as a list
//     //     Set<String> allPlaylists = new HashSet<>();
//     //     if (updatedGroup.getCurrentPlaylistId() != null) {
//     //         allPlaylists.addAll(updatedGroup.getCurrentPlaylistId());
//     //     }
//     //     if (group.getCurrentPlaylistId() != null) {
//     //         for (String playlistId : group.getCurrentPlaylistId()) {
//     //             if (!allPlaylists.add(playlistId)) {
//     //                 throw new IllegalArgumentException("Duplicate playlist ID " + playlistId + " found.");
//     //             }
//     //         }
//     //     }
//     //     updatedGroup.setCurrentPlaylistId(new ArrayList<>(allPlaylists));
//     //     for (String playlistId : allPlaylists) {
//     //         playlistService.updatePlaylistGroupId(playlistId, groupId);
//     //     }
        
//     //     updatedGroup.setUpdateDate(new Date());
//     //     updatedGroup.setLastSeen(new Date());

//     //     return groupRepository.save(updatedGroup);
//     // }


//     // Corrected updateGroupByUserIdAndGroupId method in GroupService.java
// // This method validates user ownership and updates the group, including proper handling of devices
// // (deduplication, existence check, and no duplicates). It also handles other fields consistently.

// public Group updateGroupByUserIdAndGroupId(String userId, String groupId, Group group) {
//     if (groupId == null || groupId.isEmpty()) {
//         throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
//     }
//     if (!userRepository.existsById(userId)) {
//         throw new IllegalArgumentException("User ID does not exist.");
//     }

//     Optional<Group> existingGroupOpt = groupRepository.findById(groupId);
//     if (!existingGroupOpt.isPresent()) {
//         throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
//     }

//     Group updatedGroup = existingGroupOpt.get();

//     // Verify the group belongs to the specified userId
//     if (!updatedGroup.getUserId().equals(userId)) {
//         throw new IllegalArgumentException("Group with ID " + groupId + " does not belong to user ID " + userId + ".");
//     }

//     // Validate groupName if updated
//     if (group.getGroupName() != null && !group.getGroupName().equals(updatedGroup.getGroupName())) {
//         Optional<Group> nameConflict = groupRepository.findByUserIdAndGroupName(userId, group.getGroupName());
//         if (nameConflict.isPresent() && !nameConflict.get().getId().equals(groupId)) {
//             throw new IllegalArgumentException("Group name already exists for this user.");
//         }
//         updatedGroup.setGroupName(group.getGroupName());
//     }

//     // Update other simple fields
//     if (group.getStatus() != null) {
//         updatedGroup.setStatus(group.getStatus());
//     }
//     if (group.getLocation() != null) {
//         updatedGroup.setLocation(group.getLocation());
//     }
//     if (group.getNotes() != null) {
//         updatedGroup.setNotes(group.getNotes());
//     }

//     // Handle devices: Merge existing devices with new ones, deduplicate, and validate existence
//     Set<String> allDevices = new HashSet<>();
//     if (updatedGroup.getDevices() != null) {
//         allDevices.addAll(updatedGroup.getDevices()); // Preserve existing devices
//     }
//     if (group.getDevices() != null && !group.getDevices().isEmpty()) {
//         for (String deviceId : group.getDevices()) {
//             if (!allDevices.add(deviceId)) { // If already present, it's a duplicate
//                 // Optionally throw or log; here, we just skip duplicates to allow partial updates
//                 // If strict no-duplicates required, uncomment the throw:
//                 // throw new IllegalArgumentException("Duplicate device ID " + deviceId + " found in update.");
//             }
//             if (!deviceRepository.existsById(deviceId)) {
//                 throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
//             }
//         }
//     }
//     updatedGroup.setDevices(new ArrayList<>(allDevices)); // Set the merged, unique devices

//     // Handle schedules: Merge existing with new, deduplicate, and validate no overlaps
//     Set<String> allSchedules = new HashSet<>();
//     if (updatedGroup.getSchedules() != null) {
//         allSchedules.addAll(updatedGroup.getSchedules());
//     }
//     if (group.getSchedules() != null && !group.getSchedules().isEmpty()) {
//         for (String scheduleId : group.getSchedules()) {
//             if (!allSchedules.add(scheduleId)) {
//                 // Skip duplicates or throw if strict
//                 // throw new IllegalArgumentException("Duplicate schedule ID " + scheduleId + " found in update.");
//             }
//         }
//         // Validate no overlaps after merging
//         scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(allSchedules), "update group");
//     }
//     updatedGroup.setSchedules(new ArrayList<>(allSchedules));

//     // Handle currentPlaylistId: Merge existing with new, deduplicate, validate ownership and schedules
//     Set<String> allPlaylists = new HashSet<>();
//     if (updatedGroup.getCurrentPlaylistId() != null) {
//         allPlaylists.addAll(updatedGroup.getCurrentPlaylistId());
//     }
//     if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
//         List<Playlist> playlists = new ArrayList<>();
//         for (String playlistId : group.getCurrentPlaylistId()) {
//             if (!allPlaylists.add(playlistId)) {
//                 // Skip duplicates or throw if strict
//                 // throw new IllegalArgumentException("Duplicate playlist ID " + playlistId + " found in currentPlaylistId.");
//             }
//             Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
//             if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(userId)) {
//                 throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + userId);
//             }
//             playlists.add(playlistOpt.get());
//         }
//         // Validate no identical schedules among playlists
//         validatePlaylistScheduleOverlaps(playlists, updatedGroup.getDevices());
//     }
//     updatedGroup.setCurrentPlaylistId(new ArrayList<>(allPlaylists));

//     // Handle currentPlaylistName
//     if (group.getCurrentPlaylistName() != null) {
//         updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());
//     } else if (allPlaylists.size() == 1) {
//         String singlePlaylistId = allPlaylists.iterator().next();
//         Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
//         updatedGroup.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
//     } else {
//         updatedGroup.setCurrentPlaylistName(null);
//     }

//     // Update playlist groupId for all unique playlists (only if changed)
//     for (String playlistId : allPlaylists) {
//         playlistService.updatePlaylistGroupId(playlistId, groupId);
//     }

//     updatedGroup.setUpdateDate(new Date());
//     updatedGroup.setLastSeen(new Date());

//     return groupRepository.save(updatedGroup);
// }



//     public List<Device> getLinkedDevicesByGroupId(String groupId) {
//         // Verify group exists first
//         if (!groupRepository.existsById(groupId)) {
//             throw new IllegalArgumentException("Group not found");
//         }

//         // Get devices by groupId from devices collection
//         List<Device> devices = deviceRepository.findByGroupId(groupId);
        
//         // Also verify against group's devices list for consistency
//         Optional<Group> group = groupRepository.findById(groupId);
//         if (group.isPresent() && group.get().getDevices() != null) {
//             Set<String> groupDeviceIds = new HashSet<>(group.get().getDevices());
//             devices = devices.stream()
//                 .filter(device -> groupDeviceIds.contains(device.getId()))
//                 .collect(Collectors.toList());
//         }
        
//         return devices;
//     }

//     public List<Group> getGroupsByUserId(String userId, String groupName, String status, String location) {
//         if (!userService.isUserIdExists(userId)) {
//             throw new IllegalArgumentException("User ID does not exist.");
//         }
        
//         List<Group> groups = groupRepository.findByUserId(userId);
        
//         if (groupName != null && !groupName.isEmpty()) {
//             groups = groupRepository.findByUserIdAndGroupNameContaining(userId, groupName);
//         }
        
//         if (status != null && !status.isEmpty()) {
//             groups = groupRepository.findByUserIdAndStatus(userId, status);
//         }
        
//         if (location != null && !location.isEmpty()) {
//             groups = groupRepository.findByUserIdAndLocation(userId, location);
//         }
        
//         return groups;
//     }

//     public List<Group> filterGroupsByValue(String userId, String value) {
//         String lowerCaseValue = value != null ? value.toLowerCase() : null;
//         List<Group> allGroups = groupRepository.findByUserId(userId);

//         if (lowerCaseValue != null && !lowerCaseValue.isEmpty()) {
//             return allGroups.stream()
//                     .filter(group ->
//                         (group.getGroupName() != null && group.getGroupName().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (group.getStatus() != null && group.getStatus().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (group.getLocation() != null && group.getLocation().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (group.getNotes() != null && group.getNotes().toLowerCase().startsWith(lowerCaseValue)) ||
//                         (group.getCurrentPlaylistName() != null && group.getCurrentPlaylistName().toLowerCase().startsWith(lowerCaseValue))
//                     )
//                     .collect(Collectors.toList());
//         } else {
//             return allGroups;
//         }
//     }

//     public List<Device> getDevicesByUserIdAndGroupId(String userId, String groupId) {
//         // 1. Validate userId exists
//         if (!userRepository.existsById(userId)) {
//             throw new IllegalArgumentException("User ID does not exist.");
//         }

//         // 2. Find the group by groupId
//         Optional<Group> groupOpt = groupRepository.findById(groupId);
//         if (!groupOpt.isPresent()) {
//             throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
//         }
//         Group group = groupOpt.get();

//         // 3. Verify the group belongs to the specified userId
//         if (!group.getUserId().equals(userId)) {
//             throw new IllegalArgumentException("Group with ID " + groupId + " does not belong to user ID " + userId + ".");
//         }

//         // 4. Get the list of device IDs from the group
//         List<String> deviceIds = group.getDevices();
//         if (deviceIds == null || deviceIds.isEmpty()) {
//             return Collections.emptyList(); // Return empty list if no devices are linked
//         }

//         // 5. Fetch each device using deviceRepository.findById and collect them
//         List<Device> devices = new ArrayList<>();
//         for (String deviceId : deviceIds) {
//             Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
//             deviceOpt.ifPresent(devices::add);
//         }

//         return devices;
//     }

//     public void removePlaylistFromGroup(String userId, String groupId, String playlistId) {
//     // Validate inputs
//     if (groupId == null || groupId.isEmpty()) {
//         throw new IllegalArgumentException("Group ID cannot be null or empty.");
//     }
//     if (playlistId == null || playlistId.isEmpty()) {
//         throw new IllegalArgumentException("Playlist ID cannot be null or empty.");
//     }
//     if (!userRepository.existsById(userId)) {
//         throw new IllegalArgumentException("User ID does not exist.");
//     }

//     // Verify group exists
//     Optional<Group> groupOpt = groupRepository.findById(groupId);
//     if (!groupOpt.isPresent()) {
//         throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
//     }
//     Group group = groupOpt.get();

//     // Verify group belongs to user
//     if (!group.getUserId().equals(userId)) {
//         throw new IllegalArgumentException("Group with ID " + groupId + " does not belong to user ID " + userId + ".");
//     }

//     // Check if playlist exists and belongs to user
//     Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
//     if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(userId)) {
//         throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + userId);
//     }

//     // Get current playlist IDs
//     List<String> currentPlaylistIds = group.getCurrentPlaylistId() != null ? group.getCurrentPlaylistId() : new ArrayList<>();
//     if (!currentPlaylistIds.contains(playlistId)) {
//         throw new IllegalArgumentException("Playlist ID " + playlistId + " is not associated with group ID " + groupId);
//     }

//     // Remove the playlist ID
//     currentPlaylistIds.remove(playlistId);
//     group.setCurrentPlaylistId(currentPlaylistIds);

//     // Update playlist's groupId to null
//     playlistService.updatePlaylistGroupId(playlistId, null);

//     // Update currentPlaylistName
//     if (currentPlaylistIds.size() == 1) {
//         String singlePlaylistId = currentPlaylistIds.get(0);
//         Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
//         group.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
//     } else {
//         group.setCurrentPlaylistName(null);
//     }

//     // Update timestamps
//     group.setUpdateDate(new Date());
//     group.setLastSeen(new Date());

//     // Save the updated group
//     groupRepository.save(group);
// }

// }

package com.mediaserver.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediaserver.model.Device;
import com.mediaserver.model.Group;
import com.mediaserver.model.Playlist;
import com.mediaserver.repository.DeviceRepository;
import com.mediaserver.repository.GroupRepository;
import com.mediaserver.repository.UserRepository;
import com.mediaserver.util.ScheduleOverlapValidator;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private ScheduleOverlapValidator scheduleOverlapValidator;

    @Autowired
    private SubscriptionLimitService subscriptionLimitService;

    /**
     * ✅ MAIN CREATE GROUP METHOD WITH SUBSCRIPTION LIMIT CHECK
     */
    public Group createGroup(String userId, Group group) {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("🔍 [createGroup] Starting group creation");
        System.out.println("   User ID: " + userId);
        System.out.println("   Group Name: " + group.getGroupName());
        System.out.println("════════════════════════════════════════");
        
        if (group.getId() != null && !group.getId().isEmpty()) {
            throw new IllegalArgumentException("Group ID must be null or empty when creating a new group.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("The userId is not present in the database.");
        }
        
        // ✅ CHECK SUBSCRIPTION LIMITS FIRST
        try {
            subscriptionLimitService.checkGroupLimit(userId);
            System.out.println("✅ Subscription limit check PASSED");
        } catch (SubscriptionLimitService.SubscriptionLimitExceededException e) {
            System.out.println("❌ Subscription limit check FAILED");
            System.out.println("   Error: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        
        Optional<Group> existingGroup = groupRepository.findByUserIdAndGroupName(userId, group.getGroupName());
        if (existingGroup.isPresent()) {
            System.out.println("❌ Group name already exists: " + group.getGroupName());
            throw new IllegalArgumentException("Group name already exists for this user.");
        }

        System.out.println("✅ Group name is unique");

        group.setUserId(userId);
        group.setCreateDate(new Date());
        group.setUpdateDate(new Date());
        group.setLastSeen(new Date());

        // Handle devices
        if (group.getDevices() != null && !group.getDevices().isEmpty()) {
            Set<String> uniqueDevices = new HashSet<>(group.getDevices());
            if (uniqueDevices.size() != group.getDevices().size()) {
                throw new IllegalArgumentException("Duplicate devices found in the provided list.");
            }
            for (String deviceId : group.getDevices()) {
                if (!deviceRepository.existsById(deviceId)) {
                    throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
                }
            }
            group.setDevices(new ArrayList<>(uniqueDevices));
        } else {
            group.setDevices(new ArrayList<>());
        }

        // Handle schedules
        if (group.getSchedules() != null && !group.getSchedules().isEmpty()) {
            Set<String> uniqueSchedules = new HashSet<>(group.getSchedules());
            if (uniqueSchedules.size() != group.getSchedules().size()) {
                throw new IllegalArgumentException("Duplicate schedules found in the provided list.");
            }
            scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(uniqueSchedules), "new group");
            group.setSchedules(new ArrayList<>(uniqueSchedules));
        } else {
            group.setSchedules(new ArrayList<>());
        }

        // Handle currentPlaylistId
        Set<String> allPlaylists = new HashSet<>();
        if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
            List<Playlist> playlists = new ArrayList<>();
            for (String playlistId : group.getCurrentPlaylistId()) {
                if (!allPlaylists.add(playlistId)) {
                    throw new IllegalArgumentException("Duplicate playlist ID " + playlistId + " found in currentPlaylistId.");
                }
                Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
                if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(userId)) {
                    throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + userId);
                }
                playlists.add(playlistOpt.get());
            }
            validatePlaylistScheduleOverlaps(playlists, group.getDevices());
            group.setCurrentPlaylistId(new ArrayList<>(allPlaylists));
        }

        // Handle currentPlaylistName
        if (group.getCurrentPlaylistName() != null) {
            group.setCurrentPlaylistName(group.getCurrentPlaylistName());
        } else if (allPlaylists.size() == 1) {
            String singlePlaylistId = allPlaylists.iterator().next();
            Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
            group.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
        } else {
            group.setCurrentPlaylistName(null);
        }

        Group saved = groupRepository.save(group);

        // Update playlist groupId for each unique playlist
        if (!allPlaylists.isEmpty()) {
            for (String playlistId : allPlaylists) {
                playlistService.updatePlaylistGroupId(playlistId, saved.getId());
            }
        }

        System.out.println("════════════════════════════════════════");
        System.out.println("✅ GROUP CREATED SUCCESSFULLY");
        System.out.println("   Group ID: " + saved.getId());
        System.out.println("   Name: " + saved.getGroupName());
        System.out.println("════════════════════════════════════════\n");

        return saved;
    }

    public Group updateGroup(String groupId, Group group) {
        if (groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
        }

        Optional<Group> existingGroupOpt = groupRepository.findById(groupId);
        if (!existingGroupOpt.isPresent()) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }

        Group updatedGroup = existingGroupOpt.get();

        if (group.getGroupName() != null && !group.getGroupName().equals(updatedGroup.getGroupName())) {
            Optional<Group> nameConflict = groupRepository.findByUserIdAndGroupName(updatedGroup.getUserId(), group.getGroupName());
            if (nameConflict.isPresent() && !nameConflict.get().getId().equals(groupId)) {
                throw new IllegalArgumentException("Group name already exists for this user.");
            }
            updatedGroup.setGroupName(group.getGroupName());
        }

        if (group.getStatus() != null) {
            updatedGroup.setStatus(group.getStatus());
        }
        if (group.getLocation() != null) {
            updatedGroup.setLocation(group.getLocation());
        }
        if (group.getNotes() != null) {
            updatedGroup.setNotes(group.getNotes());
        }

        Set<String> allDevices = new HashSet<>();
        if (updatedGroup.getDevices() != null) {
            allDevices.addAll(updatedGroup.getDevices());
        }
        if (group.getDevices() != null) {
            for (String deviceId : group.getDevices()) {
                if (!allDevices.add(deviceId)) {
                    // Skip duplicates
                }
                if (!deviceRepository.existsById(deviceId)) {
                    throw new IllegalArgumentException("Device ID " + deviceId + " does not exist.");
                }
            }
        }
        updatedGroup.setDevices(new ArrayList<>(allDevices));

        Set<String> allSchedules = new HashSet<>();
        if (updatedGroup.getSchedules() != null) {
            allSchedules.addAll(updatedGroup.getSchedules());
        }
        if (group.getSchedules() != null) {
            for (String scheduleId : group.getSchedules()) {
                if (!allSchedules.add(scheduleId)) {
                    // Skip duplicates
                }
            }
            scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(allSchedules), "update group");
        }
        updatedGroup.setSchedules(new ArrayList<>(allSchedules));

        Set<String> allPlaylists = new HashSet<>();
        if (updatedGroup.getCurrentPlaylistId() != null) {
            allPlaylists.addAll(updatedGroup.getCurrentPlaylistId());
        }
        if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
            List<Playlist> playlists = new ArrayList<>();
            for (String playlistId : group.getCurrentPlaylistId()) {
                if (!allPlaylists.add(playlistId)) {
                    // Skip duplicates
                }
                Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
                if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(updatedGroup.getUserId())) {
                    throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + updatedGroup.getUserId());
                }
                playlists.add(playlistOpt.get());
            }
            validatePlaylistScheduleOverlaps(playlists, updatedGroup.getDevices());
        }
        updatedGroup.setCurrentPlaylistId(new ArrayList<>(allPlaylists));

        if (group.getCurrentPlaylistName() != null) {
            updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());
        } else if (allPlaylists.size() == 1) {
            String singlePlaylistId = allPlaylists.iterator().next();
            Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
            updatedGroup.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
        } else {
            updatedGroup.setCurrentPlaylistName(null);
        }

        for (String playlistId : allPlaylists) {
            playlistService.updatePlaylistGroupId(playlistId, groupId);
        }

        updatedGroup.setUpdateDate(new Date());
        updatedGroup.setLastSeen(new Date());

        return groupRepository.save(updatedGroup);
    }

    private void validatePlaylistScheduleOverlaps(List<Playlist> playlists, List<String> groupDevices) {
        for (int i = 0; i < playlists.size(); i++) {
            Playlist p1 = playlists.get(i);
            if (p1.getScheduleInfo() == null || !p1.getScheduleInfo().isScheduled()) {
                continue;
            }
            for (int j = i + 1; j < playlists.size(); j++) {
                Playlist p2 = playlists.get(j);
                if (p2.getScheduleInfo() == null || !p2.getScheduleInfo().isScheduled()) {
                    continue;
                }
                if (haveCommonDevices(p1.getScheduleInfo().getScheduledDevices(), p2.getScheduleInfo().getScheduledDevices(), groupDevices)) {
                    if (areSchedulesIdentical(p1.getScheduleInfo(), p2.getScheduleInfo())) {
                        throw new IllegalArgumentException("Playlists " + p1.getId() + " and " + p2.getId() + " have identical schedules.");
                    }
                }
            }
        }
    }

    private boolean haveCommonDevices(List<String> devices1, List<String> devices2, List<String> groupDevices) {
        Set<String> commonDevices = new HashSet<>(devices1 != null ? devices1 : groupDevices);
        commonDevices.retainAll(devices2 != null ? devices2 : groupDevices);
        return !commonDevices.isEmpty();
    }

    private boolean areSchedulesIdentical(Playlist.PlaylistScheduleInfo s1, Playlist.PlaylistScheduleInfo s2) {
        return Objects.equals(s1.isScheduled(), s2.isScheduled()) &&
               Objects.equals(s1.getRecurring(), s2.getRecurring()) &&
               Objects.equals(s1.getStartDate(), s2.getStartDate()) &&
               Objects.equals(s1.getEndDate(), s2.getEndDate()) &&
               Objects.equals(s1.getSelectedDaysOfWeek(), s2.getSelectedDaysOfWeek()) &&
               Objects.equals(s1.getSelectedDatesOfMonth(), s2.getSelectedDatesOfMonth()) &&
               Objects.equals(s1.isSpecificTimeEnabled(), s2.isSpecificTimeEnabled()) &&
               Objects.equals(s1.getSpecificStartTime(), s2.getSpecificStartTime()) &&
               Objects.equals(s1.getSpecificEndTime(), s2.getSpecificEndTime()) &&
               Objects.equals(s1.isWeekCycleEnabled(), s2.isWeekCycleEnabled()) &&
               Objects.equals(s1.getWeekCycleType(), s2.getWeekCycleType()) &&
               Objects.equals(s1.getScheduledDevices(), s2.getScheduledDevices()) &&
               Objects.equals(s1.isEnabled(), s2.isEnabled());
    }

    public void addDevicesToGroup(String groupId, List<String> deviceIds) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            throw new IllegalArgumentException("Group not found");
        }

        Group group = groupOpt.get();
        List<String> existingDevices = group.getDevices() != null ? group.getDevices() : new ArrayList<>();
        Set<String> uniqueDevices = new HashSet<>(existingDevices);

        for (String deviceId : deviceIds) {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (!deviceOpt.isPresent()) {
                throw new IllegalArgumentException("Device " + deviceId + " not found");
            }

            Device device = deviceOpt.get();
            
            if (device.getGroupId() != null && !device.getGroupId().equals(groupId)) {
                throw new IllegalArgumentException("Device " + deviceId + " is already associated with another group: " + device.getGroupId());
            }

            device.setGroupId(groupId);
            deviceRepository.save(device);

            if (!uniqueDevices.contains(deviceId)) {
                uniqueDevices.add(deviceId);
            }
        }

        group.setDevices(new ArrayList<>(uniqueDevices));
        group.setUpdateDate(new Date());
        groupRepository.save(group);
    }

    public void removeDevicesFromGroup(String groupId, List<String> deviceIds) {
        Optional<Group> existingGroup = groupRepository.findById(groupId);
        if (existingGroup.isPresent()) {
            Group group = existingGroup.get();
            if (group.getDevices() != null) {
                for (String deviceId : deviceIds) {
                    group.getDevices().remove(deviceId);

                    Optional<Device> device = deviceRepository.findById(deviceId);
                    device.ifPresent(d -> {
                        d.setGroupId(null);
                        deviceService.updateDevice(d);
                    });
                }
                group.setUpdateDate(new Date());
                group.setLastSeen(new Date());

                groupRepository.save(group);
            }
        }
    }

    public Optional<Group> getGroupById(String groupId) {
        return groupRepository.findById(groupId);
    }

    public List<Group> getGroupByUserId(String userId) {
        return groupRepository.findByUserId(userId);
    }

    public Optional<Group> getGroupByUserIdAndGroupName(String userId, String groupName) {
        return groupRepository.findByUserIdAndGroupName(userId, groupName);
    }

    public List<String> getDevicesByGroupId(String groupId) {
        Optional<Group> group = groupRepository.findById(groupId);
        return group.map(Group::getDevices).orElse(null);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public void deleteGroupByUserIdAndGroupId(String userId, String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be null or empty.");
        }

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }

        Optional<Group> existingGroup = groupRepository.findById(groupId);
        if (!existingGroup.isPresent()) {
            throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
        }

        Group group = existingGroup.get();
        if (!group.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User ID does not match the group's user ID.");
        }

        playlistService.deletePlaylistByGroupId(groupId);
        groupRepository.deleteById(groupId);
    }

    public Group updateGroupByUserIdAndGroupId(String userId, String groupId, Group group) {
        if (groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be null or empty for update.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User ID does not exist.");
        }

        Optional<Group> existingGroupOpt = groupRepository.findById(groupId);
        if (!existingGroupOpt.isPresent()) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }

        Group updatedGroup = existingGroupOpt.get();

        if (!updatedGroup.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Group with ID " + groupId + " does not belong to user ID " + userId + ".");
        }

        if (group.getGroupName() != null && !group.getGroupName().equals(updatedGroup.getGroupName())) {
            Optional<Group> nameConflict = groupRepository.findByUserIdAndGroupName(userId, group.getGroupName());
            if (nameConflict.isPresent() && !nameConflict.get().getId().equals(groupId)) {
                throw new IllegalArgumentException("Group name already exists for this user.");
            }
            updatedGroup.setGroupName(group.getGroupName());
        }

        if (group.getStatus() != null) {
            updatedGroup.setStatus(group.getStatus());
        }
        if (group.getLocation() != null) {
            updatedGroup.setLocation(group.getLocation());
        }
        if (group.getNotes() != null) {
            updatedGroup.setNotes(group.getNotes());
        }

        Set<String> allDevices = new HashSet<>();
        if (updatedGroup.getDevices() != null) {
            allDevices.addAll(updatedGroup.getDevices());
        }
        if (group.getDevices() != null && !group.getDevices().isEmpty()) {
            for (String deviceId : group.getDevices()) {
                if (!allDevices.add(deviceId)) {
                    // Skip duplicates
                }
                if (!deviceRepository.existsById(deviceId)) {
                    throw new IllegalArgumentException("Device ID " + deviceId + " does not exist in the device database.");
                }
            }
        }
        updatedGroup.setDevices(new ArrayList<>(allDevices));

        Set<String> allSchedules = new HashSet<>();
        if (updatedGroup.getSchedules() != null) {
            allSchedules.addAll(updatedGroup.getSchedules());
        }
        if (group.getSchedules() != null && !group.getSchedules().isEmpty()) {
            for (String scheduleId : group.getSchedules()) {
                if (!allSchedules.add(scheduleId)) {
                    // Skip duplicates
                }
            }
            scheduleOverlapValidator.validateNoScheduleOverlaps(new ArrayList<>(allSchedules), groupId);
        }
        updatedGroup.setSchedules(new ArrayList<>(allSchedules));

        Set<String> allPlaylists = new HashSet<>();
        if (updatedGroup.getCurrentPlaylistId() != null) {
            allPlaylists.addAll(updatedGroup.getCurrentPlaylistId());
        }
        if (group.getCurrentPlaylistId() != null && !group.getCurrentPlaylistId().isEmpty()) {
            List<Playlist> playlists = new ArrayList<>();
            for (String playlistId : group.getCurrentPlaylistId()) {
                if (!allPlaylists.add(playlistId)) {
                    // Skip duplicates
                }
                Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
                if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(userId)) {
                    throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + userId);
                }
                playlists.add(playlistOpt.get());
            }
            validatePlaylistScheduleOverlaps(playlists, updatedGroup.getDevices());
        }
        updatedGroup.setCurrentPlaylistId(new ArrayList<>(allPlaylists));

        if (group.getCurrentPlaylistName() != null) {
            updatedGroup.setCurrentPlaylistName(group.getCurrentPlaylistName());
        } else if (allPlaylists.size() == 1) {
            String singlePlaylistId = allPlaylists.iterator().next();
            Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
            updatedGroup.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
        } else {
            updatedGroup.setCurrentPlaylistName(null);
        }

        for (String playlistId : allPlaylists) {
            playlistService.updatePlaylistGroupId(playlistId, groupId);
        }

        updatedGroup.setUpdateDate(new Date());
        updatedGroup.setLastSeen(new Date());

        return groupRepository.save(updatedGroup);
    }

    public List<Device> getLinkedDevicesByGroupId(String groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found");
        }

        List<Device> devices = deviceRepository.findByGroupId(groupId);
        
        Optional<Group> group = groupRepository.findById(groupId);
        if (group.isPresent() && group.get().getDevices() != null) {
            Set<String> groupDeviceIds = new HashSet<>(group.get().getDevices());
            devices = devices.stream()
                .filter(device -> groupDeviceIds.contains(device.getId()))
                .collect(Collectors.toList());
        }
        
        return devices;
    }

    public List<Group> getGroupsByUserId(String userId, String groupName, String status, String location) {
        if (!userService.isUserIdExists(userId)) {
            throw new IllegalArgumentException("User ID does not exist.");
        }
        
        List<Group> groups = groupRepository.findByUserId(userId);
        
        if (groupName != null && !groupName.isEmpty()) {
            groups = groupRepository.findByUserIdAndGroupNameContaining(userId, groupName);
        }
        
        if (status != null && !status.isEmpty()) {
            groups = groupRepository.findByUserIdAndStatus(userId, status);
        }
        
        if (location != null && !location.isEmpty()) {
            groups = groupRepository.findByUserIdAndLocation(userId, location);
        }
        
        return groups;
    }

    public List<Group> filterGroupsByValue(String userId, String value) {
        String lowerCaseValue = value != null ? value.toLowerCase() : null;
        List<Group> allGroups = groupRepository.findByUserId(userId);

        if (lowerCaseValue != null && !lowerCaseValue.isEmpty()) {
            return allGroups.stream()
                    .filter(group ->
                        (group.getGroupName() != null && group.getGroupName().toLowerCase().startsWith(lowerCaseValue)) ||
                        (group.getStatus() != null && group.getStatus().toLowerCase().startsWith(lowerCaseValue)) ||
                        (group.getLocation() != null && group.getLocation().toLowerCase().startsWith(lowerCaseValue)) ||
                        (group.getNotes() != null && group.getNotes().toLowerCase().startsWith(lowerCaseValue)) ||
                        (group.getCurrentPlaylistName() != null && group.getCurrentPlaylistName().toLowerCase().startsWith(lowerCaseValue))
                    )
                    .collect(Collectors.toList());
        } else {
            return allGroups;
        }
    }

    public List<Device> getDevicesByUserIdAndGroupId(String userId, String groupId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User ID does not exist.");
        }

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        Group group = groupOpt.get();

        if (!group.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Group with ID " + groupId + " does not belong to user ID " + userId + ".");
        }

        List<String> deviceIds = group.getDevices();
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Device> devices = new ArrayList<>();
        for (String deviceId : deviceIds) {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            deviceOpt.ifPresent(devices::add);
        }

        return devices;
    }

    public void removePlaylistFromGroup(String userId, String groupId, String playlistId) {
        if (groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be null or empty.");
        }
        if (playlistId == null || playlistId.isEmpty()) {
            throw new IllegalArgumentException("Playlist ID cannot be null or empty.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User ID does not exist.");
        }

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (!groupOpt.isPresent()) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        Group group = groupOpt.get();

        if (!group.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Group with ID " + groupId + " does not belong to user ID " + userId + ".");
        }

        Optional<Playlist> playlistOpt = playlistService.getPlaylistById(playlistId);
        if (!playlistOpt.isPresent() || !playlistOpt.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Playlist ID " + playlistId + " does not exist or does not belong to user " + userId);
        }

        List<String> currentPlaylistIds = group.getCurrentPlaylistId() != null ? group.getCurrentPlaylistId() : new ArrayList<>();
        if (!currentPlaylistIds.contains(playlistId)) {
            throw new IllegalArgumentException("Playlist ID " + playlistId + " is not associated with group ID " + groupId);
        }

        currentPlaylistIds.remove(playlistId);
        group.setCurrentPlaylistId(currentPlaylistIds);

        playlistService.updatePlaylistGroupId(playlistId, null);

        if (currentPlaylistIds.size() == 1) {
            String singlePlaylistId = currentPlaylistIds.get(0);
            Optional<Playlist> singlePlaylist = playlistService.getPlaylistById(singlePlaylistId);
            group.setCurrentPlaylistName(singlePlaylist.isPresent() ? singlePlaylist.get().getName() : null);
        } else {
            group.setCurrentPlaylistName(null);
        }

        group.setUpdateDate(new Date());
        group.setLastSeen(new Date());

        groupRepository.save(group);
    }
}
