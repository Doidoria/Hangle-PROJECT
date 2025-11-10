package com.example.demo.domain.competition.repository;

import com.example.demo.domain.competition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long>{

}
