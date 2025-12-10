package com.egor.back_end.repository;

import com.egor.back_end.model.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
    List<Bot> findByCompetitionId(Long competitionId);
    void deleteByCompetitionId(Long competitionId);
    boolean existsByCompetitionIdAndUsername(Long competitionId, String username);
}
