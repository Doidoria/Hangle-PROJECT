package com.example.demo.domain.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SubmissionDto {
    private Long id;
    @Schema(example = "1")
    private Long competitionId;
    @Schema(example = "user01")
    private String userid;
    @Schema(example = "0.8731")
    private double score;
    private int attempt;
    private LocalDateTime submittedAt;
}