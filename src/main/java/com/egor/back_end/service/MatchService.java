package com.egor.back_end.service;

import com.egor.back_end.dto.match.MatchCreateDto;
import com.egor.back_end.dto.match.MatchDto;
import com.egor.back_end.dto.match.MatchScoreDto;
import com.egor.back_end.dto.match.SubmitScoresDto;
import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.model.*;
import com.egor.back_end.repository.BotRepository;
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
    private final NotificationService notificationService;
    private final BotRepository botRepository;

    public MatchService(MatchRepository matchRepository, 
                       CompetitionRepository competitionRepository,
                       UserRepository userRepository,
                       MatchScoreRepository matchScoreRepository,
                       NotificationService notificationService,
                       BotRepository botRepository) {
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.matchScoreRepository = matchScoreRepository;
        this.notificationService = notificationService;
        this.botRepository = botRepository;
    }

    @Transactional
    public MatchDto createMatch(MatchCreateDto dto, String creatorUsername) {
        Competition competition = competitionRepository.findById(dto.competitionId())
                .orElseThrow(() -> new RuntimeException("Competition not found"));

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is a participant of the competition
        if (!competition.getParticipants().contains(creator)) {
            throw new RuntimeException("Only competition participants can create matches");
        }

        Integer nextMatchNumber = matchRepository.countByCompetitionId(dto.competitionId()) + 1;
        String title = dto.title() != null && !dto.title().isBlank() 
                ? dto.title() 
                : "Match " + nextMatchNumber;

        Match match = new Match(title, competition, nextMatchNumber);

        Set<User> participants = new HashSet<>();
        Set<Bot> botParticipants = new HashSet<>();
        
        for (Long participantId : dto.participantIds()) {
            if (participantId < 0) {
                // Negative ID means it's a bot
                Long botId = Math.abs(participantId);
                Bot bot = botRepository.findById(botId)
                        .orElseThrow(() -> new RuntimeException("Bot not found: " + botId));
                
                if (!bot.getCompetition().getId().equals(competition.getId())) {
                    throw new RuntimeException("Bot is not part of this competition");
                }
                botParticipants.add(bot);
            } else {
                // Positive ID means it's a user
                User user = userRepository.findById(participantId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + participantId));
                
                if (!competition.getParticipants().contains(user)) {
                    throw new RuntimeException("User is not a participant of this competition");
                }
                participants.add(user);
            }
        }
        
        match.setParticipants(participants);
        match.setBotParticipants(botParticipants);

        // Auto-start match when created
        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setStartedAt(LocalDateTime.now());

        Match savedMatch = matchRepository.save(match);
        
        // Notify all participants (except creator) about new match
        for (User participant : participants) {
            if (!participant.getId().equals(creator.getId())) {
                notificationService.createNotification(
                    participant,
                    NotificationType.MATCH_CREATED,
                    creator.getUsername() + " created a new match: " + title + " in " + competition.getTitle(),
                    savedMatch.getId()
                );
            }
        }
        
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

        // Any participant can submit scores
        if (!match.getParticipants().contains(user)) {
            throw new RuntimeException("Only participants can submit scores");
        }

        if (match.getStatus() != MatchStatus.IN_PROGRESS && match.getStatus() != MatchStatus.COMPLETED) {
            throw new RuntimeException("Cannot submit scores for this match");
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
            score.setConfirmed(true); // Auto-confirm since anyone can submit
            match.getScores().add(score);
        }

        match.setScoresSubmitted(true);
        match.setStatus(MatchStatus.COMPLETED);
        Match savedMatch = matchRepository.save(match);

        return toDto(savedMatch);
    }

    private MatchDto toDto(Match match) {
        List<UserDto> participants = new ArrayList<>();
        
        // Add regular user participants
        match.getParticipants().stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .forEach(participants::add);
        
        // Add bot participants with Role.BOT
        match.getBotParticipants().stream()
                .map(bot -> new UserDto(
                        bot.getId(),
                        bot.getUsername(),
                        null,
                        Role.BOT
                ))
                .forEach(participants::add);

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
