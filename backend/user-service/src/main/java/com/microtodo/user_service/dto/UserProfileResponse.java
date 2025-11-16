package com.microtodo.user_service.dto;

public record UserProfileResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName
) {}

