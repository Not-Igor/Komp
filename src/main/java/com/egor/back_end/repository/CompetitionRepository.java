package com.egor.back_end.repository;

import com.egor.back_end.model.Competition;
import com.egor.back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    
    // Find all competitions created by a specific user
    List<Competition> findByCreatorOrderByCreatedAtDesc(User creator);
    
    // Find all competitions where user is a participant
    @Query("SELECT c FROM Competition c JOIN c.participants p WHERE p.id = :userId ORDER BY c.createdAt DESC")
    List<Competition> findByParticipantId(@Param("userId") Long userId);
    
    // Find all competitions where user is either creator or participant
    @Query("SELECT DISTINCT c FROM Competition c LEFT JOIN c.participants p WHERE c.creator.id = :userId OR p.id = :userId ORDER BY c.createdAt DESC")
    List<Competition> findAllByUserInvolvement(@Param("userId") Long userId);
    
    // Remove a participant from a competition using native query
    @Modifying
    @Query(value = "DELETE FROM competition_participants WHERE competition_id = :competitionId AND user_id = :userId", nativeQuery = true)
    void removeParticipant(@Param("competitionId") Long competitionId, @Param("userId") Long userId);
}
