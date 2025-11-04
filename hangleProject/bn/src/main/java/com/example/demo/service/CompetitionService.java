package com.example.demo.service;

import com.example.demo.domain.CompetitionRepository;
import com.example.demo.domain.entity.Competition;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetitionService {
    private final CompetitionRepository competitionRepository;

    public CompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    public Competition create(Competition competition) {
        return competitionRepository.save(competition);
    }

    public List<Competition> findAll() {
        return competitionRepository.findAll();
    }

    public Competition findById(Long id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("대회를 찾을 수 없습니다."));
    }
}
