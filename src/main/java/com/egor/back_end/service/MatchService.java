package com.egor.back_end.service;

import com.egor.back_end.dto.match.MatchCreateDto;
import com.egor.back_end.dto.match.MatchDto;
import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.model.*;
import com.egor.back_end.repository.CompetitionRepository;
import com.egor.back_end.repository.MatchRepository;
import com.egor.back_end.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;

    public MatchService(MatchRepository matchRepository, 
                       CompetitionRepository competitionRepository,
                       UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MatchDto createMatch(MatchCreateDto dto, String creatorUsername) {
        Competition competition = competitionRepository.findById(dto.competitionId())
                .orElseThrow(() -> new RuntimeException("Competition not found"));

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!competition.getCreator().getId().equals(creator.getId())) {
            throw new RuntimeException("Only competition creator can create matches");
        }

        Integer nextMatchNumber = matchRepository.countByCompetitionId(dto.competitionId()) + 1;
        String title = dto.title() != null && !dto.title().isBlank() 
                ? dto.title() 
                : "Match " + nextMatchNumber;

        Match match = new Match(title, competition, nextMatchNumber);

        Set<User> participants = new HashSet<>();
        for (Long userId : dto.participantIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            if (!competition.getParticipants().contains(user)) {
                throw new RuntimeException("User is not a participant of this competition");
            }
            participants.add(user);
        }
        match.setParticipants(participants);

        Match savedMatch = matchRepository.save(match);
        return toDto(savedMatch);
    }

    public List<MatchDto> getMatchesByCompetition(Long competitionId) {
        List<Match> matches = matchRepository.findByCompetitionId(competitionId);
        return matches.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MatchDto getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        return toDto(match);
    }

    @Transactional
    public MatchDto startMatch(Long matchId, String username) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!match.getCompetition().getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only competition creator can start matches");
        }

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new RuntimeException("Match already started or completed");
        }

        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setStartedAt(LocalDateTime.now());
        Match savedMatch = matchRepository.save(match);

        return toDto(savedMatch);
    }

    @Transactional
    public void deleteMatch(Long matchId, String username) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!match.getCompetition().getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only competition creator can delete matches");
        }

        matchRepository.delete(match);
    }

    private MatchDto toDto(Match match) {
        List<UserDto> participants = match.getParticipants().stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();

        return new MatchDto(
                match.getId(),
                match.getTitle(),
                match.getMatchNumber(),
                match.getCompetition().getId(),
                participants,
                match.getStatus(),
                match.getStartedAt(),
                match.getCreatedAt(),
                match.getUpdatedAt()
        );
    }
}
