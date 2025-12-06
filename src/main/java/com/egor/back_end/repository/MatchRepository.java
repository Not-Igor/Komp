package com.egor.back_end.repository;

import com.egor.back_end.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByCompetitionId(Long competitionId);
    Integer countByCompetitionId(Long competitionId);
}
