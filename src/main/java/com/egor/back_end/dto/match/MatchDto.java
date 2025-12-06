package com.egor.back_end.dto.match;

import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.model.MatchStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MatchDto(
        Long id,
        String title,
        Integer matchNumber,
        Long competitionId,
        List<UserDto> participants,
        MatchStatus status,
        LocalDateTime startedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
