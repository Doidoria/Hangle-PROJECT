package com.example.demo.domain.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SubmissionResponseDto {
    @Schema(example = "0.8842")
    private double score;
    @Schema(example = "12")
    private int rank;
}