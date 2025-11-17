package com.example.demo.domain.leaderboard.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LeaderboardEntryDto {
    private Long leaderBoardId;

    private Long competitionId; //! 대회 아이디
    private String competitionTitle; //대회 이름 !

    private Long userId;          // PK
    private String userid;        // 로그인 ID
    private String username;

    private Long csvSave_id;
    private Double score; // 점수

    private Integer attempt; // N번째 제출
    private LocalDateTime submittedAt; //최근 제출 시간
    private Integer comprank;


}
