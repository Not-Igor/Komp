package com.egor.back_end.service;

import com.egor.back_end.dto.competition.CompetitionCreateDto;
import com.egor.back_end.dto.competition.CompetitionDto;
import com.egor.back_end.dto.competition.ParticipantDto;
import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.model.*;
import com.egor.back_end.repository.CompetitionRepository;
import com.egor.back_end.repository.MatchRepository;
import com.egor.back_end.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CompetitionService {
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final NotificationService notificationService;

    public CompetitionService(CompetitionRepository competitionRepository, 
                            UserRepository userRepository,
                            MatchRepository matchRepository,
                            NotificationService notificationService) {
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public CompetitionDto createCompetition(Long creatorId, CompetitionCreateDto dto) {
        // Find creator
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        // Create competition
        Competition competition = new Competition(dto.title(), dto.icon(), creator);

        // Add participants
        Set<User> participants = new HashSet<>();
        for (Long participantId : dto.participantIds()) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new IllegalArgumentException("Participant with ID " + participantId + " not found"));
            participants.add(participant);
        }
        
        // Also add creator as participant
        participants.add(creator);
        competition.setParticipants(participants);

        // Save competition
        Competition savedCompetition = competitionRepository.save(competition);

        return toDto(savedCompetition);
    }

    @Transactional
    public CompetitionDto createCompetitionByUsername(String username, CompetitionCreateDto dto) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        return createCompetition(creator.getId(), dto);
    }

    public CompetitionDto getCompetitionById(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found"));
        return toDto(competition);
    }

    public List<CompetitionDto> getAllCompetitionsByUser(Long userId) {
        return competitionRepository.findAllByUserInvolvement(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<CompetitionDto> getCompetitionsCreatedByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return competitionRepository.findByCreatorOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ParticipantDto> getParticipants(Long competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found"));
        
        // Get all completed matches for this competition
        List<Match> matches = matchRepository.findByCompetitionId(competitionId).stream()
                .filter(match -> match.getStatus() == MatchStatus.COMPLETED)
                .toList();
        
        // Initialize statistics for each participant
        Map<Long, ParticipantStats> statsMap = new HashMap<>();
        for (User participant : competition.getParticipants()) {
            statsMap.put(participant.getId(), new ParticipantStats(participant.getId(), participant.getUsername()));
        }
        
        // Calculate statistics from matches
        for (Match match : matches) {
            if (match.getScores().isEmpty()) continue;
            
            // Get all participants in this match
            Set<Long> matchParticipantIds = match.getParticipants().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            
            // Find highest score
            Integer highestScore = match.getScores().stream()
                    .map(MatchScore::getScore)
                    .max(Integer::compareTo)
                    .orElse(0);
            
            // Find all participants with highest score (for draws)
            List<Long> winners = match.getScores().stream()
                    .filter(score -> score.getScore().equals(highestScore))
                    .map(score -> score.getUser().getId())
                    .toList();
            
            boolean isDraw = winners.size() > 1;
            
            // Update stats for each participant in the match
            for (MatchScore score : match.getScores()) {
                Long userId = score.getUser().getId();
                ParticipantStats stats = statsMap.get(userId);
                if (stats != null) {
                    stats.matchesPlayed++;
                    stats.pointsScored += score.getScore();
                    
                    if (isDraw) {
                        stats.draws++;
                    } else if (winners.contains(userId)) {
                        stats.wins++;
                    } else {
                        stats.losses++;
                    }
                }
            }
        }
        
        // Create DTOs sorted by wins descending
        return statsMap.values().stream()
                .map(stats -> new ParticipantDto(
                        stats.userId,
                        stats.username,
                        stats.wins,
                        stats.matchesPlayed,
                        stats.draws,
                        stats.losses,
                        stats.pointsScored
                ))
                .sorted((a, b) -> Integer.compare(b.wins(), a.wins()))
                .collect(Collectors.toList());
    }
    
    // Helper class to track participant statistics
    private static class ParticipantStats {
        Long userId;
        String username;
        int wins = 0;
        int matchesPlayed = 0;
        int draws = 0;
        int losses = 0;
        int pointsScored = 0;
        
        ParticipantStats(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    @Transactional
    public void deleteCompetition(Long competitionId, Long userId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found"));
        
        // Only creator can delete
        if (!competition.getCreator().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the creator can delete this competition");
        }
        
        competitionRepository.delete(competition);
    }

    @Transactional
    public void deleteCompetitionByUsername(Long competitionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        deleteCompetition(competitionId, user.getId());
    }

    @Transactional
    public CompetitionDto addParticipants(Long competitionId, List<Long> participantIds, String username) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found"));
        
        User requestingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if the user is a participant in this competition
        if (!competition.getParticipants().contains(requestingUser)) {
            throw new IllegalArgumentException("Only participants can add friends to this competition");
        }
        
        // Add new participants
        Set<User> participants = competition.getParticipants();
        for (Long participantId : participantIds) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new IllegalArgumentException("Participant with ID " + participantId + " not found"));
            participants.add(participant);
        }
        competition.setParticipants(participants);
        
        Competition savedCompetition = competitionRepository.save(competition);
        return toDto(savedCompetition);
    }

    @Transactional
    public void leaveCompetition(Long competitionId, String username) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user is the creator
        if (competition.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("The creator cannot leave the competition. Delete it instead.");
        }
        
        // Get participants before leaving
        Set<User> participantsBeforeLeaving = new HashSet<>(competition.getParticipants());

        // Check if user is a participant
        boolean isParticipant = participantsBeforeLeaving.stream()
                .anyMatch(p -> p.getId().equals(user.getId()));
        
        if (!isParticipant) {
            throw new IllegalArgumentException("You are not a participant in this competition");
        }
        
        // Remove user from participants using native query to avoid Hibernate issues
        competitionRepository.removeParticipant(competitionId, user.getId());
        competitionRepository.flush(); // Ensure the removal is persisted before notifying

        // Notify remaining participants
        for (User participant : participantsBeforeLeaving) {
            if (!participant.getId().equals(user.getId())) {
                String message = String.format("%s has left the competition %s", user.getUsername(), competition.getTitle());
                notificationService.createNotification(
                    participant, 
                    NotificationType.USER_LEFT_COMPETITION, 
                    message,
                    competition.getId()
                );
            }
        }
    }

    private CompetitionDto toDto(Competition competition) {
        UserDto creatorDto = new UserDto(
                competition.getCreator().getId(),
                competition.getCreator().getUsername(),
                competition.getCreator().getEmail(),
                competition.getCreator().getRole()
        );

        List<UserDto> participantDtos = competition.getParticipants().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());

        return new CompetitionDto(
                competition.getId(),
                competition.getTitle(),
                competition.getIcon(),
                creatorDto,
                participantDtos,
                competition.getCreatedAt(),
                competition.getUpdatedAt()
        );
    }
}
