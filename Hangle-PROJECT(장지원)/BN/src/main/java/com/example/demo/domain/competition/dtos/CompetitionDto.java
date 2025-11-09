package com.example.demo.domain.competition.dtos;

import com.example.demo.domain.competition.entity.Status;
import java.time.LocalDateTime;

public record CompetitionDto(
        Long id,
        String title,
        String summary,
        String description,
        String prize,
        Status status,
        String datasetUrl,
        String ruleUrl,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
