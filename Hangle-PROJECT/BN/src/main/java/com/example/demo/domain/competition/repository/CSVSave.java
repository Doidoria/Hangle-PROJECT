package com.example.demo.domain.competition.repository;

import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CSVSave extends JpaRepository<CompetitionCSVSave, Long> {
}