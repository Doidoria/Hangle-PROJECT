package com.example.demo.domain.competition.repository;

import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CompetitionCSVSaveRepository extends JpaRepository<CompetitionCSVSave, Long> {
    // 중복 제출 제한
//    boolean existsByCompetitionIdAndUserid(Long competitionId, String userid);
    // 중복 제출 횟수 제한
long countByUseridAndCompetitionIdAndSubmittedAtBetween(
        String userid,
        Long competitionId,
        LocalDateTime start,
        LocalDateTime end
);
}
