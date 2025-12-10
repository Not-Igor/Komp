package com.egor.back_end.dto.competition;

public record ParticipantDto(
        Long id,
        String username,
        Integer wins,
        Integer matchesPlayed,
        Integer draws,
        Integer losses,
        Integer pointsScored,
        Boolean isBot
) {
    // Constructor for users (no isBot parameter, defaults to false)
    public ParticipantDto(Long id, String username, Integer wins, Integer matchesPlayed, 
                         Integer draws, Integer losses, Integer pointsScored) {
        this(id, username, wins, matchesPlayed, draws, losses, pointsScored, false);
    }
}
