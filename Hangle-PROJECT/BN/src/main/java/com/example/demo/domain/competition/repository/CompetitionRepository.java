package com.example.demo.domain.competition.repository;

import com.example.demo.domain.competition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends JpaRepository<Competition, Long>{

}
