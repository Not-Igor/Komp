package com.egor.back_end.repository;

import com.egor.back_end.model.Match;
import com.egor.back_end.model.MatchScore;
import com.egor.back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchScoreRepository extends JpaRepository<MatchScore, Long> {
    List<MatchScore> findByMatch(Match match);
    Optional<MatchScore> findByMatchAndUser(Match match, User user);
}
