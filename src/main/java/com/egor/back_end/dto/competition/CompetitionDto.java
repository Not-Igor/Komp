package com.egor.back_end.dto.competition;

import com.egor.back_end.dto.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public record CompetitionDto(
        Long id,
        String title,
        String icon,
        UserDto creator,
        List<UserDto> participants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
