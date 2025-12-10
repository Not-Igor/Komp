package com.egor.back_end.dto.user;

import com.egor.back_end.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDto(
        Long id,
        String username,
        String email,
        Role role,
        Boolean isBot
) {
    public UserDto(Long id, String username, String email, Role role) {
        this(id, username, email, role, role == Role.BOT);
    }
}

