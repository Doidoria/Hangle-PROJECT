package com.example.demo.domain.competition.dtos;

import com.example.demo.domain.competition.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CompetitionCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull Status status,           // OPEN, CLOSED, UPCOMING
        LocalDateTime startAt,            // ISO-8601: "2025-11-10T09:00:00"
        LocalDateTime endAt               // ISO-8601: "2025-12-10T18:00:00"
) {}
