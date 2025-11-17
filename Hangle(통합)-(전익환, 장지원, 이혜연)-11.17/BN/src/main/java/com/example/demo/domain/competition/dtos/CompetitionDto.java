// com.example.demo.domain.competition.dtos.CompetitionDto
package com.example.demo.domain.competition.dtos;

import com.example.demo.domain.competition.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompetitionDto(
        Long id,
        String title,
        String purpose,           // 목적(한 줄)
        String detail,            // ✅ 상세 설명
        Status status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String evaluationMetric,
        BigDecimal prizeTotal,
        Integer participantCount,
        LocalDateTime createdAt
) {}
