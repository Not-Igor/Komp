package com.egor.back_end.controller;

import com.egor.back_end.dto.competition.CompetitionCreateDto;
import com.egor.back_end.dto.competition.CompetitionDto;
import com.egor.back_end.dto.competition.ParticipantDto;
import com.egor.back_end.service.CompetitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competitions")
public class CompetitionController {
    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @PostMapping
    public ResponseEntity<CompetitionDto> createCompetition(
            @Valid @RequestBody CompetitionCreateDto dto,
            Authentication authentication) {
        String username = authentication.getName();
        
        CompetitionDto competition = competitionService.createCompetitionByUsername(username, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(competition);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionDto> getCompetition(@PathVariable Long id) {
        CompetitionDto competition = competitionService.getCompetitionById(id);
        return ResponseEntity.ok(competition);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CompetitionDto>> getUserCompetitions(@PathVariable Long userId) {
        List<CompetitionDto> competitions = competitionService.getAllCompetitionsByUser(userId);
        return ResponseEntity.ok(competitions);
    }

    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<CompetitionDto>> getCompetitionsCreatedByUser(@PathVariable Long userId) {
        List<CompetitionDto> competitions = competitionService.getCompetitionsCreatedByUser(userId);
        return ResponseEntity.ok(competitions);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantDto>> getParticipants(@PathVariable Long id) {
        List<ParticipantDto> participants = competitionService.getParticipants(id);
        return ResponseEntity.ok(participants);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetition(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        
        competitionService.deleteCompetitionByUsername(id, username);
        return ResponseEntity.noContent().build();
    }
}
