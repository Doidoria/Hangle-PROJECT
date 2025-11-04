package com.example.demo.domain;

import com.example.demo.domain.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
}
