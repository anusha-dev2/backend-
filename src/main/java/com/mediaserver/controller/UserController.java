// UserController.java
package com.mediaserver.controller;

import com.mediaserver.model.User;
import com.mediaserver.service.UserService;
import com.mediaserver.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getUserCount() {
        if (!SecurityUtil.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        int count = userService.getAllUsers().size();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userService.isUsernameExists(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (userService.isEmailExists(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginUser) {
        Optional<User> user = userService.getUserByUsername(loginUser.getUsername());
        
        if (user.isPresent() && user.get().getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.ok(user.get());
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<User> updateUser(@PathVariable String id, @Valid @RequestBody User user) {
    //     Optional<User> existingUser = userService.getUserById(id);
        
    //     if (existingUser.isPresent()) {
    //         user.setId(id);
    //         return ResponseEntity.ok(userService.updateUser(user));
    //     } else {
    //         return ResponseEntity.notFound().build();

    @PutMapping("/{id}")
public ResponseEntity<User> updateUser(@PathVariable String id, @Valid @RequestBody User updatedUser) {
    Optional<User> existingUserOptional = userService.getUserById(id);
    
    if (existingUserOptional.isPresent()) {
        User existingUser = existingUserOptional.get();
        
        // Update only the provided fields
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getImageUrl() != null) {
            existingUser.setImageUrl(updatedUser.getImageUrl());
        }
        if (updatedUser.getEmailVerified() != null) {
            existingUser.setEmailVerified(updatedUser.getEmailVerified());
        }
        if (updatedUser.getProvider() != null) {
            existingUser.setProvider(updatedUser.getProvider());
        }
        if (updatedUser.getProviderId() != null) {
            existingUser.setProviderId(updatedUser.getProviderId());
        }
        if (updatedUser.getStripeCustomerId() != null) {
            existingUser.setStripeCustomerId(updatedUser.getStripeCustomerId());
        }
        if (updatedUser.getSubscriptionTier() != null) {
            existingUser.setSubscriptionTier(updatedUser.getSubscriptionTier());
        }
        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
       
        
        // Save the updated user
        User updatedUserEntity = userService.updateUser(existingUser);
        return ResponseEntity.ok(updatedUserEntity);
    } else {
        return ResponseEntity.notFound().build();
    }
}

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    //     Optional<User> user = userService.getUserById(id);
        
    //     if (user.isPresent()) {
    //         userService.deleteUser(id);
    //         return ResponseEntity.noContent().build();
    //     } else {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

    @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    Optional<User> userOpt = userService.getUserById(id);
    if (userOpt.isPresent()) {
        userService.markAccountForDeletion(userOpt.get().getUsername(), "User requested account deletion via API");
        return ResponseEntity.noContent().build();
    } else {
        return ResponseEntity.notFound().build();
    }
}
}
