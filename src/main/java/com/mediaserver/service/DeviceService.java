

package com.mediaserver.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediaserver.dto.MonthlyActivityData;
import com.mediaserver.dto.WeeklyActivityData;
import com.mediaserver.model.Device;
import com.mediaserver.model.Group;
import com.mediaserver.model.Playlist;
import com.mediaserver.model.User;
import com.mediaserver.repository.DeviceRepository;
import com.mediaserver.repository.GroupRepository;
import com.mediaserver.repository.PlaylistRepository;
import com.mediaserver.repository.UserRepository;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    private String normalizeLayoutMode(String layoutMode) {
        if (layoutMode == null) {
            return "HORIZONTAL";
        }
        String normalized = layoutMode.trim().toUpperCase();
        if ("VERTICAL".equals(normalized)) {
            return "VERTICAL";
        }
        // Default to HORIZONTAL for null/blank/invalid values
        return "HORIZONTAL";
    }


    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SubscriptionLimitService subscriptionLimitService;

    @Scheduled(fixedRate = 60_000)
    public void updateDeviceStatuses() {
        List<Device> devices = deviceRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Device device : devices) {
            LocalDateTime lastSeen = device.getLastSeen();

            if (lastSeen == null) {
                device.setStatus("offline");
            } else {
                long seconds = Duration.between(lastSeen, now).getSeconds();

                if (seconds <= 10) {
                    device.setStatus("online");
                } else if (seconds <= 30) {
                    device.setStatus("warning");
                } else {
                    device.setStatus("offline");
                }
            }
            deviceRepository.save(device);
        }
    }

    
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Device> getDevicesByUserId(String userId) {
        return deviceRepository.findByUserId(userId);
    }

    public Optional<Device> getDeviceById(String id) {
        return deviceRepository.findById(id);
    }

    public Optional<Device> getDeviceByMacAddress(String macAddress) {
        Optional<Device> deviceOpt = deviceRepository.findByMacAddress(macAddress);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            if (device.getUserId() != null) {
                Optional<User> userOpt = userRepository.findById(device.getUserId());
                if (userOpt.isPresent() && userOpt.get().isAccountMarkedForDeletion()) {
                    throw new IllegalStateException("User account is marked for deletion");
                }
            }
        }
        return deviceOpt;
    }

    /**
     * ✅ MAIN CREATE DEVICE METHOD WITH SUBSCRIPTION LIMIT CHECK
     */
    public Device createDevice(Device device) {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("🔍 [createDevice] Starting device registration");
        System.out.println("   User ID: " + device.getUserId());
        System.out.println("   Device Name: " + device.getDeviceName());
        System.out.println("   MAC Address: " + device.getMacAddress());
        System.out.println("════════════════════════════════════════");

        // ✅ CHECK MAC UNIQUENESS PER USER (same user cannot reuse MAC)
        if (isMacAddressUsedByUser(device.getMacAddress(), device.getUserId())) {
            throw new IllegalArgumentException("MAC address already exists for this user. Delete the old device first.");
        }
        
        // ✅ CHECK SUBSCRIPTION LIMITS FIRST
        try {
            subscriptionLimitService.checkDeviceLimit(device.getUserId());
            System.out.println("✅ Subscription limit check PASSED");
        } catch (SubscriptionLimitService.SubscriptionLimitExceededException e) {
            System.out.println("❌ Subscription limit check FAILED");
            System.out.println("   Error: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }

        // Set registration timestamp if not provided
        if (device.getRegistrationDate() == null) {
            device.setRegistrationDate(LocalDateTime.now());
        }

        Device saved = deviceRepository.save(device);
        
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ DEVICE REGISTERED SUCCESSFULLY");
        System.out.println("   Device ID: " + saved.getId());
        System.out.println("   Status: " + saved.getStatus());
        System.out.println("════════════════════════════════════════\n");
        
        return saved;
    }

    public Device updateDevice(Device device) {
        Optional<Device> existingDeviceOpt = deviceRepository.findById(device.getId());

        if (!existingDeviceOpt.isPresent()) {
            throw new IllegalArgumentException("Device with ID " + device.getId() + " not found.");
        }

        Device existingDevice = existingDeviceOpt.get();

        // ✅ CHECK IF MAC CHANGED, VERIFY UNIQUENESS FOR USER
        if (device.getMacAddress() != null && !device.getMacAddress().equals(existingDevice.getMacAddress())) {
            if (isMacAddressUsedByUser(device.getMacAddress(), existingDevice.getUserId())) {
                throw new IllegalArgumentException("Cannot change to this MAC address - already used by another device for this user.");
            }
        }
        
        if (device.getIp() != null && !device.getIp().isEmpty()) {
            existingDevice.setStatus("online");
        }
        existingDevice.setLastSeen(LocalDateTime.now());
        
        if (device.getMacAddress() != null) existingDevice.setMacAddress(device.getMacAddress());
        if (device.getDeviceName() != null) existingDevice.setDeviceName(device.getDeviceName());
        
        if (device.isEnabled() != existingDevice.isEnabled()) {
            existingDevice.setEnabled(device.isEnabled());
        }
        if (device.getUserId() != null) existingDevice.setUserId(device.getUserId());
        if (device.getStatus() != null) existingDevice.setStatus(device.getStatus());
        if (device.getLocation() != null) existingDevice.setLocation(device.getLocation());
        if (device.getGroup() != null) existingDevice.setGroup(device.getGroup());
        if (device.getIp() != null) existingDevice.setIp(device.getIp());
        if (device.getLastSeen() != null) existingDevice.setLastSeen(device.getLastSeen());
        if (device.getCurrentPlaylist() != null) existingDevice.setCurrentPlaylist(device.getCurrentPlaylist());
        if (device.getDeviceType() != null) existingDevice.setDeviceType(device.getDeviceType());
        if (device.getConnectionType() != null) existingDevice.setConnectionType(device.getConnectionType());
        if (device.getRegistrationDate() != null) existingDevice.setRegistrationDate(device.getRegistrationDate());
        if (device.getNotes() != null) existingDevice.setNotes(device.getNotes());
        if (device.getGroupId() != null) existingDevice.setGroupId(device.getGroupId());
        if (device.getGroupName() != null) existingDevice.setGroupName(device.getGroupName());
        if (device.getDescription() != null) existingDevice.setDescription(device.getDescription());

        if (device.getSetting() != null) {
            if (existingDevice.getSetting() == null) {
                existingDevice.setSetting(new Device.Setting());
            }
            if (device.getSetting().getAssignedGroup() != null) existingDevice.getSetting().setAssignedGroup(device.getSetting().getAssignedGroup());
            if (device.getSetting().getCategories() != null) existingDevice.getSetting().setCategories(device.getSetting().getCategories());
            if (device.getSetting().getTimezoneOfPlayer() != null) existingDevice.getSetting().setTimezoneOfPlayer(device.getSetting().getTimezoneOfPlayer());
            if (device.getSetting().getPlayerNotes() != null) existingDevice.getSetting().setPlayerNotes(device.getSetting().getPlayerNotes());
            if (device.getSetting().getAdditionalPlaylists() != null) existingDevice.getSetting().setAdditionalPlaylists(device.getSetting().getAdditionalPlaylists());

            // Validate/normalize layoutMode (VERTICAL | HORIZONTAL). Default: HORIZONTAL.
            if (device.getSetting().getLayoutMode() != null) {
                existingDevice.getSetting().setLayoutMode(normalizeLayoutMode(device.getSetting().getLayoutMode()));
            }
        }

        return deviceRepository.save(existingDevice);
    }

    public void deleteDevice(String id) {
        deviceRepository.deleteById(id);
    }

    public boolean isMacAddressExists(String macAddress) {
        return deviceRepository.existsByMacAddress(macAddress);
    }

     public boolean isMacAddressUsedByUser(String macAddress, String userId) {
        return deviceRepository.existsByMacAddressAndUserId(macAddress, userId);
    }
    
    public List<Device> findByGroupId(String groupId) {
        return deviceRepository.findByGroupId(groupId);
    }

    public Map<String, Long> getDeviceStatusCounts(String userId) {
        String[] statuses = {"online", "offline", "warning"};
        Map<String, Long> statusCounts = new HashMap<>();
        for (String status : statuses) {
            statusCounts.put(status, 0L);
        }

        List<Device> devices = deviceRepository.findByUserId(userId);
        Map<String, Long> countsFromDb = devices.stream()
                .collect(Collectors.groupingBy(
                        device -> device.getStatus() != null ? device.getStatus() : "warning",
                        Collectors.counting()));

        for (Map.Entry<String, Long> entry : countsFromDb.entrySet()) {
            statusCounts.put(entry.getKey(), entry.getValue());
        }

        return statusCounts;
    }

    public Map<String, Long> getDeviceStatusCountsForAll() {
        String[] statuses = {"online", "offline", "warning"};
        Map<String, Long> statusCounts = new HashMap<>();
        for (String status : statuses) {
            statusCounts.put(status, 0L);
        }

        List<Device> devices = deviceRepository.findAll();
        Map<String, Long> countsFromDb = devices.stream()
                .collect(Collectors.groupingBy(
                        device -> device.getStatus() != null ? device.getStatus() : "warning",
                        Collectors.counting()));

        for (Map.Entry<String, Long> entry : countsFromDb.entrySet()) {
            statusCounts.put(entry.getKey(), entry.getValue());
        }

        return statusCounts;
    }

    public List<Device> getDevicesByUserIdAndFilters(String userId, String deviceName, Boolean enabled, String status) {
        List<Device> devices = deviceRepository.findByUserId(userId);

        if (deviceName != null) {
            devices = devices.stream()
                    .filter(device -> device.getDeviceName().equalsIgnoreCase(deviceName))
                    .collect(Collectors.toList());
        }

        if (enabled != null) {
            devices = devices.stream()
                    .filter(device -> device.isEnabled() == enabled)
                    .collect(Collectors.toList());
        }

        if (status != null) {
            devices = devices.stream()
                    .filter(device -> status.equalsIgnoreCase(device.getStatus()))
                    .collect(Collectors.toList());
        }

        return devices;
    }

    public List<Device> filterDevicesByDeviceName(String deviceName) {
        return deviceRepository.findByDeviceName(deviceName);
    }

    public List<Device> filterDevicesByDeviceNameAndEnabled(String deviceName, Boolean enabled) {
        return deviceRepository.findByDeviceNameAndEnabled(deviceName, enabled);
    }

    public List<Device> filterDevicesByStatus(String status) {
        return deviceRepository.findByStatus(status);
    }

    public List<Device> filterDevicesByDeviceNameStatusAndEnabled(String deviceName, String status, Boolean enabled) {
        return deviceRepository.findByDeviceNameAndStatusAndEnabled(deviceName, status, enabled);
    }

    public List<Device> filterDevicesByStatusAndLocation(String status, String location) {
        return deviceRepository.findByStatusAndLocation(status, location);
    }

    public List<Device> filterDevicesByDeviceNameAndLocation(String deviceName, String location) {
        return deviceRepository.findByDeviceNameAndLocation(deviceName, location);
    }

    public List<Device> filterDevicesByValue(String userId, String value) {
        String lowerCaseValue = value != null ? value.toLowerCase() : null;
        List<Device> allDevices = deviceRepository.findAll();

        if (lowerCaseValue != null && !lowerCaseValue.isEmpty()) {
            return allDevices.stream()
                    .filter(device -> 
                        userId.equals(device.getUserId()) && (
                            (device.getDeviceName() != null && device.getDeviceName().toLowerCase().startsWith(lowerCaseValue)) ||
                            (device.getMacAddress() != null && device.getMacAddress().toLowerCase().startsWith(lowerCaseValue)) ||
                            (device.getLocation() != null && device.getLocation().toLowerCase().startsWith(lowerCaseValue)) ||
                            (device.getIp() != null && device.getIp().toLowerCase().startsWith(lowerCaseValue)) ||
                            (device.getDeviceType() != null && device.getDeviceType().toLowerCase().startsWith(lowerCaseValue)) ||
                            (device.getConnectionType() != null && device.getConnectionType().toLowerCase().startsWith(lowerCaseValue)) ||
                            (device.getNotes() != null && device.getNotes().toLowerCase().startsWith(lowerCaseValue)) ||
                            (device.getStatus() != null && device.getStatus().toLowerCase().startsWith(lowerCaseValue))
                        )
                    )
                    .collect(Collectors.toList());
        } else {
            return allDevices.stream()
                    .filter(device -> userId.equals(device.getUserId()))
                    .collect(Collectors.toList());
        }
    }
    
    public List<Device> getDevicesWithStatusOlderThan(int minutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        List<Device> allDevices = deviceRepository.findAll();

        return allDevices.stream()
            .filter(device -> device.getLastSeen() == null || device.getLastSeen().isBefore(cutoffTime))
            .collect(Collectors.toList());
    }

    public void markOfflineDevicesAsOffline(int timeoutMinutes) {
        List<Device> staleDevices = getDevicesWithStatusOlderThan(timeoutMinutes);
        
        for (Device device : staleDevices) {
            if ("online".equals(device.getStatus())) {
                device.setStatus("offline");
                deviceRepository.save(device);
            }
        }
    }

    public Map<String, Object> getDeviceStatusStatistics(String userId) {
        List<Device> devices;
        if (userId != null && !userId.isEmpty()) {
            devices = deviceRepository.findByUserId(userId);
        } else {
            devices = deviceRepository.findAll();
        }
        
        Map<String, Long> statusCounts = devices.stream()
            .collect(Collectors.groupingBy(
                device -> device.getStatus() != null ? device.getStatus() : "unknown",
                Collectors.counting()
            ));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_devices", devices.size());
        stats.put("status_breakdown", statusCounts);
        stats.put("online_percentage",
            devices.size() > 0 ?
            (statusCounts.getOrDefault("online", 0L) * 100.0 / devices.size()) : 0.0);
        
        return stats;
    }

    public void updateLastSeen(String deviceId) {
        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);
        Device device = optionalDevice.orElse(new Device());
        device.setId(deviceId);
        device.setLastSeen(LocalDateTime.now());
        device.setStatus("ONLINE");
        deviceRepository.save(device);
    }

    public String getLastSeenAsString(String deviceId) {
        return deviceRepository.findById(deviceId)
                .map(d -> d.getLastSeen() != null ? d.getLastSeen().toString() : "Never")
                .orElse("Device not found");
    }

    public Device rebootDevice(String id) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();

            if (!"online".equalsIgnoreCase(device.getStatus())) {
                throw new IllegalArgumentException("Device is not online and cannot be rebooted.");
            }

            if (device.getRebootRequestedAt() != null) {
                Duration duration = Duration.between(device.getRebootRequestedAt(), LocalDateTime.now());
                if (duration.getSeconds() < 60) {
                    throw new IllegalArgumentException("Reboot already requested. Please wait 60 seconds before trying again.");
                }
            }

            device.setRebootStatus(true);
            device.setRebootRequestedAt(LocalDateTime.now());
            return deviceRepository.save(device);
        } else {
            throw new IllegalArgumentException("Device with ID " + id + " not found.");
        }
    }

    public Device resetRebootStatus(String id) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            device.setRebootStatus(false);
            return deviceRepository.save(device);
        } else {
            throw new IllegalArgumentException("Device with ID " + id + " not found.");
        }
    }

    @Transactional
    public Device assignPlaylistToDevice(String deviceId, String playlistId, String userId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isPresent()) {
            throw new IllegalArgumentException("Device with ID " + deviceId + " not found.");
        }
        Device device = deviceOpt.get();

        if (!device.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Device does not belong to user " + userId);
        }

        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (!playlistOpt.isPresent()) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found.");
        }
        Playlist playlist = playlistOpt.get();

        if (!playlist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Playlist does not belong to user " + userId);
        }

        if (device.getAssignedPlaylists() == null) {
            device.setAssignedPlaylists(new ArrayList<>());
        }
        if (playlist.getDevices() == null) {
            playlist.setDevices(new ArrayList<>());
        }

        if (device.getAssignedPlaylists().contains(playlistId)) {
            throw new IllegalArgumentException("Playlist " + playlistId + " is already assigned to device " + deviceId);
        }

        if (playlist.getScheduleInfo() != null && playlist.getScheduleInfo().isRecurring()) {
            throw new IllegalArgumentException("Cannot assign recurring scheduled playlist to avoid duplicates/conflicts.");
        }

        device.getAssignedPlaylists().add(playlistId);
        if (!playlist.getDevices().contains(deviceId)) {
            playlist.getDevices().add(deviceId);
            playlistRepository.save(playlist);
        }

        return deviceRepository.save(device);
    }

    @Transactional
    public Device unassignPlaylistFromDevice(String deviceId, String playlistId, String userId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isPresent()) {
            throw new IllegalArgumentException("Device with ID " + deviceId + " not found.");
        }
        Device device = deviceOpt.get();
        
        if (!device.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Device does not belong to user " + userId);
        }
        
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (!playlistOpt.isPresent()) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found.");
        }
        Playlist playlist = playlistOpt.get();
        
        boolean removedFromDevice = device.getAssignedPlaylists().remove(playlistId);
        if (removedFromDevice) {
            playlist.getDevices().remove(deviceId);
            playlistRepository.save(playlist);
        }
        
        return deviceRepository.save(device);
    }

    public Playlist getActivePlaylistForDevice(String deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            List<String> assignedPlaylistIds = device.getAssignedPlaylists();

            if (assignedPlaylistIds != null && !assignedPlaylistIds.isEmpty()) {
                for (String playlistId : assignedPlaylistIds) {
                    Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
                    if (playlistOpt.isPresent() && playlistOpt.get().getIsActive()) {
                        return playlistOpt.get();
                    }
                }
            }

            if (device.getGroupId() != null) {
                Optional<Group> groupOpt = groupRepository.findById(device.getGroupId());
                if (groupOpt.isPresent()) {
                    Group group = groupOpt.get();
                    List<String> groupPlaylistIds = group.getCurrentPlaylistId();
                    if (groupPlaylistIds != null && !groupPlaylistIds.isEmpty()) {
                        for (String playlistId : groupPlaylistIds) {
                            Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
                            if (playlistOpt.isPresent() && playlistOpt.get().getIsActive()) {
                                return playlistOpt.get();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void sendDeviceCreatedEmail(String userId, Device newDevice) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String subject = "New Device Added: " + newDevice.getDeviceName();
            String body = String.format(
                    "Hello %s,\n\n" +
                    "A new device has been added to your account.\n\n" +
                    "Device Name: %s\n" +
                    "MAC Address: %s\n" +
                    "Status: %s\n\n" +
                    "Thanks,\nKMPS Team",
                    user.getName(), newDevice.getDeviceName(), newDevice.getMacAddress(), newDevice.getStatus());
            emailService.sendEmail(user.getEmail(), subject, body);
        }
    }

    public void sendDeviceDeletedEmail(String userId, Device deletedDevice) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String subject = "Device Deleted: " + deletedDevice.getDeviceName();
            String body = String.format(
                    "Hello %s,\n\n" +
                    "The device '%s' (MAC: %s) has been successfully deleted from your account.\n\n" +
                    "Thanks,\nKMPS Team",
                    user.getName(), deletedDevice.getDeviceName(), deletedDevice.getMacAddress());
            emailService.sendEmail(user.getEmail(), subject, body);
        }
    }

    public void sendDeviceUpdatedEmail(String userId, Device updatedDevice) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String subject = "Device Updated: " + updatedDevice.getDeviceName();
            String body = String.format(
                    "Hello %s,\n\n" +
                    "Your device '%s' has been updated.\n\n" +
                    "Current Status: %s\n" +
                    "Last Seen: %s\n" +
                    "IP Address: %s\n\n" +
                    "Thanks,\nKMPS Team",
                    user.getName(), updatedDevice.getDeviceName(), updatedDevice.getStatus(),
                    updatedDevice.getLastSeen(), updatedDevice.getIp());
            emailService.sendEmail(user.getEmail(), subject, body);
        }
    }

    public WeeklyActivityData getWeeklyDeviceActivity(String userId, LocalDateTime from, LocalDateTime to) {
        WeeklyActivityData data = new WeeklyActivityData();
        Map<String, Double> deviceActivity = new LinkedHashMap<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Initialize with zeros
        for (String day : days) {
            deviceActivity.put(day, 0.0);
        }

        // Populate with data from repository
        deviceRepository.getWeeklyStats(userId, from, to).forEach(stat -> {
            int dayOfWeek = stat.getId() - 1; // Adjust 1-7 to 0-6 index
            if (dayOfWeek >= 0 && dayOfWeek < 7) {
                deviceActivity.put(days[dayOfWeek], (double) stat.getDeviceHits());
            }
        });

        data.setDeviceActivity(deviceActivity);
        return data;
    }

    public WeeklyActivityData getWeeklyDeviceActivityForAllUsers(LocalDateTime from, LocalDateTime to) {
        WeeklyActivityData data = new WeeklyActivityData();
        Map<String, Double> deviceActivity = new LinkedHashMap<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Initialize with zeros
        for (String day : days) {
            deviceActivity.put(day, 0.0);
        }

        // Populate with data from repository
        deviceRepository.getWeeklyStatsForAllUsers(from, to).forEach(stat -> {
            int index = (stat.getId() + 5) % 7;
            deviceActivity.put(days[index], (double) stat.getDeviceHits());
        });

        data.setDeviceActivity(deviceActivity);
        return data;
    }

    public MonthlyActivityData getMonthlyDeviceActivityForAllUsers(LocalDateTime from, LocalDateTime to) {
        MonthlyActivityData data = new MonthlyActivityData();
        Map<String, Double> deviceActivity = new LinkedHashMap<>();
        Map<String, Double> storageGb = new LinkedHashMap<>();
        Map<String, Double> bandwidthGb = new LinkedHashMap<>();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        LocalDateTime currentMonth = from.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (!currentMonth.isAfter(to)) {
            String monthKey = currentMonth.format(monthFormatter);
            deviceActivity.put(monthKey, 0.0);
            storageGb.put(monthKey, 0.0);
            bandwidthGb.put(monthKey, 0.0);
            currentMonth = currentMonth.plusMonths(1);
        }

        deviceRepository.getMonthlyStatsForAllUsers(from, to).forEach(stat -> {
            LocalDateTime monthDate = LocalDateTime.of(stat.getYear(), stat.getMonth(), 1, 0, 0);
            String monthKey = monthDate.format(monthFormatter);

            if (deviceActivity.containsKey(monthKey)) {
                deviceActivity.put(monthKey, (double) stat.getDeviceHits());
                bandwidthGb.put(monthKey, 0.0); // No bandwidth data for devices
                storageGb.put(monthKey, 0.0); // No storage data for devices
            }
        });

        data.setContentPlays(deviceActivity);
        data.setBandwidthGb(bandwidthGb);
        data.setStorageGb(storageGb);
        return data;
    }
}
