package com.mediaserver.controller;

import com.mediaserver.dto.GoogleAuthRequest;
import com.mediaserver.model.AuthProvider;
import com.mediaserver.model.User;
import com.mediaserver.payload.ApiResponse;
import com.mediaserver.payload.AuthResponse;
import com.mediaserver.payload.ForgotPasswordRequest;
import com.mediaserver.payload.LoginRequest;
import com.mediaserver.payload.RecoverAccountRequest;
import com.mediaserver.payload.ResetPasswordRequest;
import com.mediaserver.payload.SignUpRequest;
import com.mediaserver.repository.UserRepository;
import com.mediaserver.security.JwtTokenProvider;
import com.mediaserver.service.UserService;
import com.mediaserver.service.DeviceService;
import com.mediaserver.service.EmailService;
import com.mediaserver.service.SubscriptionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import com.mediaserver.model.Device;
import com.mediaserver.service.DeviceService;
import com.mediaserver.payload.DeviceAuthRequest;
import com.mediaserver.payload.DeviceAuthResponse;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid username or password"));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid username or password"));
        }

        // Check if user is suspended or inactive
        if (user.isSuspended()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Your account has been suspended. Please contact support for assistance."));
        }

        if (user.isInactive()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Your account is inactive due to prolonged inactivity. Please contact support to reactivate."));
        }

        // Update last login date
        userService.updateLastLoginDate(user.getUsername());

        // Create authentication token manually
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user.getUsername(), null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getEmail(), "./Kmpslogo.jpg", user.getId()));
    }

    @PostMapping("/googletoken")
    public ResponseEntity<?> authenticateUserGoogleToken(@RequestBody GoogleAuthRequest request) {
        String email = request.getEmail();
        String username = request.getUsername();
        Boolean isGoogleUser = request.getIsGoogleUser();
        String token = request.getToken();

        // Check if email already exists
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // Ensure the user is a Google user
            if (existingUser.getProvider() != AuthProvider.GOOGLE) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email is already in use with a different provider!"));
            }
            // Authenticate existing user
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(existingUser.getUsername(), null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String tokenGoogle = tokenProvider.generateToken(existingUser.getUsername());
            return ResponseEntity.ok(new AuthResponse(tokenGoogle, existingUser.getUsername(), existingUser.getEmail(), "./Kmpslogo.jpg", existingUser.getId()));
        }

        // Check if username already exists (only for new users)
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        // Create new user
        User user = new User();
        user.setName(username);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(username)); // ⚠ replace this with secure handling
        user.setProvider(isGoogleUser ? AuthProvider.GOOGLE : AuthProvider.LOCAL);

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{id}")
                .buildAndExpand(result.getId()).toUri();

        // Create authentication token manually
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String tokenGoogle = tokenProvider.generateToken(username);

        return ResponseEntity.ok(new AuthResponse(tokenGoogle, username, email, "./Kmpslogo.jpg", result.getId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email is already in use!"));
        }

        // Creating user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProvider(AuthProvider.LOCAL);

        User result = userRepository.save(user);

        // ✅ Create FREE trial subscription for new user
        try {
            subscriptionService.createFreeTrialSubscription(result.getId());
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to create free trial subscription for user " + result.getId() + ": " + e.getMessage());
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{id}")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest dto) {
        Optional<User> userOpt = userService.generatePasswordResetToken(dto.getEmail());

        if (userOpt.isEmpty()) {
            // We don't reveal if the email exists for security reasons.
            return ResponseEntity.ok(new ApiResponse(true, "If an account with this email exists, a password reset link has been sent."));
        }

        User user = userOpt.get();
        String token = user.getResetPasswordToken();

        // Construct the password reset link
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;

        // Create the email body
        String emailBody = "Hello " + user.getName() + ",\n\n"
                         + "You have requested to reset your password. Please click the link below to proceed:\n"
                         + resetUrl + "\n\n"
                         + "If you did not request this, please ignore this email.\n\n"
                         + "Thanks,\nKMPS Team";

        // Send the email
        emailService.sendEmail(user.getEmail(), "Password Reset Request", emailBody);

        return ResponseEntity.ok(new ApiResponse(true, "A password reset link has been sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest dto) {
        boolean success = userService.resetPassword(dto.getToken(), dto.getNewPassword());
        if (!success) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid or expired token. Please try resetting your password again."));
        }
        return ResponseEntity.ok(new ApiResponse(true, "Your password has been updated successfully."));
    }

    @PostMapping("/device")
    public ResponseEntity<?> authenticateDevice(@Valid @RequestBody DeviceAuthRequest deviceAuthRequest) {
        try {
            Optional<Device> deviceOpt = deviceService.getDeviceByMacAddress(deviceAuthRequest.getMacAddress());
            if (!deviceOpt.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Device not found with MAC address: " + deviceAuthRequest.getMacAddress()));
            }
            Device device = deviceOpt.get();
            if (!device.isEnabled()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Device is disabled"));
            }
            // Update status and lastSeen correctly
            device.setStatus("online");
            device.setLastSeen(LocalDateTime.now());

            // Optional IP update
            if (deviceAuthRequest.getIpAddress() != null && !deviceAuthRequest.getIpAddress().isEmpty()) {
                device.setIp(deviceAuthRequest.getIpAddress());
            }
            deviceService.updateDevice(device);
            String deviceToken = tokenProvider.generateDeviceToken(device.getMacAddress(), device.getId());
            DeviceAuthResponse response = new DeviceAuthResponse(
                deviceToken,
                device.getId(),
                device.getDeviceName(),
                device.getMacAddress(),
                device.getStatus(),
                device.getCurrentPlaylist(),
                device.getSetting()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Device authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/recover-account")
    public ResponseEntity<?> recoverAccount(@Valid @RequestBody RecoverAccountRequest request) {
        try {
            boolean success = userService.recoverAccount(request.getEmail(), request.getPassword());
            if (!success) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Account recovery failed. Invalid credentials."));
            }
            return ResponseEntity.ok(new ApiResponse(true, "Account recovered successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Account recovery failed: " + e.getMessage()));
        }
    }
}
