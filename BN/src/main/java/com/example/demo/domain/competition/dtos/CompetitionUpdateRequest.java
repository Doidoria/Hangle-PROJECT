package com.example.demo.domain.competition.dtos;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CompetitionUpdateRequest(
        @NotBlank String title,
        String description,
        @NotBlank String status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {}
