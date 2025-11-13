package com.example.demo.domain.competition.dtos;

import com.example.demo.domain.competition.entity.Status;

import java.time.LocalDateTime;

public record CompetitionDto(
        Long id,
        String title,
        String description,
        Status status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
