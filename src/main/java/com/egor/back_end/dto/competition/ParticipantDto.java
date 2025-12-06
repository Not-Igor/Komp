package com.egor.back_end.dto.competition;

public record ParticipantDto(
        Long id,
        String username,
        Integer wins,
        Integer matchesPlayed,
        Integer draws,
        Integer losses,
        Integer pointsScored
) {}
