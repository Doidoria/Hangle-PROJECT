// src/main/java/com/example/demo/domain/competition/repository/CompetitionRepository.java
package com.example.demo.domain.competition.repository;

import com.example.demo.domain.competition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompetitionRepository
        extends JpaRepository<Competition, Long>, JpaSpecificationExecutor<Competition> {
}
