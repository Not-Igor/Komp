package com.egor.back_end.dto.competition;

public record CompetitionParticipantDto(
        Long id,
        String username,
        Boolean isBot
) {}
