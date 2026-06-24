package com.mediaserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserSuspensionScheduler {

    @Autowired
    private UserService userService;

    /**
     * Scheduled job to automatically suspend inactive users.
     * Runs daily at 2 AM.
     * Suspends users who haven't logged in for more than 3 months.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2:00 AM
    public void suspendInactiveUsers() {
        try {
            userService.suspendInactiveUsers();
        } catch (Exception e) {
            System.err.println("Error in scheduled user suspension job: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
