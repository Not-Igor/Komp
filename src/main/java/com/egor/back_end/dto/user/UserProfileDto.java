package com.egor.back_end.dto.user;

import com.egor.back_end.model.Role;

public record UserProfileDto(
        Long id,
        String username,
        String email,
        Role role,
        String avatarUrl
) {
}
