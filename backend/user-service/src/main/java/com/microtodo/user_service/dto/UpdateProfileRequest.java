package com.microtodo.user_service.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 50) String firstName,
    @Size(max = 50) String lastName
) {}

