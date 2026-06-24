// DeviceController.java
package com.mediaserver.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.mediaserver.model.Playlist;
import com.mediaserver.model.User;
import com.mediaserver.payload.DeviceStatusUpdateRequest;
import com.mediaserver.payload.DeviceStatusUpdateResponse;
import com.mediaserver.repository.DeviceRepository;
import com.mediaserver.repository.PlaylistRepository;
import com.mediaserver.security.RootJwtTokenProvider;
import com.mediaserver.security.RootUserPrincipal;
import com.mediaserver.security.UserPrincipal;
import com.mediaserver.service.DeviceService;
import com.mediaserver.service.UserService;


@RestController
@RequestMapping("/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private RootJwtTokenProvider rootTokenProvider;

    // Updated authentication methods similar to PlaylistController
    private boolean isRootUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"));
        }
        return false;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        } else if (authentication != null && authentication.getPrincipal() instanceof RootUserPrincipal) {
            RootUserPrincipal rootUserPrincipal = (RootUserPrincipal) authentication.getPrincipal();
            return rootUserPrincipal.getId();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable String id) {
        Optional<Device> device = deviceService.getDeviceById(id);
        if (device.isPresent()) {
            if (!isRootUser() && !device.get().getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(device.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mac/{macAddress}")
    public ResponseEntity<Device> getDeviceByMacAddress(@PathVariable String macAddress) {
        Optional<Device> device = deviceService.getDeviceByMacAddress(macAddress);
        if (device.isPresent()) {
            if (!isRootUser() && !device.get().getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(device.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addDevice(@PathVariable String userId, @Valid @RequestBody Device device) {
        if (!isRootUser() && !userId.equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // if (deviceService.isMacAddressExists(device.getMacAddress())) {
        //     return ResponseEntity.badRequest().body("MAC address already registered");
        // }

        // Global MAC check removed - per-user check now in service
        
        Optional<User> user = userService.getUserById(userId);
        
        if (user.isPresent()) {
            device.setUserId(userId);
            device.setCurrentPlaylist(null);
            device.setLastSeen(LocalDateTime.now());
            String ip = device.getIp();
            device.setStatus((ip != null && !ip.isEmpty()) ? "online" : "offline");
            
            Device.Setting setting = new Device.Setting();
            setting.setAssignedGroup("default");
            setting.setCategories(List.of());
            setting.setTimezoneOfPlayer("UTC");
            setting.setPlayerNotes("");
            setting.setAdditionalPlaylists(List.of());
            setting.setLayoutMode("HORIZONTAL");
            device.setSetting(setting);

            Device createdDevice = deviceService.createDevice(device);

            deviceService.sendDeviceCreatedEmail(userId, createdDevice);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<Device> enableDevice(@PathVariable String id) {
        Optional<Device> existingDevice = deviceService.getDeviceById(id);
        
        if (existingDevice.isPresent()) {
            if (!isRootUser() && !existingDevice.get().getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Device device = existingDevice.get();
            device.setEnabled(true);
            return ResponseEntity.ok(deviceService.updateDevice(device));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<Device> disableDevice(@PathVariable String id) {
        Optional<Device> existingDevice = deviceService.getDeviceById(id);
        
        if (existingDevice.isPresent()) {
            if (!isRootUser() && !existingDevice.get().getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Device device = existingDevice.get();
            device.setEnabled(false);
            return ResponseEntity.ok(deviceService.updateDevice(device));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable String id) {
        Optional<Device> device = deviceService.getDeviceById(id);
        
        if (device.isPresent()) {
            if (!isRootUser() && !device.get().getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Device deletedDevice = device.get();
            String userId = deletedDevice.getUserId();
            deviceService.deleteDevice(id);

            deviceService.sendDeviceDeletedEmail(userId, deletedDevice);

            return ResponseEntity.ok("Device Deleted Successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Device>> getDevicesByUser(
            @PathVariable String userId,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String status) {
        
        if (!isRootUser() && !getCurrentUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Device> devices = deviceService.getDevicesByUserIdAndFilters(userId, deviceName, enabled, status);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/filter/deviceName/{deviceName}")
    public ResponseEntity<List<Device>> filterDevicesByDeviceName(@PathVariable String deviceName) {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Device> devices = deviceService.filterDevicesByDeviceName(deviceName);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/filter/deviceName/{deviceName}/enabled/{enabled}")
    public ResponseEntity<List<Device>> filterDevicesByDeviceNameAndEnabled(
            @PathVariable String deviceName, 
            @PathVariable Boolean enabled) {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Device> devices = deviceService.filterDevicesByDeviceNameAndEnabled(deviceName, enabled);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/filter/status/{status}")
    public ResponseEntity<List<Device>> filterDevicesByStatus(@PathVariable String status) {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Device> devices = deviceService.filterDevicesByStatus(status);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/filter/deviceName/{deviceName}/status/{status}/enabled/{enabled}")
    public ResponseEntity<List<Device>> filterDevicesByDeviceNameStatusAndEnabled(
            @PathVariable String deviceName, 
            @PathVariable String status, 
            @PathVariable Boolean enabled) {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Device> devices = deviceService.filterDevicesByDeviceNameStatusAndEnabled(deviceName, status, enabled);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/filter/status/{status}/location/{location}")
    public ResponseEntity<List<Device>> filterDevicesByStatusAndLocation(
            @PathVariable String status, 
            @PathVariable String location) {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Device> devices = deviceService.filterDevicesByStatusAndLocation(status, location);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/filter/deviceName/{deviceName}/location/{location}")
    public ResponseEntity<List<Device>> filterDevicesByDeviceNameAndLocation(
            @PathVariable String deviceName, 
            @PathVariable String location) {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Device> devices = deviceService.filterDevicesByDeviceNameAndLocation(deviceName, location);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/filter/{userId}/{value}")
    public ResponseEntity<List<Device>> filterDevicesByValue(
            @PathVariable String userId, 
            @PathVariable String value) {
        if (!isRootUser() && !getCurrentUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Device> devices = deviceService.filterDevicesByValue(userId, value);
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/user/{userId}/{deviceId}")
    public ResponseEntity<?> updateDevice(
            @PathVariable String userId,
            @PathVariable String deviceId,
            @Valid @RequestBody Device device) {
        
        if (!isRootUser() && !userId.equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Optional<Device> existingDeviceOpt = deviceService.getDeviceById(deviceId);
            if (!existingDeviceOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Device existingDevice = existingDeviceOpt.get();
            if (!userId.equals(existingDevice.getUserId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Device does not belong to the specified user"));
            }

            if (device.getIp() != null && "null".equalsIgnoreCase(device.getIp().trim())) {
                device.setIp(null);
            }

            device.setId(deviceId);
            Device updatedDevice = deviceService.updateDevice(device);

            deviceService.sendDeviceUpdatedEmail(userId, updatedDevice);

            return ResponseEntity.ok(updatedDevice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update device: " + e.getMessage()));
        }
    }

    @PutMapping("/{deviceId}/status")
    public ResponseEntity<?> updateDeviceStatus(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceStatusUpdateRequest statusRequest) {
        try {
            Optional<Device> deviceOpt = deviceService.getDeviceById(deviceId);
            if (!deviceOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Device device = deviceOpt.get();
            
            if (!isRootUser() && !device.getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            if (statusRequest.getStatus() != null) {
                device.setStatus(statusRequest.getStatus());
            }
            
            device.setLastSeen(LocalDateTime.now());
            
            if (statusRequest.getIpAddress() != null && !statusRequest.getIpAddress().isEmpty()) {
                device.setIp(statusRequest.getIpAddress());
            }
            
            if (statusRequest.getCurrentPlaylist() != null) {
                device.setCurrentPlaylist(statusRequest.getCurrentPlaylist());
            }
            
            if (statusRequest.getLocation() != null && !statusRequest.getLocation().isEmpty()) {
                device.setLocation(statusRequest.getLocation());
            }
            
            if (statusRequest.getConnectionType() != null && !statusRequest.getConnectionType().isEmpty()) {
                device.setConnectionType(statusRequest.getConnectionType());
            }
            
            if (statusRequest.getDeviceInfo() != null && !statusRequest.getDeviceInfo().isEmpty()) {
                device.setNotes(statusRequest.getDeviceInfo());
            }
            
            Device updatedDevice = deviceService.updateDevice(device);
            
            DeviceStatusUpdateResponse response = new DeviceStatusUpdateResponse(
                updatedDevice.getId(),
                updatedDevice.getStatus(),
                updatedDevice.getLastSeen().toString(),
                updatedDevice.getIp(),
                updatedDevice.getCurrentPlaylist(),
                updatedDevice.getLocation(),
                "Device status updated successfully"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update device status: " + e.getMessage()));
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<String> heartbeat(@RequestParam String deviceId) {
        Optional<Device> device = deviceService.getDeviceById(deviceId);
        if (device.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (!isRootUser() && !device.get().getUserId().equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        deviceService.updateLastSeen(deviceId);
        return ResponseEntity.ok("Heartbeat received for device: " + deviceId);
    }

    @GetMapping("/lastSeen")
    public ResponseEntity<String> getLastSeen(@RequestParam String deviceId) {
        Optional<Device> device = deviceService.getDeviceById(deviceId);
        if (device.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (!isRootUser() && !device.get().getUserId().equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String lastSeen = deviceService.getLastSeenAsString(deviceId);
        return ResponseEntity.ok(lastSeen);
    }

    @PutMapping("/{id}/heartbeat")
    public ResponseEntity<String> updateDeviceHeartbeat(@PathVariable String id) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            
            if (!isRootUser() && !device.getUserId().equals(getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            device.setLastSeen(LocalDateTime.now());
            device.setStatus("online");
            deviceRepository.save(device);
            return ResponseEntity.ok("Heartbeat received");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reboot")
    public ResponseEntity<Device> rebootDevice(@PathVariable String id) {
        Optional<Device> device = deviceService.getDeviceById(id);
        if (device.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (!isRootUser() && !device.get().getUserId().equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Device rebootedDevice = deviceService.rebootDevice(id);
            return ResponseEntity.ok(rebootedDevice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/resetRebootStatus")
    public ResponseEntity<Device> resetRebootStatus(@PathVariable String id) {
        Optional<Device> device = deviceService.getDeviceById(id);
        if (device.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (!isRootUser() && !device.get().getUserId().equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Device updatedDevice = deviceService.resetRebootStatus(id);
            return ResponseEntity.ok(updatedDevice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user/{userId}/{deviceId}/playlists/{playlistId}")
    public ResponseEntity<?> assignPlaylistToDevice(
            @PathVariable String userId,
            @PathVariable String deviceId,
            @PathVariable String playlistId) {

        if (!isRootUser() && !userId.equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        if (!device.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Device does not belong to user"));
        }

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));
        if (!playlist.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Playlist does not belong to user"));
        }

        List<String> assigned = device.getAssignedPlaylists();
        if (assigned == null) {
            assigned = new ArrayList<>();
            device.setAssignedPlaylists(assigned);
        }
        if (!assigned.contains(playlistId)) {
            assigned.add(playlistId);
            deviceRepository.save(device);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Playlist assigned to device",
                "deviceId", deviceId,
                "playlistId", playlistId));
    }

    @DeleteMapping("/user/{userId}/{deviceId}/playlists/{playlistId}")
    public ResponseEntity<?> unassignPlaylistFromDevice(
            @PathVariable String userId,
            @PathVariable String deviceId,
            @PathVariable String playlistId) {

        if (!isRootUser() && !userId.equals(getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        if (!device.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Device does not belong to user"));
        }

        List<String> assigned = device.getAssignedPlaylists();
        if (assigned != null && assigned.remove(playlistId)) {
            deviceRepository.save(device);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Playlist un-assigned from device",
                "deviceId", deviceId,
                "playlistId", playlistId));
    }

    @GetMapping("/status-counts")
    public ResponseEntity<Map<String, Long>> getDeviceStatusCounts() {
        if (!isRootUser()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Map<String, Long> statusCounts = deviceService.getDeviceStatusCountsForAll();
        return ResponseEntity.ok(statusCounts);
    }
}
