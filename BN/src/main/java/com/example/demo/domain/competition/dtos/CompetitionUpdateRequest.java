// com.example.demo.domain.competition.dtos.CompetitionUpdateRequest
package com.example.demo.domain.competition.dtos;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompetitionUpdateRequest(
        @NotBlank String title,
        String description,
        String detail,                  // ✅ 상세 설명
        @NotBlank String status,        // 문자열로 들어와서 서비스에서 valueOf
        LocalDateTime startAt,
        LocalDateTime endAt,
        String evaluationMetric,        // ✅ 평가 지표
        BigDecimal prizeTotal           // ✅ 상금
) {}
