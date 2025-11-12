package com.microtodo.user_service.dto;

public record LoginResponse(
        String token,
        Long userId,
        String username
) {}