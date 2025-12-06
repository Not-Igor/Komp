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

@Service
public class CompetitionService {
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    public CompetitionService(CompetitionRepository competitionRepository, 
                            UserRepository userRepository,
                            MatchRepository matchRepository) {
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
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
        
        // Calculate wins for each participant
        Map<Long, Integer> winCounts = new HashMap<>();
        for (User participant : competition.getParticipants()) {
            winCounts.put(participant.getId(), 0);
        }
        
        // Count wins
        for (Match match : matches) {
            // Find the winner (participant with highest score)
            Long winnerId = findMatchWinner(match);
            if (winnerId != null) {
                winCounts.put(winnerId, winCounts.getOrDefault(winnerId, 0) + 1);
            }
        }
        
        // Create DTOs with win counts, sorted by wins descending
        return competition.getParticipants().stream()
                .map(user -> new ParticipantDto(
                        user.getId(), 
                        user.getUsername(), 
                        winCounts.getOrDefault(user.getId(), 0)
                ))
                .sorted((a, b) -> Integer.compare(b.score(), a.score())) // Sort by score descending
                .collect(Collectors.toList());
    }
    
    private Long findMatchWinner(Match match) {
        if (match.getScores().isEmpty()) {
            return null;
        }
        
        // Find the participant with the highest score
        return match.getScores().stream()
                .max(Comparator.comparing(MatchScore::getScore))
                .map(score -> score.getUser().getId())
                .orElse(null);
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
