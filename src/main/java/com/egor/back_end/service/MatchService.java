package com.egor.back_end.service;

import com.egor.back_end.dto.match.MatchCreateDto;
import com.egor.back_end.dto.match.MatchDto;
import com.egor.back_end.dto.match.MatchScoreDto;
import com.egor.back_end.dto.match.SubmitScoresDto;
import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.model.*;
import com.egor.back_end.repository.CompetitionRepository;
import com.egor.back_end.repository.MatchRepository;
import com.egor.back_end.repository.MatchScoreRepository;
import com.egor.back_end.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;
    private final MatchScoreRepository matchScoreRepository;

    public MatchService(MatchRepository matchRepository, 
                       CompetitionRepository competitionRepository,
                       UserRepository userRepository,
                       MatchScoreRepository matchScoreRepository) {
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.matchScoreRepository = matchScoreRepository;
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

        // Auto-start match when created
        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setStartedAt(LocalDateTime.now());

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

    @Transactional
    public MatchDto submitScores(SubmitScoresDto dto, String username) {
        Match match = matchRepository.findById(dto.matchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!match.getCompetition().getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Only competition creator can submit scores");
        }

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new RuntimeException("Match must be in progress to submit scores");
        }

        // Clear existing scores
        match.getScores().clear();
        matchScoreRepository.flush();

        // Add new scores
        for (Map.Entry<Long, Integer> entry : dto.scores().entrySet()) {
            User participant = userRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!match.getParticipants().contains(participant)) {
                throw new RuntimeException("User is not a participant of this match");
            }

            MatchScore score = new MatchScore(match, participant, entry.getValue());
            match.getScores().add(score);
        }

        match.setScoresSubmitted(true);
        Match savedMatch = matchRepository.save(match);

        return toDto(savedMatch);
    }

    @Transactional
    public MatchDto confirmScores(Long matchId, String username) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!match.getParticipants().contains(user)) {
            throw new RuntimeException("Only participants can confirm scores");
        }

        if (!match.isScoresSubmitted()) {
            throw new RuntimeException("Scores have not been submitted yet");
        }

        // Find user's score and mark as confirmed
        MatchScore userScore = matchScoreRepository.findByMatchAndUser(match, user)
                .orElseThrow(() -> new RuntimeException("Score not found for user"));
        
        userScore.setConfirmed(true);
        matchScoreRepository.save(userScore);

        // Check if all participants have confirmed
        boolean allConfirmed = match.getScores().stream()
                .allMatch(MatchScore::isConfirmed);

        if (allConfirmed) {
            match.setStatus(MatchStatus.COMPLETED);
            matchRepository.save(match);
        }

        return toDto(match);
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

        List<MatchScoreDto> scores = match.getScores().stream()
                .map(score -> new MatchScoreDto(
                        score.getUser().getId(),
                        score.getUser().getUsername(),
                        score.getScore(),
                        score.isConfirmed()
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
                match.getUpdatedAt(),
                scores,
                match.isScoresSubmitted()
        );
    }
}
