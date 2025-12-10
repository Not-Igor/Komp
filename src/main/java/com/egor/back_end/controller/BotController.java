package com.egor.back_end.controller;

import com.egor.back_end.dto.bot.BotCreateDto;
import com.egor.back_end.dto.bot.BotDto;
import com.egor.back_end.service.BotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions/{competitionId}/bots")
public class BotController {
    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BotDto>> getBots(@PathVariable Long competitionId) {
        List<BotDto> bots = botService.getBotsByCompetition(competitionId);
        return ResponseEntity.ok(bots);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BotDto>> createBots(
            @PathVariable Long competitionId,
            @Valid @RequestBody BotCreateDto createDto) {
        List<BotDto> bots = botService.createBots(competitionId, createDto);
        return ResponseEntity.ok(bots);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBots(@PathVariable Long competitionId) {
        botService.deleteBotsByCompetition(competitionId);
        return ResponseEntity.noContent().build();
    }
}
