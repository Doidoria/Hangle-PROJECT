package com.example.demo.domain.leaderboard.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LeaderboardEntryDto {
    private Long leaderBoardId;
    private Long compid;
    private String userid;
    private Long submissionid;
    private String compname;
    private String username;
    private Double score; // 점수
    private Integer attempt; // N번째 제출
    private LocalDateTime submittedAt; //최근 제출 시간
    private Integer comprank;
}
