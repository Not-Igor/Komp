package com.egor.back_end.dto.match;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MatchCreateDto(
        @NotNull Long competitionId,
        String title,
        @NotNull List<Long> participantIds
) {}
