package com.egor.back_end.dto.match;

public record MatchScoreDto(
        Long userId,
        String username,
        Integer score,
        boolean confirmed
) {}
