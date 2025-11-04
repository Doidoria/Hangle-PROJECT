package com.example.demo.domain.competition.repository;

import com.example.demo.domain.competition.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByCompetitionIdOrderByScoreDesc(Long competitionId);
    List<Submission> findByCompetitionIdAndUseridOrderBySubmittedAtDesc(Long competitionId, String userid);
    int countByCompetitionIdAndUserid(Long competitionId, String userid);
}
