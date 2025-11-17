// com.example.demo.domain.competition.dtos.CompetitionCreateRequest
package com.example.demo.domain.competition.dtos;

import com.example.demo.domain.competition.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompetitionCreateRequest(
        @NotBlank String title,
        String description,              // 목적(=엔티티 purpose)
        String detail,                   // ✅ 상세 설명
        @NotNull Status status,          // UPCOMING/OPEN/CLOSED (프론트에 안 보이더라도 기본값으로 전송)
        LocalDateTime startAt,
        LocalDateTime endAt,
        String evaluationMetric,         // ✅ 평가 지표 (예: ACCURACY/F1/AUC/RMSE/MAE)
        BigDecimal prizeTotal            // ✅ 상금
) {}
