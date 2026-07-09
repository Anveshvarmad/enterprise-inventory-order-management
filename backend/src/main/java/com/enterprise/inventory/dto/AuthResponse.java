package com.enterprise.inventory.dto;

import com.enterprise.inventory.enums.UserRole;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String fullName,
        String email,
        UserRole role
) {
}
