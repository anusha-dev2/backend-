// // AnalyticsController.java (updated for weekly; add monthly endpoint to ContentController if needed)
// package com.mediaserver.controller;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.mediaserver.dto.WeeklyActivityData;
// import com.mediaserver.security.SecurityUtil;
// import com.mediaserver.service.ContentService;
// import com.mediaserver.service.DeviceService;
// @RestController
// @RequestMapping("/analytics")
// public class AnalyticsController {

//     @Autowired
//     private ContentService contentService;

//     @Autowired
//     private DeviceService deviceService;

//     @GetMapping("/user/{userId}/weekly-activity")
//     public ResponseEntity<WeeklyActivityData> getWeeklyActivity(@PathVariable String userId,
//                                                                @RequestParam(required = false) String from,
//                                                                @RequestParam(required = false) String to) {
//         if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
//             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//         }
//         try {
//             LocalDateTime now = LocalDateTime.now();
//             DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//             LocalDate startDate = from != null ? LocalDate.parse(from, dateFormatter) : now.toLocalDate().minusDays(6);
//             LocalDate endDate = to != null ? LocalDate.parse(to, dateFormatter) : now.toLocalDate();
//             LocalDateTime start = startDate.atStartOfDay();
//             LocalDateTime end = endDate.atTime(23, 59, 59, 999999999);
//             WeeklyActivityData contentData = contentService.getWeeklyContentActivity(userId, start, end);
//             WeeklyActivityData deviceData = deviceService.getWeeklyDeviceActivity(userId, start, end);

//             // Combine data
//             contentData.setDeviceActivity(deviceData.getDeviceActivity());
//             return ResponseEntity.ok(contentData);
//         } catch (Exception e) {
//             // Handle exception
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
// }

package com.mediaserver.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.dto.WeeklyActivityData;
import com.mediaserver.security.SecurityUtil;
import com.mediaserver.service.ContentService;
import com.mediaserver.service.DeviceService;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/user/{userId}/weekly-activity")
    public ResponseEntity<WeeklyActivityData> getWeeklyActivity(@PathVariable String userId,
                                                               @RequestParam(required = false) String from,
                                                               @RequestParam(required = false) String to) {
        if (!SecurityUtil.isRoot() && !userId.equals(SecurityUtil.currentUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = from != null ? LocalDate.parse(from, dateFormatter) : now.toLocalDate().minusDays(6);
        LocalDate endDate = to != null ? LocalDate.parse(to, dateFormatter) : now.toLocalDate();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59, 999999999);
        WeeklyActivityData contentData = contentService.getWeeklyContentActivity(userId, start, end);
        WeeklyActivityData deviceData = deviceService.getWeeklyDeviceActivity(userId, start, end);

        // Combine data
        contentData.setDeviceActivity(deviceData.getDeviceActivity());
        return ResponseEntity.ok(contentData);
    }

    @GetMapping("/weekly-activity")
    public ResponseEntity<Map<String, Object>> getWeeklyActivityForAllUsers(@RequestParam(required = false) String from,
                                                                             @RequestParam(required = false) String to) {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = from != null ? LocalDate.parse(from, dateFormatter) : now.toLocalDate().minusDays(6);
        LocalDate endDate = to != null ? LocalDate.parse(to, dateFormatter) : now.toLocalDate();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59, 999999999);
        WeeklyActivityData contentData = contentService.getWeeklyContentActivityForAllUsers(start, end);
        WeeklyActivityData deviceData = deviceService.getWeeklyDeviceActivityForAllUsers(start, end);

        // Combine data
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contentPlays", contentData.getContentPlays() != null ? contentData.getContentPlays() : new LinkedHashMap<>());
        result.put("deviceActivity", deviceData.getDeviceActivity() != null ? deviceData.getDeviceActivity() : new LinkedHashMap<>());
        return ResponseEntity.ok(result);
    }


}