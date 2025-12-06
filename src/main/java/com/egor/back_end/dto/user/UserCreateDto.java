package com.egor.back_end.dto.user;

import com.egor.back_end.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateDto(@NotBlank String username,
                            @NotBlank String password
) { }
