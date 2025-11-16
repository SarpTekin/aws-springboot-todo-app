package com.microtodo.user_service.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.microtodo.user_service.dto.ChangePasswordRequest;
import com.microtodo.user_service.dto.UpdateProfileRequest;
import com.microtodo.user_service.dto.UserProfileResponse;
import com.microtodo.user_service.dto.UserRequest;
import com.microtodo.user_service.dto.UserResponse;
import com.microtodo.user_service.model.User;
import com.microtodo.user_service.repository.UserRepository;
import com.microtodo.user_service.security.CurrentUser;
import com.microtodo.user_service.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 1) Public: Register
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }

    // 2) Public: Availability checks
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean available = !userRepository.existsByUsername(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = !userRepository.existsByEmail(email);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // 3) Authenticated: Me (profile)
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me() {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(toProfile(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.firstName() != null) user.setFirstName(req.firstName());
        if (req.lastName() != null) user.setLastName(req.lastName());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toProfile(saved));
    }

    // 4) Authenticated: Change password & delete account
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new SecurityException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // 5) Authenticated: Get user by id (same-user only for now)
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        Long requesterId = CurrentUser.getUserId();

        if (!id.equals(requesterId)) {
            throw new SecurityException("Forbidden");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(toProfile(user));
    }

    private static UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}