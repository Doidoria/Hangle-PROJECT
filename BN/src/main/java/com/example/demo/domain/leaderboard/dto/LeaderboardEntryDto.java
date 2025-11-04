package com.example.demo.domain.leaderboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LeaderboardEntryDto {
    private int rank;
    @Schema(example = "user01")
    private String userid;
    @Schema(example = "0.9123")
    private double bestScore;
    @Schema(example = "7")
    private int submissions; // 제출 횟수
}
