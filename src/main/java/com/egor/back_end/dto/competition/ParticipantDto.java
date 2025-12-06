package com.egor.back_end.dto.competition;

public record ParticipantDto(
        Long id,
        String username,
        Integer score
) {}
