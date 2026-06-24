package com.mediaserver.controller;

import com.mediaserver.model.AuthProvider;
import com.mediaserver.model.RootUser;
import com.mediaserver.payload.ApiResponse;
import com.mediaserver.payload.AuthResponse;
import com.mediaserver.payload.LoginRequest;
import com.mediaserver.payload.SignUpRequest;
import com.mediaserver.repository.RootUserRepository;
import com.mediaserver.security.RootJwtTokenProvider;

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

@RestController
@RequestMapping("/root/auth")
public class RootAuthController {

    @Autowired
    private RootUserRepository rootUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RootJwtTokenProvider rootTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateRootUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<RootUser> rootUserOpt = rootUserRepository.findByUsername(loginRequest.getUsername());

        if (rootUserOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid username or password"));
        }

        RootUser rootUser = rootUserOpt.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), rootUser.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid username or password"));
        }

        // Create authentication token manually
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(rootUser.getUsername(), null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = rootTokenProvider.generateToken(rootUser.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, rootUser.getUsername(), rootUser.getEmail(), "./Kmpslogo.jpg", rootUser.getId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerRootUser(@Valid @RequestBody SignUpRequest signUpRequest) {

        if (rootUserRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        if (rootUserRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email is already in use!"));
        }

        // Creating root user's account
        RootUser rootUser = new RootUser();
        rootUser.setName(signUpRequest.getName());
        rootUser.setUsername(signUpRequest.getUsername());
        rootUser.setEmail(signUpRequest.getEmail());
        rootUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        rootUser.setProvider(AuthProvider.LOCAL);

        RootUser result = rootUserRepository.save(rootUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/root/users/{id}")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Root user registered successfully"));
    }
}
