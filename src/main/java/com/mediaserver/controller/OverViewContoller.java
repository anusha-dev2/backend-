// OverView.java
package com.mediaserver.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.model.OverView;
import com.mediaserver.model.User;
import com.mediaserver.security.SecurityUtil;
import com.mediaserver.service.DeviceService;
import com.mediaserver.service.OverViewService;
import com.mediaserver.service.UserService;

@RestController
@RequestMapping("/overview")
public class OverViewContoller {

    // @Autowired 
    // private GroupDeviceService groupDeviceService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private OverViewService overViewService;
    

    @Autowired
    private UserService userService;

    @GetMapping("/summary/{userId}")
    public ResponseEntity<OverView> getTheDeviceCount(@PathVariable String userId) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Optional<User> user = userService.getUserById(userId);

            OverView overView = new OverView(overViewService.getTheDeviceCount(userId), overViewService.getTheOnlineDeviceCount("online"), overViewService.getTheMediaCount(userId), overViewService.getThePlayListCount(userId));

            if (user.isPresent()) {
                return ResponseEntity.ok(overView);
            } else {
                return ResponseEntity.notFound().build();
                //   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found with ID: " + userId);
            }
        } catch (Exception e) {
            // Handle exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/device-stats/{userId}")
    public ResponseEntity<Map<String, Long>> getDeviceStats(@PathVariable String userId)
    {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
        Map<String, Long> statusCounts = deviceService.getDeviceStatusCounts(userId);
        return ResponseEntity.ok(statusCounts);
        } catch (Exception e) {
            // Handle exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/summary/all")
    public ResponseEntity<OverView> getAllUsersOverview() {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            OverView overView = new OverView(overViewService.getTotalDeviceCount(), overViewService.getTotalOnlineDeviceCount(), overViewService.getTotalMediaCount(), overViewService.getTotalPlayListCount());
            return ResponseEntity.ok(overView);
        } catch (Exception e) {
            // Handle exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // @GetMapping("/DeviceStatus/{userId}")
    // public ResponseEntity<OverView.DevicesStatus> getTheDevicestatus(@PathVariable String userId) {

    //     Optional<User> user = userService.getUserById(userId);

    //     int onlineDevices = overViewService.getTheDevicestatus(userId, "online");
    //     int offileDevices = overViewService.getTheDevicestatus(userId, "offile");
    //     int warningDevices = overViewService.getTheDevicestatus(userId, "warning");

    //     // OverView.DevicesStatus devicesStatus = new OverView.DevicesStatusList(onlineDevices, offileDevices, warningDevices);

    //     OverView.DevicesStatus devicesStatus = new OverView.DevicesStatus(onlineDevices, offileDevices, warningDevices);

    //         if (user.isPresent()) {
    //                     return ResponseEntity.ok(devicesStatus);
    //                 } else {
    //                     return ResponseEntity.notFound().build();
    //                 }
    //             }
}
