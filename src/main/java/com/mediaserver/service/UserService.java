// UserService.java
package com.mediaserver.service;

import com.mediaserver.model.User;
import com.mediaserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mediaserver.payload.UserProfileDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;



    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUserIdExists(String userId) {
    return userRepository.existsById(userId);
}



public Optional<User> generatePasswordResetToken(String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) return Optional.empty();

    User user = userOpt.get();
    String token = UUID.randomUUID().toString();
    user.setResetPasswordToken(token);
    user.setResetPasswordTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES));
    return Optional.of(userRepository.save(user));
}

public boolean resetPassword(String token, String newRawPassword) {
    Optional<User> userOpt = userRepository.findByResetPasswordToken(token);
    if (userOpt.isEmpty()) return false;

    User user = userOpt.get();
    if (user.getResetPasswordTokenExpiry().isBefore(Instant.now())) return false;

    user.setPassword(passwordEncoder.encode(newRawPassword));
    user.setResetPasswordToken(null);
    user.setResetPasswordTokenExpiry(null);
    userRepository.save(user);
    return true;
}

// settings
// settings

@Transactional(readOnly = true)
    public UserProfileDto getFullProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Check if user is marked for deletion
        if (user.isAccountMarkedForDeletion()) {
            throw new IllegalStateException("Account is marked for deletion and cannot be accessed");
        }

        UserProfileDto dto = new UserProfileDto();
        // Map User Profile fields
        dto.setFirstName(user.getName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setJobTitle(user.getJobTitle());
        dto.setPhone(user.getPhone());
        dto.setTimezone(user.getTimezone());
        dto.setLanguage(user.getLanguage());
        dto.setAvatarUrl(user.getImageUrl());

        // Map Company Info fields
        dto.setCompanyName(user.getCompanyName());
        dto.setCompanyIndustry(user.getCompanyIndustry());
        dto.setCompanySize(user.getCompanySize());
        dto.setCompanyWebsite(user.getCompanyWebsite());
        dto.setCompanyLogoUrl(user.getCompanyLogoUrl());
        dto.setCompanyAddress(user.getCompanyAddress());
        dto.setCompanyCity(user.getCompanyCity());
        dto.setCompanyState(user.getCompanyState());
        dto.setCompanyZipCode(user.getCompanyZipCode());
        dto.setCompanyCountry(user.getCompanyCountry());
        
        return dto;
    }

    /**
     * Updates all profile and company data for a user.
     */
    @Transactional
    public UserProfileDto updateFullProfile(String username, UserProfileDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Check if user is marked for deletion
        if (user.isAccountMarkedForDeletion()) {
            throw new IllegalStateException("Account is marked for deletion and cannot be updated");
        }

        // Update User Profile fields
        user.setName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setJobTitle(dto.getJobTitle());
        user.setPhone(dto.getPhone());
        user.setTimezone(dto.getTimezone());
        user.setLanguage(dto.getLanguage());
        user.setImageUrl(dto.getAvatarUrl());

        // Update Company Info fields
        user.setCompanyName(dto.getCompanyName());
        user.setCompanyIndustry(dto.getCompanyIndustry());
        user.setCompanySize(dto.getCompanySize());
        user.setCompanyWebsite(dto.getCompanyWebsite());
        user.setCompanyLogoUrl(dto.getCompanyLogoUrl());
        user.setCompanyAddress(dto.getCompanyAddress());
        user.setCompanyCity(dto.getCompanyCity());
        user.setCompanyState(dto.getCompanyState());
        user.setCompanyZipCode(dto.getCompanyZipCode());
        user.setCompanyCountry(dto.getCompanyCountry());

        userRepository.save(user);
        return getFullProfile(username); // Return the updated full profile
    }

    /**
     * Updates the user's password.
     * Uses a Map to avoid creating a specific DTO.
     */
    @Transactional
    public void updateUserPassword(String username, Map<String, String> passwordRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if user is marked for deletion
        if (user.isAccountMarkedForDeletion()) {
            throw new IllegalStateException("Account is marked for deletion and password cannot be updated");
        }

        String currentPassword = passwordRequest.get("currentPassword");
        String newPassword = passwordRequest.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            throw new IllegalArgumentException("Request must contain 'currentPassword' and 'newPassword'");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Soft deletion methods

    /**
     * Marks a user account for deletion (soft delete).
     * The account will be retained for 15 days before permanent deletion.
     */
    @Transactional
    public void markAccountForDeletion(String username, String reason) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.isAccountMarkedForDeletion()) {
            throw new IllegalStateException("Account is already marked for deletion");
        }

        user.markForDeletion(reason);
        userRepository.save(user);
    }

    /**
     * Recovers a user account that was marked for deletion.
     * This is only possible within the 15-day retention period.
     */
    @Transactional
    public boolean recoverAccount(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        
        // Check if account is marked for deletion
        if (!user.isAccountMarkedForDeletion()) {
            throw new IllegalStateException("Account is not marked for deletion");
        }

        // Check if permanent deletion period has passed
        if (user.isPermanentDeletionDue()) {
            throw new IllegalStateException("Account recovery period has expired");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Recover the account
        user.recoverAccount();
        userRepository.save(user);
        return true;
    }

    /**
     * Checks if an account is marked for deletion.
     */
    @Transactional(readOnly = true)
    public boolean isAccountMarkedForDeletion(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        return userOpt.get().isAccountMarkedForDeletion();
    }

    /**
     * Gets the deletion status and remaining days for recovery.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAccountDeletionStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isAccountMarkedForDeletion()) {
            return Map.of(
                "isMarkedForDeletion", false,
                "deletionDate", null,
                "remainingDays", 0,
                "deletionReason", null
            );
        }

        // Calculate remaining days
        Instant deletionDate = user.getDeletionDate();
        Instant expiryDate = deletionDate.plus(15, ChronoUnit.DAYS);
        long remainingDays = ChronoUnit.DAYS.between(Instant.now(), expiryDate);
        
        return Map.of(
            "isMarkedForDeletion", true,
            "deletionDate", deletionDate,
            "remainingDays", Math.max(0, remainingDays),
            "deletionReason", user.getDeletionReason(),
            "isPermanentDeletionDue", user.isPermanentDeletionDue()
        );
    }

    /**
     * Permanently deletes accounts that have exceeded the 15-day retention period.
     * This method should be called by a scheduled job.
     */
    @Transactional
    public void permanentlyDeleteExpiredAccounts() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.isPermanentDeletionDue()) {
                // Here you would also delete related data (playlists, groups, devices, etc.)
                // For now, we just delete the user
                userRepository.deleteById(user.getId());
            }
        }
    }

    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    // Suspension methods
    @Transactional
    public void suspendUser(String userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isSuspended()) {
            throw new IllegalStateException("User is already suspended");
        }

        user.suspend(reason);
        userRepository.save(user);

        // Send email notification
        try {
            emailService.sendSuspensionNotification(user.getEmail(), user.getName(), reason);
        } catch (Exception e) {
            // Log error but don't fail the suspension
            System.err.println("Failed to send suspension email: " + e.getMessage());
        }
    }

    @Transactional
    public void unsuspendUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isSuspended() && !user.isInactive()) {
            throw new IllegalStateException("User is not suspended or inactive");
        }

        user.unsuspend();
        userRepository.save(user);

        // Send email notification
        try {
            emailService.sendUnsuspensionNotification(user.getEmail(), user.getName());
        } catch (Exception e) {
            // Log error but don't fail the unsuspension
            System.err.println("Failed to send unsuspension email: " + e.getMessage());
        }
    }

    @Transactional
    public void updateLastLoginDate(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setLastLoginDate(Instant.now());
        userRepository.save(user);
    }

    // Automatic suspension for inactive users (3+ months)
    @Transactional
    public void suspendInactiveUsers() {
        Instant threeMonthsAgo = Instant.now().minus(90, ChronoUnit.DAYS);
        List<User> inactiveUsers = userRepository.findAll().stream()
                .filter(user -> user.isActive())
                .filter(user -> user.getLastLoginDate() == null || user.getLastLoginDate().isBefore(threeMonthsAgo))
                .collect(Collectors.toList());

        for (User user : inactiveUsers) {
            user.markInactive();
            userRepository.save(user);

            try {
                emailService.sendInactivityNotification(user.getEmail(), user.getName());
            } catch (Exception e) {
                System.err.println("Failed to send inactivity email to " + user.getEmail() + ": " + e.getMessage());
            }
        }

        System.out.println("Suspended " + inactiveUsers.size() + " inactive users");
    }

    // Check if user is suspended or inactive
    public boolean isUserSuspendedOrInactive(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return user.isSuspended() || user.isInactive();
    }

    // Get suspension details
    public Map<String, Object> getUserSuspensionStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return Map.of(
            "isSuspended", user.isSuspended(),
            "isInactive", user.isInactive(),
            "status", user.getStatus(),
            "suspensionReason", user.getSuspensionReason(),
            "suspensionDate", user.getSuspensionDate(),
            "lastLoginDate", user.getLastLoginDate()
        );
    }
}
