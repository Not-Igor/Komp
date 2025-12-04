package com.egor.back_end.service;

import com.egor.back_end.dto.competition.CompetitionCreateDto;
import com.egor.back_end.dto.competition.CompetitionDto;
import com.egor.back_end.dto.competition.ParticipantDto;
import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.model.Competition;
import com.egor.back_end.model.User;
import com.egor.back_end.repository.CompetitionRepository;
import com.egor.back_end.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompetitionService {
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;

    public CompetitionService(CompetitionRepository competitionRepository, UserRepository userRepository) {
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
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
        
        return competition.getParticipants().stream()
                .map(user -> new ParticipantDto(user.getId(), user.getUsername(), 0)) // TODO: Add actual score
                .collect(Collectors.toList());
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
