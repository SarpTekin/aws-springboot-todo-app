package com.microtodo.user_service.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.microtodo.user_service.dto.LoginRequest;
import com.microtodo.user_service.dto.LoginResponse;
import com.microtodo.user_service.model.User;
import com.microtodo.user_service.repository.UserRepository;
import com.microtodo.user_service.security.CustomUserDetails;
import com.microtodo.user_service.security.JwtService;

@Service

public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password());
        authenticationManager.authenticate(authToken);

        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails, user.getId());

        return new LoginResponse(token, user.getId(), user.getUsername());
    }
}