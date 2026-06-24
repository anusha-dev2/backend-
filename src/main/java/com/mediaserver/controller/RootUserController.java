package com.mediaserver.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediaserver.model.User;
import com.mediaserver.security.JwtTokenProvider;
import com.mediaserver.security.RootJwtTokenProvider;
import com.mediaserver.service.UserService;

@RestController
@RequestMapping("/root/users")
public class RootUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RootJwtTokenProvider rootTokenProvider;

    @Autowired
    private JwtTokenProvider userTokenProvider;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(HttpServletRequest request) {
        // Validate root token
        String rootToken = getRootTokenFromRequest(request);
        if (rootToken == null || !rootTokenProvider.validateToken(rootToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id, HttpServletRequest request) {
        // Validate root token
        String rootToken = getRootTokenFromRequest(request);
        boolean isRootTokenValid = rootToken != null && rootTokenProvider.validateToken(rootToken);

        // Validate user token
        String userToken = getUserTokenFromRequest(request);
        boolean isUserTokenValid = userToken != null && userTokenProvider.validateToken(userToken);

        // Allow access if root token is valid OR (user token is valid and matches the requested user)
        if (isRootTokenValid) {
            // Root user can access any user's data
        } else if (isUserTokenValid) {
            // Check if user token belongs to the requested user
            String usernameFromToken = userTokenProvider.getUsernameFromJWT(userToken);
            Optional<User> userFromToken = userService.getUserByUsername(usernameFromToken);
            if (!userFromToken.isPresent() || !userFromToken.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String getRootTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Root-Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getUserTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name, HttpServletRequest request) {
        // Validate root token
        String rootToken = getRootTokenFromRequest(request);
        if (rootToken == null || !rootTokenProvider.validateToken(rootToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.searchUsersByName(name));
    }
}
