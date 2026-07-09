package com.enterprise.inventory.dto;

import com.enterprise.inventory.entity.AppUser;
import com.enterprise.inventory.enums.UserRole;

public record CurrentUserResponse(
        Long id,
        String fullName,
        String email,
        UserRole role
) {
    public static CurrentUserResponse from(AppUser user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
