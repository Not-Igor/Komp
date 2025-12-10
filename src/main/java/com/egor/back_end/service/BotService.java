package com.egor.back_end.service;

import com.egor.back_end.dto.bot.BotCreateDto;
import com.egor.back_end.dto.bot.BotDto;
import com.egor.back_end.model.Bot;
import com.egor.back_end.model.Competition;
import com.egor.back_end.repository.BotRepository;
import com.egor.back_end.repository.CompetitionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BotService {
    private final BotRepository botRepository;
    private final CompetitionRepository competitionRepository;

    public BotService(BotRepository botRepository, CompetitionRepository competitionRepository) {
        this.botRepository = botRepository;
        this.competitionRepository = competitionRepository;
    }

    public List<BotDto> getBotsByCompetition(Long competitionId) {
        return botRepository.findByCompetitionId(competitionId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BotDto> createBots(Long competitionId, BotCreateDto createDto) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new RuntimeException("Competition not found"));

        // Delete existing bots for this competition
        botRepository.deleteByCompetitionId(competitionId);

        // Create new bots
        List<Bot> bots = createDto.getUsernames().stream()
                .limit(createDto.getCount())
                .map(username -> new Bot(username, competition))
                .collect(Collectors.toList());

        List<Bot> savedBots = botRepository.saveAll(bots);

        return savedBots.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBotsByCompetition(Long competitionId) {
        botRepository.deleteByCompetitionId(competitionId);
    }

    private BotDto convertToDto(Bot bot) {
        return new BotDto(bot.getId(), bot.getUsername());
    }
}
