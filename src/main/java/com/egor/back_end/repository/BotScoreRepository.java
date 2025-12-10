package com.egor.back_end.repository;

import com.egor.back_end.model.BotScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotScoreRepository extends JpaRepository<BotScore, Long> {
}
