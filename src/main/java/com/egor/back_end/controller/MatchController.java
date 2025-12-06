package com.egor.back_end.controller;

import com.egor.back_end.dto.match.MatchCreateDto;
import com.egor.back_end.dto.match.MatchDto;
import com.egor.back_end.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchDto> createMatch(
            @Valid @RequestBody MatchCreateDto dto,
            Authentication authentication) {
        try {
            MatchDto match = matchService.createMatch(dto, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(match);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<List<MatchDto>> getMatchesByCompetition(@PathVariable Long competitionId) {
        List<MatchDto> matches = matchService.getMatchesByCompetition(competitionId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDto> getMatch(@PathVariable Long matchId) {
        try {
            MatchDto match = matchService.getMatch(matchId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{matchId}/start")
    public ResponseEntity<MatchDto> startMatch(
            @PathVariable Long matchId,
            Authentication authentication) {
        try {
            MatchDto match = matchService.startMatch(matchId, authentication.getName());
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteMatch(
            @PathVariable Long matchId,
            Authentication authentication) {
        try {
            matchService.deleteMatch(matchId, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
