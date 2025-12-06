package com.egor.back_end.dto.match;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitScoresDto(
        @NotNull Long matchId,
        @NotNull Map<Long, Integer> scores  // userId -> score
) {}
